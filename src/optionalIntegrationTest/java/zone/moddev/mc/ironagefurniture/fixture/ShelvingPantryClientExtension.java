package zone.moddev.mc.ironagefurniture.fixture;

import zone.moddev.mc.ironagefurniture.client.render.SurfaceDisplayRenderHelper;
import zone.moddev.mc.skysfurniturecore.client.render.SurfaceRenderRegistry;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
final class ShelvingPantryClientExtension {
	static int renderCalls;

	private ShelvingPantryClientExtension() { }

	static void register() {
		ModelLoader.setCustomModelResourceLocation(OptionalIntegrationRecipeProbe.SHELF_LAYOUT_SAMPLE, 0,
			new ModelResourceLocation("minecraft:apple", "inventory"));
		SurfaceRenderRegistry.register(OptionalIntegrationRecipeProbe.SHELF_LAYOUT_SAMPLE, (stack, context) -> {
			renderCalls++;
			SurfaceDisplayRenderHelper.renderSurfaceItem(new ItemStack(Items.GOLDEN_APPLE), context);
		});
	}
}
