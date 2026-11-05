package zone.moddev.mc.ironagefurniture.fixture;

import java.util.LinkedHashMap;
import java.util.Map;
import java.lang.reflect.Method;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.Enchantments;
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
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightHolderSconceFloor;
import zone.moddev.mc.ironagefurniture.api.Blocks.LightSourceSconceCandleFloor;
import zone.moddev.mc.ironagefurniture.api.MetalVariantHelper;
import zone.moddev.mc.ironagefurniture.api.MetalVariantHelper.MetalVariant;

/** Runs normal Forge mining and support-loss paths, including hidden sconce states. */
public final class SconceHarvestProbe {
    private BlockPos cancelled;
    @SubscribeEvent public void preventBreak(BlockEvent.BreakEvent event) {
        if (event.getPos().equals(cancelled)) event.setCanceled(true);
    }

    public static void verify(MinecraftServer server) {
        WorldServer world = server.worldServerForDimension(0);
        EntityPlayerMP player = FakePlayerFactory.getMinecraft(world);
        NetHandlerPlayServer oldConnection = player.connection;
        player.connection = new NetHandlerPlayServer(server,
                new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
            @Override public void sendPacket(Packet<?> packet) { }
        };
        SconceHarvestProbe guard = new SconceHarvestProbe();
        MinecraftForge.EVENT_BUS.register(guard);
        BlockPos pos = new BlockPos(160, 110, 160);
        String previousDrops = world.getGameRules().getString("doTileDrops");
        int cases = 0, states = 0;
        try {
            for (Block block : Block.REGISTRY) {
                if (!(block instanceof LightHolderSconceFloor)) continue;
                String path = block.getRegistryName().getResourcePath();
                boolean wall = path.contains("_wall_"), lava = path.contains("_lava_");
                ++states;
                for (MetalVariant metal : MetalVariantHelper.getAvailableVariants())
                for (EnumFacing facing : EnumFacing.HORIZONTALS)
                for (int mode = 0; mode < 9; ++mode) {
                    clearItems(world, pos);
                    world.setBlockToAir(pos);
                    world.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 2);
                    BlockPos support = wall ? pos.offset(facing.getOpposite()) : pos.down();
                    world.setBlockState(pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState(), 2);
                    IBlockState state = block.getDefaultState().withProperty(BlockHorizontal.FACING, facing);
                    world.setBlockState(pos, state, 2);
                    MetalVariantHelper.setMetal(world, pos, metal);
                    Map<String, Integer> expected = counts(block.getDrops(world, pos, state, 0));
                    String frame = key(new ItemStack(BlockObjectHolder.light_metal_ironage_sconce_floor_empty_iron, 1, metal.getMeta()));
                    check(expected.get(frame) != null && expected.get(frame) == 1, "Missing original metal frame: " + path);
                    if (path.contains("_twin"))
                        check(expected.get(key(new ItemStack(Blocks.TORCH))) == 2, "Twin torch count changed");
                    if (block instanceof LightSourceSconceCandleFloor) {
                        Method count = LightSourceSconceCandleFloor.class.getDeclaredMethod("CandleCount");
                        count.setAccessible(true);
                        check(expected.get(key(new ItemStack(BlockObjectHolder.light_metal_ironage_candle_floor)))
                                .equals(count.invoke(block)), "Candle count changed");
                    }
                    ItemStack tool = mode == 0 ? null
                            : new ItemStack(mode == 1 ? Items.WOODEN_PICKAXE : Items.DIAMOND_PICKAXE);
                    if (mode == 3) tool.addEnchantment(Enchantments.SILK_TOUCH, 1);
                    if (mode == 4) tool.addEnchantment(Enchantments.FORTUNE, 3);
                    player.setHeldItem(EnumHand.MAIN_HAND, tool);
                    player.interactionManager.setGameType(mode == 5 ? GameType.CREATIVE : GameType.SURVIVAL);
                    world.getGameRules().setOrCreateGameRule("doTileDrops", mode == 7 ? "false" : "true");
                    guard.cancelled = mode == 8 ? pos : null;
                    String context = path + " " + metal + " " + facing + " mode=" + mode;
                    if (mode == 6) world.setBlockToAir(support);
                    else check(player.interactionManager.tryHarvestBlock(pos) == (mode != 8), "Mining result: " + context);
                    WallLavaMiningProbe.settleReleased(world, pos);
                    Map<String, Integer> actual = itemCounts(world, pos);
                    if (mode == 5 || mode == 7 || mode == 8) expected.clear();
                    if (lava && mode == 3) expected.put(key(new ItemStack(BlockObjectHolder.light_metal_ironage_block_floor_lava_clear)), 1);
                    check(expected.equals(actual), "Lost or duplicated sconce contents: " + context + " expected=" + expected + " actual=" + actual);
                    if (mode == 8) check(world.getBlockState(pos).getBlock() == block
                            && MetalVariantHelper.getMetal(world, pos) == metal, "Cancelled break changed holder");
                    else if (lava && mode != 3 && mode != 5 && mode != 6)
                        check(world.getBlockState(pos).getBlock() == Blocks.FIRE, "Lamp failed to shatter: " + context);
                    else if (!lava || mode != 6)
                        check(world.isAirBlock(pos), "Mining left a hidden block: " + context);
                    ++cases;
                }
            }
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        } finally {
            guard.cancelled = null;
            MinecraftForge.EVENT_BUS.unregister(guard);
            world.getGameRules().setOrCreateGameRule("doTileDrops", previousDrops);
            world.setBlockToAir(pos);
            clearItems(world, pos);
            player.setHeldItem(EnumHand.MAIN_HAND, null);
            player.interactionManager.setGameType(GameType.SURVIVAL);
            player.connection = oldConnection;
        }
        LogManager.getLogger().info("IAF SCONCE HARVEST MATRIX PASSED: {} states, {} cases; hands, weak/strong picks, Silk Touch, Fortune, Creative, support loss, doTileDrops and cancellation", states, cases);
    }

    private static String key(ItemStack stack) {
        return stack.getItem().getRegistryName() + "/" + stack.getMetadata();
    }
    private static Map<String, Integer> counts(Iterable<ItemStack> stacks) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (ItemStack stack : stacks) result.put(key(stack), result.getOrDefault(key(stack), 0) + stack.stackSize);
        return result;
    }
    private static Map<String, Integer> itemCounts(WorldServer world, BlockPos pos) {
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
        for (EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expand(1, 1, 1)))
            if (!item.isDead) stacks.add(item.getEntityItem());
        return counts(stacks);
    }
    private static void clearItems(WorldServer world, BlockPos pos) {
        for (EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expand(1, 1, 1))) {
            item.setDead();
            world.removeEntityDangerously(item);
        }
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
