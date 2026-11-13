package zone.moddev.mc.ironagefurniture.fixture;

import zone.moddev.mc.ironagefurniture.api.Blocks.DiningTable;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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

/** Exercises baked item models and selection/ray tracing inside a disposable client world. */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class SurfaceBoundsClientProbe {
    private static boolean started;
    private static boolean complete;
    private static int ticks;

    private SurfaceBoundsClientProbe() { }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) throws Exception {
        if (complete || !Boolean.getBoolean("iaf.probe.surfaceBoundsClient")
                || event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getMinecraft();
        if (!started && client.currentScreen instanceof GuiMainMenu) {
            started = true;
            client.launchIntegratedServer("surface-bounds-world", "IAF surface bounds test",
                    new WorldSettings(44L, GameType.CREATIVE, false, false, WorldType.FLAT));
            return;
        }
        if (client.world == null || client.player == null || ++ticks < 40) return;
        complete = true;
        boolean galleryStarted = false;
        try {
            int cases = run(client);
            VanillaFoodNetworkClientProbe.start(client, cases);
            galleryStarted = true;
        } finally {
            if (!galleryStarted) client.shutdown();
        }
    }

    private static int run(Minecraft client) {
        BlockPos owner = client.player.getPosition().add(2, 1, 2);
        BlockPos display = owner.up();
        Block blocker = Block.getBlockFromName("ironagefurniture:surface_display_blocker");
        int cases = 0;
        for (String hostId : new String[]{"table_dining_oak", "cabinet_wood_ironage_oak"}) {
            Block host = Block.getBlockFromName("ironagefurniture:" + hostId);
            for (ItemStack item : new ItemStack[]{new ItemStack(Blocks.STONE), new ItemStack(Blocks.STONE_SLAB),
                    new ItemStack(Blocks.OAK_FENCE_GATE), new ItemStack(Items.ARMOR_STAND),
                    new ItemStack(Items.DIAMOND), new ItemStack(Items.PAPER)}) {
                for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
                    client.world.setBlockToAir(display);
                    client.world.setBlockToAir(owner);
                    client.world.setBlockState(owner.down(), Blocks.STONE.getDefaultState(), 2);
                    IBlockState state = host.getDefaultState();
                    if (host instanceof DiningTable) state = state.withProperty(DiningTable.DATA, true);
                    client.world.setBlockState(owner, state, 2);
                    TileEntity tile = client.world.getTileEntity(owner);
                    require(tile instanceof SurfaceSettingHost, "Missing furniture surface host");
                    SurfaceSettingHost surface = (SurfaceSettingHost)tile;
                    surface.getSurfaceSetting().setSingleItem(item, facing);
                    client.world.setBlockState(display, blocker.getDefaultState(), 2);
                    IBlockState displayState = client.world.getBlockState(display);

                    AxisAlignedBB bounds = blocker.getSelectedBoundingBox(displayState, client.world, display);
                    boolean flat = !(item.getItem() instanceof ItemBlock);
                    double halfX = 0.1375D;
                    double halfZ = 0.1375D;
                    double bottom = 0.0D;
                    double top = 0.275D;
                    if (flat) {
                        halfX = halfZ = 0.25D;
                        bottom = 0.024375D;
                        top = 0.055625D;
                    } else if (item.getItem() == Item.getItemFromBlock(Blocks.OAK_FENCE_GATE)) {
                        bottom = 0.0859375D;
                        if (facing.getAxis() == EnumFacing.Axis.X) halfX = 0.0171875D;
                        else halfZ = 0.0171875D;
                    } else if (item.getItem() == Item.getItemFromBlock(Blocks.STONE_SLAB)) {
                        top = 0.1375D;
                    }
                    // Forge grows generated sprite edges by 0.00045 before the 0.5 display scale.
                    double tolerance = flat ? 0.00025D : 0.00001D;
                    close(bounds.minX, display.getX() + 0.5D - halfX, "selection min X", tolerance);
                    close(bounds.maxX, display.getX() + 0.5D + halfX, "selection max X", tolerance);
                    close(bounds.minZ, display.getZ() + 0.5D - halfZ, "selection min Z", tolerance);
                    close(bounds.maxZ, display.getZ() + 0.5D + halfZ, "selection max Z", tolerance);
                    close(bounds.minY, display.getY() + bottom, "selection base", tolerance);
                    close(bounds.maxY, display.getY() + top, "selection top", tolerance);
                    Vec3d center = new Vec3d((bounds.minX + bounds.maxX) / 2.0D,
                            (bounds.minY + bounds.maxY) / 2.0D, (bounds.minZ + bounds.maxZ) / 2.0D);
                    for (EnumFacing face : EnumFacing.values()) {
                        Vec3d start = center.addVector(face.getFrontOffsetX(), face.getFrontOffsetY(),
                                face.getFrontOffsetZ());
                        RayTraceResult hit = blocker.collisionRayTrace(displayState, client.world, display, start, center);
                        require(hit != null && hit.sideHit == face, "Ray missed displayed block from " + face);
                        cases++;
                    }
                    RayTraceResult outside = blocker.collisionRayTrace(displayState, client.world, display,
                            new Vec3d(display.getX() + (flat ? 0.8D : 0.7D),
                                    display.getY() + 1.0D, display.getZ() + 0.5D),
                            new Vec3d(display.getX() + (flat ? 0.8D : 0.7D),
                                    display.getY(), display.getZ() + 0.5D));
                    require(outside == null, "Old oversized selection area still catches the ray");
                    cases++;
                    client.player.setHeldItem(EnumHand.MAIN_HAND, null);
                    client.playerController.setGameType(GameType.CREATIVE);
                    require(client.playerController.onPlayerDestroyBlock(display), "Client could not break the display");
                    require(client.world.isAirBlock(display), "Client retained the display blocker");
                    require(!surface.getSurfaceSetting().hasAnyItem(), "Client still renders a broken display");
                    require(client.world.getBlockState(owner).getBlock() == host, "Client broke the furniture");
                    cases++;
                }
            }
        }
        return cases + VanillaFoodClientProbe.verify(client, owner);
    }

    private static void close(double actual, double expected, String description, double tolerance) {
        require(Math.abs(actual - expected) < tolerance,
                description + ": expected " + expected + ", got " + actual);
    }

    private static void require(boolean pass, String message) {
        if (!pass) throw new IllegalStateException(message);
    }
}
