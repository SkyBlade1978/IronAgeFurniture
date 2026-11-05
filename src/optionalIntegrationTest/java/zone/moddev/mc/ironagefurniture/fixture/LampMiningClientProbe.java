package zone.moddev.mc.ironagefurniture.fixture;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import zone.moddev.mc.ironagefurniture.BlockObjectHolder;

/** Runs real client mining and the hit particles drawn after the final swing. */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class LampMiningClientProbe {
    private static boolean started, complete;
    private static int ticks;
    private LampMiningClientProbe() { }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) throws Exception {
        if (complete || !Boolean.getBoolean("iaf.probe.lampMiningClient")
                || event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getMinecraft();
        if (!started && client.currentScreen instanceof GuiMainMenu) {
            started = true;
            client.launchIntegratedServer("world", "IAF lamp test",
                    new WorldSettings(44L, GameType.SURVIVAL, false, false, WorldType.FLAT));
            return;
        }
        if (client.world == null || client.player == null || ++ticks < 40) return;
        complete = true;
        try {
            int cases = run(client);
            Files.write(Paths.get("lamp-client-pass.properties"),
                    ("status=PASS\nmining_cases=" + cases + "\n").getBytes(StandardCharsets.UTF_8));
            LogManager.getLogger().info("IAF LAMP MINING CLIENT PROBE PASSED: {} cases", cases);
        } finally { client.shutdown(); }
    }

    private static int run(Minecraft client) {
        BlockPos pos = client.player.getPosition().add(2, 1, 2);
        client.player.clearActivePotions();
        client.player.onGround = true;
        int cases = 0;
        zone.moddev.mc.ironagefurniture.api.entity.EntityReleasedLavaLamp falling =
                new zone.moddev.mc.ironagefurniture.api.entity.EntityReleasedLavaLamp(client.world,
                        pos.getX() + .5, pos.getY() + .3, pos.getZ() + .5,
                        BlockObjectHolder.light_metal_ironage_block_floor_lava_clear.getDefaultState(),
                        pos.getY(), false);
        net.minecraft.client.renderer.entity.Render<?> renderer = client.getRenderManager().getEntityRenderObject(falling);
        require(renderer instanceof net.minecraft.client.renderer.entity.RenderFallingBlock,
                "Released wall lamp has no falling-block renderer");
        ((net.minecraft.client.renderer.entity.RenderFallingBlock)renderer).doRender(falling, -100, -100, -100, 0, 0);
        cases++;
        for (Block block : Block.REGISTRY) {
            String path = block.getRegistryName().getResourcePath();
            if (!"ironagefurniture".equals(block.getRegistryName().getResourceDomain())
                    || !path.startsWith("light_metal_ironage_block_floor_")
                    || !(path.contains("_lava_") || path.contains("_glow_") || path.contains("_red_"))) continue;
            for (IBlockState state : block.getBlockState().getValidStates()) {
                client.world.setBlockState(pos, state, 2);
                AxisAlignedBB shape = state.getBoundingBox(client.world, pos);
                require(shape != null && shape.maxX > shape.minX && shape.maxY > shape.minY
                        && shape.maxZ > shape.minZ, "Empty lamp selection box: " + state);
                for (EnumFacing face : EnumFacing.values()) {
                    client.effectRenderer.addBlockHitEffects(pos, hit(pos, face));
                    cases++;
                }
            }
            require(mine(client, pos, block.getDefaultState(), null, false) <= 15,
                    "Glass vial takes too long to mine: " + path);
            cases++;
        }
        ItemStack silk = new ItemStack(Items.DIAMOND_PICKAXE);
        silk.addEnchantment(Enchantments.SILK_TOUCH, 1);
        Block[] lights = {BlockObjectHolder.light_metal_ironage_block_floor_lava_clear,
                BlockObjectHolder.light_metal_ironage_sconce_floor_lava_iron,
                BlockObjectHolder.light_metal_ironage_sconce_wall_lava_iron};
        for (Block block : lights) for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
            for (ItemStack tool : new ItemStack[]{null, new ItemStack(Items.DIAMOND_PICKAXE), silk})
                for (boolean creative : new boolean[]{false, true}) {
                    mine(client, pos, block.getDefaultState().withProperty(BlockHorizontal.FACING, facing), tool, creative);
                    cases++;
                }
        return cases;
    }

    private static int mine(Minecraft client, BlockPos pos, IBlockState state, ItemStack tool, boolean creative) {
        client.playerController.resetBlockRemoving();
        client.playerController.setGameType(creative ? GameType.CREATIVE : GameType.SURVIVAL);
        client.player.setHeldItem(EnumHand.MAIN_HAND, tool == null ? null : tool.copy());
        client.world.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 2);
        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
            client.world.setBlockState(pos.offset(side), Blocks.STONE.getDefaultState(), 2);
        client.world.setBlockState(pos, state, 2);
        require(client.playerController.clickBlock(pos, EnumFacing.UP), "Cannot start mining: " + state);
        int swings = 0;
        while (client.world.getBlockState(pos).getBlock() == state.getBlock() && swings++ < 250)
            if (client.playerController.onPlayerDamageBlock(pos, EnumFacing.UP))
                client.effectRenderer.addBlockHitEffects(pos, hit(pos, EnumFacing.UP));
        // Legacy Forge retains a harvestable sconce until the server sends its
        // removal packet. Its selection box must remain usable during that wait.
        boolean deferredHolder = state.getBlock().getRegistryName().getResourcePath().contains("_sconce_")
                && !creative && client.world.getBlockState(pos).getBlock() == state.getBlock();
        require(client.world.isAirBlock(pos) || deferredHolder,
                "Client predicted fire or failed removal: " + state + ", creative=" + creative + ", tool=" + tool);
        return swings;
    }

    private static RayTraceResult hit(BlockPos pos, EnumFacing face) {
        return new RayTraceResult(new Vec3d(pos).addVector(.5, .3, .5), face, pos);
    }
    private static void require(boolean passed, String message) {
        if (!passed) throw new IllegalStateException(message);
    }
}
