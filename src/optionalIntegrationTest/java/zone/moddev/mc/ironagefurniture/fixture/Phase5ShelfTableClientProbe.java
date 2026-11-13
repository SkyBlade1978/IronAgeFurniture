package zone.moddev.mc.ironagefurniture.fixture;

import zone.moddev.mc.ironagefurniture.Ironagefurniture;
import zone.moddev.mc.ironagefurniture.api.Blocks.DiningTable;
import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf;
import zone.moddev.mc.ironagefurniture.api.Blocks.SurfaceItemHit;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityDiningTable;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityWallShelf;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;

/** Bake every registered wood/state, then check real lower-surface item targeting and removal. */
final class Phase5ShelfTableClientProbe {
	private Phase5ShelfTableClientProbe() { }

	static int verify(Minecraft client) {
		int models = 0, cases = 0;
		for (String id : Ironagefurniture.BlockRegistry.keySet()) {
			if (!id.startsWith("table_dining_") && !id.startsWith("table_low_")
					&& !id.startsWith("shelf_wall_") && !id.startsWith("cabinet_wood_ironage_")) continue;
			Block block = Ironagefurniture.BlockRegistry.get(id);
			for (IBlockState state : block.getBlockState().getValidStates()) {
				IBakedModel model = client.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
				require(model != client.getRenderItem().getItemModelMesher().getModelManager().getMissingModel(), "Missing furniture model: " + id + " " + state);
				for (int side = -1; side < 6; side++) {
					for (BakedQuad quad : model.getQuads(state, side < 0 ? null : EnumFacing.getFront(side), 0L)) {
						require(!quad.getSprite().getIconName().contains("missingno"), "Missing furniture texture: " + id + " " + state);
					}
				}
				models++;
			}
		}
		double oldX = client.player.posX, oldY = client.player.posY, oldZ = client.player.posZ;
		float oldYaw = client.player.rotationYaw, oldPitch = client.player.rotationPitch;
		RayTraceResult oldHit = client.objectMouseOver;
		BlockPos pos = client.player.getPosition().add(3, 2, 3);
		try {
			for (String id : new String[]{"table_low_oak", "shelf_wall_oak"}) {
				Block block = Ironagefurniture.BlockRegistry.get(id);
				for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
					for (ItemStack item : new ItemStack[]{new ItemStack(Items.BREAD), new ItemStack(Items.APPLE),
							new ItemStack(Items.ARMOR_STAND), new ItemStack(Blocks.STONE)}) {
						client.world.setBlockToAir(pos.up()); client.world.setBlockToAir(pos);
						for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) client.world.setBlockToAir(pos.offset(side));
						client.world.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 2);
						IBlockState state = block.getDefaultState();
						if (block instanceof WallShelf) {
							client.world.setBlockState(pos.offset(facing.getOpposite()), Blocks.STONE.getDefaultState(), 2);
							state = state.withProperty(net.minecraft.block.BlockHorizontal.FACING, facing).withProperty(WallShelf.DATA, true);
						} else state = state.withProperty(DiningTable.DATA, true);
						client.world.setBlockState(pos, state, 2);
						SurfaceSettingHost host = (SurfaceSettingHost)client.world.getTileEntity(pos);
						host.getSurfaceSetting().setSingleItem(item, facing);
						AxisAlignedBB bounds = SurfaceItemHit.bounds(host, Slot.CENTER, true).offset(pos);
						require(Math.abs(bounds.minY - (pos.getY() + host.getSurfaceHeight())) < 0.05D, "Lower surface item is floating or submerged");
						double x = (bounds.minX + bounds.maxX) / 2, z = (bounds.minZ + bounds.maxZ) / 2;
						RayTraceResult hit = block.collisionRayTrace(state, client.world, pos,
							new Vec3d(x, pos.getY() + 3, z), new Vec3d(x, pos.getY(), z));
						require(hit != null && hit.subHit >= 100, "Client cannot target lower food model");
						client.objectMouseOver = hit;
						AxisAlignedBB selection = block.getSelectedBoundingBox(state, client.world, pos);
						require(Math.abs(selection.minY - bounds.minY) < 0.00001D && Math.abs(selection.maxY - bounds.maxY) < 0.00001D,
							"Lower surface selection follows furniture instead of item");
						client.player.setPositionAndRotation(x, pos.getY() + 2, z, 0, 90);
						require(client.playerController.onPlayerDestroyBlock(pos), "Client could not mine lower display");
						require(client.world.getBlockState(pos).getBlock() == block && !host.getSurfaceSetting().hasAnyItem(),
							"Lower display mining removed furniture or retained ghost");
						cases++;
					}
				}
			}
		} finally {
			client.player.setPositionAndRotation(oldX, oldY, oldZ, oldYaw, oldPitch);
			client.objectMouseOver = oldHit;
		}
		LogManager.getLogger().info("IAF PHASE 5 FURNITURE CLIENT PROBE PASSED: {} baked wood/state models, {} lower display cases", models, cases);
		return cases;
	}

	static BlockPos prepareScene(Minecraft client, BlockPos origin) {
		String[] woods = {"oak", "spruce", "birch", "jungle", "acacia", "big_oak"};
		for (int x = -1; x < 7; x++) for (int z = -1; z <= 4; z++) {
			client.world.setBlockState(origin.add(x, -1, z), Blocks.STONEBRICK.getDefaultState(), 2);
			for (int y = 0; y < 4; y++) client.world.setBlockToAir(origin.add(x, y, z));
			if (z == 4) for (int y = 0; y < 4; y++) client.world.setBlockState(origin.add(x, y, z), Blocks.STONEBRICK.getDefaultState(), 2);
		}
		for (int i = 0; i < woods.length; i++) {
			Block low = Ironagefurniture.BlockRegistry.get("table_low_" + woods[i]);
			BlockPos table = origin.add(i, 0, 0);
			client.world.setBlockState(table, low.getDefaultState().withProperty(DiningTable.DATA, true), 2);
			TileEntityDiningTable tableTile = (TileEntityDiningTable)client.world.getTileEntity(table);
			if (i == 3) tableTile.setEmbeddedContent(DiningTable.TableEmbeddedContent.CANDLE,
				new ItemStack(Ironagefurniture.BlockRegistry.get("light_metal_ironage_candle_floor")));
			else tableTile.getSurfaceSetting().setSingleItem(new ItemStack(i % 2 == 0 ? Items.BREAD : Items.APPLE), EnumFacing.NORTH);
			Block shelf = Ironagefurniture.BlockRegistry.get("shelf_wall_" + woods[i]);
			BlockPos shelfPos = origin.add(i, 1, 3);
			client.world.setBlockState(shelfPos, shelf.getDefaultState().withProperty(WallShelf.DATA, true), 2);
			TileEntityWallShelf tile = (TileEntityWallShelf)client.world.getTileEntity(shelfPos);
			if (i == 0) for (int n = 0; n < 5; n++) tile.addEmbeddedContent(WallShelf.ShelfContentKind.BOOKS, new ItemStack(Items.BOOK));
			else if (i == 1) for (int n = 0; n < 6; n++) tile.addEmbeddedContent(WallShelf.ShelfContentKind.RECORDS, new ItemStack(Items.RECORD_CAT));
			else if (i == 2) {
				tile.addEmbeddedContent(WallShelf.ShelfContentKind.FLOWER_POT, new ItemStack(Items.FLOWER_POT));
				tile.addEmbeddedContent(WallShelf.ShelfContentKind.FLOWER_POT, new ItemStack(Blocks.RED_FLOWER));
			} else if (i == 3) tile.addEmbeddedContent(WallShelf.ShelfContentKind.CANDLE, new ItemStack(Ironagefurniture.BlockRegistry.get("light_metal_ironage_candle_floor")));
			else tile.getSurfaceSetting().setSingleItem(new ItemStack(i == 4 ? Items.BREAD : Items.APPLE), EnumFacing.NORTH);
		}
		return origin;
	}

	private static void require(boolean pass, String message) { if (!pass) throw new IllegalStateException(message); }
}
