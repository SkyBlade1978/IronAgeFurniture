package zone.moddev.mc.ironagefurniture.api.surface;

import zone.moddev.mc.ironagefurniture.api.surface.SurfaceContentRegistry.Descriptor;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Category;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/** Optional layouts for modular shelving levels. Positions face north and start at the board centre. */
public enum ShelfLayout {
	NONE(0, false),
	BREAD_ROWS(5, false),
	APPLE_TRAY(9, true),
	PRODUCE_TRAY(6, true),
	CARROT_TRAY(15, true),
	COOKIE_STACKS(6, false),
	TOOL(1, false),
	FOOD_PAIR(2, false),
	CENTRED_FOOD(1, false),
	MEAT_TRAY(2, true),
	FISH_TRAY(3, true),
	COMPACT_TOOL(1, false),
	SHIELD(1, false),
	ARROW_STACKS(6, false),
	FLINT_AND_STEEL_ROW(3, false),
	BUNDLE_TRAY(3, true),
	INGOT_STACK(6, false),
	BOWL_STACK(6, false),
	CROSSWISE_TRAY(3, true),
	GEM_PLATTER(6, true),
	LAMP_PAIR(2, false),
	CANDLE_PAIR(2, false),
	FLAT_STACK(4, false),
	HARDWARE_ROW(3, false),
	PANEL_STACK(3, false),
	CUTTINGS(15, false);

	static final Descriptor INGOT_MODEL = model(INGOT_STACK, 7.4D, 4.8D, 2.7D);
	static final Descriptor NUGGET_MODEL = model(PRODUCE_TRAY, 4.6D, 4.2D, 2.8D);
	static final Descriptor INGREDIENT_BOWL_MODEL = model(FOOD_PAIR, 7.2D, 6.8D, 3.0D);
	static final Descriptor BUCKET_MODEL = model(FOOD_PAIR, 7.2D, 6.8D, 6.4D);
	static final Descriptor HORSE_ARMOUR_MODEL = model(CENTRED_FOOD, 9.99D, 7.64D, 8.89D);

	public static final float GEM_PLATTER_ANGLE = -22.5F;
	private static final double GEM_PLATTER_CENTRE = 2.25D / 16;
	private static final double GEM_SINE = Math.sin(Math.toRadians(-GEM_PLATTER_ANGLE));
	private static final double GEM_COSINE = Math.cos(Math.toRadians(GEM_PLATTER_ANGLE));
	private static final double TRAY_FLOOR = 0.5D / 16;
	private static final double TRAY_INNER_DEPTH = 0.38D;
	private static final double SHELF_CANDLE_HEIGHT_SCALE = 0.70D;
	private static final int[] APPLE_TOP_COLUMNS = { 0, 2, 1 };
	private static final int CARROTS_IN_BOTTOM_LAYER = 6;
	private static final int CUTTINGS_IN_BOTTOM_LAYER = 6;
	private static final AxisAlignedBB TOOL_BOUNDS = new AxisAlignedBB(-0.39D, 0, -0.18D, 0.39D, 0.045D, 0.18D);
	private static final AxisAlignedBB COMPACT_TOOL_BOUNDS = new AxisAlignedBB(-0.22D, 0, -0.17D, 0.22D, 0.045D, 0.17D);
	private static final double SHIELD_SCALE = 0.45D;
	private static final AxisAlignedBB SHIELD_BOUNDS = new AxisAlignedBB(-11 * SHIELD_SCALE / 16, 0, -6 * SHIELD_SCALE / 16,
		11 * SHIELD_SCALE / 16, SHIELD_SCALE / 16, 6 * SHIELD_SCALE / 16);
	private static final AxisAlignedBB ARROW_BOUNDS = new AxisAlignedBB(-0.32D, 0, -0.027D, 0.32D, 0.044D, 0.027D);
	private static final AxisAlignedBB FLINT_AND_STEEL_BOUNDS = new AxisAlignedBB(-0.10D, 0, -0.15D, 0.10D, 1.5D / 16, 1.15D / 16);
	private static final AxisAlignedBB TRAY_BOUNDS = new AxisAlignedBB(-0.40D, 0, -0.215D, 0.40D, 0.075D, 0.215D);

	private final int capacity;
	private final boolean tray;

	ShelfLayout(int capacity, boolean tray) {
		this.capacity = capacity;
		this.tray = tray;
	}

	static Descriptor model(ShelfLayout layout, double width, double depth, double height) {
		return new Descriptor(Category.SINGLETON,
			new AxisAlignedBB(-width / 32, 0, -depth / 32, width / 32, height / 16, depth / 32)).withShelfLayout(layout);
	}

	public int getCapacity() {
		return this.capacity;
	}

	public boolean hasTray() {
		return this.tray;
	}

	public static AxisAlignedBB getTrayBounds() {
		return TRAY_BOUNDS;
	}

	public static AxisAlignedBB getGemPlatterBounds() {
		return new AxisAlignedBB(-6.6D / 16, 0, -3.45D / 16, 6.6D / 16, 3.9D / 16, 3.45D / 16);
	}

	public Placement place(int index, SurfaceContentRegistry.Descriptor descriptor) {
		if (index < 0 || index >= this.capacity) {
			throw new IllegalArgumentException("Item index is outside the shelf layout: " + index);
		}

		switch (this) {
		case BREAD_ROWS:
			double loafX = index < 3 ? 0.29D - index * 0.29D : 0.145D - (index - 3) * 0.29D;
			return fit(descriptor, EnumFacing.EAST, loafX, 0, index < 3 ? -0.105D : 0.105D,
				0.48D, 0.138D, 0.095D, 0.30D);
		case APPLE_TRAY:
			if (index < 6) {
				return produce(index, descriptor, 0.0D);
			}
			Placement base = produce(0, descriptor, 0.0D);
			return produce(APPLE_TOP_COLUMNS[index - 6], descriptor, 0.70D * (base.bounds.maxY - TRAY_FLOOR), 0.0D);
		case PRODUCE_TRAY:
			return produce(index, descriptor, 0.0D);
		case GEM_PLATTER:
			return gem(index, descriptor);
		case CARROT_TRAY:
			return carrot(index, descriptor);
		case COOKIE_STACKS:
			double cookieX = index % 2 == 0 ? 0.18D : -0.18D;
			Placement cookie = fit(descriptor, EnumFacing.NORTH, cookieX, 0, 0,
				0.65D, 0.16D, 0.20D, 0.115D);
			return cookie.raise((index / 2) * cookie.bounds.maxY);
		case INGOT_STACK:
			return ingot(index, descriptor);
		case BOWL_STACK:
			return bowl(index, descriptor);
		case TOOL:
			return new Placement(0, 0, 0, 0, 1, TOOL_BOUNDS);
		case FOOD_PAIR:
			return fit(descriptor, EnumFacing.NORTH, index == 0 ? 0.20D : -0.20D, 0, 0,
				0.78D, 0.18D, 0.20D, 0.375D);
		case LAMP_PAIR:
		case CANDLE_PAIR:
			AxisAlignedBB light = descriptor.getBounds(EnumFacing.NORTH);
			double lightX = (index == 0 ? 0.225D : -0.225D) - (light.minX + light.maxX) / 2;
			double lightZ = -(light.minZ + light.maxZ) / 2;
			double lightHeightScale = this == CANDLE_PAIR ? SHELF_CANDLE_HEIGHT_SCALE : 1.0D;
			AxisAlignedBB lightBounds = new AxisAlignedBB(light.minX, 0, light.minZ,
				light.maxX, light.maxY * lightHeightScale, light.maxZ).offset(lightX, 0, lightZ);
			return new Placement(lightX, 0, lightZ, 0, 1.0D, lightHeightScale, lightBounds);
		case CENTRED_FOOD:
			return fit(descriptor, EnumFacing.NORTH, 0, 0, 0, 0.78D, 0.40D, 0.20D, 0.375D);
		case FLAT_STACK:
			Placement layer = fit(descriptor, EnumFacing.NORTH, index * 0.018D, 0, 0,
				0.78D, 0.35D, 0.185D, 0.09D);
			return layer.raise(index * layer.bounds.maxY);
		case HARDWARE_ROW:
			return fit(descriptor, EnumFacing.NORTH, 0.24D * (1 - index), 0, 0,
				0.78D, 0.112D, 0.17D, 0.36D);
		case PANEL_STACK:
			// Work from back to front so the edges of each pane or panel stay visible.
			return fit(descriptor, EnumFacing.NORTH, 0.075D * (1 - index), 0, 0.105D * (1 - index),
				0.78D, 0.36D, 0.05D, 0.36D);
		case CUTTINGS:
			return cutting(index, descriptor);
		case MEAT_TRAY:
			return fitTrayItem(descriptor, index == 0 ? 0.19D : -0.19D, 0.34D);
		case FISH_TRAY:
		case BUNDLE_TRAY:
			return fitTrayItem(descriptor, 0.24D - index * 0.24D, 0.22D);
		case CROSSWISE_TRAY:
			return fit(descriptor, EnumFacing.EAST, index % 2 == 0 ? 0.035D : -0.035D, TRAY_FLOOR, 0.12D * (index - 1),
				0.78D, 0.34D, 0.058D, 0.21D);
		case COMPACT_TOOL:
			return new Placement(0, 0, 0, 0, 1, COMPACT_TOOL_BOUNDS);
		case SHIELD:
			return new Placement(0, 0, 0, 0, SHIELD_SCALE, SHIELD_BOUNDS);
		case ARROW_STACKS:
			double stagger = index % 2 == 0 ? 0.025D : -0.025D;
			double depth = -0.16D + index * 0.064D;
			return new Placement(stagger, 0, depth, 0, 1, ARROW_BOUNDS.offset(stagger, 0, depth));
		case FLINT_AND_STEEL_ROW:
			double strikerX = 0.25D - index * 0.25D;
			double strikerZ = 0.04D;
			return new Placement(strikerX, 0, strikerZ, 0, 1, FLINT_AND_STEEL_BOUNDS.offset(strikerX, 0, strikerZ));
		default:
			throw new IllegalArgumentException("Cannot position items without a shelf layout");
		}
	}

	private static Placement cutting(int index, SurfaceContentRegistry.Descriptor descriptor) {
		int layer = 0;
		int rowWidth = CUTTINGS_IN_BOTTOM_LAYER;
		while (index >= rowWidth) {
			index -= rowWidth;
			rowWidth--;
			layer++;
		}

		double x = ((rowWidth - 1) / 2.0D - index) * 0.126D;
		Placement cutting = fit(descriptor, EnumFacing.NORTH, x, 0, layer * 0.012D,
			1.0D, 0.060D, 0.188D, 0.116D);
		return cutting.raise(layer * cutting.bounds.maxY * 0.84D);
	}

	private static Placement gem(int index, SurfaceContentRegistry.Descriptor descriptor) {
		double x = (1 - index % 3) * 3.95D / 16;
		double row = (index < 3 ? -1.65D : 1.65D) / 16;
		double y = GEM_PLATTER_CENTRE + row * GEM_SINE;
		double z = row * GEM_COSINE;
		AxisAlignedBB stone = descriptor.getBounds(EnumFacing.NORTH);
		double halfDepth = stone.maxZ;
		AxisAlignedBB tilted = new AxisAlignedBB(stone.minX, -halfDepth * GEM_SINE,
			-halfDepth * GEM_COSINE - stone.maxY * GEM_SINE, stone.maxX,
			halfDepth * GEM_SINE + stone.maxY * GEM_COSINE, halfDepth * GEM_COSINE);
		return new Placement(x, y, z, 0, 1, tilted.offset(x, y, z));
	}

	private static Placement bowl(int index, SurfaceContentRegistry.Descriptor descriptor) {
		Placement bowl = fit(descriptor, EnumFacing.NORTH, 0, 0, 0, 0.78D, 0.22D, 0.22D, 0.185D);
		if (index == 0) {
			return bowl;
		}

		// One of the model's five steps stays visible above the rim of the preceding bowl.
		double step = bowl.bounds.maxY / 5.0D;
		Placement nested = bowl.raise(index * step);
		AxisAlignedBB bounds = nested.bounds;
		AxisAlignedBB rim = new AxisAlignedBB(bounds.minX, bounds.maxY - step, bounds.minZ,
			bounds.maxX, bounds.maxY, bounds.maxZ);
		return new Placement(nested.x, nested.y, nested.z, nested.yaw, nested.scale, rim);
	}

	private static Placement ingot(int index, SurfaceContentRegistry.Descriptor descriptor) {
		int layer = 0;
		int layerWidth = 3;
		while (index >= layerWidth) {
			index -= layerWidth;
			layerWidth--;
			layer++;
		}

		double x = ((layerWidth - 1) / 2.0D - index) * 0.25D;
		Placement ingot = fit(descriptor, EnumFacing.NORTH, x, 0, 0, 0.52D, 0.122D, 0.19D, 0.115D);
		return ingot.raise(layer * ingot.bounds.maxY);
	}

	private static Placement carrot(int index, SurfaceContentRegistry.Descriptor descriptor) {
		int layer = 0;
		int layerWidth = CARROTS_IN_BOTTOM_LAYER;
		while (index >= layerWidth) {
			index -= layerWidth;
			layerWidth--;
			layer++;
		}

		double x = ((layerWidth - 1) / 2.0D - index) * 0.12D;
		EnumFacing facing = EnumFacing.SOUTH;
		AxisAlignedBB bounds = descriptor.getBounds(facing);
		double scale = Math.min(0.58D, TRAY_INNER_DEPTH / (bounds.maxZ - bounds.minZ));
		Placement carrot = fit(descriptor, facing, x, TRAY_FLOOR, 0.0D, scale, 0.058D, 0.22D, 0.105D);

		// Centre the tapered model in the tray and stack each layer on the one below it.
		double z = -(bounds.minZ + bounds.maxZ) * carrot.scale / 2;
		double lift = layer * (carrot.bounds.maxY - TRAY_FLOOR);
		return new Placement(x, carrot.y + lift, z, carrot.yaw, carrot.scale, carrot.bounds.offset(0, lift, z));
	}

	private static Placement produce(int index, SurfaceContentRegistry.Descriptor descriptor, double lift) {
		return produce(index, descriptor, lift, index < 3 ? -0.09D : 0.09D);
	}

	private static Placement produce(int index, SurfaceContentRegistry.Descriptor descriptor, double lift, double depth) {
		return fit(descriptor, EnumFacing.NORTH, 0.22D - (index % 3) * 0.22D, TRAY_FLOOR + lift, depth,
			0.50D, 0.10D, 0.085D, 0.21D);
	}

	private static Placement fitTrayItem(SurfaceContentRegistry.Descriptor descriptor, double x, double width) {
		EnumFacing facing = EnumFacing.NORTH;
		AxisAlignedBB bounds = descriptor.getBounds(facing);
		double scale = Math.min(0.80D, width / (bounds.maxX - bounds.minX));
		scale = Math.min(scale, TRAY_INNER_DEPTH / (bounds.maxZ - bounds.minZ));
		scale = Math.min(scale, 0.35D / (bounds.maxY - bounds.minY));

		// Find the centre of the visible model so a protruding bone or tail does not skew its spacing.
		double centreX = (bounds.minX + bounds.maxX) * 0.5D;
		double centreZ = (bounds.minZ + bounds.maxZ) * 0.5D;
		return position(bounds, facing, x - centreX * scale, TRAY_FLOOR, -centreZ * scale, scale);
	}

	private static Placement fit(SurfaceContentRegistry.Descriptor descriptor, EnumFacing facing, double x, double y, double z,
			double scale, double halfWidth, double halfDepth, double height) {
		AxisAlignedBB bounds = descriptor.getBounds(facing);
		scale = Math.min(scale, halfWidth / Math.max(-bounds.minX, bounds.maxX));
		scale = Math.min(scale, halfDepth / Math.max(-bounds.minZ, bounds.maxZ));
		scale = Math.min(scale, height / (bounds.maxY - bounds.minY));

		return position(bounds, facing, x, y, z, scale);
	}

	private static Placement position(AxisAlignedBB bounds, EnumFacing facing, double x, double y, double z, double scale) {
		double lift = y - bounds.minY * scale;
		AxisAlignedBB fitted = new AxisAlignedBB(bounds.minX * scale, bounds.minY * scale, bounds.minZ * scale,
			bounds.maxX * scale, bounds.maxY * scale, bounds.maxZ * scale).offset(x, lift, z);
		return new Placement(x, lift, z, facing.getOpposite().getHorizontalAngle(), scale, fitted);
	}

	public static final class Placement {
		public final double x;
		public final double y;
		public final double z;
		public final float yaw;
		public final double scale;
		public final double scaleY;
		public final AxisAlignedBB bounds;

		private Placement(double x, double y, double z, float yaw, double scale, AxisAlignedBB bounds) {
			this(x, y, z, yaw, scale, scale, bounds);
		}

		private Placement(double x, double y, double z, float yaw, double scale, double scaleY, AxisAlignedBB bounds) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
			this.scale = scale;
			this.scaleY = scaleY;
			this.bounds = bounds;
		}

		private Placement raise(double lift) {
			return new Placement(this.x, this.y + lift, this.z, this.yaw, this.scale, this.scaleY,
				this.bounds.offset(0, lift, 0));
		}
	}
}
