package zone.moddev.mc.ironagefurniture.init;

import java.util.Map;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.DrawerUnit;
import zone.moddev.mc.ironagefurniture.api.Enumerations.PaddedBenchColour;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockPaddedBench;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockStorageBench;
import zone.moddev.mc.ironagefurniture.client.model.PaddedBenchModelLoader;
import zone.moddev.mc.ironagefurniture.client.model.UpholsteryModelLoader;
import zone.moddev.mc.ironagefurniture.api.Enumerations.UpholsteryColour;
import zone.moddev.mc.ironagefurniture.api.Items.ItemBlockUpholsteredFurniture;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientModelInitialiser {
	private static boolean registered;

	private ClientModelInitialiser() {
		throw new IllegalAccessError("This class cannot be instantiated");
	}

	public static void registerPaddedBenchModels() {
		if (registered) {
			return;
		}
		registered = true;
		ModelLoaderRegistry.registerLoader(PaddedBenchModelLoader.INSTANCE);
		ModelLoaderRegistry.registerLoader(UpholsteryModelLoader.INSTANCE);
	}

	public static void registerDrawerModels() {
		// Multipart selects quads from the actual state, so it needs only one baked model per block.
		StateMap mapper = new StateMap.Builder().ignore(DrawerUnit.FACING, DrawerUnit.PAIR, DrawerUnit.ABOVE,
			DrawerUnit.BELOW, DrawerUnit.LOWER_OPEN, DrawerUnit.UPPER_OPEN).build();

		for (Block block : Ironagefurniture.BlockRegistry.values()) {
			if (block instanceof DrawerUnit) {
				ModelLoader.setCustomStateMapper(block, mapper);
			}
		}
	}

	public static void registerPaddedBenchItemModels() {
		for (Map.Entry<String, Item> entry : Ironagefurniture.ItemRegistry.entrySet()) {
			if (!(entry.getValue() instanceof ItemBlockPaddedBench)) {
				continue;
			}
			for (PaddedBenchColour colour : PaddedBenchColour.values()) {
				ModelLoader.setCustomModelResourceLocation(entry.getValue(), colour.getItemMetadata(),
						new ModelResourceLocation(Ironagefurniture.MODID + ":padded/"
								+ colour.getSerializedName() + "/" + entry.getKey(), "inventory"));
			}
		}
	}

	public static void registerStorageBenchItemModels() {
		for (Map.Entry<String, Item> entry : Ironagefurniture.ItemRegistry.entrySet()) {
			if (!(entry.getValue() instanceof ItemBlockStorageBench)) {
				continue;
			}

			ModelLoader.setCustomModelResourceLocation(entry.getValue(), 0,
				new ModelResourceLocation(Ironagefurniture.MODID + ":" + entry.getKey(), "inventory"));
			for (PaddedBenchColour colour : PaddedBenchColour.values()) {
				ModelLoader.setCustomModelResourceLocation(entry.getValue(), ItemBlockStorageBench.colourMetadata(colour),
					new ModelResourceLocation(Ironagefurniture.MODID + ":padded/" + colour.getSerializedName()
						+ "/" + entry.getKey() + "_padded", "inventory"));
			}
		}
	}

	public static void registerUpholsteryItemModels() {
		for (Map.Entry<String, Item> entry : Ironagefurniture.ItemRegistry.entrySet()) {
			if (!(entry.getValue() instanceof ItemBlockUpholsteredFurniture)) continue;
			for (UpholsteryColour colour : UpholsteryColour.values()) {
				ModelLoader.setCustomModelResourceLocation(entry.getValue(), colour.getItemMetadata(),
					new ModelResourceLocation(Ironagefurniture.MODID + ":upholstered/"
						+ colour.getSerializedName() + "/" + entry.getKey(), "inventory"));
			}
		}
	}
}
