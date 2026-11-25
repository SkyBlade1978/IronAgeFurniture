package zone.moddev.mc.ironagefurniture;

import java.util.HashMap;
import java.util.Map;

import zone.moddev.mc.ironagefurniture.api.entity.Seat;
import zone.moddev.mc.ironagefurniture.api.CreativeModeBreakTracker;
import zone.moddev.mc.ironagefurniture.api.StorageGuiHandler;
import zone.moddev.mc.ironagefurniture.api.FluidVesselInteractions;
import zone.moddev.mc.ironagefurniture.api.network.StorageNetwork;
import zone.moddev.mc.ironagefurniture.api.network.ShelfMapSync;
import zone.moddev.mc.ironagefurniture.api.Blocks.SurfaceReservationRepair;
import zone.moddev.mc.ironagefurniture.api.entity.EntityThrownLavaLamp;
import zone.moddev.mc.ironagefurniture.api.entity.EntityReleasedLavaLamp;
import zone.moddev.mc.ironagefurniture.api.CfmChairMigration;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityMetalVariant;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityShieldChair;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityUpholstery;
import zone.moddev.mc.ironagefurniture.client.resources.GeneratedModelResourcePack;
import zone.moddev.mc.ironagefurniture.client.particle.HeldCandleSmoke;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityPaddedBench;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.skysfurniturecore.api.surface.SurfaceInteractionRegistry;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaFoodPlacement;
import zone.moddev.mc.ironagefurniture.api.FluidAgingRegistry;
import zone.moddev.mc.ironagefurniture.api.LegacyFluidRegistration;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry;
import zone.moddev.mc.ironagefurniture.init.BlockInitialiser;
import zone.moddev.mc.ironagefurniture.init.ClientItemInitialiser;
import zone.moddev.mc.ironagefurniture.init.ClientModelInitialiser;
import zone.moddev.mc.ironagefurniture.init.ClientRenderInitialiser;
import zone.moddev.mc.ironagefurniture.init.RecipeInitialiser;
import zone.moddev.mc.ironagefurniture.init.FurnitureCreativeTabs;
import zone.moddev.mc.ironagefurniture.init.ItemInitialiser;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = Ironagefurniture.MODID, version = Ironagefurniture.VERSION,
	dependencies = "required-after:skysfurniturecore@[0.1.0.110021,0.2)")
public class Ironagefurniture {
	public static final String MODID = "ironagefurniture";
	public static final String VERSION = "0.5.0.110021";

	@Mod.Instance(MODID)
	public static Ironagefurniture instance;

	public static final Map<String, Block> BlockRegistry = new HashMap<>();
	public static final Map<String, Item> ItemRegistry = new HashMap<>();
	public static CreativeTabs ironagefurnitureTab = FurnitureCreativeTabs.CHAIRS;

	private final CfmChairMigration cfmChairMigration = new CfmChairMigration();

	@EventHandler
	public void init(FMLInitializationEvent event) {
		if (event.getSide().isClient()) {
			ClientItemInitialiser.registerItemRenders();
		}

		EntityRegistry.registerModEntity(Seat.class, MODID + ":seat", 0, this, 80, 1, false);
		EntityRegistry.registerModEntity(EntityThrownLavaLamp.class,
				MODID + ":thrown_lava_lamp", 1, this, 64, 10, true);
		EntityRegistry.registerModEntity(EntityReleasedLavaLamp.class,
				MODID + ":released_lava_lamp", 2, this, 160, 20, true);

		RecipeInitialiser.init();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LegacyFluidRegistration.runPending();
		IronAgeFurnitureConfiguration.init(event);

		MinecraftForge.EVENT_BUS.register(new CreativeModeBreakTracker());
		MinecraftForge.EVENT_BUS.register(new VanillaFoodPlacement());
		MinecraftForge.EVENT_BUS.register(cfmChairMigration);
		MinecraftForge.EVENT_BUS.register(ShelfMapSync.INSTANCE);
		FMLCommonHandler.instance().bus().register(cfmChairMigration);
		FMLCommonHandler.instance().bus().register(ShelfMapSync.INSTANCE);
		FMLCommonHandler.instance().bus().register(new SurfaceReservationRepair());

		GameRegistry.registerTileEntity(TileEntityPaddedBench.class, MODID + ":padded_bench_colour");
		GameRegistry.registerTileEntity(TileEntityUpholstery.class, MODID + ":upholstery_colour");
		GameRegistry.registerTileEntity(TileEntityShieldChair.class, MODID + ":shield_chair");
		GameRegistry.registerTileEntity(TileEntityMetalVariant.class, MODID + ":metal_variant");

		ItemInitialiser.init();

		if (event.getSide().isClient()) {
			ClientModelInitialiser.registerPaddedBenchModels();
		}

		BlockInitialiser.init();
		StorageNetwork.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new StorageGuiHandler());

		if (event.getSide().isClient()) {
			GeneratedModelResourcePack.install();
			FMLCommonHandler.instance().bus().register(new HeldCandleSmoke());
			ClientModelInitialiser.registerDrawerModels();
			ClientModelInitialiser.registerPaddedBenchItemModels();
			ClientModelInitialiser.registerStorageBenchItemModels();
			ClientModelInitialiser.registerUpholsteryItemModels();
			ClientItemInitialiser.registerVariantItemModels();
			ClientRenderInitialiser.registerShieldChairRenderer();
			ClientRenderInitialiser.registerSurfaceFurnitureRenderers();
			ClientRenderInitialiser.registerEntityRenderers();
		}

	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		FluidAgingRegistry.freeze();
		FluidVesselInteractions.freeze();

	}

	@EventHandler
	public void missingMappings(FMLMissingMappingsEvent event) {
		CfmChairMigration.remapMissingMappings(event);
	}
}
