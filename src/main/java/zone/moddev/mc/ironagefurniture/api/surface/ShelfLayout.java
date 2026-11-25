package zone.moddev.mc.ironagefurniture.api.surface;

/** Legacy names for registering a preset through the Iron Age compatibility API. */
@Deprecated
public enum ShelfLayout {
	NONE, BREAD_ROWS, APPLE_TRAY, PRODUCE_TRAY, CARROT_TRAY, COOKIE_STACKS, TOOL, FOOD_PAIR,
	CENTRED_FOOD, MEAT_TRAY, FISH_TRAY, COMPACT_TOOL, SHIELD, ARROW_STACKS, FLINT_AND_STEEL_ROW,
	BUNDLE_TRAY, INGOT_STACK, BOWL_STACK, CROSSWISE_TRAY, GEM_PLATTER, LAMP_PAIR, CANDLE_PAIR,
	FLAT_STACK, HARDWARE_ROW, PANEL_STACK, CUTTINGS;

	zone.moddev.mc.skysfurniturecore.api.surface.ShelfLayout core() {
		return zone.moddev.mc.skysfurniturecore.api.surface.ShelfLayout.valueOf(this.name());
	}

	public int getCapacity() { return this.core().getCapacity(); }
	public boolean hasTray() { return this.core().hasTray(); }
}
