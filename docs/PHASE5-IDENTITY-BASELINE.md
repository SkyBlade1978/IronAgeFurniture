# Phase 5 identity baseline (Minecraft 1.10.2 Forge)

The immutable comparison point is MMD release `0.4.0.110021`, Git commit
`fce2de35f87d0ee74d653094ec61800ba247eb94`. Phase 5 must not change
the meaning of any block, item, entity, tile-entity ID or saved metadata in
that release. The committed `gradle/furniture-catalog.snapshot` (SHA-256
`4A662F4D580A544382BFF12ADB3902C415B96E94747701954CF15BBA9669F295`)
records the supported woods, colours, lighting IDs and Phase 4 furniture
families; the catalog audit remains the machine-readable block/item guard.
The core names are enumerated in `PHASE4-CORE-REGISTRY-IDS.txt` (185 blocks,
97 items in a vanilla-wood run). This was read from a disposable Forge save
of the Phase 5 candidate after confirming the Phase 4 registration sources
are unchanged; optional wood variants remain specified by the catalog.

## Published Phase 4

- Mod ID and registry namespace: `ironagefurniture`.
- Tile entities: `ironagefurniture:padded_bench_colour`,
  `ironagefurniture:upholstery_colour`, `ironagefurniture:shield_chair`,
  `ironagefurniture:metal_variant`.
- Mod entities: `ironagefurniture:seat` (numeric ID 0),
  `ironagefurniture:thrown_lava_lamp` (1), and
  `ironagefurniture:released_lava_lamp` (2).
- Registered fluid IDs: none. The published IAF artifact must continue to
  load without IronAgeBrewing, HarvestCraft or Power Advantage.
- Save contracts include the coloured upholstery and padded-bench tile data,
  shield-chair `Empty`/`Shield` ItemStack tags, the metal-variant tile data,
  bed colour/part metadata, sconce metal/facing data, and CFM chair remaps.
  Preserve their existing serializers and migration tests unchanged.

## Unreleased 1.0 donor, not part of the published baseline

The working donor is `feature/1.10-v1.0.0` at `4428da0bcc693bd29df3d460068e4dc4f4d49b74`
with substantial uncommitted work. Its source must be copied selectively;
neither its branch nor its working tree may be reset or merged wholesale.

- Donor tile IDs include `ironagefurniture:table_dining`,
  `ironagefurniture:cabinet_wood_ironage`,
  `ironagefurniture:half_cabinet_wood_ironage`,
  `ironagefurniture:barrel_wood_ironage`,
  `ironagefurniture:foudre_wood_ironage`,
  `ironagefurniture:foudre_wood_ironage_port`,
  `ironagefurniture:pot_still_wood_ironage`,
  `ironagefurniture:pot_still_wood_ironage_port`,
  `ironagefurniture:ornament_glass_vase`,
  `ironagefurniture:shelf_wall`,
  `ironagefurniture:bottle_rack_wood_ironage`,
  `ironagefurniture:chandelier_grand_sconce`,
  `ironagefurniture:hanging_inn_sign`, and
  `ironagefurniture:surface_display`.
- Donor surface NBT: `SurfaceItems`, `SurfaceAxis`, and legacy
  `DisplayedItem`; the contained ItemStacks and facing must round-trip.
- Donor vessel NBT: generic barrel `Tank` and `Sealed`; foudre
  `FoudreLabel`, `Ingredients`, `BrewTime`, `BrewTimeTotal`, `BrewRecipe`,
  `BrewRecipeName`, `InfusionComplete`, `PortsConfigured`, `InletOpen`,
  `OutletOpen`, and `PortMode`; pot still `InputTank`, `OutputTank`, `Fuel`,
  `DistillTime`, `DistillTimeTotal`, `BatchInput`, `BatchOutput`,
  `BatchName`, and the port configuration tags.
- Donor fluid names are registered by `FoudreBrewingRegistry` and
  `PotStillDistillingRegistry`. Preserve those exact global names and their
  fluid-stack NBT when moving brewing behaviour to a new mod.

Before publishing either new addon, produce an expanded registry inventory
from its packaged JAR and test remapping of disposable 1.0 donor saves to
the new `ironagebrewing` block/item IDs. Do not treat the donor IDs as
published Phase 4 identities, and do not register storage furniture in the
Phase 5 foundation slice.
