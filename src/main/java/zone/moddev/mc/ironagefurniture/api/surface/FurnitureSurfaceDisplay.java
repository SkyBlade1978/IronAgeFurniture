package zone.moddev.mc.ironagefurniture.api.surface;

import java.util.IdentityHashMap;
import java.util.Map;

import zone.moddev.mc.ironagefurniture.BlockObjectHolder;
import zone.moddev.mc.ironagefurniture.ItemObjectHolder;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Category;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;

public enum FurnitureSurfaceDisplay {
	GLOW_LAMP(6, 6, 10, 6, 10, ShelfLayout.LAMP_PAIR),
	LAVA_LAMP(6, 6, 10, 6, 10, ShelfLayout.LAMP_PAIR),
	RED_LAMP(6, 6, 10, 6, 10, ShelfLayout.LAMP_PAIR),
	CANDLE(4, 4, 9, 6.7D, 11, ShelfLayout.CANDLE_PAIR),
	EMPTY_SCONCE(5.5D, 5.5D, 10.5D, 9, 10.5D),
	OBSIDIAN_CHUNK(7, 6, 10, 3, 9),
	TALLOW(4.2D, 5.4D, 11.8D, 2.8D, 10.6D);

	private static final Map<Item, FurnitureSurfaceDisplay> DISPLAYS = new IdentityHashMap<>();
	private final SurfaceContentRegistry.Descriptor descriptor;

	FurnitureSurfaceDisplay(double minX, double minZ, double maxX, double height, double maxZ) {
		this(minX, minZ, maxX, height, maxZ, ShelfLayout.NONE);
	}

	FurnitureSurfaceDisplay(double minX, double minZ, double maxX, double height, double maxZ, ShelfLayout layout) {
		this.descriptor = new SurfaceContentRegistry.Descriptor(Category.SINGLETON,
			new AxisAlignedBB(minX / 16 - 0.5D, 0, minZ / 16 - 0.5D,
				maxX / 16 - 0.5D, height / 16, maxZ / 16 - 0.5D)).withShelfLayout(layout);
	}

	public static void register() {
		GLOW_LAMP.register(BlockObjectHolder.light_metal_ironage_block_floor_glow_clear);
		LAVA_LAMP.register(BlockObjectHolder.light_metal_ironage_block_floor_lava_clear);
		RED_LAMP.register(BlockObjectHolder.light_metal_ironage_block_floor_red_clear);
		CANDLE.register(BlockObjectHolder.light_metal_ironage_candle_floor);
		EMPTY_SCONCE.register(BlockObjectHolder.light_metal_ironage_sconce_floor_empty_iron);
		OBSIDIAN_CHUNK.register(BlockObjectHolder.obsidian_chunk);
		TALLOW.register(ItemObjectHolder.tallow);
	}

	private void register(Block block) {
		if (block != null) {
			this.register(Item.getItemFromBlock(block));
		}
	}

	private void register(Item item) {
		if (item != null && item != Item.getItemFromBlock(Blocks.AIR)) {
			DISPLAYS.put(item, this);
			SurfaceContentRegistry.register(item, stack -> this.descriptor);
		}
	}

	public static FurnitureSurfaceDisplay find(ItemStack stack) {
		return stack == null ? null : DISPLAYS.get(stack.getItem());
	}

	public static int getLight(ItemStack stack) {
		if (stack == null) {
			return 0;
		}

		Item item = stack.getItem();
		if (item == Items.LAVA_BUCKET) {
			return Blocks.LAVA.getDefaultState().getLightValue();
		}

		return item instanceof ItemBlock ? ((ItemBlock)item).getBlock().getDefaultState().getLightValue() : 0;
	}
}
