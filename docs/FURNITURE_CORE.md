# Skys Furniture Core for Forge 1.10.2

Phase 5 Iron Age Furniture requires Skys Furniture Core, a standalone project
in the sibling workspace `SkysFurnitureCore110/SkysFurnitureCore`. Core's mod
ID is `skysfurniturecore` and its Maven coordinate is
`zone.moddev.mc:skys-furniture-core:0.1.0.110021`. Install the Core and
Furniture runtime JARs together on each client and server.

Both Eclipse preparation tasks automatically build the sibling's development
JAR. Use `-PfurnitureCoreDir=...` for a different checkout or supply
`-PfurnitureCoreDeobfJar=...` and, for packaged server tests,
`-PfurnitureCoreRuntimeJar=...`. Builds without the sibling can use
`publishToMavenLocal` from Core or resolve the pinned version from the
repository set by `FURNITURE_CORE_MAVEN_URL`.

| Feature | API owner | How to extend it |
| --- | --- | --- |
| Chairs and benches | Core | Create a block with `FurnitureBlocks.chair` or `FurnitureBlocks.bench`. Supply north-facing selection and collision boxes and the seat height. |
| Drawer units | Core | Supply one or more `DrawerDefinition`s and a `DrawerTile` subclass that passes those definitions to its constructor. Register the tile under your own mod ID. Set the paired flag for double-width drawers. |
| Placed items | Core | Use `FurnitureBlocks.surface` for a simple host or implement `SurfaceSettingHost` in an existing tile. Register bounds and layout with `SurfaceContentRegistry` and special interactions with `SurfaceInteractionRegistry`. Register custom drawing through the client-only `SurfaceRenderRegistry`. |
| Lamps | Core | Use `FurnitureBlocks.lamp` or extend `LampBlock` for a particular mount, light level and lit effects. |
| Generic barrel ageing | Core | Register a `BarrelRecipe`. A recipe mod can work with any vessel that opts into the recipe's vessel type, including IAF's ordinary barrels. |
| Drinks, brewing and distilling | Iron Age Brewing | Register a `DrinkDefinition` with `BrewingContributionRegistry` for strength, nourishment and drink ageing stages. The registry also accepts ingredients and water-based brewing recipes. |

Content mods register their own blocks and item blocks under their own
namespaces, with their own recipes, models, textures and translations. IAF
owns its furniture styles, shield chairs, lava-lamp behaviour, shelving
pickers, and saved IDs. IAB owns drinkware, brews and brewing appliances and
continues to require IAF. The HarvestCraft addon uses Core for item placement
and rendering and contributes brewing recipes only when IAB is loaded.

Register surface and barrel contributions during Forge pre-initialization or
initialization after the required items and fluids exist. Keep renderer
registration in the client proxy. Core freezes its registries at
post-initialization. Brewing collects its contributions before finalising
its recipe catalogue.

## Barrel recipes

An external mod can use `BarrelAgingRegistry.register` with a namespaced
recipe ID, the vessel type (use `BarrelRecipe.BARREL` for standard barrels),
the required fluid stack, an optional solid stack, the number of loaded
server ticks, and up to one fluid and one solid output. Item matching includes
metadata and NBT. A null input-fluid tag accepts any tag. The recipe owner
registers and names its own fluids and items.

The standard barrel keeps its compact fluid screen by default, including when
Iron Age Brewing is installed. Brewing registers drink-ageing rules separately.
Registering a batch recipe for a vessel enables its ingredient and result
slots and the corresponding automation ports.

Players add fluid and any solid ingredient, then seal the barrel to select the
batch. The top item capability takes matching ingredients and the bottom
capability extracts the result. The barrel retains its inputs if two recipes
match, a recipe is removed, or the output cannot fit. If an add-on is removed
and no batch recipes remain for the vessel, its batch items stay saved and the
compact screen returns. Reinstalling the add-on makes the slots available
again. Breaking and placing the barrel retains the batch as item data.

The examples under `src/probe/java/fixture/furniture` in the Core checkout
show a content mod registering furniture and an independent recipe mod
contributing milk-to-curd and a tanning recipe with solid and liquid outputs.
In the Core checkout, run `gradlew.bat renameProbeJar`. Run the resulting
`build/probe/furniture-core-probe.jar` with only the Core runtime JAR on a
disposable Forge 1.10.2 dedicated server. The probe reports
`SKYS FURNITURE CORE STANDALONE PROBE PASSED`.

## Release order

Publish Core before IAF, then publish compatible Brewing and addon builds.
Use the Core version built for the same Minecraft version and loader. When
the Core file is available, set the repository variable
`FURNITURE_CORE_CURSEFORGE_SLUG` to the Core project slug. The IAF release
workflow uses it to declare Core as a required CurseForge dependency.
Set `FURNITURE_CORE_MAVEN_URL` to the Maven repository hosting the exact
Core version and its development classifier for the hosted builds.
