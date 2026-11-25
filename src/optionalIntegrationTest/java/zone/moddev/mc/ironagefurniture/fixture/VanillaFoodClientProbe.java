package zone.moddev.mc.ironagefurniture.fixture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import zone.moddev.mc.ironagefurniture.api.Blocks.DiningTable;
import zone.moddev.mc.skysfurniturecore.SkysFurnitureCore;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceSettingHost;
import zone.moddev.mc.skysfurniturecore.api.surface.VanillaSurfaceFoods;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaFoodPlacement;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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
    private static long galleryTime = 6000L;
    private VanillaFoodClientProbe() { }

    static int verify(Minecraft client, BlockPos pos) {
        Block blocker = Block.getBlockFromName("ironagefurniture:surface_display_blocker");
        ModelManager models = client.getRenderItem().getItemModelMesher().getModelManager();
        List<ItemStack> variants = new ArrayList<>();
        for (VanillaSurfaceFoods.Food food : VanillaSurfaceFoods.all()) {
            ItemStack item = new ItemStack(food.item, 1, Math.max(0, food.metadata));
            variants.add(item);
            IBakedModel model = models.getModel(new ModelResourceLocation(
                    SkysFurnitureCore.MODID + ":surface_food/" + food.model, "inventory"));
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
                require((SkysFurnitureCore.MODID + ":blocks/surface_foods").equals(quad.getSprite().getIconName()),
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
    public static void capture(TickEvent.ClientTickEvent event) throws IOException {
        if (gallery == null || event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getMinecraft();
        client.world.setWorldTime(galleryTime);
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
            positionCamera(client, 4.5D, 1.05D - client.player.getEyeHeight(), -0.8D, 4.5D, 0.8D, 0.5D);
        }
        if (galleryTicks == 140) {
            screenshot(client, "phase5-vase-closeup-front.png");
            positionCamera(client, 4.5D, 1.05D - client.player.getEyeHeight(), 1.8D, 4.5D, 0.8D, 0.5D);
        }
        if (galleryTicks == 155) {
            screenshot(client, "phase5-vase-closeup-back.png");
            gallery = Phase5StorageClientScene.scene(client, gallery.east(12));
            positionCamera(client, 4.0D, 1.8D, -4.5D, 4.0D, 0.8D, 3.0D);
        }
        if (galleryTicks == 185) {
            screenshot(client, "phase5-storage.png");
            Phase5StorageClientScene.gui(client, gallery);
        }
        if (galleryTicks == 215) {
            screenshot(client, "phase5-barrel-gui.png");
            client.displayGuiScreen(new net.minecraft.client.gui.inventory.GuiInventory(client.player));
        }
        if (galleryTicks == 245) {
            screenshot(client, "phase5-rack-inventory.png");
            client.displayGuiScreen(null);
            client.gameSettings.hideGUI = true;
            gallery = Phase5StorageClientScene.rackJoins(client, gallery.east(16));
            positionCamera(client, 3.5D, 4.3D - client.player.getEyeHeight(), -1.5D, 3.5D, 0.8D, 1.6D);
        }
        if (galleryTicks == 275) {
            screenshot(client, "phase5-rack-joins-wall.png");
            positionCamera(client, 3.5D, 4.3D - client.player.getEyeHeight(), 8.0D, 3.5D, 0.8D, 5.5D);
        }
        if (galleryTicks == 305) {
            screenshot(client, "phase5-rack-joins-standing.png");
            gallery = SurfaceBottleClientProbe.scene(client, gallery.east(16));
            double centre = Phase5BottleProbe.samples().size() / 2.0D;
            positionCamera(client, centre, 3.0D - client.player.getEyeHeight(), -4.5D, centre, 1.0D, 1.3D);
        }
        if (galleryTicks == 335) {
            screenshot(client, "phase5-surface-bottles.png");
            positionCamera(client, 1.5D, 1.2D - client.player.getEyeHeight(), -0.8D, 1.5D, 0.75D, 0.5D);
        }
        boolean brewing = Phase5BottleProbe.samples().size() > 6;
        if (galleryTicks == 365) {
            screenshot(client, "phase5-potion-closeup.png");
            if (brewing) {
                positionCamera(client, 8.0D, 1.2D - client.player.getEyeHeight(), -1.2D, 8.0D, 0.85D, 0.5D);
            }
        }
        if (brewing && galleryTicks == 395) {
            screenshot(client, "phase5-brewing-bottle-levels.png");
        }
        if (galleryTicks == (brewing ? 395 : 365)) {
            gallery = Phase5StorageClientScene.shelvingUnits(client, gallery.east(16));
            positionCamera(client, 2.5D, 2.8D - client.player.getEyeHeight(), -2.2D, 2.5D, 1.1D, 2.2D);
        }
        int pantryStart = brewing ? 425 : 395;
        if (galleryTicks == pantryStart - 12) {
            positionCamera(client, 4.0D, 2.25D - client.player.getEyeHeight(), 0.35D, 4.5D, 1.5D, 2.5D);
        }
        if (galleryTicks == pantryStart - 1) {
            screenshot(client, "phase5-shelf-paired-vases.png");
        }
        if (galleryTicks == pantryStart) {
            screenshot(client, "phase5-shelving-units.png");
            gallery = Phase5StorageClientScene.pantryShelves(client, gallery.east(16));
        }
        if (galleryTicks >= pantryStart && galleryTicks < pantryStart + 30) {
            positionCamera(client, 2.5D, 1.75D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int kitchenStart = pantryStart + 30;
        if (galleryTicks == kitchenStart) {
            screenshot(client, "phase5-shelf-pantry.png");
            require(ShelvingPantryClientExtension.renderCalls > 0, "Shelf layout did not invoke the add-on renderer");
            org.apache.logging.log4j.LogManager.getLogger().info("IAF SHELVING PANTRY CLIENT PASSED: add-on renderer and native display capture");
            gallery = Phase5StorageClientScene.kitchenShelves(client, gallery.east(16));
        }
        if (galleryTicks >= kitchenStart && galleryTicks < kitchenStart + 30) {
            positionCamera(client, 2.0D, 1.8D - client.player.getEyeHeight(), -1.15D, 2.0D, 0.5D, 2.55D);
        }
        int butcherStart = kitchenStart + 30;
        if (galleryTicks == butcherStart) {
            screenshot(client, "phase5-shelf-kitchen.png");
            gallery = Phase5StorageClientScene.butcherShelves(client, gallery.east(16));
        }
        if (galleryTicks >= butcherStart && galleryTicks < butcherStart + 30) {
            positionCamera(client, 2.5D, 1.75D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int fishmongerStart = butcherStart + 30;
        if (galleryTicks == fishmongerStart) {
            screenshot(client, "phase5-shelf-butcher.png");
            gallery = Phase5StorageClientScene.fishmongerShelves(client, gallery.east(16));
        }
        if (galleryTicks >= fishmongerStart && galleryTicks < fishmongerStart + 30) {
            positionCamera(client, 2.0D, 1.8D - client.player.getEyeHeight(), -1.15D, 2.0D, 0.5D, 2.55D);
        }
        int workshopStart = fishmongerStart + 30;
        if (galleryTicks == workshopStart) {
            screenshot(client, "phase5-shelf-fishmonger.png");
            gallery = Phase5StorageClientScene.workshopShelves(client, gallery.east(16));
        }
        if (galleryTicks >= workshopStart && galleryTicks < workshopStart + 30) {
            positionCamera(client, 2.5D, 1.75D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int strikerStart = workshopStart + 30;
        if (galleryTicks == strikerStart) {
            screenshot(client, "phase5-shelf-workshop.png");
        }
        if (galleryTicks >= strikerStart && galleryTicks < strikerStart + 30) {
            positionCamera(client, 3.52D, 1.32D - client.player.getEyeHeight(), 1.06D, 3.50D, 0.58D, 2.55D);
        }
        int archeryStart = strikerStart + 30;
        if (galleryTicks == archeryStart) {
            screenshot(client, "phase5-shelf-flint-and-steel.png");
            gallery = Phase5StorageClientScene.archeryShelves(client, gallery.east(16));
        }
        if (galleryTicks >= archeryStart && galleryTicks < archeryStart + 30) {
            positionCamera(client, 1.0D, 1.62D - client.player.getEyeHeight(), 0.30D, 1.0D, 0.5D, 2.55D);
        }
        int ingredientStart = archeryStart + 30;
        if (galleryTicks == ingredientStart) {
            screenshot(client, "phase5-shelf-archery.png");
            gallery = Phase5StorageClientScene.cookingIngredientShelves(client, gallery.east(16));
        }
        if (galleryTicks >= ingredientStart && galleryTicks < ingredientStart + 30) {
            positionCamera(client, 2.5D, 1.75D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int remainingFoodStart = ingredientStart + 30;
        if (galleryTicks == remainingFoodStart) {
            screenshot(client, "phase5-shelf-cooking-ingredients.png");
            gallery = Phase5StorageClientScene.remainingFoodShelves(client, gallery.east(16));
        }
        if (galleryTicks >= remainingFoodStart && galleryTicks < remainingFoodStart + 30) {
            positionCamera(client, 2.5D, 1.75D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int brewingPowderStart = remainingFoodStart + 30;
        if (galleryTicks == brewingPowderStart) {
            screenshot(client, "phase5-shelf-seeds-and-forage.png");
            gallery = Phase5StorageClientScene.brewingPowderShelves(client, gallery.east(16));
        }
        if (galleryTicks >= brewingPowderStart && galleryTicks < brewingPowderStart + 30) {
            positionCamera(client, 2.5D, 1.75D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int brewingIngredientStart = brewingPowderStart + 30;
        if (galleryTicks == brewingIngredientStart) {
            screenshot(client, "phase5-shelf-potion-powders-and-specimens.png");
            gallery = Phase5StorageClientScene.brewingIngredientShelves(client, gallery.east(16));
        }
        if (galleryTicks >= brewingIngredientStart && galleryTicks < brewingIngredientStart + 30) {
            positionCamera(client, 2.5D, 1.75D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int smithingStart = brewingIngredientStart + 30;
        if (galleryTicks == smithingStart) {
            screenshot(client, "phase5-shelf-potion-ingredients.png");
            gallery = Phase5StorageClientScene.smithingShelves(client, gallery.east(16));
        }
        if (galleryTicks >= smithingStart && galleryTicks < smithingStart + 30) {
            positionCamera(client, 2.5D, 1.75D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int containerStart = smithingStart + 30;
        if (galleryTicks == containerStart) {
            screenshot(client, "phase5-shelf-mining-and-smithing.png");
            gallery = Phase5StorageClientScene.containerShelves(client, gallery.east(16));
        }
        if (galleryTicks >= containerStart && galleryTicks < containerStart + 30) {
            positionCamera(client, 2.5D, 1.75D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int travelStart = containerStart + 30;
        if (galleryTicks == travelStart) {
            screenshot(client, "phase5-shelf-containers.png");
            gallery = Phase5StorageClientScene.travelShelves(client, gallery.east(16));
        }
        if (galleryTicks >= travelStart && galleryTicks < travelStart + 60) {
            positionCamera(client, 2.05D, 1.52D - client.player.getEyeHeight(), -1.45D, 2.05D, 0.5D, 2.6D);
        }
        if (galleryTicks == travelStart + 30) {
            require(client.world.getWorldTime() == 6000 && client.world.getCelestialAngle(0) < 0.01F,
                "Travel gallery expected noon, found time " + client.world.getWorldTime()
                    + " at angle " + client.world.getCelestialAngle(0));
            screenshot(client, "phase5-shelf-travel-gear.png");
            galleryTime = 18000L;
        }
        int armourStart = travelStart + 60;
        if (galleryTicks == armourStart) {
            require(client.world.getWorldTime() == 18000 && Math.abs(client.world.getCelestialAngle(0) - 0.5F) < 0.01F,
                "Travel gallery expected midnight, found time " + client.world.getWorldTime()
                    + " at angle " + client.world.getCelestialAngle(0));
            screenshot(client, "phase5-shelf-travel-gear-night.png");
            galleryTime = 6000L;
            gallery = Phase5StorageClientScene.armourShelves(client, gallery.east(16));
        }
        if (galleryTicks == armourStart + 24) {
            screenshot(client, "phase5-shelf-armour.png");
            gallery = Phase5StorageClientScene.trophyShelves(client, gallery.east(16));
        }
        if (galleryTicks >= armourStart && galleryTicks < armourStart + 48) {
            positionCamera(client, 2.5D, 1.65D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int craftingSupplyStart = armourStart + 48;
        if (galleryTicks == craftingSupplyStart) {
            screenshot(client, "phase5-shelf-trophies.png");
            gallery = Phase5StorageClientScene.craftingSupplyShelves(client, gallery.east(16));
        }
        if (galleryTicks >= craftingSupplyStart && galleryTicks < craftingSupplyStart + 30) {
            positionCamera(client, 2.5D, 1.75D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int dyeStart = craftingSupplyStart + 30;
        if (galleryTicks == dyeStart) {
            screenshot(client, "phase5-shelf-crafting-supplies.png");
            gallery = Phase5StorageClientScene.dyeShelves(client, gallery.east(16), 0);
        }
        if (galleryTicks == dyeStart + 24) {
            screenshot(client, "phase5-shelf-dyes-0-to-7.png");
            gallery = Phase5StorageClientScene.dyeShelves(client, gallery.east(16), 8);
        }
        if (galleryTicks >= dyeStart && galleryTicks < dyeStart + 48) {
            positionCamera(client, 2.5D, 1.94D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int mountedGearStart = dyeStart + 48;
        if (galleryTicks == mountedGearStart) {
            screenshot(client, "phase5-shelf-dyes-8-to-15.png");
            gallery = Phase5StorageClientScene.mountedGearShelves(client, gallery.east(16));
        }
        if (galleryTicks >= mountedGearStart && galleryTicks < mountedGearStart + 24) {
            positionCamera(client, 2.5D, 1.66D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int materialDropStart = mountedGearStart + 24;
        if (galleryTicks == materialDropStart) {
            screenshot(client, "phase5-shelf-mounted-gear.png");
            gallery = Phase5StorageClientScene.materialDropShelves(client, gallery.east(16));
        }
        if (galleryTicks == materialDropStart + 20) {
            screenshot(client, "phase5-shelf-materials-and-drops.png");
            gallery = Phase5StorageClientScene.prismarineAndChorusShelves(client, gallery.east(16));
        }
        if (galleryTicks >= materialDropStart && galleryTicks < materialDropStart + 40) {
            positionCamera(client, 2.5D, 1.82D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        int magicStart = materialDropStart + 40;
        if (galleryTicks == magicStart) {
            screenshot(client, "phase5-shelf-prismarine-and-chorus.png");
            gallery = Phase5StorageClientScene.magicShelves(client, gallery.east(16));
        }
        if (galleryTicks == magicStart + 20) {
            screenshot(client, "phase5-shelf-magic.png");
            gallery = Phase5StorageClientScene.fireworkAndEggShelves(client, gallery.east(16));
        }
        if (galleryTicks >= magicStart && galleryTicks < magicStart + 40) {
            positionCamera(client, 2.5D, 1.80D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        if (galleryTicks == magicStart + 40) {
            screenshot(client, "phase5-shelf-fireworks-and-spawn-eggs.png");
            gallery = Phase5StorageClientScene.boatShelves(client, gallery.east(16));
            positionCamera(client, 2.5D, 1.90D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        if (galleryTicks == magicStart + 60) {
            screenshot(client, "phase5-shelf-boats.png");
            gallery = Phase5StorageClientScene.cartAndDecorShelves(client, gallery.east(16));
            positionCamera(client, 2.5D, 1.75D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        if (galleryTicks == magicStart + 80) {
            screenshot(client, "phase5-shelf-minecarts-and-decor.png");
            gallery = Phase5StorageClientScene.potShelves(client, gallery.east(16));
            positionCamera(client, 2.0D, 1.48D - client.player.getEyeHeight(), 0.25D, 2.0D, 0.5D, 2.5D);
        }
        if (galleryTicks == magicStart + 100) {
            screenshot(client, "phase5-shelf-pots-and-labels.png");
            gallery = Phase5StorageClientScene.utilityShelves(client, gallery.east(16));
            positionCamera(client, 1.0D, 1.23D - client.player.getEyeHeight(), -0.12D, 1.0D, 0.5D, 2.5D);
        }
        if (galleryTicks == magicStart + 120) {
            screenshot(client, "phase5-shelf-brewing-and-redstone.png");
            gallery = Phase5StorageClientScene.doorAndBedShelves(client, gallery.east(16));
            positionCamera(client, 2.5D, 1.36D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        if (galleryTicks == magicStart + 140) {
            screenshot(client, "phase5-shelf-doors-and-bed.png");
            gallery = Phase5StorageClientScene.lightingShelves(client, gallery.east(16));
            positionCamera(client, 2.5D, 1.45D - client.player.getEyeHeight(), -1.55D, 2.5D, 0.5D, 2.5D);
        }
        if (galleryTicks == magicStart + 160) {
            screenshot(client, "phase5-shelf-mod-items.png");
            galleryTime = 18000L;
        }
        if (galleryTicks == magicStart + 183) {
            ShelfLightingClientProbe.verify(client, gallery);
        }
        boolean baseMetals = net.minecraftforge.fml.common.Loader.isModLoaded("basemetals");
        boolean modernMetals = net.minecraftforge.fml.common.Loader.isModLoaded("modernmetals");
        boolean baseGems = net.minecraftforge.fml.common.Loader.isModLoaded("basegems");
        boolean baseMinerals = net.minecraftforge.fml.common.Loader.isModLoaded("baseminerals");
        if (galleryTicks == magicStart + 185) {
            screenshot(client, "phase5-shelf-lighting.png");
            if (baseMetals) {
                galleryTime = 6000L;
                gallery = Phase5StorageClientScene.baseMetalMaterials(client, gallery.east(16));
                positionCamera(client, 2.5D, 1.72D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
            }
        }
        if (baseMetals && galleryTicks == magicStart + 209) {
            screenshot(client, "phase5-shelf-base-metals-materials.png");
            gallery = Phase5StorageClientScene.baseMetalGear(client, gallery.east(16));
            positionCamera(client, 2.5D, 1.35D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        if (baseMetals && galleryTicks == magicStart + 233) {
            screenshot(client, "phase5-shelf-base-metals-gear.png");
            gallery = Phase5StorageClientScene.baseMetalHardware(client, gallery.east(16));
            positionCamera(client, 2.0D, 1.55D - client.player.getEyeHeight(), -0.95D, 2.0D, 0.5D, 2.5D);
        }
        if (baseMetals && galleryTicks == magicStart + 257) {
            screenshot(client, "phase5-shelf-base-metals-hardware.png");
            if (modernMetals) {
                gallery = Phase5StorageClientScene.modernMetalMaterials(client, gallery.east(16));
                positionCamera(client, 2.5D, 1.72D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
            }
        }
        if (modernMetals && galleryTicks == magicStart + 281) {
            screenshot(client, "phase5-shelf-modern-metals-materials.png");
            gallery = Phase5StorageClientScene.modernMetalGear(client, gallery.east(16));
            positionCamera(client, 2.5D, 1.55D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
        }
        if (modernMetals && galleryTicks == magicStart + 305) {
            screenshot(client, "phase5-shelf-modern-metals-gear.png");
        }
        int galleryEnd = modernMetals ? 305 : (baseMetals ? 257 : 185);
        if (baseGems) {
            if (galleryTicks == magicStart + galleryEnd) {
                gallery = Phase5StorageClientScene.baseGemPlatters(client, gallery.east(16));
                positionCamera(client, 2.0D, 1.5D - client.player.getEyeHeight(), -0.25D, 2.0D, 0.52D, 2.5D);
            }
            if (galleryTicks == magicStart + galleryEnd + 24) {
                screenshot(client, "phase5-shelf-base-gems.png");
                positionCamera(client, 3.05D, 1.47D - client.player.getEyeHeight(), 0.25D, 3.4D, 0.52D, 2.5D);
            }
            galleryEnd += 48;
            if (galleryTicks == magicStart + galleryEnd) {
                screenshot(client, "phase5-shelf-base-gems-close-up.png");
            }
        }
        if (baseMinerals) {
            if (galleryTicks == magicStart + galleryEnd) {
                gallery = Phase5StorageClientScene.baseMineralSamples(client, gallery.east(16));
                positionCamera(client, 2.5D, 1.55D - client.player.getEyeHeight(), -1.65D, 2.5D, 0.5D, 2.5D);
            }
            galleryEnd += 24;
            if (galleryTicks == magicStart + galleryEnd) {
                screenshot(client, "phase5-shelf-base-minerals.png");
            }
        }
        if (galleryTicks == magicStart + galleryEnd) {
            galleryTime = 6000L;
            gallery = Phase5StorageClientScene.bulkBarrels(client, gallery.east(16));
            positionCamera(client, 2.5D, 4.65D - client.player.getEyeHeight(), -0.2D, 2.5D, 0.48D, 2.5D);
        }
        galleryEnd += 26;
        if (galleryTicks == magicStart + galleryEnd) {
            screenshot(client, "phase5-bulk-barrels.png");
            positionCamera(client, -1.0D, 3.45D - client.player.getEyeHeight(), 3.0D, 0.60D, 0.75D, 3.0D);
        }
        galleryEnd += 16;
        if (galleryTicks == magicStart + galleryEnd) {
            screenshot(client, "phase5-carrot-barrels.png");
            positionCamera(client, 2.5D, 1.45D - client.player.getEyeHeight(), -1.9D, 2.5D, 0.55D, 2.5D);
        }
        galleryEnd += 24;
        if (galleryTicks == magicStart + galleryEnd) {
            screenshot(client, "phase5-barrel-labels.png");
            gallery = Phase5StorageClientScene.bedsideCabinets(client, gallery.east(12));
            positionCamera(client, 2.35D, 1.6D - client.player.getEyeHeight(), -1.5D, 2.0D, 0.65D, 2.5D);
        }
        galleryEnd += 24;
        if (galleryTicks == magicStart + galleryEnd) {
            screenshot(client, "phase5-bedside-cabinets.png");
            gallery = Phase5StorageClientScene.shippingCrates(client, gallery.east(12));
            positionCamera(client, 2.05D, 1.48D - client.player.getEyeHeight(), -2.4D, 1.5D, 0.92D, 2.5D);
        }
        galleryEnd += 24;
        if (galleryTicks == magicStart + galleryEnd) {
            screenshot(client, "phase5-shipping-crates.png");
            gallery = Phase5StorageClientScene.storageBenches(client, gallery.east(12));
            positionCamera(client, 2.35D, 1.72D - client.player.getEyeHeight(), -0.85D, 2.0D, 0.45D, 2.45D);
        }
        galleryEnd += 24;
        if (galleryTicks == magicStart + galleryEnd) {
            screenshot(client, "phase5-storage-benches.png");
            gallery = Phase5StorageClientScene.produceFurniture(client, gallery.east(12));
            ShelvingPantryClientExtension.renderCalls = 0;
            positionCamera(client, 2.45D, 2.15D - client.player.getEyeHeight(), -1.25D, 2.45D, 0.95D, 2.5D);
        }
        galleryEnd += 24;
        if (galleryTicks == magicStart + galleryEnd) {
            require(ShelvingPantryClientExtension.renderCalls > 0, "Produce tray did not use the add-on renderer");
            screenshot(client, "phase5-produce-furniture.png");
            positionCamera(client, -3.9D, 1.78D - client.player.getEyeHeight(), -1.15D, -0.8D, 0.98D, 2.5D);
        }
        galleryEnd += 24;
        if (galleryTicks == magicStart + galleryEnd) {
            screenshot(client, "phase5-produce-rack-braces.png");
            Phase5StorageClientScene.showFurnitureInventory(client);
        }
        galleryEnd += 10;
        if (galleryTicks == magicStart + galleryEnd) {
            screenshot(client, "phase5-furniture-inventory.png");
            LogManager.getLogger().info("IAF PRODUCE CLIENT PASSED: inventory models, slope display, joins and add-on renderer");
            VanillaFoodNetworkClientProbe.writePassReceipt();
            gallery = null;
            // Let Minecraft stop its integrated server before closing the client connection.
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

    static void screenshot(Minecraft client, String name) {
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
