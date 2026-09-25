# Hearthwind 1.0.0: Aged 3.1.2 parity plan

**This is the governing document for 1.0.0.** Where any other doc
(PROJECT_DIRECTION, FEATURE_PARITY, NOT_IMPLEMENTED, MOD_ROLES,
DROPPED_78_STUDY, PATCH_PORT_STUDY, AGENTS.md "Next steps") disagrees
with this file about scope or status, this file wins.

Audit date: 2026-09-24. Target: MC 26.2 (Fabric), client **and** server.

## 1. Scope rule for 1.0.0

1.0.0 = **Aged 3.1.2, on 26.2, looking and playing like Aged.**

- **In scope:** every mod Aged 3.1.2 ships (212 Modrinth jars +
  `agedaddition-1.0.6` bundled in overrides), its tuned config overrides,
  its resource packs and shader packs, and its datapack. Both the server
  pack and the client pack.
- **Allowed exception:** Hearthwind's own modules (`hearthwind-survival`,
  `-skills`, `-jobs`, `-primitive`, `-world`, `-client`) that already
  replace Aged mods, including changes they already made (5-group diet,
  container spoilage, and so on). Record those as deviations; don't
  undo them.
- **Newer mod versions are fine.** When a mod ships in a newer version than
  Aged's, compare its behaviour with Aged's version. Record any deviation in
  the ledger and fix it on the current version where possible, instead of
  pinning the old one.
- **Nothing is marked obsolete.** Every Aged mod either ships or waits in
  the port queue. Whether to port, adopt upstream, or replace is a
  **team decision made one mod at a time**, recorded in the ledger below.
- **Out of scope until after 1.0.0:** new mechanics and anything not in
  Aged (see section 7). Deferred, not cancelled.

## 2. Reference artifact (single source of truth)

`.tmp/Aged-3.1.2.mrpack`, sha1 `1b01452a11ef6e2f0c1dc0fd9733349bf6bf08e8`
(<https://modrinth.com/modpack/aged/version/3.1.2>). Its
`modrinth.index.json` has 226 files: 212 `mods/`, 12 `resourcepacks/`,
2 `shaderpacks/`. Overrides add `mods/agedaddition-1.0.6.jar`,
48 config entries, `config/paxi` (the datapack), and
`resources/aged` (title-screen art).

Work from the downloaded artifact and its hashes, not from memory or hand-typed slugs. The current `modrinth.index.json` exposes 226 file entries (212 mod jars) but does not include project-id fields, so the manifest still needs an explicit alias/review pass before any future automated project-id migration. The current audit is recorded in `docs/AGED_3.1.2_PARITY_AUDIT.md`; its filename audit leaves 57 Aged jars for manual alias/dependency review rather than silently treating them as absent.

## 3. Scoreboard (212 Aged mods)

| Bucket | Count | Meaning |
|---|---|---|
| Shipped | 96 | In the 26.2 pack today (Modrinth, vendored jar, `custom-mods` port, or datapack) |
| Rebuilt in Hearthwind modules | 20 | Replaced by our code; parity gaps listed in section 5.3 |
| **26.2 build exists, not added** | **30** | Official Fabric 26.2 release on Modrinth, missing from the manifest. Cheapest parity win. |
| Needs port | 65 + 1 WIP | No 26.2 Fabric build. Port-tier column ranks the effort |
| Plus: `agedaddition` | 1 | Rebuilt in `hearthwind-primitive` (section 5.4) |

**2026-09-24 reconciliation:** the scoreboard above is a historical planning ledger, not a claim that every bucket is shipped. The verified current build contains 53 resolved upstream mods (50 server, 3 client-only), 68 resolver `missing_for_target` records, and 12 API-error records. The authoritative current counts and Aged-only review list are in `docs/AGED_3.1.2_PARITY_AUDIT.md`.

## 4. What drifted, and what was fixed in this audit

Fixed (small bug fixes, 2026-09-24):

1. **Vendored lavender jar was stale:** the guidebook commit never
   refreshed `conversion/vendored/lavender-26.2+0.1.0.jar`, so the pack
   shipped no Aged guidebook, and the built jar lacked the 26.x item
   definition (missing-texture book). Both fixed. Other vendored ports
   match their builds. W0 should add a check that vendored jars equal
   their `custom-mods` builds.
2. **mrpacks shipped without any local jars.** `build_pack.py` read
   `build/dist/server/mods` before `--server-dir` filled it, so on a clean
   run every `.mrpack` had 0 of our ports (no hearthwind-*, letsdo-*,
   YUNG, dungeonz, ...). Now jars are collected from `conversion/vendored`
   and `custom-mods/*/build/libs` directly: 56 bundled jars in the server
   pack, 57 in the client pack.
3. **Season length.** Aged `config/seasons.json` = 504000 ticks per season
   = **21 days**. `hearthwind-world` defaulted to 18. Default is now 21 (the
   client already assumed 21).
4. **AgedAddition coverage.** `coal_piece` now burns 400 ticks (Aged
   `FuelRegistry.add(COAL_PIECE, 400)`), and the 11 piece/nugget items are
   listed in the Ingredients creative tab like Aged's. Gametest
   `agedAdditionCoalPieceIsFuel` added.

5. **Item and entity names showed raw keys** (`item.bakery.bread`).
   26.x builds a BlockItem's name key from the item id, where 1.20.1
   borrowed the block's name, and the in-house Naturalist/Inmis/Atlas
   stand-ins shipped no lang files. 118 item names and 24 entity names
   were added (Aged's exact wording where Aged had the item, vanilla-style
   names otherwise). New client gametest `ItemNameGameTests` walks the live
   item and entity registries and fails on any raw key.

Still open (do these first, section 6, W0):

- **Client gametest harness passed without running every test** (fixed
  2026-09-24). Closing the in-process dedicated server in
  `PackServerConnectGameTests` exits the whole client JVM with code 0, so
  `BiomeTempGameTests`, `ShowcaseTourGameTests` and the new
  `ItemNameGameTests`, registered after it, never ran, and the harness
  still printed PASS. That test is now registered last. Since the harness
  already requires its screenshot, a PASS proves every earlier test ran.
- **Runaway datapack function.** Every client run logs
  `Command execution stopped due to limit (executed 65536 commands)` about
  1600 times, starting the moment a player joins a world, alongside
  constant "Can't keep up!" lag. Suspects: self-recursive
  raycast/find-ground functions in the vendored True Ending jar (1.21.5 and
  1.21.9 overlays, e.g. `boss/shockwave/find_ground`,
  `respawning/place_end_crystal_raycast`) and `medieval_buildings:main_1s`.
  Confirm by booting a server with each suspect removed.
- **Gaps found by checking Aged's guidebook against our code (2026-09-24,
  verified in code; full checklist in `docs/GUIDEBOOK_PARITY.md`):**
  - ~~The diet low-nutrient effect lists reuse the *positive* attribute
    values, so deficiency grants a buff.~~ Fixed 2026-09-24: the bug is in
    Aged's own NutritionZ 1.0.11 data too (its code applies the values as
    written), but Aged's guidebook documents the mirror penalties, so we
    follow the documented design. Gametest `nutrientDeficiencyIsAPenalty`.
  - The config keys `dirtyWaterSicknessChance/Duration`,
    `throatIrritationDuration` and the biome thirst-drain multipliers are
    never read.
  - Aged's Dehydration water chain is missing: purifying bottles on a
    campfire, the campfire cauldron, the copper rain cauldron and the bamboo
    pump.
  - The fallback written guidebook in `StarterKit` has wrong facts.
  - The Comfort card screen (`SurvivalInfoScreen`) can't be opened in game.
  - Jobs earn XP only from breaking ladder blocks and killing ladder mobs.
    Aged also pays builder for placing blocks, fisher for fishing, smither
    for anvil/smithing/furnace output, farmer for crafting/smoking food, and
    brewer for brewing. Those jobs barely level today (`JobEvents.java`).
  - ~~Party XP sharing was not wired.~~ Fixed 2026-09-24 to PartyAddon's real
    behaviour (orb XP pools at the leader, split evenly among members in the
    leader's level); see 5.10 for the advertised-but-unapplied bonus.
  - End Remastered eyes (16 `endrem:*_eye` items) have no recipe or loot
    source, and throwing one only consumes it (no eye entity to follow).
  - `SkillInfoScreen` (skill drill-down) can't be opened in play.
  - Docs describing a "5 food groups" diet are wrong: the implementation is
    Aged's NutritionZ model (Carbohydrates, Protein, Fat, Vitamins, Minerals,
    0-300).
- **Duplicate jars in the pack:** vendored `architectury-fabric-21.0.7`
  next to Modrinth 21.1.10, vendored `supermartijn642corelib-1.1.24a`
  next to 1.1.24b, vendored `modmenu-20.0.1` next to 20.0.2. Remove the
  stale vendored copies (check `patch_vendored.py` doesn't target them
  first).
- **Aged config tuning isn't carried over** for mods we already ship:
  `lootr.json`, `sparsestructures.json5`, `immersive_aircraft.json`,
  `immersive_armors.json`, `smallships-common.toml`, `adventurez.json5`,
  `couplings.toml`, `logbegone.toml`, `combatroll`, `modmenu.json`. Only
  `combat_roll` and `kiwi-client.yaml` exist in `conversion/overrides/config`.
- **Docs claiming ✅ that the data contradicts:** FEATURE_PARITY rates
  jobs/rpgdifficulty/seasons ✅ where AGED_PARITY shows 🟡 gaps.
  AGED_PARITY says Aged had no revive mod, but Aged ships `revive-1.0.7`.
  DROPPED_78_STUDY says every mod with a 26.2 build is deployed, but 30 are not.
- **Non-Aged additions currently in the pack:** `c2me-fabric` (manifest
  `add`). Libraries pulled in as dependencies are fine
  (`lithostitched` for Nature's Spirit, `placeholder-api`, `strawberrylib`,
  `kaleido-config`, `MoogsStructureLib`, `PlayerAnimationLib`). Terralith,
  Tectonic, Visuality and Waterfall Particles are correctly being removed
  (uncommitted manifest change in the working tree).

## 5. Ledger

### 5.1 Official 26.2 Fabric build exists, not in the pack (30)

Add to the manifest by **Modrinth id**. Client-only mods go into the client
pack only. Notes: Iris needs Sodium; FancyMenu needs Konkrete + Melody (all
three are in this list); `shatterbyte-lib` is OctoLib's renamed project.

| Aged mod | Aged file | Side | 26.2 build | 26.3 build |
|---|---|---|---|---|
| `3dskinlayers` | skinlayers3d-fabric-1.7.2-mc1.20.1.jar | both | 1.11.3 | 1.11.3 |
| `badoptimizations` | BadOptimizations-2.2.0-1.20.1.jar | both | 2.4.1 | 2.4.1 |
| `better-archeology` | betterarcheology-1.2.1-1.20.1.jar | both | 26.2.x-1.3.8 | - |
| `entityculling` | entityculling-fabric-1.7.1-mc1.20.1.jar | both | 1.11.2 | 1.11.2 |
| `first-person-model` | firstperson-fabric-2.4.6-mc1.20.1.jar | both | 2.7.2 | - |
| `immediatelyfast` | ImmediatelyFast-Fabric-1.3.2+1.20.4.jar | both | 1.16.5+26.2-fabric | 1.17.1+26.3-fabric |
| `iris` | iris-1.7.5+mc1.20.1.jar | both | 1.11.4+26.2-fabric | 1.11.6+26.3-fabric |
| `konkrete` | konkrete_fabric_1.8.1_MC_1.20.1.jar | both | 1.11.1-26.2-fabric | 1.11.1-26.3-fabric |
| `neruina` | Neruina-2.2.4-fabric+1.20.1.jar | both | 3.3.3 | - |
| `overflowing-bars` | OverflowingBars-v8.0.1-1.20.1-Fabric.jar | both | 26.2.0 | - |
| `presence-footsteps` | PresenceFootsteps-1.10.1+1.20.1.jar | both | 1.13.3+26.2 | 1.14.0+26.3 |
| `puzzles-lib` | PuzzlesLib-v8.1.25-1.20.1-Fabric.jar | both | 26.2.4 | 26.3.4 |
| `rsls` | rsls-1.1.5.jar | both | 1.2.2 | - |
| `shatterbyte-lib` | OctoLib-FABRIC-0.4.2+1.20.1.jar | both | 0.7.0-beta.1+26.2 | - |
| `sodium` | sodium-fabric-0.5.11+mc1.20.1.jar | both | mc26.2-0.9.2-fabric | mc26.3-0.9.3-alpha.1-fabric |
| `sound` | Sounds-2.2.1+1.20.1+fabric.jar | both | 2.5.1+edge+26.2-fabric | 2.5.2+edge+26.3-fabric |
| `sound-physics-remastered` | sound-physics-remastered-fabric-1.20.1-1.4.5.jar | both | fabric-1.5.1+26.2 | fabric-1.5.1+26.3 |
| `spawn-animations` | spawnanimations-v1.9.4-mc1.17x-1.20x-mod.jar | both | 1.11.5+mod | - |
| `stoneworks` | Stoneworks-v8.0.0-1.20.1-Fabric.jar | both | 26.2.0 | - |
| `advancements-fullscreen` | advancementsfullscreen-mc1.20+1.0.jar | client | 2.0.1 | 2.0.1 |
| `advancements-search` | advancementssearch-mc1.20+1.0.jar | client | 1.3 | 1.3.1 |
| `blur-plus` | blur-3.1.0.jar | client | 6.3.1+26.2-fabric | 6.3.1+26.3-fabric |
| `cameraoverhaul` | CameraOverhaul-1.4.1-fabric-universal.jar | client | 2.1.1-fabric+mc.26.1-plus | - |
| `default-options` | defaultoptions-fabric-1.20-18.0.1.jar | client | 26.2.0.3+fabric-26.2 | 26.3.0.1+fabric-26.3 |
| `distanthorizons` | DistantHorizons-2.2.1-a-1.20.1-forge-fabric.jar | client | 3.3.2-26.2 | 3.3.2-26.3 |
| `fancymenu` | fancymenu_fabric_3.3.2_MC_1.20.1.jar | client | 3.9.12-26.2-fabric | 3.9.13-26.3-fabric |
| `melody` | melody_fabric_1.0.4_MC_1.20.1-1.20.4.jar | client | 1.0.17-26.2-fabric | 1.0.17-26.3-fabric |
| `moreculling` | moreculling-1.20.4-0.24.0.jar | client | 1.8.1 | 1.9.0-beta.1 |
| `smooth-scrolling-refurbished` | SmoothScrollingRefurbished+1.20-1.1.2.jar | client | 1.9.0 | 1.10.0 |
| `smooth-swapping` | smoothswapping-0.9.3.1-1.20.2-fabric.jar | client | 0.9.10-26.2 | 0.9.11-26.3 |

### 5.2 Needs a port (66 + surveyor WIP)

Port tier: **A nudge** = upstream already has Fabric 26.1.x (a 26.1 to 26.2
bump). **B near** = Fabric 1.21.9-1.21.11. **C full** = older, full port
through `docs/PORTING.md`. **D no-source** = no public source (ARR/binary
only), so it needs a permission request or an in-house rebuild. Licenses
matter for redistribution: ARR and CC-BY-NC-ND mods cannot ship as a
modified jar without the author's permission. The team makes each call and
writes it in the Decision column of the tracker (section 8).

| Aged mod | Aged file | Side | Newest Fabric MC | License | Source | Port tier |
|---|---|---|---|---|---|---|
| `fbp-renewed` | FancyBlockParticles-1.20.1-fabric-20.1.2.0.jar | both | 26.1.2 | GPL-3.0-only | yes | A nudge |
| `fishing-real` | Fishingreal-1.20.1-1.7.2.jar | both | 26.1.2 | MIT | yes | A nudge |
| `moonlight` | moonlight-1.20-2.13.33-fabric.jar | both | 26.1.2 | LGPL-with-additional-dependency-clause | yes | A nudge |
| `surveyor` | surveyor-0.6.25+1.20.jar | both | 26.1.2 | LGPL-3.0-or-later | yes | A nudge |
| `ships` | ships-3.0.3.jar | both | 1.20.1 | Apache-2.0 | yes (EMD0123/Ships) | C full |
| `connectiblechains` | connectiblechains-2.2.1+1.20.1.jar | both | 1.21.11 | LGPL-3.0-only | yes | B near |
| `hearths` | Hearths v1.0.1 f12-48.jar | both | 1.21.10 | All-Rights-Reserved | none listed | B near |
| `immersive-ui` | ImmersiveUI-FABRIC-0.2.2.jar | both | 1.21.11 | All-Rights-Reserved | yes | B near |
| `lmft` | lmft-1.0.2+1.20-fabric.jar | both | 1.21.11 | CC0-1.0 | yes | B near |
| `modelfix` | modelfix-1.15-fabric.jar | both | 1.21.10 | GPL-3.0-only | yes | B near |
| `niftycarts` | niftycarts-20.1.3.jar | both | 1.21.11 | MIT | yes | B near |
| `dripsounds` | DripSounds-1.19.4-0.3.2.jar | client | 1.21.11 | LGPL-3.0-only | yes | B near |
| `shut-up-gl-error` | Shut Up GL Error-fabric-1.20.1-1.0.0.jar | client | 1.21.11 | MIT | yes | B near |
| `additionz` | additionz-1.3.2.jar | both | 1.21.1 | MIT | yes | C full |
| `another-furniture` | another_furniture-fabric-1.20.1-3.0.1.jar | both | 1.21.1 | Custom | yes | C full |
| `antique-atlas-4` | antique-atlas-2.10.0+1.20.jar | both | 1.21.1 | LGPL-3.0-or-later | yes | C full |
| `backslot` | backslot-1.2.15.jar | both | 1.21.1 | GPL-3.0-only | yes | C full |
| `backslotaddon` | backslotaddon-1.1.1.jar | both | 1.21.1 | GPL-3.0-only | yes | C full |
| `bento-box` | sushi_bar-0.2.2+1.20.jar | both | 1.21.1 | LGPL-3.0-only | yes | C full |
| `building-but-better` | bbb-1.20.1-fabric-1.0.2.jar | both | 1.20.1 | Starfish-License | yes | C full |
| `creeper-overhaul` | creeperoverhaul-3.0.2-fabric.jar | both | 1.21.1 | All-Rights-Reserved | yes | C full |
| `dungeon-now-loading` | Dungeon Now Loading-fabric-1.20.1-1.5.jar | both | 1.20.1 | MIT | yes | C full |
| `emi` | emi-1.1.18+1.20.1+fabric.jar | both | 1.21.1 | MIT | yes | C full |
| `emi-enchanting` | emi_enchanting-0.1.2+1.20.1.jar | both | 1.21.1 | MIT | yes | C full |
| `emi-loot` | emi_loot-0.7.4+1.20.1+fabric.jar | both | 1.21.1 | MIT | yes | C full |
| `emi-ores` | emi_ores-1.0+1.20.1+fabric.jar | both | 1.21.1 | LGPL-3.0-only | yes | C full |
| `enderman-overhaul` | endermanoverhaul-fabric-1.20.1-1.0.4.jar | both | 1.20.4 | All-Rights-Reserved | yes | C full |
| `extendeddrawersaddon` | extendeddrawersaddon-1.0.2.jar | both | 1.21.1 | MIT | yes | C full |
| `grass-overhaul` | Grass_Overhaul-Fabric-23.10.11-MC1.20.1.jar | both | 1.21.1 | Custom | yes | C full |
| `immersive-snow` | immersivesnow-1.20.1-1.3.0.jar | both | 1.21.4 | MIT | yes | C full |
| `indium` | indium-1.0.34+mc1.20.1.jar | both | 1.21.1 | Apache-2.0 | yes | C full |
| `inmis` | inmis-2.7.2-1.20.1.jar | both | 1.21.1 | MIT | yes | C full |
| `lets-do-emi-compat` | emi-letsdo-compat-1.3.jar | both | 1.21.1 | MIT | yes | C full |
| `libz` | libz-1.0.3.jar | both | 1.21.1 | MIT | yes | C full |
| `lootbeams` | lootbeams-2.1.1+1.20.1.jar | both | 1.21.5 | MPL-2.0 | yes | C full |
| `medievalweapons` | medievalweapons-1.4.8.jar | both | 1.21.1 | MIT | yes | C full |
| `modernfix` | modernfix-fabric-5.19.5+mc1.20.1.jar | both | 1.21.4 | LGPL-3.0-only | yes | C full |
| `nameplate` | nameplate-1.1.4.jar | both | 1.21.1 | MIT | yes | C full |
| `noisium` | noisium-fabric-2.3.0+mc1.20-1.20.1.jar | both | 1.21.6 | LGPL-3.0-only | yes | C full |
| `particular` | particular-1.1.1.jar | both | 1.21.4 | LGPL-3.0-only | yes | C full |
| `phantom-loader` | library-fabric-20.1.5.jar | both | 1.20.4 | Apache-2.0 | yes | C full |
| `smarter-farmers-farmers-replant` | smarterfarmers-1.20-2.1.0-fabric.jar | both | 1.21.1 | All-Rights-Reserved | yes | C full |
| `smitherz` | smitherz-1.0.4.jar | both | 1.21.1 | MIT | yes | C full |
| `spider-caves` | spirder-caves-fabric-20.1.0.jar | both | 1.20.4 | CC-BY-NC-ND-4.0 | yes | C full |
| `time-wind` | time-and-wind-ct-1.4.8+1.20-1.20.1.jar | both | 1.20.4 | GPL-3.0-only | yes | C full |
| `travelerz` | travelerz-1.0.1.jar | both | 1.21.1 | MIT | yes | C full |
| `treechop` | TreeChop-1.20.1-fabric-0.19.0.jar | both | 1.21.1 | MIT | yes | C full |
| `trinkets` | trinkets-3.7.2.jar | both | 1.21.1 | MIT | yes | C full |
| `villagertradefix` | villagerfix-1.0.4.jar | both | 1.21.1 | MIT | yes | C full |
| `voidz` | voidz-1.0.11.jar | both | 1.21.1 | GPL-3.0-only | yes | C full |
| `welcomescreen` | welcomescreen-1.0.1.jar | both | 1.21.1 | MIT | yes | C full |
| `borderless-mining` | borderless-mining-1.1.8+1.20.1.jar | client | 1.20.2 | MIT | yes | C full |
| `emiffect` | emiffect-fabric-1.1.2+mc1.20.1.jar | client | 1.21.1 | MIT | yes | C full |
| `emitrades` | emitrades-fabric-1.2.1+mc1.20.1.jar | client | 1.20.4 | MIT | yes | C full |
| `euphonium` | euphonium-1.0.3+1.20.jar | client | 1.21.1 | LGPL-3.0-or-later | yes | C full |
| `geckoanimfix` | GeckoLibIrisCompat-Fabric-1.0.0.jar | client | 1.21 | MIT | yes | C full |
| `immersivethunder` | ImmersiveThunder-1.20.1+1.2.2.jar | client | 1.21.6 | MIT | yes | C full |
| `inmisaddon` | inmisaddon-1.0.4.jar | client | 1.21.1 | MIT | yes | C full |
| `load-my-resources` | loadmyresources_fabric_1.0.4-1_MC_1.20.jar | client | 1.20.4 | GPL-3.0-only | yes | C full |
| `nimble` | Nimble-1.20.1-fabric-5.0.1.jar | client | 1.20.1 | MIT | yes | C full |
| `seamless-loading-screen` | seamless-loading-screen-2.0.3+1.20.1-fabric.jar | client | 1.21.1 | MIT | yes | C full |
| `tooltipfix` | tooltipfix-1.1.1-1.20.jar | client | 1.21.1 | MIT | yes | C full |
| `translucencyfix` | translucencyfix-2.2.0-fabric-quilt.jar | client | 1.21.8 | MIT | yes | C full |
| `amarite` | amarite-1.5-1.0.8.jar | both | 1.20.1 | All-Rights-Reserved | none listed | D no-source |
| `astrocraft` | astrocraft-1.4.5+1.20.1.jar | both | 1.21.4 | MIT | none listed | D no-source |
| `deuf-refabricated` | DEUF_Refabricated-MC1.20.1-1.1.0.jar | both | 1.21.3 | MIT | none listed | D no-source |
| `villager-transportation` | villager-transportation-1.3.1.jar | both | 1.21.4 | All-Rights-Reserved | none listed | D no-source |

### 5.3 Rebuilt in Hearthwind modules (20)

| Aged mod | Replaced by | Known parity gaps / notes |
|---|---|---|
| `autotag` | datapack | tags shipped in conversion/datapacks/hearthwind |
| `crop-growth-modifier` | hearthwind-world `SeasonCrops` | 15 per-crop multipliers live |
| `dehydration` | hearthwind-survival | dirty-water duration 600t vs Aged 200t; 35/130 hydration items resolve; bad-potion thirst roll missing |
| `earlystage` | hearthwind-primitive | steel recipe matches Aged (2 iron + 2 coal, extra blast-furnace slot @5200t); knapping on crafting_rock pending |
| `endrem` | hearthwind-world `EndRemasteredItems` | DECISION: upstream has Fabric 26.1.2 (nudge-port). In-house version: verify eye set + stronghold frame vs 5.2.4 |
| `environmentz` | hearthwind-survival | continuous biome drift vs Aged banded deltas; acclimatization loaded but not applied |
| `fabric-seasons` | hearthwind-world | default now 21 days (was 18); bonemeal not season-affected (Aged isSeasonMessingBonemeal=true) |
| `herdspanic` | hearthwind-world `HerdPanic` | DECISION: upstream HerdPanic now has a Fabric 26.3 build |
| `jobsaddon` | hearthwind-jobs | no 150-level exponential curve, no 3 concurrent jobs, no 1-day switch cooldown |
| `levelz` | hearthwind-skills | 649 gates live; crafting/smithing/brewing gates loaded but not enforced; XP curve differs (Aged 25x1.6^n) |
| `naturalist` | hearthwind-world (fauna port) | DECISION: upstream Naturalist now has a 26.2 Fabric build (2.0.5+26.2) |
| `nutritionz` | hearthwind-survival | HearthWind 5-group diet replaces near-inert NutritionZ (intentional HW change) |
| `partyaddon` | hearthwind-skills `party/` | verify vs partyaddon config |
| `paxi` | datapack | world datapack instead of paxi loader |
| `reciperemover` | hearthwind-primitive `RecipeRemovals` | 95 removals active |
| `revive` | hearthwind-survival `ReviveManager` | verify against revive-1.0.7 config (overrides/config/revive.json5) |
| `rpgdifficulty` | hearthwind-skills `MobScaling` | no hp/dmg/prot/speed caps, no special zombies, no boss scaling |
| `seasonhud-fabric` | hearthwind-client `SeasonHud` | upstream has a 26.2 build but needs fabric-seasons |
| `spoiledz` | hearthwind-survival | superset (container spoilage) |
| `tieredz` | hearthwind-primitive `TierRegistry` | 199 affix files + reforge; verify rarity weights 50/35/15/8/3/0 |

### 5.4 AgedAddition 1.0.6 (bundled jar, not on Modrinth)

Full content, from decompiling the jar: 11 items (`copper_nugget`,
`raw_copper_nugget`, `raw_gold_nugget`, `raw_iron_nugget`, `coal_piece`,
`lapis_lazuli_piece`, `emerald_piece`, `diamond_piece`,
`netherite_scrap_piece`, `nether_star_piece`, `quartz_piece`) in the
Ingredients tab, and `coal_piece` as 400-tick fuel. `BlockInit` and
`RenderInit` are empty. The config only holds button offsets for
numismatic/diet screens, and `ScreenMixin` is an accessor for them.
Unused `*_prospector_pick` models/textures ship in the jar but are never
registered. **Status: fully covered by `hearthwind-primitive`** (items,
fuel and tab as of this audit). Recipes and loot live in the migrated
datapack.

### 5.5 Shipped (95)

`adventurez`, `almanac`, `ambient-environment`, `appleskin`, `architectury-api`, `arrp`, `async-locator`, `athena-ctm`, `balm`, `better-combat`, `better-end-cities-base`, `birds-boids-addon`, `boids`, `chalk`, `chalk-colorful-addon`, `chipped`, `cloth-config`, `combat-roll`, `couplings`, `crawl`, `decubed-dungeons` (same project as Dungeons+; vendored 1.12.0), `desert-dungeon-dungeonz-addon`, `do-api` (folded into `custom-mods/letsdo-*` ports), `dungeons-and-taverns`, `dungeons-and-taverns-ancient-city-overhaul`, `dungeons-and-taverns-pillager-outpost-overhaul`, `dungeons-and-taverns-stronghold-overhaul`, `dungeonz`, `entity-model-features`, `entitytexturefeatures`, `exposure`, `extended-drawers`, `fabric-api`, `fabric-language-kotlin`, `ferrite-core`, `fleshz`, `forge-config-api-port`, `formations`, `formations-nether`, `formations-overworld`, `fzzy-config`, `gardens-of-the-dead`, `geckolib`, `hopo-better-mineshaft`, `hopo-better-underwater-ruins`, `immersive-aircraft`, `immersive-armors`, `jump-over-fences`, `kiwi`, `lavender`, `lets-do-bakery-farmcharm-compat`, `lets-do-brewery-farmcharm-compat`, `lets-do-candlelight-farmcharm-compat`, `lets-do-farm-charm`, `lets-do-herbalbrews`, `lets-do-meadow`, `lets-do-nethervinery`, `lets-do-vinery`, `lithium`, `lmd`, `log-begone`, `lootr`, `lukis-grand-capitals` (datapack `conversion/datapacks/lukis-grand-capitals`), `medieval-buildings`, `memoryleakfix`, `mes-moogs-end-structures`, `mns-moogs-nether-structures`, `modmenu`, `mru`, `natures-spirit`, `owo-lib`, `passable-foliage`, `playeranimator`, `pockets`, `profundis`, `resourceful-config`, `resourceful-lib`, `scholar`, `small-ships`, `sparsestructures`, `superb-steeds`, `supermartijn642s-config-lib`, `tcdcommons`, `terrablender`, `the-lost-castle`, `true-ending`, `underground-worlds` (Modrinth project renamed from Underground Jungle; vendored 3.2.1), `unnamed-desert`, `villages-and-pillages`, `yungs-api`, `yungs-better-desert-temples`, `yungs-better-end-island`, `yungs-better-jungle-temples`, `yungs-better-nether-fortresses`, `yungs-better-ocean-monuments`

### 5.6 Resource packs, shaders, client look

| Aged ships | Hearthwind |
|---|---|
| FreshAnimations 1.9.2 + FA+ Classic Horses, Details, Emissive, Objects, Quivers, Spiders; Expressive Fresh Moves 3.0.1 | FreshAnimations 1.10.5 only |
| Let's Do - Pixel Perfect | missing |
| RAY's 3D Ladders, RAY's 3D Rails, FancyFast Bushy Leaves | missing |
| Shaderpacks: Complementary Unbound r5.3, Photon 1.0a (need Iris + Sodium) | missing |
| `config/sodium-options.json`, `moreculling.toml`, `presencefootsteps`, `fbp`, `DistantHorizons.toml`, `cameraoverhaul.json`, `defaultoptions`, `emi.css`, `nameplate.json`, `lmft.json` | missing (come with the mods in 5.1/5.2) |
| FancyMenu title screen + `resources/aged` art | intentional deviation: Hearthwind's own title screen |

### 5.7 HUD and screen look and feel

Compared 2026-09-24: `.tmp/aged-ref/aged-hud-reference.png` and
`.tmp/aged-gallery/Inventory.png` (live Aged 3.1.2) against our client
gametest captures `.tmp/shots/cgt/0022_tour_inventory.png` and
`0029_tour_temperature.png`.

**In-game HUD**

| Element | Aged (source mod) | Hearthwind | Status |
|---|---|---|---|
| Thirst droplets above hunger | Dehydration | hearthwind-client | ✅ |
| Thermometer right of hotbar + unit box + trend arrow | EnvironmentZ | hearthwind-client `TempHud` | ✅ |
| Body-status icon above the hotbar centre | EnvironmentZ | hearthwind-client | ✅ |
| Season line top-left `Season, Day N/M` | SeasonHUD | `SeasonHud` | 🟡 text depends on day length (see below) |
| Hunger/saturation preview | AppleSkin | AppleSkin 26.2 | ✅ |
| Hearts/armor past 10 drawn as coloured overlay rows | **Overflowing Bars** | vanilla stacking rows | ❌ adopt (26.2 build exists). This matters because LevelZ health grows hearts |
| Mob level/name plates | **Nameplate** | none | ❌ port |
| Item drop beams | **LootBeams** | none | ❌ port |
| Long days: 20 min day + 10 min night | **Time & Wind** (`dayDuration 24000`, `nightDuration 12000`) | vanilla 20 min cycle | ❌ port. Biggest single feel difference |
| Particles, camera, first-person body | FBP, Particular, Camera Overhaul, First-person Model, 3D Skin Layers, Spawn Animations, ImmersiveThunder | none | ❌ (5.1 / 5.2) |
| Comfort status card beside inventory | none | hearthwind-client | ➕ Hearthwind addition: keep or hide? (team decision) |

**Season day count.** Aged runs 504000-tick seasons with Time & Wind's
36000-tick days, and SeasonHUD computes `springLength / day_length`. The
shipped Aged configs give 14 days, yet the reference capture shows `/18`
(likely a different config at capture time). What is certain is the tick
length: an Aged season is 504000 ticks (about 7 real hours). With vanilla
24000-tick days, 21 days matches that, so the default is 21. When Time &
Wind parity lands, store season length **in ticks** (504000) and derive the
displayed day count from the real day length, as SeasonHUD does.

**Inventory screen**

| Element | Aged | Hearthwind | Status |
|---|---|---|---|
| LibZ tabs above the panel (bag, sword, axe, figure) | LibZ | `TabStrip` (bag, sword, anvil, head) | 🟡 icon set differs |
| Left accessory column (5 slots) | **Trinkets** + **BackSlot**/addon + **Inmis** | none | ❌ ports. These define Aged's inventory silhouette |
| Weapon/bow slots beside the player preview | **BackSlot** + BackSlotAddon | offhand only | ❌ port |
| `Lv. N` label on the player preview | LevelZ | none | ❌ small hearthwind-client addition |
| Guidebook in the starter hotbar | Lavender `aged_guide_book` | rendered as missing texture; the vendored jar lacked the book entirely | ✅ fixed 2026-09-24 (item definition + refreshed vendored jar); re-verify with client gametests |
| Starter hotbar: bread x4, apple x4, book, bottle, campfire | Aged capture | book, bottle, campfire | 🟡 verify where Aged's food comes from before changing `StarterKit` |

**Screens** (details in `docs/AGED_UI_PARITY.md`): Level screen still lacks
the attribute slide-out, scroll/slider and restriction rail;
SkillInfoScreen lacks the full bonus/restriction list; SkillRestrictionScreen
is missing; Jobs lacks multi-job and 14x14 job textures. The EMI recipe
sidebar arrives with the EMI port. The title screen is an intentional
deviation.

### 5.8 The Hearthwind guidebook

Aged ships `aged:aged_guide_book` (Lavender, loaded through Paxi from
`config/paxi/resourcepacks/aged_guide_book`): **12 categories, about 62
entries** (start, character, hydration, nutrition, temperature, produce,
storage, tools and weapons, armors, battle, archeology, ...) plus
multiblock structure previews. Ours: `lavender:aged_guide_book`, 4
categories, 9 entries.

Direction (team decision 2026-09-24):

- **Rename** to `hearthwind:hearthwind_guide_book`, and move the book's
  content out of the `custom-mods/lavender` library port into a Hearthwind
  module, so the library stays a clean port. Update `StarterKit`, `/guide`,
  advancements and the guidebook gametest. Pre-1.0 worlds will lose the old
  item; `/guide` hands out the new one.
- **Content parity first:** cover every topic of Aged's 62 entries, then
  go further for Hearthwind's own systems (Ages, sieve, jobs, parties,
  downed/revive, seasons).
- **Genuinely helpful:** each entry answers "what do I do next and how".
  Include short steps, the actual recipe (Lavender recipe embeds), item icons,
  and the numbers players need. Tables such as skill gates, sieve drops,
  hydration tiers and heat sources are **generated from the same data
  corpora the mods read**, so the book can't drift from the game. Entries
  unlock with the matching advancement so the book grows with the player.
- **Medieval look and feel:** a leather-bound cover with iron or brass
  corners, parchment page texture, illuminated drop capitals and small
  woodcut-style section icons, and period voice in titles ("Of Water and
  Thirst", "The Smith's Craft"). Body text stays in the vanilla font for
  readability. Art follows `docs/PLACEHOLDER_ART.md` provenance rules.
- **Tests:** a gametest that every entry parses and every referenced
  item/recipe/advancement exists, plus a client screenshot tour of the book.

### 5.9 Version drift review (latest versions, Aged behaviour)

Policy: ship the **latest** version of every mod. For each mod whose version
differs from Aged's, review the changelog and the code or config defaults for
gameplay-affecting changes (combat numbers, loot, spawning, balance, recipes,
removed features). Keep Aged's behaviour through config, datapack or a small
patch where the new version allows it; otherwise record the deviation here.
Review gameplay-heavy mods first (Better Combat 1.8.6 to 3.x, Combat Roll,
Lootr, Immersive Armors, Sparse Structures, Superb Steeds, Immersive Aircraft,
AppleSkin), then worldgen and structures, then libraries.

Modrinth-resolved shipped mods (Aged version to Hearthwind version). Vendored
and ported mods are reviewed in their port notes.

| Mod | Aged file | Hearthwind version |
|---|---|---|
| `ambient-environment` | AmbientEnvironment-fabric-1.20.1-11.0.0.1.jar | 26.2.1 |
| `boids` | Boids-1.2.2.jar | 2.0.0+26.2 |
| `extended-drawers` | ExtendedDrawers-2.1.1+mc.1.20.1.jar | 5.1.0+mc.26.2 |
| `forge-config-api-port` | ForgeConfigAPIPort-v8.0.1-1.20.1-Fabric.jar | 26.2.1 |
| `hopo-better-mineshaft` | HopoBetterMineshaft-[1.20-1.20.1]-1.1.8.jar | 1.3.7 |
| `hopo-better-underwater-ruins` | HopoBetterUnderwaterRuins-[1.20-1.20.2]-1.1.4b.jar | 1.2.8 |
| `mru` | MRU-1.0.4+1.20.1+fabric.jar | 1.0.40+26.2-fabric |
| `terrablender` | TerraBlender-fabric-1.20.1-3.0.1.7.jar | 26.2.0.0.2 |
| `almanac` | almanac-1.20.x-fabric-1.0.2.jar | 1.26.7.3 |
| `appleskin` | appleskin-fabric-mc1.20.1-2.5.1.jar | 3.0.10+mc26.2 |
| `architectury-api` | architectury-9.2.14-fabric.jar | 21.1.10+fabric |
| `balm` | balm-fabric-1.20.1-7.3.9.jar | 26.2.0.8+fabric-26.2 |
| `better-combat` | bettercombat-fabric-1.8.6+1.20.1.jar | 3.2.2+26.2-fabric |
| `chalk` | chalk-2.2.4.jar | 3.2.0+26.2 |
| `chalk-colorful-addon` | chalk-colorful-addon-2.1.1.jar | universal |
| `cloth-config` | cloth-config-11.1.136-fabric.jar | 26.2.155+fabric |
| `combat-roll` | combatroll-fabric-1.3.3+1.20.1.jar | 3.0.1+26.2-fabric |
| `crawl` | crawl-0.12.0.jar | 0.15.0 |
| `dungeons-and-taverns` | dungeons-and-taverns-3.0.3.f.jar | 5.3.2+mod |
| `dungeons-and-taverns-ancient-city-overhaul` | dungeons-and-taverns-ancient-city-overhaul-1.jar | 3.4+mod |
| `dungeons-and-taverns-pillager-outpost-overhaul` | dungeons-and-taverns-pillager-outpost-rework-1.1.jar | v3.3+mod |
| `dungeons-and-taverns-stronghold-overhaul` | dungeons-and-taverns-stronghold-rework-1.jar | v2.4.0+mod |
| `entity-model-features` | entity_model_features_fabric_1.20.1-2.2.6.jar | 3.3.8-fabric-26.2 |
| `entitytexturefeatures` | entity_texture_features_fabric_1.20.1-6.2.8.jar | 7.2.4-fabric-26.2 |
| `fabric-api` | fabric-api-0.92.2+1.20.1.jar | 0.161.0+26.2 |
| `fabric-language-kotlin` | fabric-language-kotlin-1.13.0+kotlin.2.1.0.jar | 1.14.0+kotlin.2.4.20 |
| `ferrite-core` | ferritecore-6.0.1-fabric.jar | 9.0.0-fabric |
| `formations` | formations-1.0.3-fabric-mc1.20.2.jar | 1.0.4-fabric-mc26.2 |
| `formations-nether` | formationsnether-1.0.5.jar | 1.0.5a-mc1.21+ |
| `formations-overworld` | formationsoverworld-1.0.4.jar | 1.0.5a-mc1.21+ |
| `fzzy-config` | fzzy_config-0.5.8+1.20.1.jar | 0.7.7+26.2 |
| `geckolib` | geckolib-fabric-1.20.1-4.4.9.jar | 5.5.5 |
| `immersive-aircraft` | immersive_aircraft-1.1.5+1.20.1-fabric.jar | 1.5.2+26.2 |
| `immersive-armors` | immersive_armors-1.6.1+1.20.1-fabric.jar | 1.8.2+26.2 |
| `jump-over-fences` | jumpoverfences-fabric-1.20.1-1.3.1.jar | 1.8.1 |
| `lmd` | letmedespawn-1.20.x-fabric-1.4.4.jar | 1.26.7.3 |
| `lithium` | lithium-fabric-mc1.20.1-0.11.2.jar | mc26.2-0.25.3-fabric |
| `lootr` | lootr-fabric-1.20-0.7.33.81.jar | 1.24.39.122 |
| `modmenu` | modmenu-7.2.2.jar | 20.0.2 |
| `owo-lib` | owo-lib-0.11.2+1.20.jar | 0.13.1+26.2 |
| `resourceful-config` | resourcefulconfig-fabric-1.20.1-2.1.2.jar | 5.0.0 |
| `resourceful-lib` | resourcefullib-fabric-1.20.1-2.1.29.jar | 5.0.4 |
| `scholar` | scholar-1.20.1-1.0.0-fabric.jar | 1.2.5 |
| `sparsestructures` | sparsestructures-fabric-1.20.1-2.2.0.jar | 3.1.4 |
| `superb-steeds` | superbsteeds-1.20-4.jar | 26.2-r2 |
| `supermartijn642s-config-lib` | supermartijn642configlib-1.1.8a-fabric-mc1.20.jar | 1.1.8-fabric-mc26.2 |
| `tcdcommons` | tcdcommons-3.12.3+fabric-1.20.1.jar | 5.5.6+fn-26.2 |

Deviations found: (none recorded yet)

### 5.10 Aged bugs: for review

Policy: when Aged's shipped behaviour is a genuine bug (its code contradicts
its own guide or UI), and we or a newer mod version fix it, keep the fix,
mark it in code with a `Review note`, and list it here for the team to
confirm. When we keep Aged's behaviour instead, list that too.

| Area | Aged's actual behaviour | Hearthwind | Status |
|---|---|---|---|
| Diet deficiency (NutritionZ 1.0.11) | low-nutrient lists reuse the positive values, and the code applies them as written, so a deficiency **gives** the bonus | negated to the penalties Aged's guidebook documents (-attack speed, -max health, ...); gametest `nutrientDeficiencyIsAPenalty` | **fixed, for review** |
| Party XP bonus (PartyAddon 1.0.4) | the party screen advertises +5% per member and +50% for a full group; the code never applies either | matches the applied behaviour: orb XP pools at the leader and splits evenly, no bonus; gametest `partyOrbXpPoolsAtLeaderAndSplitsEvenly` | **kept as Aged, for review** (apply the bonus?) |

## 6. Workstreams toward 1.0.0, in priority order

**W0: Make the truth machine-checked (days, not weeks)**
1. Re-key `mods-manifest.json` to Aged's Modrinth project ids (`pid`),
   one entry per Aged jar, plus separate entries for our own modules and
   non-Aged deps. Resolve the 83 unmatched Aged mods (about 20 are wrong
   slugs for mods we already cover; the rest are simply absent).
2. Remove the three stale vendored duplicates (section 4).
3. Put a parity check in CI: diff Aged's index against the built mrpacks
   and fail on any Aged mod that is neither shipped, rebuilt, nor tracked
   in 5.2. This stops "we have parity" claims from drifting again.
4. Port Aged's config overrides for mods we already ship (section 4 list),
   migrating keys where the 26.2 version renamed them.

**W1: First-impression and HUD parity (what a player sees in the first 10 minutes).**
Adopt Overflowing Bars; port Time & Wind and move seasons to tick length;
add the `Lv. N` preview label; match the tab icons; re-verify the
guidebook in the starter kit; decide on the Comfort card; the Hearthwind
guidebook (section 5.8). Then port the
accessory stack (Trinkets, BackSlot + addon, Inmis + addon), because it
defines the inventory silhouette. Screens: finish the Level-screen rail and
SkillRestrictionScreen. Every item lands with a client-gametest screenshot
next to the Aged reference.

**W1b: Version drift review (section 5.9)** for shipped mods, gameplay-heavy first.

**W2: Adopt the 30 mods with official 26.2 builds.**
Batch by side: (a) server/both gameplay (`better-archeology`,
`stoneworks`, `spawn-animations`, `sound`, `neruina`, `puzzles-lib` +
`overflowing-bars`, `rsls`, `first-person-model`, `3dskinlayers`,
`presence-footsteps`, `sound-physics-remastered`, `immediatelyfast`,
`entityculling`, `badoptimizations`, `konkrete`, `shatterbyte-lib`);
(b) client stack (`sodium`, `iris`, `fancymenu` + `melody`,
`distanthorizons`, `cameraoverhaul`, `moreculling`, `smooth-swapping`,
`smooth-scrolling-refurbished`, `blur-plus`, `default-options`,
`advancements-fullscreen`, `advancements-search`). Boot-smoke each batch
on the server and run the client gametests (a Sodium + Iris client is a
real rendering change: re-check HUD/inventory screenshots).

**W3: Close parity gaps in the Hearthwind rebuilds (section 5.3).**
Highest gameplay impact first: jobs curve, 3 concurrent jobs and switch
cooldown; RPGDifficulty caps and boss scaling; steel ratio 2 iron + 2 coal
@5200t; LevelZ craft/smithing/brewing gate enforcement; dirty-water
duration 200t; seasonal bonemeal. Every fix lands with a gametest pinned to
Aged's value.

**W4: Port queue (section 5.2).** Suggested order: tier A (4), then
gameplay-content mods in tier B/C (medievalweapons, smitherz, additionz,
voidz, travelerz, inmis + inmisaddon, backslot + addon, trinkets,
antique-atlas + surveyor, another-furniture, bento-box, connectiblechains,
niftycarts, villager-transportation, smarter-farmers, villagertradefix,
fishing-real, treechop, hearths, immersive-snow, grass-overhaul, lootbeams,
nameplate, particular, time-wind, welcomescreen, the EMI family), then
client-only feel mods, then perf/fix libraries. Each is a per-mod team
decision; record it before starting.

**W5: Look and feel.** Resource packs and shaders from 5.6 (check each
license before redistributing), and client configs as their mods land.

**W6: 1.0.0 release gate.**
- CI parity check green: every Aged mod shipped, rebuilt, or carrying a
  signed-off team decision.
- `./gradlew build` + full server gametests + client gametests green.
- Server boot-smoke from the built mrpack (not from a hand-staged dir)
  reaches `Done` with no parse errors in our namespaces.
- One-hour play test on the client pack against a dedicated server.
- PLAYER_CHANGES.md lists every deviation from Aged.

## 7. Deferred until after 1.0.0

Not cancelled; parked so 1.0.0 stays a stable base:

- Ages 0-5 enforcement/advancement chain beyond what Aged's gates already
  do, Mechanical Age / Create preview, aggregate-power mob scaling
  (PROJECT_DIRECTION phases B/C).
- De-kludge/dedupe audit (removing overlapping Aged mods conflicts with
  parity; revisit after 1.0.0).
- Rivers/waves/water motion (`ideas/rivers-and-waves.md`), Terralith or
  Tectonic, Genesis-style ideas, FarmZ.
- Real art replacement, knapping minigame redesign, new HUD features
  beyond Aged's.
- Shelter warmth: walls and a roof holding in a fire's heat (a real
  insulation model). Aged has none (verified in EnvironmentZ 2.0.8
  bytecode), so this is a post-1.0 Hearthwind improvement.
- MC 26.3 bump: 61 of the 212 Aged mods already have 26.3 Fabric builds.
  Bump **after** 1.0.0 on 26.2, via `build.conf.json` only.

## 8. Decisions the team needs to make (one mod at a time)

| Item | Question |
|---|---|
| `naturalist` | Upstream has Fabric 26.2 (2.0.5+26.2). Keep the hearthwind-world fauna rebuild, or ship upstream for parity? |
| `herdspanic` | Upstream has Fabric 26.3. Keep the in-house `HerdPanic`, or adopt upstream when we bump? |
| `endrem` | Upstream has Fabric 26.1.2 (nudge port). In-house or port? |
| `seasonhud-fabric` | Upstream 26.2 needs fabric-seasons (not ported). Keep `SeasonHud`? |
| `spider-caves` | CC-BY-NC-ND: cannot redistribute a modified jar. Ask the author, or rebuild the feature? |
| ARR mods (`amarite`, `hearths`, `creeper-overhaul`, `enderman-overhaul`, `immersive-ui`, `smarter-farmers`, `villager-transportation`) | Ask the authors for permission to ship a port, or rebuild? |
| `c2me-fabric` | Not in Aged. Keep as a server-perf exception, or remove for 1.0.0? |
| Nutrition | Hearthwind's 5-group diet replaces NutritionZ. Confirm it as an accepted deviation. |

Tracker: record each decision as a `decision` field on the manifest entry
(W0.1), so the CI parity check can read it.

## 9. How to reproduce this audit

1. `unzip -p .tmp/Aged-3.1.2.mrpack modrinth.index.json`, then take the
   `mods/` entries and their Modrinth project ids (download URL segment 4).
2. `GET https://api.modrinth.com/v2/projects?ids=[...]` for slugs and licenses.
3. Per project: `GET /v2/project/<id>/version?loaders=["fabric"]&game_versions=["26.2"]`
   (and `26.3`). Throttle to about 5 requests/s; Modrinth returns 429 above that.
4. Match against `conversion/build/resolved.json` picks, `conversion/vendored/*.jar`
   and `custom-mods/*/build/libs`.
W0.3 turns this into a committed script and a CI job.
