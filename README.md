[![Discord](https://img.shields.io/badge/Discord-MMD-green.svg?style=flat&logo=Discord)](https://discord.moddev.zone)
[![CurseForge](https://img.shields.io/badge/CurseForge-Iron%20Age%20Furniture-orange.svg)](https://www.curseforge.com/minecraft/mc-mods/iron-age-furniture)
[![Build, test, and audit](https://github.com/MinecraftModDevelopmentMods/IronAgeFurniture/actions/workflows/ci.yml/badge.svg?branch=master-1.10)](https://github.com/MinecraftModDevelopmentMods/IronAgeFurniture/actions/workflows/ci.yml?query=branch%3Amaster-1.10)

# IronAgeFurniture

IronAgeFurniture adds functional chairs, stools, benches, beds, lamps, and
sconces to Minecraft. The published `0.4.0.110021` release adds Phase 4
furniture and lighting for Minecraft 1.10.2 with Forge 12.18.3.2511.
The unreleased `0.5.0.110021` candidate adds dining tables, low tables, wall
shelves and cabinets, surface extension APIs, and fixes to Phase 4 crafting
and sconce drops. These four furniture families cover the same 65 woods as
the chairs: six vanilla woods plus the enabled Biomes O' Plenty, Natura,
Forestry and Immersive Engineering variants.

The remaining storage set includes half-depth cabinets, upright fluid barrels,
side barrels and bottle racks in those same woods. Half cabinets hold 18
stacks and pair into 36 slots. Barrels hold 16,000 mB and preserve their
contents and seal when mined or converted between orientations. Bottle racks
hold nine exact bottles and can stand on the floor or mount on a wall.
Shelving units have two open display levels and are half a block deep.
Click a wall face to mount one, or the top of a solid block to centre it
on the floor. Matching units join side by side and stack vertically.
Right-click a level to place an item, and right-click the item with an
empty hand to retrieve it.
Each level can display up to five upright books, ten music discs, or eight
bottles in two rows of four. The disc tray and each bottle row fill from left
to right.
Right-click an empty level or a matching arrangement to add one at a time.
An empty-hand right-click opens a list of the books, discs or bottles on that level.
Click an item's name or icon to move it to the next free inventory slot.
The picker stays open so you can take several items. Click Close when done.
Each level also holds five loaves, nine apples, six potatoes, beetroot, cookies
or melon slices, fifteen carrots in layers of six, five and four, two pumpkin
pies or matching bowls of soup or stew, one cake, or one axe, pickaxe, shovel or hoe.
Wooden trays also display two matching cuts of meat or three matching fish.
Raw and cooked foods, and each fish variant, use separate arrangements.
One level can also display a single sword, bow, fishing rod, carrot on a stick,
pair of shears, or shield. Up to three flint and steel tools can share a level,
each retaining its own wear, name, and enchantments. Arrow displays hold six
matching arrows laid side by side, including spectral and tipped arrows.
Food must match the first item on that level, including its variant and custom name.
Produce trays also hold six eggs, mushrooms, poisonous potatoes, spider eyes
or chorus fruit. The meat tray holds two pieces of rotten flesh. Each level
can display three bundles of wheat or sugar cane, two matching bowls of
sugar, cocoa beans or seeds, or two milk pails.
Brewing ingredients have their own shelf displays. A level holds two bowls of
matching powder, six matching ingredients on a tray, or three rabbit's feet.
Dragon's breath shares the eight-bottle row and picker with potions.
Iron and gold ingots stack three, two, then one per level. Each wooden
tray holds six matching gold nuggets, coal, charcoal, diamonds, emeralds
or Nether quartz.
Each level can also hold six nested empty bowls or two matching empty,
water, lava or milk buckets.
Travel displays hold one working clock or compass, three rolled blank maps,
one open filled map, two coiled leads, or one saddle per level.
One shelf level can also display an individual piece of vanilla armour,
a wooden armour stand, any of the six kinds of head, mounted horse armour,
folded elytra, or a banner with its original pattern.
Crafting supply displays hold two matching leather or rabbit hides, three
spools of string, three stick bundles or feathers, six sheets of paper, or
one name tag per level.
Each shelf level fits two matching bowls of dye, or a wooden tray with up to
six ink sacs or pieces of lapis lazuli.
Material trays hold three bones or blaze rods, or six matching pieces of flint,
clay, slime, snow, prismarine shards, prismarine crystals, or popped chorus fruit.
Magic displays hold one End Crystal or Nether Star, three matching rockets,
or six matching pearls, Eyes of Ender, fire charges, firework stars, or spawn eggs.
Firework stars and spawn eggs use the colours of the displayed item.
Empty-hand right-click takes one displayed item directly from these arrangements.
Boats, minecarts, paintings and item frames each have a model sized for
one shelf level. A flower pot fits even when planted. Right-click it with
a pottable plant to fill it, then empty-hand right-click to take the plant
and again to take the pot.
Right-click a level with a vanilla sign to add a one-line label on its
front edge. Empty-hand right-click the label to edit it or mine the label
to remove it. The label leaves the level free for a display.
Utility displays hold one brewing stand, cauldron, vanilla bed, wooden or
iron door, repeater or comparator on each level.
Displayed glow and lava lamps light the shelves. Lava lamps and lava
buckets bubble and pop, and candles give off light, flame and smoke.
Empty sconces, obsidian chunks and tallow also have shelf-sized models.
Modular shelves display Base Metals and Modern Metals materials, tools,
armour and hardware. Ingots stack, nuggets go in trays, and powders go in
bowls. Metal-filled buckets show the stored fluid's texture and colour.
With Base Gems installed, each modular shelf level holds up to six cut stones
on an angled, fabric-lined platter. Different stones can share it. Right-click
a stone with an empty hand to retrieve it. Base Gems tools, blocks and other
material forms use the standard shelf displays.

Base Minerals powders go in wooden bowls. Its ores and storage blocks display
as miniature blocks, with separate layouts for ingots, nuggets and molten buckets.
Foudres, pot stills and brewing recipes belong to the separate Brewing addon.

- One full cabinet crafts into two matching half cabinets.
- Six matching planks across the top and bottom rows, with one iron ingot
  in the centre, craft three matching barrels.
- A barrel and two wooden sticks craft a side barrel. Craft it alone to
  recover the upright barrel and both sticks; fluid data stays intact.
- Four matching slabs in the crafting-grid corners and a central stick
  craft two bottle racks.
- Three matching slabs across the middle row and four sticks in the
  corners craft one shelving unit.

Barrels accept buckets and Forge fluid containers. With Brewing installed,
its bottles and drinkware use the same barrel without duplicating Brewing
content in IAF. Right-click an empty-handed barrel to open its fluid gauge
and seal controls. Sealing blocks fluid transfer; Brewing supplies any
applicable ageing policies.

Upright barrels also hold one kind of loose solid. Right-click the top with
wheat, seeds, pantry ingredients, produce or raw fish to start a batch. The
barrel opens and shows its fill level. Use an empty hand to open the bulk
storage screen, or run hoppers into the top and out of the bottom. Capacity
is 512 grain or seed, 256 small ingredients, or 128 produce or fish. Removing
the last item closes the barrel and frees it for fluids.

Right-click a barrel face with a sign to give it a one-line label. Right-click
the fitted sign with an empty hand to edit it, or break the sign to remove it.

- Craft a bedside cabinet with matching planks across the top row, then a
  plank, a wooden stick and a plank in each of the other two rows. Right-click
  either drawer from the front to open its nine slots. Right-click the top to
  display items such as a vase, lamp or drink.

- Craft a drawer unit with eight matching planks around a stick. For the
  half-depth version, leave the spaces to either side of the stick empty.
  Place matching units side by side for
  two 18-slot drawers, or stack them to any height. Each row opens
  separately. One block has nine slots per drawer, and each block keeps its
  own items when a pair is split. Hoppers can feed from the top, back or
  sides and pull from below.

- Craft a storage bench from a wooden bench and a chest. Craft it with a
  carpet to add or recolour the cushion. Right-click the seat to sit, or
  right-click the panel below it to open the 27-slot inventory. Hoppers can
  feed and empty the bench. Place two benches with matching wood, facing
  and cushion finish side by side to join them. Both panels open the same
  54-slot inventory, and the two seats can be used independently.

- Craft two wall tool racks with three matching planks in a row and a
  wooden stick under each end. Place them on the side of a solid wall.
  Matching racks join side by side. Right-click each hook to hang a tool,
  sword or vanilla shield, and use an empty hand to retrieve it. A hopper
  can fill the open hooks.

- Craft two produce trays with matching planks at either end of the middle
  row and three matching slabs below. Right-click a tray with food or an
  ingredient to stock one item and with an empty hand to take one. Each
  tray holds one exact type, with the same capacity as its shelf display.
  Place a second tray on top of the first to stack them in one block.
  Sneak-right-click with an empty hand to lift the top tray with its stock.
- Craft a market rack with matching planks down the centre and sticks
  down both sides. Leave two blocks of height for the frame. Right-click
  any of its three tiers with a tray to install it and sneak-right-click
  to remove it intact. Matching racks join side by side. Hoppers and item
  pipes can fill trays, starting with the highest matching or empty tray.

- Craft a shipping crate with matching planks across the top and bottom rows
  and wooden sticks at each end of the middle row. Right-click the front for
  its 16 slots. Sneak-right-click the front latch with an empty hand to seal
  or unseal it. A sealed crate keeps its contents and sign when broken and
  blocks automation. An unsealed crate spills its contents when broken.

Hoppers and Forge item pipes can feed bottle racks and both kinds of shelf.
Modular shelving fills the upper level first, then tries the lower level.
Bottle racks accept only bottles. Built-in wall-shelf displays can be fed
from above or the side. Use a side feed for loose wall-shelf displays that
need the space above. Pick up displayed items by hand.

- Craft an oak dining table with three oak slabs across the top row and
  sticks below the outer slabs. Adjacent tables join. Sneak-place against
  another table to leave a seam.
- Craft an oak cabinet with a chest and an oak plank, in any arrangement.
  Right-click it to open its 27 slots. Matching cabinets can pair side by
  side or vertically.
- Craft a low table with three matching wood slabs across the top row,
  another slab in the centre, and sticks in the two bottom corners.
- Craft three wall shelves with three matching slabs across the top row
  and a stick below the centre slab. Shelves extend along walls and around
  corners. They hold books, records, lamps, candles, flower pots and plants,
  as well as loose surface items. Empty-hand clicks retrieve their contents.
- Right-click a furniture surface with an item to display it. Right-click the
  displayed item with an empty hand to retrieve it. The cabinet inventory
  is separate from its display.
- Empty bottles, drinking/splash/lingering potions and experience bottles
  stand upright in 3D on furniture, with their liquid colour and exact item
  data preserved. Addon items registered as bottles share this renderer.
- To put a known vanilla food on a full-height vanilla surface, hold it in
  the main hand, empty the offhand, and sneak-right-click the top. Pick it
  up with an empty-hand right-click. Modpacks can disable new placements
  using `options.VANILLA_FOOD_SURFACES`; general item displays stay on IAF furniture.

The dining-table, low-table, shelf and cabinet recipes use the matching
wood throughout. Optional wood variants appear when their source mod and
the corresponding integration setting are enabled.

Dining chairs come in those same woods. Craft one with matching wooden slabs
in the top-left slot and across the middle row, and sticks in the bottom corners.
Sneak-right-click an empty chair facing a dining table to tuck it under the
table. Sneak-right-click again to pull it out before sitting down.

Creative inventory categories separate chairs, benches, beds, lighting,
tables/shelves, storage and decorations. Decorations include sixteen glass
vase colours: arrange four matching stained-glass blocks in a diamond
(` x `, `x x`, ` x `) to craft four vases. Place them normally or display
them on furniture. Right-click a vase with a flower or other supported
potted plant to add it; an empty-hand click removes the plant first.
Breaking a planted vase returns the vase and plant separately in Survival.

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
- MrCrayfish's Furniture Mod (chair conversion recipes and saved-furniture migration)

Phase 5 migrates the six wooden variants of CFM chairs, cabinets, bedside
cabinets, dining tables and coffee tables when CFM is absent. Placed
and packed CFM crates convert to dark oak shipping crates with their slots
and seal preserved. To convert this furniture
with CFM installed, enable `FORCE_CFM_FURNITURE_CONVERSION` in
`config/ironagefurniture.cfg` before loading the world. Conversion happens
as chunks load. Cabinet slots are preserved, and the original bedside
inventory goes into the upper drawer. CFM-only cabinet items with no IAF
replacement stay usable while CFM is installed. If their mod is absent,
their saved data is kept in the cabinet until the items become available again.

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
