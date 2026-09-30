# Phase 5 1.10 surface and vessel contract (unreleased)

This is an API candidate for storage design, not a Phase 5 furniture release.
It adds no cabinet, shelf, bottle rack, table, or craftable barrel. The
published `0.4.0.110021` identities remain the baseline; `update.json` still
advertises that release.

## Surface host

- A future furniture tile implements `SurfaceSettingHost` and owns one
  `SurfaceSetting`. The host persists it using `readFromNBT`/`writeToNBT`,
  marks changes after server-side insertion or removal, and drops the exact
  stored ItemStacks when broken. The test-only host proves this without
  registering a block.
- Common-side `SurfaceContentRegistry` describes layout categories and
  footprints. `SurfaceInteractionRegistry` adds optional handling for an
  already displayed item. Duplicate item providers/handlers are errors.
  Registration closes at IAF post-init.
- Client-side `SurfaceRenderRegistry` accepts specialised visuals before
  post-init. Unclaimed stacks use the generic Minecraft item renderer.
  Persistent NBT contains real ItemStacks and orientation, never renderer
  names. No client renderer is loaded by a dedicated server.
- Current tags are `SurfaceItems` and `SurfaceAxis`; an older single
  `DisplayedItem` is read and rewritten to the current form. The three
  slots are `GUEST_A`, `CENTER`, and `GUEST_B`.

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

## 4a handoff

First expand and agree the storage furniture set in the 1.0 donor. For each
4b slice, implement inventory/capability, surface host, interactions, exact
drops, NBT round-trip, facing, and vanilla generic rendering in IAF alone.
Then test IAB and HarvestCraft specialised visuals separately. Do not port
the whole 1.0 checkout or add ordinary barrels until their independent
storage behaviour is proven. Older 1.10 saves and removed-addon cases need
disposable copied-world tests before a release or upward port.
