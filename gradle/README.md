# Build logic

`build.gradle` contains the Forge 1.10 setup: dependencies, Java 8 compilation,
development runs, processed resources and ordinary jars. The longer tasks live
in small, applied scripts so their purpose is easier to find. These scripts are
part of this repository; they are not separate Gradle plugins.

| Script | Purpose |
| --- | --- |
| `furniture-catalog.gradle` | Check the generated furniture catalog and update it only when explicitly requested. |
| `phase5-furniture-assets.gradle` | Generate and verify the four Phase 5 furniture families from the chair wood catalog. |
| `verification/legacy-fixtures.gradle` | Rebuild and compare the checked-in old-world migration fixtures. |
| `verification/optional-integrations.gradle` | Inspect pinned optional-mod jars and run the opt-in packaged-server probe. |
| `verification/project-audits.gradle` | Check source resources, metadata, namespace, workflows and repository hygiene. |
| `release/artifacts.gradle` | Audit the three release jars, checksums and prepared bundle. |
| `release/publishing.gradle` | Set Maven publication details and require explicit remote credentials. |
| `ide/eclipse.gradle` | Prepare Buildship and keep normal Eclipse launches free of test/probe output. |
| `ide/phase5.gradle` | Build sibling prototypes, verify pinned mods and stage the Phase 5 launches. |

The root build applies these scripts in dependency order and shares only the
few values they need. CI, Eclipse setup and the release workflow call the
existing task names directly, so renaming a task would also require updating
those callers.

For a routine local check, run `gradlew.bat clean check build javadoc
verifyReleaseArtifacts verifyReleaseChecksums`. Run `gradlew.bat prepareEclipse
verifyEclipseProductionClasspath` after changing the build or importing the
project into Eclipse. The optional-integration server probe is separate from
the routine build. It needs the pinned mod jars listed in
`verification/optional-integration-mods.json` and the official Forge installer
checked by `verifyOptionalIntegrationForgeInstaller`.

## Phase 5 Eclipse profiles

From the active IAF checkout, run `gradlew.bat preparePhase5Eclipse` with
Java 17 for Gradle and a Java 8 toolchain for the game. Eclipse must have a
JavaSE-1.8 runtime configured. Refresh the project and choose a
`Phase5-<profile>-client` or `Phase5-<profile>-server` Java launch. Run the
task again after editing any of the three mods: the launches use the rebuilt
development JARs so classes and processed resources always match.

| Profile | Additional mods |
| --- | --- |
| `furniture-only` | None |
| `furniture-brewing` | Iron Age Brewing |
| `furniture-harvestcraft` | HarvestCraft and the HarvestCraft addon |
| `furniture-brewing-harvestcraft` | Brewing, HarvestCraft and its addon |
| `full-modern-advantage` | The previous stack plus basePower, baseSteam, baseElectric, Base Metals and OreSpawn; JEI on the client |
| `brewing-legacy-advantage` | Brewing, original Power Advantage, Base Metals and OreSpawn |
| `optional-woods` | Biomes O' Plenty, Natura, Mantle, Forestry and Immersive Engineering |

Default sibling projects are `../../IAB110/IronAgeBrewing` and
`../../IAFPHC110/IronAgeFurniture-PamsHarvestCraft-Addon`. Override them with
`-Pphase5BrewingDir=...` and `-Pphase5HarvestCraftAddonDir=...` as needed.
Ordinary `prepareEclipse` and Buildship sync still work with IAF alone.

The `optional-woods` client uses an 8 GB heap because baking all of the
enabled furniture models exceeds 4 GB. Other client profiles and all servers
use 2 GB. These limits are recorded in the fixture manifest and regenerated
with the launches.

After changing the Phase 5 family templates or wood catalog, run
`gradlew.bat updatePhaseFiveFurnitureAssets updateFurnitureCatalog` and
review the generated resource changes. Ordinary builds verify those files
without rewriting them.

[The fixture manifest](ide/phase5-fixtures.json) locks published file IDs and
checksums. The task fetches them through Curse Maven (JEI from BlameJared),
uses the existing optional integration hashes for the wood fixtures, and
remaps development copies. Advantage and optional wood launches use isolated
mapped Forge copies with the selected mods' access rules applied. The ordinary Forge
runtime, release artifacts and existing `run` directory are unchanged.
Each profile and side has its own `run-phase5/profiles` directory; preparation
refreshes its managed JARs and leaves its saves and config alone. Stop an
active game before refreshing, and accept the EULA in a new server profile
before launching it interactively.

For stock Forge tests, run `gradlew.bat stagePhase5PackagedServers` and
`installOptionalIntegrationServer` (which needs the verified installer).
Then run `gradle/verification/phase5-smoke.ps1 -Java8 <java.exe> -Mode
PackagedServer -ForgeServerTemplate <installed-server-directory>` in
PowerShell 7. The same script supports `-Mode Client` and `-Mode Server`
against the generated development launches, with optional `-Profile`
selection. Smoke runs use fresh disposable directories. The packaged test
checks the exact mod set and probes full, partial and zero fluid acceptance
on each Advantage family, including matching changes in the two vessels.
