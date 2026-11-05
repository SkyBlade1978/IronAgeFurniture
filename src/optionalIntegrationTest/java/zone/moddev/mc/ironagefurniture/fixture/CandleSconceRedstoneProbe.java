package zone.moddev.mc.ironagefurniture.fixture;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.api.MetalVariantHelper;
import zone.moddev.mc.ironagefurniture.api.MetalVariantHelper.MetalVariant;

/** Extinguishes candles with a bucket, then waits for normal redstone ticks. */
public final class CandleSconceRedstoneProbe {
    private final MinecraftServer server;
    private final WorldServer world;
    private final EntityPlayerMP player;
    private final List<Fixture> fixtures = new ArrayList<>();
    private int ticks;
    private int bucketCases;

    private CandleSconceRedstoneProbe(MinecraftServer server) throws ReflectiveOperationException {
        this.server = server;
        world = server.worldServerForDimension(0);
        player = FakePlayerFactory.getMinecraft(world);
        verifyStandaloneBuckets();
        for (boolean wall : new boolean[]{false, true}) for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
            for (MetalVariant metal : MetalVariantHelper.getAvailableVariants()) for (int count = 1; count <= 4; count++) {
                int index = fixtures.size();
                BlockPos pos = new BlockPos(1200 + index % 16 * 3, 104, 1200 + index / 16 * 3);
                Fixture fixture = new Fixture(pos, wall, facing, metal, count);
                fixtures.add(fixture);
                // Scheduled updates require the surrounding chunks to be loaded.
                for (int x : new int[]{-8, 8}) for (int z : new int[]{-8, 8})
                    world.getChunkFromBlockCoords(pos.add(x, 0, z));
                if (world.getBlockState(pos).getBlock() == fixture.lit)
                    check(fixture, true, "Saved candle changed");
                else require(world.isAirBlock(pos), "Unexpected block in disposable fixture", fixture);
                world.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 3);
                world.setBlockState(pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState(), 3);
                power(fixture, false);
                place(fixture, false);
                power(fixture, true);
            }
    }

    public static void start(MinecraftServer server) {
        try {
            FMLCommonHandler.instance().bus().register(new CandleSconceRedstoneProbe(server));
        } catch (Exception failure) {
            server.initiateShutdown();
            throw new IllegalStateException("Could not start candle sconce redstone probe", failure);
        }
    }

    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ++ticks % 8 != 0) return;
        try {
            for (Fixture fixture : fixtures) {
                if (ticks == 8) {
                    check(fixture, true, "Signal did not relight extinguished candles");
                    power(fixture, false);
                } else if (ticks == 16) {
                    check(fixture, true, "Dry candles went out after losing power");
                    extinguish(fixture);
                    check(fixture, false, "Water bucket did not extinguish candles");
                    power(fixture, true);
                    power(fixture, false);
                } else if (ticks == 24) {
                    check(fixture, false, "Expired pulse relit candles");
                    power(fixture, true);
                } else if (ticks == 32) {
                    check(fixture, true, "Repeated signal did not light candles");
                    extinguish(fixture);
                } else if (ticks == 40) {
                    check(fixture, true, "Bucket extinguishing in an existing signal did not relight");
                    place(fixture, false);
                } else if (ticks == 48) {
                    check(fixture, true, "Placement in an existing signal did not relight");
                    power(fixture, false);
                } else if (ticks == 56) {
                    check(fixture, true, "Dry candle lost its flame after the final signal");
                }
            }
            if (ticks == 56) {
                Files.write(Paths.get("candle-redstone-pass.properties"), ("status=PASS\ncandle_sconce_cases="
                        + fixtures.size() + "\nsignal_stages=7\nstandalone_bucket_cases=" + bucketCases + "\n").getBytes(StandardCharsets.UTF_8));
                server.initiateShutdown();
            }
        } catch (Exception failure) {
            server.initiateShutdown();
            throw new IllegalStateException("Candle sconce scheduled-redstone probe failed", failure);
        }
    }

    private void verifyStandaloneBuckets() throws ReflectiveOperationException {
        BlockPos pos = new BlockPos(1100, 104, 1100);
        boolean previousCreative = player.capabilities.isCreativeMode;
        try {
            for (boolean wall : new boolean[]{false, true}) for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
                for (boolean lit : new boolean[]{false, true}) for (EnumHand hand : EnumHand.values())
                    for (boolean creative : new boolean[]{false, true}) {
                        String name = "light_metal_ironage_candle_" + (wall ? "wall" : "floor");
                        Block source = (Block)BlockObjectHolder.class.getField(name + (lit ? "" : "_unlit")).get(null);
                        Block unlit = (Block)BlockObjectHolder.class.getField(name + "_unlit").get(null);
                        world.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 2);
                        world.setBlockState(pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState(), 2);
                        IBlockState original = source.getDefaultState().withProperty(BlockHorizontal.FACING, facing);
                        world.setBlockState(pos, original, 2);
                        player.capabilities.isCreativeMode = creative;
                        player.setHeldItem(hand, new ItemStack(Items.WATER_BUCKET));
                        if (!source.onBlockActivated(world, pos, original, player, hand, player.getHeldItem(hand), facing, .5F, .5F, .5F)
                                || world.getBlockState(pos) != unlit.getDefaultState().withProperty(BlockHorizontal.FACING, facing)
                                || player.getHeldItem(hand).getItem() != Items.WATER_BUCKET)
                            throw new IllegalStateException("Standalone candle bucket click changed state or emptied its bucket");
                        bucketCases++;
                    }
        } finally { player.capabilities.isCreativeMode = previousCreative; }
    }

    private void power(Fixture fixture, boolean powered) {
        world.setBlockState(fixture.pos.offset(fixture.facing.rotateY()),
                (powered ? Blocks.REDSTONE_BLOCK : Blocks.STONE).getDefaultState(), 3);
    }

    private void place(Fixture fixture, boolean lit) {
        world.setBlockState(fixture.pos, fixture.state(lit), 3);
        MetalVariantHelper.setMetal(world, fixture.pos, fixture.metal);
    }

    private void extinguish(Fixture fixture) {
        player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        Block block = world.getBlockState(fixture.pos).getBlock();
        require(block.onBlockActivated(world, fixture.pos, world.getBlockState(fixture.pos),
                player, EnumHand.MAIN_HAND, player.getHeldItemMainhand(), fixture.facing, .5F, .5F, .5F), "Bucket click refused", fixture);
    }

    private void check(Fixture fixture, boolean lit, String message) {
        IBlockState state = world.getBlockState(fixture.pos);
        require(state.getBlock() == (lit ? fixture.lit : fixture.unlit)
                && state.getValue(BlockHorizontal.FACING) == fixture.facing
                && MetalVariantHelper.getMetal(world, fixture.pos) == fixture.metal,
                message + "; actual=" + state, fixture);
        require(state.getLightValue(world, fixture.pos) == (lit ? 11 + fixture.count : 0),
                "Wrong emitted candle light", fixture);
    }

    private static void require(boolean passed, String message, Fixture fixture) {
        if (!passed) throw new IllegalStateException(message + ": " + fixture.pos + "/" + fixture.metal
                + "/" + fixture.facing + "/" + fixture.count);
    }

    private static final class Fixture {
        private final BlockPos pos;
        private final EnumFacing facing;
        private final MetalVariant metal;
        private final int count;
        private final Block lit, unlit;
        private Fixture(BlockPos pos, boolean wall, EnumFacing facing, MetalVariant metal, int count)
                throws ReflectiveOperationException {
            this.pos = pos; this.facing = facing; this.metal = metal; this.count = count;
            String name = "light_metal_ironage_sconce_" + (wall ? "wall" : "floor") + "_candle_iron"
                    + new String[]{"", "", "_two", "_three", "_four"}[count];
            lit = (Block)BlockObjectHolder.class.getField(name).get(null);
            unlit = (Block)BlockObjectHolder.class.getField(name + "_unlit").get(null);
        }
        private IBlockState state(boolean litState) {
            return MetalVariantHelper.withMetal((litState ? lit : unlit).getDefaultState()
                    .withProperty(BlockHorizontal.FACING, facing), metal);
        }
    }
}
