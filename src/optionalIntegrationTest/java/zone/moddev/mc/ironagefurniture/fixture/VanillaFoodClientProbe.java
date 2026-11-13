package zone.moddev.mc.ironagefurniture.fixture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import zone.moddev.mc.ironagefurniture.api.Blocks.DiningTable;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaSurfaceFoods;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaFoodPlacement;
import zone.moddev.mc.ironagefurniture.client.render.SurfaceRenderRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;

/** Checks baked food geometry, exact stacks and a rendered gallery in the client fixture. */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class VanillaFoodClientProbe {
    private static BlockPos gallery;
    private static int galleryTicks;
    private VanillaFoodClientProbe() { }

    static int verify(Minecraft client, BlockPos pos) {
        Block blocker = Block.getBlockFromName("ironagefurniture:surface_display_blocker");
        ModelManager models = client.getRenderItem().getItemModelMesher().getModelManager();
        List<ItemStack> variants = new ArrayList<ItemStack>();
        for (VanillaSurfaceFoods.Food food : VanillaSurfaceFoods.all()) {
            ItemStack item = new ItemStack(food.item, 1, Math.max(0, food.metadata));
            variants.add(item);
            IBakedModel model = models.getModel(new ModelResourceLocation(
                    "ironagefurniture:surface_food/" + food.model, "inventory"));
            require(model != models.getMissingModel(), "Missing baked food " + food.model);
            require(client.getRenderItem().getItemModelMesher().getItemModel(item) != model,
                    "Surface food replaced its ordinary inventory model");
            AxisAlignedBB baked = bakedBounds(model).offset(-0.5D, 0.0D, -0.5D);
            sameBounds(food.descriptor.getBounds(EnumFacing.NORTH), baked, food.model);
            require(SurfaceRenderRegistry.get(item) != null, "Food renderer was not registered");
        }
        variants.add(new ItemStack(Items.GOLDEN_APPLE, 1, 1));
        int cases = 0;
        for (String id : new String[]{"table_dining_oak", "cabinet_wood_ironage_oak"}) {
            Block host = Block.getBlockFromName("ironagefurniture:" + id);
            for (ItemStack item : variants) for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
                client.world.setBlockToAir(pos.up());
                client.world.setBlockToAir(pos);
                client.world.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 2);
                IBlockState state = host.getDefaultState();
                if (host instanceof DiningTable) state = state.withProperty(DiningTable.DATA, true);
                client.world.setBlockState(pos, state, 2);
                SurfaceSettingHost surface = (SurfaceSettingHost)client.world.getTileEntity(pos);
                ItemStack original = item.copy().setStackDisplayName("Food model test");
                original.getTagCompound().setInteger("TestData", 73);
                surface.getSurfaceSetting().setSingleItem(original, facing);
                client.world.setBlockState(pos.up(), blocker.getDefaultState(), 2);
                AxisAlignedBB bounds = blocker.getSelectedBoundingBox(blocker.getDefaultState(), client.world, pos.up());
                sameBounds(bounds, VanillaSurfaceFoods.find(original).descriptor.getBounds(facing)
                        .offset(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D), "Food selection");
                require(bounds.maxY - bounds.minY > 0.06D, "Food still has a flat sprite selection");
                double x = (bounds.minX + bounds.maxX) / 2.0D;
                double z = (bounds.minZ + bounds.maxZ) / 2.0D;
                require(blocker.collisionRayTrace(blocker.getDefaultState(), client.world, pos.up(),
                        new Vec3d(x, bounds.maxY + 1.0D, z), new Vec3d(x, bounds.minY, z)) != null,
                        "Cannot target food model");
                TileEntity tile = (TileEntity)surface;
                SurfaceSettingHost reloaded = (SurfaceSettingHost)TileEntity.create(client.world,
                        tile.writeToNBT(new NBTTagCompound()));
                require(ItemStack.areItemStacksEqual(original, reloaded.getSurfaceSetting().getCompatibilityItem()),
                        "Food metadata or NBT changed");
                require(client.playerController.onPlayerDestroyBlock(pos.up()), "Cannot break food display");
                require(!surface.getSurfaceSetting().hasAnyItem()
                        && client.world.getBlockState(pos).getBlock() == host, "Food break damaged furniture");
                cases++;
            }
        }
        LogManager.getLogger().info("IAF VANILLA FOOD CLIENT PROBE PASSED: {} variants, {} placements", variants.size(), cases);
        return cases + verifyVanillaSurfaces(client, pos, variants);
    }

    private static int verifyVanillaSurfaces(Minecraft client, BlockPos pos, List<ItemStack> variants) {
        Block display = Block.getBlockFromName("ironagefurniture:vanilla_food_display");
        int cases = 0;
        for (ItemStack item : variants) {
            if (!VanillaFoodPlacement.isKnownFood(item)) continue;
            for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
                client.world.setBlockToAir(pos.up());
                client.world.setBlockState(pos, Blocks.PLANKS.getDefaultState(), 2);
                client.world.setBlockState(pos.up(), display.getDefaultState(), 2);
                SurfaceSettingHost host = (SurfaceSettingHost)client.world.getTileEntity(pos.up());
                host.getSurfaceSetting().setSingleItem(item, facing);
                AxisAlignedBB bounds = display.getSelectedBoundingBox(display.getDefaultState(), client.world, pos.up());
                sameBounds(bounds, VanillaSurfaceFoods.find(item).descriptor.getBounds(facing)
                        .offset(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D), "Vanilla surface selection");
                double x = (bounds.minX + bounds.maxX) / 2.0D;
                double z = (bounds.minZ + bounds.maxZ) / 2.0D;
                require(display.collisionRayTrace(display.getDefaultState(), client.world, pos.up(),
                        new Vec3d(x, bounds.maxY + 1.0D, z), new Vec3d(x, bounds.minY, z)) != null,
                        "Cannot target food on vanilla block");
                require(client.playerController.onPlayerDestroyBlock(pos.up()), "Cannot break vanilla food display");
                require(!host.getSurfaceSetting().hasAnyItem() && client.world.isAirBlock(pos.up())
                        && client.world.getBlockState(pos).getBlock() == Blocks.PLANKS,
                        "Creative food removal damaged the vanilla block or retained a ghost");
                cases++;
            }
        }
        LogManager.getLogger().info("IAF VANILLA FOOD SURFACE CLIENT PROBE PASSED: {} placements", cases);
        return cases;
    }

    private static AxisAlignedBB bakedBounds(IBakedModel model) {
        AxisAlignedBB result = null;
        for (int side = -1; side < 6; side++) {
            for (BakedQuad quad : model.getQuads(null, side < 0 ? null : EnumFacing.getFront(side), 0L)) {
                require("ironagefurniture:blocks/surface_foods".equals(quad.getSprite().getIconName()),
                        "Food model has a missing texture");
                int[] data = quad.getVertexData();
                int stride = quad.getFormat().getIntegerSize();
                for (int vertex = 0; vertex < 4; vertex++) {
                    int start = vertex * stride;
                    double x = Float.intBitsToFloat(data[start]);
                    double y = Float.intBitsToFloat(data[start + 1]);
                    double z = Float.intBitsToFloat(data[start + 2]);
                    AxisAlignedBB point = new AxisAlignedBB(x, y, z, x, y, z);
                    result = result == null ? point : result.union(point);
                }
            }
        }
        require(result != null, "Food model has no geometry");
        return result;
    }

    static void prepareGallery(Minecraft client) {
        gallery = client.player.getPosition().add(-2, 5, 3);
        ItemStack[] items = {new ItemStack(Items.BREAD), new ItemStack(Items.APPLE),
                new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.COOKIE),
                new ItemStack(Items.MUSHROOM_STEW), new ItemStack(Items.COOKED_CHICKEN),
                new ItemStack(Items.FISH, 1, 1), new ItemStack(Items.PUMPKIN_PIE),
                new ItemStack(Items.CARROT), new ItemStack(Items.MELON),
                new ItemStack(Items.CAKE), new ItemStack(Items.BEETROOT)};
        Block table = Block.getBlockFromName("ironagefurniture:table_dining_oak");
        Block blocker = Block.getBlockFromName("ironagefurniture:surface_display_blocker");
        for (int index = 0; index < items.length; index++) {
            BlockPos pos = gallery.add(index % 4, 0, index / 4);
            client.world.setBlockState(pos.down(), Blocks.PLANKS.getDefaultState(), 2);
            client.world.setBlockState(pos, table.getDefaultState().withProperty(DiningTable.DATA, true), 2);
            ((SurfaceSettingHost)client.world.getTileEntity(pos)).getSurfaceSetting()
                    .setSingleItem(items[index], EnumFacing.NORTH);
            client.world.setBlockState(pos.up(), blocker.getDefaultState(), 2);
        }
        client.displayGuiScreen(null);
        client.gameSettings.hideGUI = true;
        client.gameSettings.pauseOnLostFocus = false;
        client.gameSettings.showInventoryAchievementHint = false;
        client.guiAchievement.clearAchievements();
        client.gameSettings.viewBobbing = false;
        client.gameSettings.fovSetting = 40.0F;
        client.player.capabilities.isFlying = true;
        positionCamera(client, 1.5D, 1.3D, -1.9D, 1.5D, 1.2D, 0.6D);
    }

    @SubscribeEvent
    public static void capture(TickEvent.ClientTickEvent event) {
        if (gallery == null || event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getMinecraft();
        client.world.setWorldTime(6000L);
        client.player.motionX = client.player.motionY = client.player.motionZ = 0.0D;
        galleryTicks++;
        if (galleryTicks == 35) {
            screenshot(client, "bread-and-apples.png");
            positionCamera(client, 6.0D, 3.5D, -4.5D, 2.0D, 1.2D, 1.4D);
        }
        if (galleryTicks == 65) {
            screenshot(client, "vanilla-food-gallery.png");
            Block display = Block.getBlockFromName("ironagefurniture:vanilla_food_display");
            BlockPos bar = gallery.east(8);
            for (int x = 0; x < 4; x++) for (int z = 0; z < 2; z++) {
                BlockPos pos = bar.add(x, 0, z);
                client.world.setBlockState(pos.down(), Blocks.PLANKS.getDefaultState(), 2);
                client.world.setBlockState(pos, Blocks.PLANKS.getDefaultState(), 2);
                client.world.setBlockState(pos.up(), display.getDefaultState(), 2);
                ItemStack[] foods = {new ItemStack(Items.BREAD), new ItemStack(Items.APPLE),
                        new ItemStack(Items.COOKED_BEEF), new ItemStack(Items.MUSHROOM_STEW),
                        new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.COOKIE),
                        new ItemStack(Items.COOKED_CHICKEN), new ItemStack(Items.PUMPKIN_PIE)};
                ((SurfaceSettingHost)client.world.getTileEntity(pos.up())).getSurfaceSetting()
                        .setSingleItem(foods[x + z * 4], EnumFacing.NORTH);
            }
            gallery = bar;
            positionCamera(client, 1.5D, 1.3D, -1.9D, 1.5D, 1.2D, 0.6D);
        }
        if (galleryTicks == 95) {
            screenshot(client, "vanilla-food-plank-bar.png");
            gallery = Phase5ShelfTableClientProbe.prepareScene(client, gallery.east(8));
            positionCamera(client, 2.5D, 2.2D, -4.5D, 2.5D, 0.8D, 1.7D);
        }
        if (galleryTicks == 125) {
            screenshot(client, "phase5-low-tables-and-shelves.png");
            gallery = null;
            client.shutdown();
        }
    }

    private static void positionCamera(Minecraft client, double x, double y, double z,
            double targetX, double targetY, double targetZ) {
        double dx = targetX - x;
        double dz = targetZ - z;
        double dy = targetY - y - client.player.getEyeHeight();
        float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float)-Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
        client.player.setPositionAndRotation(gallery.getX() + x, gallery.getY() + y, gallery.getZ() + z, yaw, pitch);
        client.player.prevPosX = client.player.lastTickPosX = client.player.posX;
        client.player.prevPosY = client.player.lastTickPosY = client.player.posY;
        client.player.prevPosZ = client.player.lastTickPosZ = client.player.posZ;
        client.player.prevRotationYaw = yaw;
        client.player.prevRotationPitch = pitch;
    }

    private static void screenshot(Minecraft client, String name) {
        ScreenShotHelper.saveScreenshot(new File("."), name, client.displayWidth, client.displayHeight, client.getFramebuffer());
        require(new File("screenshots", name).isFile(), "Food screenshot was not saved");
    }

    private static void sameBounds(AxisAlignedBB a, AxisAlignedBB b, String message) {
        require(Math.abs(a.minX - b.minX) < 0.00001D && Math.abs(a.minY - b.minY) < 0.00001D
                && Math.abs(a.minZ - b.minZ) < 0.00001D && Math.abs(a.maxX - b.maxX) < 0.00001D
                && Math.abs(a.maxY - b.maxY) < 0.00001D && Math.abs(a.maxZ - b.maxZ) < 0.00001D, message);
    }

    private static void require(boolean pass, String message) {
        if (!pass) throw new IllegalStateException(message);
    }
}
