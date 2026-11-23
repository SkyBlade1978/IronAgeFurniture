package zone.moddev.mc.ironagefurniture.fixture;

import org.apache.logging.log4j.LogManager;

import zone.moddev.mc.ironagefurniture.api.Blocks.SideBarrel;
import zone.moddev.mc.ironagefurniture.api.Blocks.StorageBarrel;
import zone.moddev.mc.ironagefurniture.api.StorageGuiHandler;
import zone.moddev.mc.ironagefurniture.api.container.ContainerFurnitureLabel;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityBarrel;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

final class BarrelLabelProbe {
	private BarrelLabelProbe() { }

	static void verify(WorldServer world, FakePlayer player, BlockPos origin) {
		Block upright = Block.getBlockFromName("ironagefurniture:barrel_wood_ironage_oak");
		Block side = Block.getBlockFromName("ironagefurniture:side_barrel_wood_ironage_oak");
		for (EnumFacing face : EnumFacing.Plane.HORIZONTAL) {
			int index = face.getHorizontalIndex();
			verifyLabel(world, player, origin.east(5 * index), upright.getDefaultState(), face, true);
			EnumFacing axis = index % 2 == 0 ? face : face.rotateY();
			verifyLabel(world, player, origin.south(5).east(5 * index), side.getDefaultState().withProperty(SideBarrel.FACING, axis),
				face, false);
		}
		player.inventory.clear();
		LogManager.getLogger().info("IAF BARREL LABEL PROBE PASSED: all faces, exact signs, NBT and separate label removal");
	}

	private static void verifyLabel(WorldServer world, FakePlayer player, BlockPos pos, IBlockState state,
			EnumFacing face, boolean solid) {
		Block block = state.getBlock();
		world.setBlockState(pos, state);
		TileEntityBarrel barrel = (TileEntityBarrel)world.getTileEntity(pos);
		if (solid) {
			require(barrel.getBulkStorage().deposit(new ItemStack(Items.POTATO, 42), false) == 42, "Could not fill bulk fixture");
		} else {
			require(barrel.getFluidHandler().fill(new FluidStack(FluidRegistry.WATER, 1000), true) == 1000, "Could not fill fluid fixture");
			barrel.setSealed(true);
		}

		ItemStack signs = new ItemStack(Items.SIGN, 2).setStackDisplayName("Market sign");
		player.setHeldItem(EnumHand.OFF_HAND, signs);
		Vec3d centre = new Vec3d(pos).add(((StorageBarrel)block).getLabelAnchor(state, face)).addVector(0, 0.04D, 0);
		Vec3d eye = centre.addVector(1.5D * face.getFrontOffsetX(), 0, 1.5D * face.getFrontOffsetZ());
		float yaw = (float)Math.toDegrees(Math.atan2(face.getFrontOffsetX(), -face.getFrontOffsetZ()));
		player.setPositionAndRotation(eye.xCoord, eye.yCoord - player.getEyeHeight(), eye.zCoord, yaw, 0);

		ContainerFurnitureLabel menu = menu(world, player, pos, face);
		ContainerFurnitureLabel stale = menu(world, player, pos, face);
		require(menu.getText().isEmpty() && menu.save(player, "Market\n stock") && signs.stackSize == 1,
			"One valid sign was not consumed on Done");
		require(!stale.save(player, "Stale"), "An older editor overwrote the label");
		require(barrel.getLabelFacing() == face && "Market stock".equals(barrel.getBarrelLabel().getText())
			&& ItemStack.areItemStacksEqual(signs, barrel.getBarrelLabel().getSign()), "The label lost its face, text or exact sign");

		ContainerFurnitureLabel edit = menu(world, player, pos, face.getOpposite());
		require(edit.save(player, "Market batch") && signs.stackSize == 1 && barrel.getLabelFacing() == face,
			"Editing moved the label or used a second sign");
		NBTTagCompound saved = ((TileEntity)barrel).writeToNBT(new NBTTagCompound());
		TileEntity restored = TileEntity.create(world, saved);
		require(restored != null && saved.equals(restored.writeToNBT(new NBTTagCompound())), "Label changed on reload");
		world.setTileEntity(pos, restored);
		require(!edit.save(player, "Old tile"), "Editor saved into a replaced tile");

		Vec3d through = centre.addVector(-face.getFrontOffsetX(), 0, -face.getFrontOffsetZ());
		RayTraceResult hit = state.collisionRayTrace(world, pos, eye, through);
		require(hit != null && hit.sideHit == face && hit.subHit >= 0, "Could not target the sign on " + face);
		RayTraceResult back = state.collisionRayTrace(world, pos, through, eye);
		require(back == null || back.subHit != hit.subHit, "Label was targetable through the back of its barrel");

		player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
		player.interactionManager.tryHarvestBlock(pos);
		TileEntityBarrel after = (TileEntityBarrel)world.getTileEntity(pos);
		require(after == restored && after.getBarrelLabel().getSign() == null && world.getBlockState(pos).equals(state),
			"Breaking the label removed the barrel or left the sign behind");
		require(solid ? after.getBulkStorage().getCount() == 42 : after.getFluidAmount() == 1000 && after.isSealed(),
			"Label operations changed barrel contents");
		require(countSigns(world, pos, signs) == 1, "Label removal lost or duplicated the exact sign");

		require(menu(world, player, pos, face).save(player, "Spill test"), "Could not attach a sign to a second batch");
		world.setBlockToAir(pos);
		int droppedSigns = countSigns(world, pos, new ItemStack(Items.SIGN).setStackDisplayName("Market sign"));
		require(droppedSigns == 1,
			"Breaking the barrel dropped " + droppedSigns + " fitted signs");
	}

	private static ContainerFurnitureLabel menu(WorldServer world, FakePlayer player, BlockPos pos, EnumFacing face) {
		Object menu = new StorageGuiHandler().getServerGuiElement(StorageGuiHandler.barrelLabelId(face, EnumHand.OFF_HAND),
			player, world, pos.getX(), pos.getY(), pos.getZ());
		require(menu instanceof ContainerFurnitureLabel, "Barrel did not supply the shared label editor");
		return (ContainerFurnitureLabel)menu;
	}

	private static int countSigns(WorldServer world, BlockPos pos, ItemStack expected) {
		int signs = 0;
		for (EntityItem drop : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expand(1, 1, 1))) {
			if (!drop.isDead && ItemStack.areItemsEqual(expected, drop.getEntityItem())
					&& ItemStack.areItemStackTagsEqual(expected, drop.getEntityItem())) {
				signs += drop.getEntityItem().stackSize;
			}
			drop.setDead();
		}
		return signs;
	}

	private static void require(boolean result, String message) {
		if (!result) {
			throw new IllegalStateException(message);
		}
	}
}
