# Phase 5 1.10 surface and vessel contract (unreleased)

Dining tables, low tables, wall shelves and cabinets are production surface
hosts for the same enabled woods as the chairs.
The published `0.4.0.110021` identities remain the baseline, and `update.json`
still advertises that release.

## Surface host

- Each furniture tile implements `SurfaceSettingHost` and owns one
  `SurfaceSetting`. The host persists it using `readFromNBT`/`writeToNBT`,
  marks changes after server-side insertion or removal, and drops the exact
  stored ItemStacks when broken. The hidden `surface_display_blocker`
  reserves the cell above an occupied display and routes item clicks to the
  host below. Cabinet storage has its own inventory and automation.
- Common-side `SurfaceContentRegistry` describes layout categories and
  footprints. `SurfaceInteractionRegistry` adds optional handling for an
  already displayed item. Duplicate item providers/handlers are errors.
  Registration closes at IAF post-init.
- `SurfaceSettingHost` supplies a surface height and layout offsets, with
  defaults preserving full-height tables and cabinets. Low tables use
  9/16 block height; shelves use 13/16 and a facing-relative offset.
  Below-ceiling displays participate in the owner's ray tracing, selection
  and mining. Their rendered bounds also drive selection; removing a loose
  item leaves the furniture in place.
- Shelves retain a separate exact-stack inventory for built-in books,
  records, lights, pots, plants and vessel arrangements. Loose settings use
  the shared registries. Vessel arrangements render through the same
  per-item renderer registry, without referencing addon classes. Low-table
  embedded content retains the donor's `EmbeddedContent`, `EmbeddedItem`
  and `EmbeddedFlowerPotPlant` tags. Shelf `EmbeddedKind`/`EmbeddedItems`
  remain compatible; legacy `DisplayedItem` data reads into `SurfaceItems`.
- Descriptors can supply rectangular bounds relative to the item centre,
  facing north. The host rotates these with the stored facing for selection
  and server interactions. The width/height constructor remains available.
- Vanilla edible items and cake use the catalogue at
  `assets/ironagefurniture/surface_foods.json`. It selects 35 surface models,
  including fish metadata variants, and supplies their common bounds.
  The client bakes the dedicated models during resource loading.
  A single meal sits in the centre; adding a drink forms a place setting.
- Client-side `SurfaceRenderRegistry` accepts specialised visuals before
  post-init. Unclaimed stacks use the generic Minecraft item renderer.
  Persistent NBT contains real ItemStacks and orientation, never renderer
  names. No client renderer is loaded by a dedicated server.
- Current tags are `SurfaceItems` and `SurfaceAxis`; an older single
  `DisplayedItem` is read and rewritten to the current form. The three
  slots are `GUEST_A`, `CENTER`, and `GUEST_B`.

## Food on vanilla surfaces

- With `options.VANILLA_FOOD_SURFACES=true` (default), sneak-right-click the
  top of a full-height vanilla block with a known vanilla food in the main
  hand and an empty offhand. Ordinary clicks keep their normal behaviour.
  Containers/tile entities, crafting tables, partial-height surfaces,
  modded blocks and occupied cells are excluded. The handler honours
  cancelled events, explicit block/item-use results and edit permissions.
  Automated fake-player clicks are left to their owning mod.
- Only the edible items listed in `surface_foods.json` are accepted. This
  is independent of the general surface-content registry: addon drinkware,
  arbitrary items and unknown modded foods remain furniture-only. Cake
  keeps its native block-placement/eating behaviour on vanilla surfaces.
- One portion is stored in the hidden, no-item `vanilla_food_display`
  block above the unchanged support, with `vanilla_food_display` tile data.
  It uses the same food models and bounds as furniture. Empty-hand
  right-click returns the exact item; Survival breaking, support loss,
  explosions and piston clearance drop it once. Creative mining deletes
  the displayed portion. It has no collision and does not block pistons.
- Forge 1.10 falls through from a cancelled block click to item use in air.
  A four-tick cooldown on the placed food suppresses this immediate eating
  attempt; no general item-use interceptor is registered. An occupied
  offhand opts out so this gesture cannot also activate an offhand item.
- Disabling placement preserves existing displays and their retrieval.
  No world lookups occur during the food tile's loading callback.

## Vessel and addon seam

- `api.Blocks.Barrel` and `api.tile.TileEntityBarrel` are non-craftable,
  unregistered base types for a future ordinary barrel and the separate
  Brewing foudre. They preserve `Tank` and `Sealed` fluid NBT. IAF does not
  register brewing fluids or a generic barrel block.
- `FluidAgingRegistry` accepts optional fluid policies during initialization
  and freezes at post-init. IAB contributes its brewing-specific ageing
  policies; IAF alone has none.
- Forge 1.10 saves the active mod ID in each fluid's default identity. IAB
  queues its legacy fluid registration with `LegacyFluidRegistration` during
  construction; IAF executes that callback in its own pre-init, retaining
  `ironagefurniture:*` defaults for unreleased 1.0 test saves. New IAB
  blocks, items, and tile entities use `ironagebrewing` names and remap
  the corresponding donor IDs. This callback does not register IAB content
  when IAB is absent.

## Furniture slices

The furniture retains the donor tables' lazy tiles, shelf corners and the
cabinet's 27-slot inventory, paired models and joins. Check standalone behaviour
before testing IAB and HarvestCraft visuals. Older 1.10 saves and
removed-addon cases need disposable copied-world tests before release or
upward port.
