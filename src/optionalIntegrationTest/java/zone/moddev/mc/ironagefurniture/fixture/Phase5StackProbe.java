package zone.moddev.mc.ironagefurniture.fixture;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;

/** Only in the test JAR: check the loaded stack and conserve fluid over real pipe pulses. */
final class Phase5StackProbe {
    private static final String WASH = "ironagefurniture_sugar_wash";

    private Phase5StackProbe() { }

    static void verify(MinecraftServer server) {
        Set<String> actual = new HashSet<String>(Loader.instance().getIndexedModList().keySet());
        require(actual.remove("ironagefurniture"), "Furniture did not load");
        actual.removeAll(Arrays.asList("minecraft", "mcp", "FML", "Forge", "forge",
                OptionalIntegrationRecipeProbe.MOD_ID));
        String expectedProperty = System.getProperty("iaf.probe.phase5Mods");
        Set<String> expected = new HashSet<String>();
        if (!expectedProperty.isEmpty()) expected.addAll(Arrays.asList(expectedProperty.split(",")));
        require(actual.equals(expected), "Expected Phase 5 mods " + expected + ", got " + actual);
        Phase5FurnitureProbe.verify(server.worldServerForDimension(0), expected);
        if (expected.contains("ironagebrewing")) {
            require(FluidRegistry.getFluid(WASH) != null, "Brewing did not register its wash");
        }
        if (expected.contains("basepower") || expected.contains("poweradvantage")) {
            String network = expected.contains("basepower") ? "basepower" : "poweradvantage";
            verifyTransfer(server.worldServerForDimension(0), network);
        }
        LogManager.getLogger().info("IAF PHASE 5 STACK PROBE PASSED: {}", expected);
    }

    private static void verifyTransfer(WorldServer world, String network) {
        BlockPos sourcePos = new BlockPos(8, 100, 8);
        BlockPos sinkPos = new BlockPos(9, 99, 14);
        BlockPos outletPos = new BlockPos(9, 100, 9);
        for (int x = 6; x <= 11; x++) for (int z = 7; z <= 16; z++) {
            world.setBlockState(new BlockPos(x, 98, z), Blocks.STONE.getDefaultState());
            if (z < 13) world.setBlockState(new BlockPos(x, 99, z), Blocks.STONE.getDefaultState());
        }
        placeVessel(world, sourcePos, "foudre_wood_ironage_oak", EnumFacing.SOUTH);
        placeVessel(world, sinkPos, "pot_still_wood_ironage_oak", EnumFacing.NORTH);
        world.setBlockState(outletPos.south(2), lookup(network + ":fluid_pipe").getDefaultState());
        IBlockState terminal = lookup(network + ":fluid_pipe_terminal").getDefaultState();
        world.setBlockState(outletPos.south(), terminal);
        world.setBlockState(outletPos.south(3), terminal);
        TileEntity source = world.getTileEntity(sourcePos);
        TileEntity sink = world.getTileEntity(sinkPos);
        TileEntity outlet = world.getTileEntity(outletPos);
        require(source != null && sink != null && outlet instanceof ITickable, "Missing Phase 5 vessel parts");
        try {
            int capacity = sink.getClass().getField("INPUT_CAPACITY").getInt(null);
            Fluid fluid = FluidRegistry.getFluid(WASH);
            setFluid(source, "Tank", fluid, 4250);
            setFluid(sink, "InputTank", fluid, capacity - 2750);
            sink.getClass().getMethod("setInletOpen", boolean.class).invoke(sink, true);
            source.getClass().getMethod("setOutletOpen", boolean.class).invoke(source, true);
            require(Boolean.TRUE.equals(sink.getClass().getMethod("isInletOpen").invoke(sink))
                    && Boolean.TRUE.equals(source.getClass().getMethod("isOutletOpen").invoke(source)),
                    "A vessel did not recognize its attached pipe");
            // Populate both terminals' neighbor lists without letting their
            // normal transfer loop race the Brewing outlet being measured.
            for (int offset : new int[]{1, 3}) {
                TileEntity pipe = world.getTileEntity(outletPos.south(offset));
                require(pipe != null, "Missing terminal at offset " + offset);
                pipe.getClass().getMethod("tickUpdate", boolean.class).invoke(pipe, true);
            }

            long initialTime = world.getTotalWorldTime();
            int remaining = 4250;
            int filled = capacity - 2750;
            try {
                for (int amount : new int[]{1000, 1000, 750, 0}) {
                    // Match the outlet tile's staggered eight-tick cadence.
                    long now = world.getTotalWorldTime();
                    world.getWorldInfo().setWorldTotalTime(now + 8 - ((now + 6) % 8));
                    ((ITickable)outlet).update();
                    remaining -= amount;
                    filled += amount;
                    require(amount(source, "Tank") == remaining && amount(sink, "InputTank") == filled,
                            network + " transfer did not conserve the accepted " + amount + " mB"
                            + "; source=" + amount(source, "Tank") + ", sink=" + amount(sink, "InputTank"));
                }
            } finally {
                world.getWorldInfo().setWorldTotalTime(initialTime);
            }
            LogManager.getLogger().info("IAF PHASE 5 TRANSFER PASSED: {} accepted 1000+1000+750+0 mB", network);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Cannot configure Brewing vessels", failure);
        }
    }

    private static void placeVessel(WorldServer world, BlockPos pos, String id, EnumFacing facing) {
        Block block = lookup("ironagebrewing:" + id);
        IBlockState state = block.getDefaultState().withProperty(BlockHorizontal.FACING, facing);
        require(world.setBlockState(pos, state), "Could not place " + id);
        block.onBlockPlacedBy(world, pos, state, null, new ItemStack(block));
    }

    private static Block lookup(String name) {
        Block result = Block.getBlockFromName(name);
        require(result != null && result != Blocks.AIR, "Missing " + name);
        return result;
    }

    private static void setFluid(TileEntity tile, String tag, Fluid fluid, int amount) {
        NBTTagCompound data = tile.writeToNBT(new NBTTagCompound());
        data.setTag(tag, new FluidStack(fluid, amount).writeToNBT(new NBTTagCompound()));
        tile.readFromNBT(data);
    }

    private static int amount(TileEntity tile, String tag) {
        return tile.writeToNBT(new NBTTagCompound()).getCompoundTag(tag).getInteger("Amount");
    }

    private static void require(boolean pass, String message) {
        if (!pass) throw new IllegalStateException(message);
    }
}
