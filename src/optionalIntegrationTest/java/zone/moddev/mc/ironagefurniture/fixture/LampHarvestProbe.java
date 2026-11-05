package zone.moddev.mc.ironagefurniture.fixture;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.apache.logging.log4j.LogManager;
import zone.moddev.mc.ironagefurniture.BlockObjectHolder;

/** Checks registered glass-vial mining speed and real Forge lava-light harvesting. */
public final class LampHarvestProbe {
    private LampHarvestProbe() { }
    public static void verify(MinecraftServer server) {
        WorldServer world = server.worldServerForDimension(0);
        EntityPlayerMP player = FakePlayerFactory.getMinecraft(world);
        NetHandlerPlayServer previous = player.connection;
        GameType oldMode = player.interactionManager.getGameType();
        String oldDrops = world.getGameRules().getString("doTileDrops");
        player.connection = new NetHandlerPlayServer(server,
                new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
            @Override public void sendPacket(Packet<?> packet) { }
        };
        BlockPos pos = new BlockPos(176, 110, 176);
        int cases = 0;
        try {
            world.getGameRules().setOrCreateGameRule("doTileDrops", "true");
            player.setHeldItem(EnumHand.MAIN_HAND, null);
            player.clearActivePotions();
            player.onGround = true;
            for (Block lamp : Block.REGISTRY) {
                String path = lamp.getRegistryName().getResourcePath();
                if (!"ironagefurniture".equals(lamp.getRegistryName().getResourceDomain())
                        || !path.startsWith("light_metal_ironage_block_floor_")
                        || !(path.contains("_lava_") || path.contains("_glow_") || path.contains("_red_"))) continue;
                IBlockState state = lamp.getDefaultState();
                require(state.getMaterial() == Material.GLASS
                        && Math.abs(state.getBlockHardness(world, pos) - .3F) < .00001F,
                        "Registered vial lost its glass mining settings: " + path);
                require(state.getPlayerRelativeBlockHardness(player, world, pos) >= .1F,
                        "Registered vial is slow to mine by hand: " + path);
                cases++;
            }
            Block[] lights = {BlockObjectHolder.light_metal_ironage_block_floor_lava_clear,
                    BlockObjectHolder.light_metal_ironage_sconce_floor_lava_iron,
                    BlockObjectHolder.light_metal_ironage_sconce_wall_lava_iron};
            Item lampItem = Item.getItemFromBlock(lights[0]);
            Item frameItem = Item.getItemFromBlock(BlockObjectHolder.light_metal_ironage_sconce_floor_empty_iron);
            for (Block light : lights) for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
                for (int mode = 0; mode < 5; mode++) {
                    world.setBlockToAir(pos);
                    world.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 2);
                    for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
                        world.setBlockState(pos.offset(side), Blocks.STONE.getDefaultState(), 2);
                    world.setBlockState(pos, light.getDefaultState().withProperty(BlockHorizontal.FACING, facing), 2);
                    world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expandXyz(2))
                            .forEach(EntityItem::setDead);
                    ItemStack tool = mode == 0 ? null : new ItemStack(Items.DIAMOND_PICKAXE);
                    if (mode == 2) tool.addEnchantment(Enchantments.EFFICIENCY, 2);
                    if (mode == 3) tool.addEnchantment(Enchantments.SILK_TOUCH, 1);
                    player.setHeldItem(EnumHand.MAIN_HAND, tool);
                    player.interactionManager.setGameType(mode == 4 ? GameType.CREATIVE : GameType.SURVIVAL);
                    require(player.interactionManager.tryHarvestBlock(pos), "Cannot harvest lava light");
                    WallLavaMiningProbe.settleReleased(world, pos);
                    List<EntityItem> drops = world.getEntitiesWithinAABB(EntityItem.class,
                            new AxisAlignedBB(pos).expandXyz(2), entity -> !entity.isDead);
                    int lamps = drops.stream().filter(entity -> entity.getEntityItem().getItem() == lampItem)
                            .mapToInt(entity -> entity.getEntityItem().stackSize).sum();
                    int frames = drops.stream().filter(entity -> entity.getEntityItem().getItem() == frameItem)
                            .mapToInt(entity -> entity.getEntityItem().stackSize).sum();
                    require(lamps == (mode == 3 ? 1 : 0) && frames == (light != lights[0] && mode != 4 ? 1 : 0),
                            "Wrong lava-light drops: " + light.getRegistryName() + "/" + mode);
                    require(world.getBlockState(pos).getBlock() == (mode < 3 ? Blocks.FIRE : Blocks.AIR),
                            "Wrong shatter/Creative/Silk Touch result");
                    drops.forEach(EntityItem::setDead);
                    cases++;
                }
            Files.write(Paths.get("lamp-server-pass.properties"),
                    ("status=PASS\nharvest_cases=" + cases + "\n").getBytes(StandardCharsets.UTF_8));
            LogManager.getLogger().info("IAF LAMP HARVEST SERVER PROBE PASSED: {} cases", cases);
        } catch (java.io.IOException failure) {
            throw new IllegalStateException("Cannot write lamp result", failure);
        } finally {
            world.setBlockToAir(pos);
            world.getGameRules().setOrCreateGameRule("doTileDrops", oldDrops);
            player.interactionManager.setGameType(oldMode);
            player.connection = previous;
        }
    }
    private static void require(boolean passed, String message) {
        if (!passed) throw new IllegalStateException(message);
    }
}
