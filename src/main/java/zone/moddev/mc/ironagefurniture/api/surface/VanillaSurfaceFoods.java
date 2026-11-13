package zone.moddev.mc.ironagefurniture.api.surface;

import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Category;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;

/** Vanilla food models and common placement bounds, including fish metadata. */
public final class VanillaSurfaceFoods {
	public static final class Food {
		public final Item item;
		public final int metadata;
		public final String model;
		public final SurfaceContentRegistry.Descriptor descriptor;

		Food(JsonObject data) {
			this.item = Item.getByNameOrId(data.get("item").getAsString());
			if (this.item == null) throw new IllegalArgumentException("Unknown vanilla surface food " + data);
			this.metadata = data.get("metadata").getAsInt();
			this.model = data.get("model").getAsString();
			JsonArray bounds = data.getAsJsonArray("bounds");
			this.descriptor = new SurfaceContentRegistry.Descriptor(Category.MEAL, new AxisAlignedBB(
				bounds.get(0).getAsDouble(), bounds.get(1).getAsDouble(), bounds.get(2).getAsDouble(),
				bounds.get(3).getAsDouble(), bounds.get(4).getAsDouble(), bounds.get(5).getAsDouble()));
		}
	}

	private static final List<Food> FOODS = load();
	private static final Map<Item, List<Food>> BY_ITEM = index();

	private VanillaSurfaceFoods() { }

	public static List<Food> all() { return FOODS; }

	public static Food find(ItemStack stack) {
		if (stack == null) return null;
		List<Food> variants = BY_ITEM.get(stack.getItem());
		if (variants == null) return null;
		for (Food food : variants) {
			if (food.metadata < 0 || food.metadata == stack.getMetadata()) return food;
		}
		return variants.get(0);
	}

	public static void register() {
		for (Item item : BY_ITEM.keySet()) {
			SurfaceContentRegistry.register(item, stack -> find(stack).descriptor);
		}
	}

	private static List<Food> load() {
		List<Food> foods = new ArrayList<Food>();
		try (InputStreamReader reader = new InputStreamReader(
				VanillaSurfaceFoods.class.getResourceAsStream("/assets/ironagefurniture/surface_foods.json"),
				StandardCharsets.UTF_8)) {
			for (JsonElement element : new JsonParser().parse(reader).getAsJsonArray()) {
				foods.add(new Food(element.getAsJsonObject()));
			}
		} catch (IOException failure) {
			throw new IllegalStateException("Cannot read vanilla surface food catalogue", failure);
		}
		return Collections.unmodifiableList(foods);
	}

	private static Map<Item, List<Food>> index() {
		Map<Item, List<Food>> byItem = new IdentityHashMap<Item, List<Food>>();
		for (Food food : FOODS) {
			List<Food> variants = byItem.get(food.item);
			if (variants == null) {
				variants = new ArrayList<Food>();
				byItem.put(food.item, variants);
			}
			variants.add(food);
		}
		return byItem;
	}
}
