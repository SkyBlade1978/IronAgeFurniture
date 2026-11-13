package zone.moddev.mc.ironagefurniture.api.surface;

import zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration;
import zone.moddev.mc.ironagefurniture.api.Blocks.VanillaFoodDisplay;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Deliberate food placement only; ordinary item/block use remains Minecraft's responsibility. */
public final class VanillaFoodPlacement {

	public static boolean isKnownFood(ItemStack stack) {
		if (stack == null || stack.stackSize <= 0 || !(stack.getItem() instanceof ItemFood)) return false;
		for (VanillaSurfaceFoods.Food food : VanillaSurfaceFoods.all()) {
			if (food.item == stack.getItem() && (food.metadata < 0 || food.metadata == stack.getMetadata())) return true;
		}
		return false;
	}

	public static boolean isSupport(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		return block.getRegistryName() != null
			&& "minecraft".equals(block.getRegistryName().getResourceDomain())
			&& block != Blocks.CRAFTING_TABLE && !block.hasTileEntity(state)
			&& state.isFullCube() && state.isSideSolid(world, pos, EnumFacing.UP);
	}

	public static boolean mayEdit(World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
		return !player.isSpectator() && player.capabilities.allowEdit
			&& world.getWorldBorder().contains(pos) && world.isBlockModifiable(player, pos)
			&& player.canPlayerEdit(pos, EnumFacing.UP, stack);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.isCanceled() || event.getUseBlock() != Result.DEFAULT || event.getUseItem() != Result.DEFAULT
				|| !IronAgeFurnitureConfiguration.VANILLA_FOOD_SURFACES
				|| event.getHand() != EnumHand.MAIN_HAND || event.getFace() != EnumFacing.UP) return;
		EntityPlayer player = event.getEntityPlayer();
		ItemStack held = event.getItemStack();
		if (player instanceof FakePlayer || !player.isSneaking() || !isKnownFood(held)
				|| player.getHeldItemOffhand() != null && player.getHeldItemOffhand().stackSize > 0) return;
		World world = event.getWorld();
		BlockPos support = event.getPos();
		BlockPos display = support.up();
		if (display.getY() >= world.getHeight() || !world.isBlockLoaded(display)
				|| world.getBlockState(display).getBlock() != Blocks.AIR || !isSupport(world, support)
				|| !mayEdit(world, support, player, held) || !mayEdit(world, display, player, held)) return;
		if (!world.isRemote && !VanillaFoodDisplay.place(world, display, held, player.getHorizontalFacing())) return;
		if (!world.isRemote && !player.capabilities.isCreativeMode && --held.stackSize <= 0) {
			player.setHeldItem(event.getHand(), null);
		}
		// Forge 1.10 reports a cancelled block click as PASS, then tries the held food in air.
		// Suppress only that immediate follow-up on both sides, without a global item-use hook.
		player.getCooldownTracker().setCooldown(held.getItem(), 4);
		event.setCanceled(true);
	}
}
