package zone.moddev.mc.ironagefurniture.client.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zone.moddev.mc.ironagefurniture.api.Blocks.ProduceFurniture;
import zone.moddev.mc.ironagefurniture.api.surface.ShelfLayout;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceContentRegistry;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaShelfDisplay;
import zone.moddev.mc.ironagefurniture.api.tile.ProduceTray;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityProduceFurniture;
import zone.moddev.mc.ironagefurniture.init.WoodVariantHelper;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class TileEntityProduceFurnitureRenderer extends TileEntitySpecialRenderer<TileEntityProduceFurniture> {
	private final TileEntityShelvingUnitRenderer shelves;
	private final List<String> woods = WoodVariantHelper.getEnabledWoodSuffixes();
	private final Map<String, IBakedModel> trays = new HashMap<>();
	private final Map<String, IBakedModel[]> racks = new HashMap<>();
	private final ItemStack modelStack = new ItemStack(Items.STICK);

	private TileEntityProduceFurnitureRenderer(TileEntityShelvingUnitRenderer shelves) {
		this.shelves = shelves;
	}

	static void register(TileEntityShelvingUnitRenderer shelves) {
		TileEntityProduceFurnitureRenderer renderer = new TileEntityProduceFurnitureRenderer(shelves);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityProduceFurniture.class, renderer);
		MinecraftForge.EVENT_BUS.register(renderer);
	}

	@SubscribeEvent
	public void stitch(TextureStitchEvent.Pre event) {
		for (String wood : this.woods) {
			TileEntityShelvingUnitRenderer.loadModel("produce_tray_" + wood).getTextures().forEach(event.getMap()::registerSprite);
			TileEntityShelvingUnitRenderer.loadModel("produce_rack_" + wood + "_0").getTextures().forEach(event.getMap()::registerSprite);
		}
	}

	@SubscribeEvent
	public void bake(ModelBakeEvent event) {
		for (String wood : this.woods) {
			this.trays.put(wood, bake("produce_tray_" + wood));

			IBakedModel[] models = new IBakedModel[4];
			for (int joins = 0; joins < models.length; joins++) {
				models[joins] = bake("produce_rack_" + wood + "_" + joins);
			}
			this.racks.put(wood, models);
		}
	}

	private static IBakedModel bake(String name) {
		return TileEntityShelvingUnitRenderer.bakeModel(TileEntityShelvingUnitRenderer.loadModel(name));
	}

	@Override
	public void renderTileEntityAt(TileEntityProduceFurniture tile, double x, double y, double z, float partialTicks, int destroyStage) {
		BlockPos pos = tile.getPos();
		IBlockState state = tile.getWorld().getBlockState(pos);
		if (!(state.getBlock() instanceof ProduceFurniture)) {
			return;
		}
		ProduceFurniture furniture = (ProduceFurniture)state.getBlock();
		if (furniture.isRack() && state.getValue(ProduceFurniture.SECOND)) {
			return;
		}

		EnumFacing front = state.getValue(BlockHorizontal.FACING);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(x + 0.5D, y, z + 0.5D);
			GlStateManager.rotate(ProduceFurniture.yaw(front), 0, 1, 0);
			if (furniture.isRack()) {
				String wood = tile.getWood();
				int joins = furniture.connectsTo(tile.getWorld(), pos.offset(front.rotateYCCW()), state, wood) ? 1 : 0;
				if (furniture.connectsTo(tile.getWorld(), pos.offset(front.rotateY()), state, wood)) {
					joins |= 2;
				}
				IBakedModel[] models = this.racks.getOrDefault(wood, this.racks.get(ProduceTray.DEFAULT_WOOD));
				TileEntityShelvingUnitRenderer.renderBoardModel(this.modelStack, models[joins], 0, 0, 0, 1, 0);
			}
			for (int slot = 0; slot < furniture.getTrayCount(state); slot++) {
				ProduceTray tray = tile.getTray(slot);
				if (tray != null) {
					this.renderTray(tray, furniture.tierHeight(slot), (float)furniture.tierPitch(slot));
				}
			}
		} finally {
			GlStateManager.popMatrix();
		}
	}

	private void renderTray(ProduceTray tray, double height, float pitch) {
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate(0, height, 0);
			GlStateManager.rotate(pitch, 1, 0, 0);
			IBakedModel model = this.trays.getOrDefault(tray.getWood(), this.trays.get(ProduceTray.DEFAULT_WOOD));
			TileEntityShelvingUnitRenderer.renderBoardModel(this.modelStack, model, 0, 0, 0, 1, 0);

			ItemStack contents = tray.peekOne();
			if (contents == null || ProduceTray.capacity(contents) == 0) {
				return;
			}
			SurfaceContentRegistry.Descriptor descriptor = SurfaceContentRegistry.describe(contents);
			VanillaShelfDisplay display = VanillaShelfDisplay.find(contents);
			ShelfLayout layout = descriptor.getShelfLayout();
			SurfaceContentRegistry.Descriptor bounds = display == null ? descriptor : display.getShelfModel();
			GlStateManager.translate(0, 1.0D / 16, 0);
			GlStateManager.scale(0.90D, 0.90D, 1.20D);
			int count = Math.min(tray.getContents().stackSize, layout.getCapacity());
			for (int index = 0; index < count; index++) {
				this.shelves.renderProduce(contents, layout.place(index, bounds));
			}
		} finally {
			GlStateManager.popMatrix();
		}
	}
}
