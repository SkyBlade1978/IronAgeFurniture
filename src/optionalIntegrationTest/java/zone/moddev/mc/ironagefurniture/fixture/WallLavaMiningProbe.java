package zone.moddev.mc.ironagefurniture.fixture;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.api.MetalVariantHelper;
import zone.moddev.mc.ironagefurniture.api.MetalVariantHelper.MetalVariant;
import zone.moddev.mc.ironagefurniture.api.entity.EntityReleasedLavaLamp;

/** Mines a suspended wall holder and follows its vial to a dry or water landing. */
public final class WallLavaMiningProbe {
    private final List<EntityReleasedLavaLamp> released = new ArrayList<EntityReleasedLavaLamp>();
    private BlockPos cancelledBreak;
    private boolean cancelSpawn;

    @SubscribeEvent public void breaking(BlockEvent.BreakEvent event) {
        if (event.getPos().equals(cancelledBreak)) event.setCanceled(true);
    }
    @SubscribeEvent public void joining(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityReleasedLavaLamp) {
            released.add((EntityReleasedLavaLamp)event.getEntity());
            if (cancelSpawn) event.setCanceled(true);
        }
    }

    public static void verify(MinecraftServer server) {
        WorldServer world = server.worldServerForDimension(0);
        EntityPlayerMP player = FakePlayerFactory.getMinecraft(world);
        NetHandlerPlayServer previous = player.connection;
        GameType oldMode = player.interactionManager.getGameType();
        String oldDrops = world.getGameRules().getString("doTileDrops");
        WallLavaMiningProbe listener = new WallLavaMiningProbe();
        MinecraftForge.EVENT_BUS.register(listener);
        player.connection = new NetHandlerPlayServer(server,
                new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
            @Override public void sendPacket(Packet<?> packet) { }
        };
        int cases = 0;
        try {
            Block sconce =
                    BlockObjectHolder.light_metal_ironage_sconce_wall_lava_iron;
            for (MetalVariant metal : new MetalVariant[]{MetalVariant.IRON, MetalVariant.GOLD, MetalVariant.ADAMANTINE}) {
                if (!metal.isAvailable()) continue;
                for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
                    for (boolean water : new boolean[]{false, true}) for (int mode = 0; mode < 10; mode++) {
                        BlockPos source = new BlockPos(768 + (cases % 16) * 4, 110, 240 + (cases / 16) * 4);
                        BlockPos landing = source.down(4);
                        for (int y = 0; y < 6; y++) world.setBlockState(landing.up(y), Blocks.AIR.getDefaultState(), 2);
                        world.setBlockState(landing.down(), Blocks.STONE.getDefaultState(), 2);
                        if (water) world.setBlockState(landing, Blocks.WATER.getDefaultState(), 2);
                        world.setBlockState(source.offset(facing.getOpposite()), Blocks.STONE.getDefaultState(), 2);
                        clearDrops(world, source);
                        IBlockState original = MetalVariantHelper.withMetal(sconce.getDefaultState()
                                .withProperty(BlockHorizontal.FACING, facing), metal);
                        world.setBlockState(source, original, 2);
                        MetalVariantHelper.setMetal(world, source, metal);
                        player.setPosition(source.getX() + 2, source.getY(), source.getZ() + .5);
                        player.interactionManager.setGameType(mode == 4 ? GameType.CREATIVE : GameType.SURVIVAL);
                        ItemStack tool = mode == 1 ? new ItemStack(Items.WOODEN_PICKAXE)
                                : mode == 2 || mode == 3 ? new ItemStack(Items.DIAMOND_PICKAXE) : null;
                        if (mode == 3) tool.addEnchantment(Enchantments.SILK_TOUCH, 1);
                        if (mode == 6) tool = new ItemStack(BlockObjectHolder.light_metal_ironage_block_floor_lava_clear);
                        player.setHeldItem(EnumHand.MAIN_HAND, tool);
                        world.getGameRules().setOrCreateGameRule("doTileDrops", Boolean.toString(mode != 7));
                        listener.cancelledBreak = mode == 8 ? source : null;
                        listener.cancelSpawn = mode == 9;
                        int before = listener.released.size();
                        String context = metal + "/" + facing + "/water=" + water + "/mode=" + mode;
                        if (mode == 5 || mode == 6) {
                            sconce.onBlockActivated(world, source, original,
                                    player, EnumHand.MAIN_HAND, tool, facing, .5F, .5F, .5F);
                            require(world.getBlockState(source).getBlock() == BlockObjectHolder.light_metal_ironage_sconce_wall_empty_iron
                                    && MetalVariantHelper.getMetal(world, source) == metal
                                    && world.getBlockState(source).getValue(BlockHorizontal.FACING) == facing,
                                    "Safe removal changed the holder: " + context);
                            require(player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem()
                                    == Item.getItemFromBlock(BlockObjectHolder.light_metal_ironage_block_floor_lava_clear)
                                    && player.getHeldItemMainhand().stackSize == (mode == 6 ? 2 : 1), "Safe removal lost the lamp");
                        } else {
                            boolean removed = player.interactionManager.tryHarvestBlock(source);
                            // Legacy Forge returns the removal hook's result; newer
                            // versions may report the mining request as handled.
                            if (mode != 9) require(removed == (mode != 8),
                                    "Wrong mining/cancellation result: " + context);
                            require(world.getBlockState(source).getBlock() == (mode == 8 || mode == 9 ? sconce : Blocks.AIR),
                                    "Mining left fire at the wall or lost a cancelled holder: " + context);
                            if (mode == 8 || mode == 9) require(MetalVariantHelper.getMetal(world, source) == metal,
                                    "Cancelled removal lost the original metal: " + context);
                        }
                        boolean falls = mode < 3 || mode == 7;
                        require(listener.released.size() == before + (falls || mode == 9 ? 1 : 0),
                                "Mining did not release exactly one lamp: " + context);
                        if (falls) {
                            EntityReleasedLavaLamp lamp = listener.released.get(before);
                            require(!((Entity)lamp).isDead && ((Entity)lamp).posY > landing.getY() + 2, "Lamp did not start in flight");
                            for (int tick = 0; tick < 100 && !((Entity)lamp).isDead; tick++) ((Entity)lamp).onUpdate();
                            require(((Entity)lamp).isDead && !((Entity)lamp).noClip, "Lamp failed to fall and land: " + context);
                            require(world.getBlockState(landing).getBlock() == (water ? BlockObjectHolder.obsidian_chunk : Blocks.FIRE),
                                    "Lamp vanished instead of shattering at its landing: " + context);
                        } else {
                            require(world.getBlockState(landing).getBlock() == (water ? Blocks.WATER : Blocks.AIR),
                                    "Safe removal, Silk Touch or cancellation altered the landing");
                        }
                        List<EntityItem> drops = drops(world, source);
                        int frames = 0, lamps = 0, total = 0;
                        for (EntityItem entity : drops) {
                            ItemStack drop = entity.getEntityItem();
                            total += drop.stackSize;
                            if (drop.getItem() == Item.getItemFromBlock(BlockObjectHolder.light_metal_ironage_sconce_floor_empty_iron)) {
                                require(drop.getMetadata() == metal.getMeta(), "Frame lost its metal: " + context);
                                frames += drop.stackSize;
                            }
                            if (drop.getItem() == Item.getItemFromBlock(BlockObjectHolder.light_metal_ironage_block_floor_lava_clear))
                                lamps += drop.stackSize;
                        }
                        require(frames == (mode < 4 ? 1 : 0) && lamps == (mode == 3 ? 1 : 0) && total == frames + lamps,
                                "Wrong frame or intact-lamp drops: " + context);
                        clearDrops(world, source);
                        world.setBlockToAir(source);
                        listener.cancelSpawn = false;
                        listener.cancelledBreak = null;
                        cases++;
                    }
            }
            Files.write(Paths.get("wall-lava-mining-pass.properties"),
                    ("status=PASS\nwall_lava_mining_cases=" + cases + "\n").getBytes(StandardCharsets.UTF_8));
            org.apache.logging.log4j.LogManager.getLogger().info("IAF WALL LAVA MINING PROBE PASSED: {} cases", cases);
        } catch (java.io.IOException failure) {
            throw new IllegalStateException("Cannot write wall-lava result", failure);
        } finally {
            world.getGameRules().setOrCreateGameRule("doTileDrops", oldDrops);
            player.interactionManager.setGameType(oldMode);
            player.setHeldItem(EnumHand.MAIN_HAND, null);
            player.connection = previous;
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
    }

    /** Existing harvest probes use a floor immediately below the wall sconce. */
    static void settleReleased(WorldServer world, BlockPos source) {
        for (EntityReleasedLavaLamp lamp : world.getEntitiesWithinAABB(EntityReleasedLavaLamp.class,
                new AxisAlignedBB(source).expandXyz(2), entity -> !((Entity)entity).isDead)) {
            for (int tick = 0; tick < 100 && !((Entity)lamp).isDead; tick++) ((Entity)lamp).onUpdate();
            require(((Entity)lamp).isDead, "Mined wall lamp failed to land");
        }
    }
    private static List<EntityItem> drops(WorldServer world, BlockPos source) {
        return world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(source).expandXyz(6), entity -> !entity.isDead);
    }
    private static void clearDrops(WorldServer world, BlockPos source) { drops(world, source).forEach(EntityItem::setDead); }
    private static void require(boolean passed, String message) { if (!passed) throw new IllegalStateException(message); }
}
