[![Discord](https://img.shields.io/badge/Discord-MMD-green.svg?style=flat&logo=Discord)](https://discord.moddev.zone)
[![CurseForge](https://img.shields.io/badge/CurseForge-Iron%20Age%20Furniture-orange.svg)](https://www.curseforge.com/minecraft/mc-mods/iron-age-furniture)
[![Build, test, and audit](https://github.com/MinecraftModDevelopmentMods/IronAgeFurniture/actions/workflows/ci.yml/badge.svg?branch=master-1.10)](https://github.com/MinecraftModDevelopmentMods/IronAgeFurniture/actions/workflows/ci.yml?query=branch%3Amaster-1.10)

# IronAgeFurniture

IronAgeFurniture adds functional chairs, stools, benches, beds, lamps, and
sconces to Minecraft. The published `0.4.0.110021` release adds Phase 4
furniture and lighting for Minecraft 1.10.2 with Forge 12.18.3.2511.
The unreleased `0.5.0.110021` candidate adds an oak dining table, an oak
cabinet, surface extension APIs, and fixes to Phase 4 crafting and sconce drops.

- Craft an oak dining table with three oak slabs across the top row and
  sticks below the outer slabs. Adjacent tables join. Sneak-place against
  another table to leave a seam.
- Craft an oak cabinet with a chest, an oak plank, and an iron nugget
  (an iron ingot when no mod supplies iron nuggets). Right-click the cabinet
  to open its 27 slots. Matching cabinets can pair side by side or vertically.
- Right-click either top with an item to display it. Right-click the
  displayed item with an empty hand to retrieve it. The cabinet inventory
  is separate from its display.
- To put a known vanilla food on a full-height vanilla surface, hold it in
  the main hand, empty the offhand, and sneak-right-click the top. Pick it
  up with an empty-hand right-click. Modpacks can disable new placements
  using `options.VANILLA_FOOD_SURFACES`; general item displays stay on IAF furniture.

Phase 3 contains the established seating collection plus empty, torch,
redstone-torch, glow, lava, and redstone lighting, including throwable lava
lamps and obsidian chunks.

Phase 4 adds multiblock wooden and canopy beds, wingback and throne chairs,
removable shield chairs, twin-torch sconces, candles, Mineralogy rock-salt
sconces, and gold/Base Metals sconce materials. Wall lava-lamp sconces can
release their lamp on redstone, landing intact in Creative but shattering and
igniting the landing area in other modes. Seventeen locale choices and CFM
wooden-chair migration support are included.

Water buckets extinguish candle sconces. Redstone relights them, including
when power was already present before the bucket was used. Removing the
signal does not extinguish these dry candles; their count and metal stay intact.

Mining a lava-lamp sconce with a suitable Silk Touch pickaxe returns the empty
sconce and intact lamp separately. Without Silk Touch, the sconce still drops
in its original metal. A wall-mounted lamp falls before shattering on dry
ground, or cooling into an obsidian chunk in water. A floor-mounted lamp
shatters where it stands. Right-click removal is safe and returns the intact
lamp without breaking the frame. These rules include available Base Metals.

Carried candles show a small animated flame and a little smoke without any
extra mod. With OptiFine's Dynamic Lights enabled, a held candle can also light
the area around you; that illumination is an OptiFine visual effect, not a
placed light block.

Wooden and canopy beds are flammable like chairs. If fire consumes any part,
the whole bed burns away.

Sconces return their holder and contents even when broken by hand or when
their support is removed. A pickaxe is still faster, and the chosen metal
still determines toughness. Lava lamps retain the shattering behaviour above.

Chair upgrades are shapeless: a classic chair, matching plank and carpet make
a wingback; a wingback, matching plank and matching carpet make a throne.
The throne keeps the wingback's colour rather than recolouring it.

Minecraft 1.10 has only red vanilla beds. A vanilla bed and one plank make a
red wooden bed; recolour it with carpet if you want another colour. Add a
matching plank and matching carpet to that wooden bed to make a canopy bed.
Two matching single beds make a double bed. Three iron nuggets make four
empty sconces; without iron nuggets, the older three-ingot recipe makes 32.

## Requirements

- Minecraft 1.10.2
- Forge 12.18.3.2511
- Java 8 for the game

Padded benches and padded back benches support all sixteen vanilla carpet
colours without consuming additional block or item IDs. Existing benches and
damage-zero items remain red, while newly crafted colours persist through
placement, drops, pick block, connected-shape changes, and chunk reloads.
Items retain a stable colour name for compatibility with future Minecraft
versions as well as the metadata used by Minecraft 1.10. Mixed-colour bench
runs may connect normally.

## Optional integrations

Matching furniture and Java-side recipes are registered when these mods are
loaded:

- Biomes O' Plenty
- Natura
- Forestry
- Immersive Engineering
- Base Metals (extra sconce materials)
- Mineralogy (rock-salt lamps)
- MrCrayfish's Furniture Mod (chair conversion recipes)

All integrations are optional. A normal installation requires none of them,
and furniture for an absent integration is not registered. Minecraft 1.10.2
predates the recipe-book advancement format, so this target registers recipes
in Java rather than packaging later-version recipe or advancement JSON.
The available Base Metals sconce materials follow the installed Base Metals
build; older 1.10 builds do not provide Antimony, Bismuth, or Pewter. Sconce
mining hardness and blast resistance also vary with the chosen metal.

## Compatibility

The persistent mod ID, registry names, resource paths, configuration names,
entities, and saved-world identity remain under `ironagefurniture`. Existing
Phase 2 and Phase 3 worlds therefore retain their surviving furniture
identities. Untagged older furniture uses red upholstery or a plain shield;
older sconces with no metal data remain iron. When CFM is absent, its six
wooden chair block and saved item identities are recovered as classic chairs;
IAF records the old numeric IDs in the world so unopened chunks still migrate
after a restart. Forced conversion while CFM remains installed is a separate,
default-off option. Back up older worlds before first opening them in Phase 4.

The supported Java namespace is `zone.moddev.mc.ironagefurniture`. Add-ons
compiled against the former `com.mcmoddev.ironagefurniture` packages must
update their imports. The Maven coordinate is
`zone.moddev.mc:iron-age-furniture:0.5.0.110021`.

See [CHANGELOG.md](CHANGELOG.md) for release notes and
[docs/VERSIONS.md](docs/VERSIONS.md) for the versioning scheme. Report bugs
through the [MMD issue tracker](https://github.com/MinecraftModDevelopmentMods/IronAgeFurniture/issues).

## Building from source

Use the checked-in Gradle wrapper with Java 17 or newer for Gradle and a
Temurin Java 8 compiler toolchain. CI pins Java 17 for Gradle and Java
8.0.502+7 for compilation; Eclipse may run Buildship on a newer JVM and may
use a newer Temurin Java 8 patch release for the compiler toolchain.

```text
gradlew.bat clean check build javadoc verifyReleaseArtifacts verifyReleaseChecksums
gradlew.bat prepareEclipse verifyEclipseProductionClasspath
```

The build tasks and their scripts are described in [gradle/README.md](gradle/README.md).

For the Phase 5 addon development stack, run `gradlew.bat preparePhase5Eclipse`
and refresh the Eclipse project. See the [profile setup and smoke tests](gradle/README.md#phase-5-eclipse-profiles).

The release process only publishes commits that have passed the hosted build,
test, artifact, and security checks. Running the Gradle commands above creates
local artifacts only.

IronAgeFurniture is licensed under LGPL-2.1.
