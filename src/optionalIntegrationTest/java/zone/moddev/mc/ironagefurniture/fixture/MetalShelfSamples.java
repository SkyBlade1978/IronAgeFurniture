package zone.moddev.mc.ironagefurniture.fixture;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.UniversalBucket;

final class MetalShelfSamples {
	private MetalShelfSamples() { }

	static ItemStack item(String name) {
		return item("basemetals", name);
	}

	static ItemStack item(String namespace, String name) {
		Item item = Item.getByNameOrId(namespace + ":" + name);
		if (item == null && name.endsWith("_door")) {
			item = Item.getByNameOrId(namespace + ":" + name + "_item");
		}
		if (item == null) {
			throw new IllegalStateException("The metals fixture is missing " + namespace + ":" + name);
		}
		return new ItemStack(item);
	}

	static ItemStack mercuryBucket() {
		return bucket("mercury");
	}

	static ItemStack bucket(String fluid) {
		ItemStack stack = UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket,
			FluidRegistry.getFluid(fluid));
		if (stack == null) {
			throw new IllegalStateException("The metals fixture did not register a bucket for " + fluid);
		}
		return stack;
	}
}
