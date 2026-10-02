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
2. **Guidebook widget + HUD parity (2026-09-26 live test):** the Lavender
   recipe widget laid a display's row-major ingredient list into the 3-wide
   grid using the widget's column count instead of the recipe's trimmed
   width, so a 2x3 recipe (flint axe) rendered in the wrong cells; the
   crafting builder now reads the 26.2 `ShapedCraftingRecipeDisplay`
   (width/height/ingredients) and hides the crafting-table station icon for
   recipes that fit the 2x2 inventory grid (the crafting rock no longer
   shows a table). The drop-cap label is fixed-width (104 px flow / 78 px
   label inside the 112 px page anchor) because owo wraps fill/content
   labels using the previous inflate pass's width and clipped words
   mid-line. `conversion/vendored/lavender-26.2+0.1.0.jar` refreshed from
   the rebuild. The skill level-up toast was removed (Aged announces skill
   and job gains in chat only) and the temperature HUD trimmed to
   EnvironmentZ 2.0.8's body icon + thermometer (unit box, trend chevron,
   ambient glyphs and status words were Hearthwind additions).
3. **mrpacks shipped without any local jars.** `build_pack.py` read
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
- ~~**Runaway datapack function.**~~ Resolved 2026-09-26: the flood was
  NOT in the pack. The clean client harness logs `Command execution
  stopped due to limit` zero times; the source was a stale
  `waterfall-particle-1.0.jar` (3.7 MB `minecraft:tick` function) that had
  been dropped from the manifest but lingered in the Prism test instances
  because `update_prism.sh` never removed mods that left the pack. The
  deploy tool now reconciles the instance against the built mrpack (add,
  update AND prune), and the parity gate diffs the built dist with a
  reverse check (every shipped jar must be an Aged mod, a dependency
  auto-added by the resolver, or a hearthwind module).
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
  - Aged's Dehydration water chain is partly here: purifying bottles on a
    campfire shipped in 0.1.33 (`CampfirePurification`, reverse-engineered off
    Dehydration 1.3.6 - the 1000-tick boil, the block-corner drop, the frozen
    progress on a dark fire, documented with the full audit in
    `docs/AGED_HYDRATION.md`), and the campfire fuel tag came across (the
    sapling-to-sticks recipe was already in the pack). The three brewing
    recipes and the `dehydration:hydration` potion shipped in 0.1.35
    (`PurifiedWater.registerBrewing` + `HydrationMobEffect`, on Fabric's
    `FabricPotionBrewingBuilder.BUILD` hook instead of the reference's mixin).
    Still missing: the
    campfire cauldron, the copper rain
    cauldron, the bamboo pump. The audit's 28 deviations and 14 gaps are the
    hydration workstream.
  - ~~The fallback written guidebook in `StarterKit` has wrong facts.~~ Fixed
    0.1.34: a claim-by-claim audit against the implementation (and against
    Aged's own `levelz.json5`) found two false claims and four stale ones.
    The false ones were real bugs in our own build, not just in the book:
    a new player was handed **no** skill points where Aged's
    `"startPoints": 2, "enableStartPoints": true` hands out two (now
    `SkillsConfig.startSkillPoints`, granted once on first join), and an
    empty glass bottle restores no thirst in this pack and cannot be filled
    from open water, which the book told players to do. The book also now
    says the thermometer is the ambient reading (not the body value), that
    the bare-hand sip needs *still* water held for ~4 s and uses the source
    up, that loose items are "Rock"/"Flint" in five biome groups, and that a
    second killing blow while downed is fatal.
  - ~~The Comfort card screen (`SurvivalInfoScreen`) can't be opened in
    game.~~ Closed 0.1.34, see the HUD table row: Aged has no such panel at
    all, so the screen was deleted. For the record, the one stat panel Aged
    does have - NutritionZ's - is already a pixel-exact rebuild: the capture
    measures 176x142 at S=6 with the value column starting at exactly x+127
    and the five bars on rows 36/59/82/105/128 (23 px pitch), which is what
    `NutrientsScreen` draws.
  - ~~Jobs earn XP only from breaking ladder blocks and killing ladder
    mobs.~~ Fixed 0.1.28: every Aged earn path now pays. Breaking (`AFTER`)
    and kills feed the ladders; item gains feed them through the crafting,
    furnace, fishing, **anvil and smithing** result slots; a new
    `BlockItem#place` hook pays the builder for the reference
    `jobsaddon:builder_placing_blocks` tag (deliberately a different list from
    the ladder, and breaking a block never pays the builder); and a
    `BrewingStandMenu$PotionSlot#onTake` hook pays the brewer by POTION id,
    since the brewer corpus is keyed by potions and could otherwise never
    level. Remaining sub-gap: campfire cooking, whose 26.2 result is dropped
    into the world by a playerless tick (no menu, no slot) - smoker and blast
    furnace outputs do pay, they share `FurnaceResultSlot`.
  - ~~Party XP sharing was not wired.~~ Fixed 2026-09-24 to PartyAddon's real
    behaviour (orb XP pools at the leader, split evenly among members in the
    leader's level); see 5.10 for the advertised-but-unapplied bonus.
  - End Remastered: the 16 `endrem:*_eye` items plus `undead_soul`/`witch_pupil`
    now match 5.2.4 (ids, CC0 art, lore lang, 0.1.19), but acquisition is still
    unauthored (upstream drives it from code - `ERTrades`, loot hooks - which we
    have not ported) and eyes insert into vanilla end portal frames rather than
    upstream's `ancient_portal_frame` block: tracked deviation.
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
| `additionz` | additionz-1.3.2.jar | both | 1.21.1 | MIT | yes | ✅ 0.1.45 (partial): five of its twelve keys ship, rebuilt in hearthwind-survival from the reference bytecode. Animals stay babies 252 000 ticks (3.5 hours) instead of vanilla's 20 minutes (`@ModifyConstant` on `AgeableMob.getBabyStartAge`); rain puts a campfire out on the reference's awkward schedule - the first 60 samples only count, the 61st is the first that can put it out and every later second is a 1-in-60 roll, so ~121 s on average, with the counter stored in NBT and only zeroed when the fire goes out; phantoms need 144 000 ticks since rest instead of 72 000; a villager stops summoning iron golems at 8; and a spawner gives up after 20 waves and forgets ten minutes later, throwing electric sparks while it is locked. Five keys are deliberately NOT reproduced because they are no-ops on 26.2 (`disable_elytra_underwater` - vanilla already blocks gliding into water; `trident_buried_treasure` - its listener is never registered; `botte_air_amount` - adds zero air; `passive_age_calculation`/`passive_max_age` - computed then never read). `fletching_table_use` needs nothing because vanilla already matches. `villager_gender` is NOT done and needs its own decision: 26.2 has no `VillagerBreedTask` at all, so opposite-sex breeding would have to be reimplemented without the female-villager texture swap, which would itself be a deviation. The spawner's block-break branch is dead in the reference because Aged ships the deactivation window, so it is not reproduced either |
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
| `welcomescreen` | welcomescreen-1.0.1.jar | both | 1.21.1 | MIT | yes | ✅ rebuilt in-tree 0.1.30 (not ported) |
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

### 5.3 Rebuilt in Hearthwind modules (21)

| Aged mod | Replaced by | Known parity gaps / notes |
|---|---|---|
| `autotag` | datapack | tags shipped in conversion/datapacks/hearthwind |
| `crop-growth-modifier` | hearthwind-world `SeasonCrops` | 15 per-crop multipliers live |
| `dehydration` | hearthwind-survival | ✅ 0.1.18 audit: flask dirty-water 200t, bad-potion roll 15%/300t, hydration corpus and bowl quench all match Aged's dehydration.json5 |
| `earlystage` | hearthwind-primitive | steel blasting matches Aged 1.1.1 (3 iron + 1 coal -> 1 steel @600t, extra blast-furnace slot); knapping on crafting_rock pending |
| `endrem` | hearthwind-world `EndRemasteredItems` | ✅ items/art/lang parity 0.1.19 (16 upstream eyes + `undead_soul`/`witch_pupil`, CC0 textures and lore); deviation: eyes still insert into vanilla end portal frames instead of upstream's `ancient_portal_frame` block, and code-driven acquisition is not ported |
| `environmentz` | hearthwind-survival | continuous biome drift vs Aged banded deltas; acclimatization loaded but not applied |
| `fabric-seasons` | hearthwind-world | ✅ seasonLengthTicks 504000 with the day count derived from the live day length; bonemeal blocked out of season (isSeasonMessingBonemeal) since 0.1.18 |
| `herdspanic` | hearthwind-world `HerdPanic` | DECISION: upstream HerdPanic now has a Fabric 26.3 build |
| `jobsaddon` | hearthwind-jobs | ✅ 0.1.28: the Aged shape is live - 3 employed slots, 150-level cap, 24000t change cooldown, exponential `100 + 1.6*L` curve, 8 jobs with corpus ladders, and every earn path hooked (break, kill, craft, furnace/smoker/blast, fish, anvil, smithing, place, brew); open: campfire cooking has no player to credit, `jobCraftGating` (off by default on purpose) |
| `levelz` | hearthwind-skills | ✅ 0.1.35: 649 gates live and **enforced**, and the XP curve already matches. Crafting is blocked on the gated item/ingredient, smithing through `SmithingGateMixin`, brewing through `BrewingPotionSlotMixin`, and breaking through the `PlayerBlockBreakEvents.BEFORE` handler in `SkillGates`. The cost curve is `(int)(xpBaseCost + 1.6 * L^exponent)` with Aged's own `xpCostMultiplicator: 1.6`. (This row said "loaded but not enforced; XP curve differs" until 0.1.35 - both halves were stale.) **0.1.39**: 55 of the 71 `levelz/block` files gate their real block through an `object` field behind a `minecraft:custom_block` placeholder that does not exist here, so the loader was dropping all of them; it now falls back to `object` and block-use gates went 16 -> 68 in the full pack. **0.1.44**: that fallback was still gated to the `block` category, so the same placeholder hid 39 item gates - including Alchemy 15's teleport potion and scroll, which point at AdditionZ ids we do not have and so still drop correctly. The condition is now simply "the file has an `object` field"; `entity` uses it 10 times and `mining`/`crafting`/`smithing`/`brewing` never do, so there is nothing to exclude. Covered by a test on a vanilla object (a brush at Luck 8) so it cannot pass on a mod we may never ship |
| `naturalist` | hearthwind-world (fauna port) | DECISION: upstream Naturalist now has a 26.2 Fabric build (2.0.5+26.2) |
| `nutritionz` | hearthwind-survival | HearthWind 5-group diet replaces near-inert NutritionZ (intentional HW change) |
| `partyaddon` | hearthwind-skills `party/` | verify vs partyaddon config |
| `paxi` | datapack | world datapack instead of paxi loader |
| `reciperemover` | hearthwind-primitive `RecipeRemovals` | 95 removals active |
| `revive` | hearthwind-survival `ReviveManager` | verify against revive-1.0.7 config (overrides/config/revive.json5) |
| `rpgdifficulty` | hearthwind-skills `MobScaling` | 🟡 0.1.29: distanceFactor 0.05/200 blocks + heightFactor 0.1/25 blocks with 4x health, 3x damage and 2x armor caps, the warden/ender-dragon exclusion list, overworld-only steps (`exclude*InOtherDimension`), adult passive scaling and the boss path (distance factor + 0.3 per nearby player, 3x health cap). ✅ 0.1.37 adds the three per-mob rolls: a 30% chance to jitter health and damage by +/-3%, a 5% speed zombie (-10 HP, x1.2 speed) and a 10% big zombie (x0.7 speed, +10 HP, +2 damage, 1.3x hitbox and model), all rolled in rpgdifficulty's order and with no early return, so a mob spawned on top of spawn still rolls. ✅ 0.1.38 adds the payout keys: `extraXp` (drops scale with the mob's health factor, capped at `maxXPFactor` 4.0), `dropMoreLoot` (one roll against factor x 0.02, capped at 2.0, and on a hit the table is rolled again with half its stacks skipped and the survivors incremented), and `creeperExplosionFactor` 1.1. **That last key is a naming trap upstream**: rpgdifficulty never checks for a creeper, it multiplies the *general* damage factor, so Aged gives every mob 10% more damage growth than the name suggests and we do the same. The `maxFactorSpeed` 1.8 cap is dead upstream - the speed factor is only ever initialised to 1.0 and never incremented - so we deliberately do not ship it. Deviations on purpose: Aged's `c:bosses` tag ships as `entity_types/` (plural) so it resolves empty - we read the tag as written and also route the ender dragon, whose "players nearby" box in Aged is a malformed AABB at world origin that never matches; we use the intended 128-block radius |
| `seasonhud-fabric` | hearthwind-client `SeasonHud` | upstream has a 26.2 build but needs fabric-seasons |
| `spoiledz` | hearthwind-survival | superset (container spoilage) |
| `tieredz` | hearthwind-primitive `TierRegistry` | 199 affix files + reforge; verify rarity weights 50/35/15/8/3/0 |
| `welcomescreen` | hearthwind-client `WelcomeScreen` + hearthwind-survival `StarterKit` | ✅ 0.1.30: the Aged first-join welcome screen (title, three text blocks, the 256x256 pack image left of centre, the `Start` button) and the five `/item replace entity @s hotbar.N` commands it runs - bread x4, apples x4, the guide book, a purified water bottle, a campfire, in Aged's hotbar slots. Escape starts the game too, so nobody is stuck without supplies. Deviations on purpose: the image is the Hearthwind title art we own (Aged's `aged:textures/pack.png` is not redistributable), there is no Discord button (no invite to link), the second text block drops Aged's knapping line because the Hearthwind crafting rock is a 3x3 grid and not a knapping bench, and the granted potion is our real `dehydration:purified_water` where Aged's command names a `minecraft:purified_water` potion that only Dehydration 1.3.6 ever registered |

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
| FancyMenu title screen + `resources/aged` art | intentional deviation, and 0.1.40 corrected the reason: Hearthwind's own title screen (`HearthwindTitleScreenMixin`) draws the menu from art inside the client jar, because Aged's layout could only ever paint unresolvable `aged:` paths |

### 5.7 HUD and screen look and feel

Compared 2026-09-24: `.tmp/aged-ref/aged-hud-reference.png` and
`.tmp/aged-gallery/Inventory.png` (live Aged 3.1.2) against our client
gametest captures `.tmp/shots/cgt/0022_tour_inventory.png` and
`0029_tour_temperature.png`.

**In-game HUD**

| Element | Aged (source mod) | Hearthwind | Status |
|---|---|---|---|
| Thirst droplets above hunger | Dehydration | hearthwind-client | ✅ |
| Thermometer right of hotbar (2.0.8 draws no unit box or trend arrow) | EnvironmentZ | hearthwind-client `TempHud` | ✅ |
| Body-status icon above the hotbar centre | EnvironmentZ | hearthwind-client | ✅ |
| Season line top-left `Season, Day N/M` | SeasonHUD | `SeasonHud` | ✅ day count derives from the live day length (Time & Wind port) |
| Hunger/saturation preview | AppleSkin | AppleSkin 26.2 | ✅ |
| Hearts/armor past 10 drawn as coloured overlay rows | **Overflowing Bars** | `OverflowingBars-v26.2.0-mc26.2.x-Fabric.jar` | ✅ shipped (W2 adopt). This matters because LevelZ health grows hearts |
| Mob level/name plates | **Nameplate** | none | ➖ not needed: Aged ships `config/nameplate.json` with `showLevel=false`, which the 1.1.4 bytecode uses as the first gate of the whole renderer - Aged 3.1.2 shows no nameplates |
| Item drop beams | **LootBeams** | hearthwind `lootbeams` | ✅ ported 0.1.13 (rarity/name colour, white hidden, enchant sparkles, 12-tick age gate) |
| Long days: 20 min day + 10 min night | **Time & Wind** (`dayDuration 24000`, `nightDuration 12000`) | hearthwind-world `TimeAndWind` | ✅ 2026-09-26 port. Reads Aged's `config/time-and-wind/` files; day runs at 0.5x clock rate, night 1.0x, sleeping races the clock (Aged's config is v1-patched by the upstream mod to 30x, which the port mirrors), rates travel in vanilla time packets so every client sees the correct sky |
| Particles, camera, first-person body | FBP, Particular, Camera Overhaul, First-person Model, 3D Skin Layers, Spawn Animations, ImmersiveThunder | none | ❌ (5.1 / 5.2) |
| Hydration / temperature stat panel | none (NutritionZ 1.0.11 has **only** a diet panel; Dehydration 1.3.6 and EnvironmentZ 2.0.8 ship **no** screen class and register **no** keybind - their jars contain zero `KeyBinding` references) | none | 🟢 decided 0.1.34: **hide it.** Our own `SurvivalInfoScreen` (a "Hydration Level 12.4 / 20.0" card) had no Aged counterpart and no way to open it, so it was deleted rather than bound. Aged shows thirst as the droplet row above the hunger bar and temperature as the mannequin + thermometer, which we already match |

**Season day count.** A cycle is 24000 + 12000 = 36000 ticks wide, because
Time & Wind runs the 24000-tick day counter at 0.5x (24000 real ticks) and the
12000-tick night at 1.0x (12000 real ticks) - that sum, not any single config
number, is where 36000 comes from. SeasonHUD only *consumes* it:
`day_length: 36000` in its own toml is a display setting, and SeasonHUD divides
the season length by the live day length,
which reads 14 days with Aged's shipped config (the reference capture shows
`/18`, likely a different config at capture time - the tick length is what is
certain: 504000 ticks, about 7 real hours). Hearthwind stores
`seasonLengthTicks = 504000` and derives the displayed day count from the
world clock, so a vanilla 24000-tick world still reads 21 days while the
shipped Time & Wind data reads 14, exactly like Aged.

**Inventory screen**

| Element | Aged | Hearthwind | Status |
|---|---|---|---|
| LibZ tabs floating above the panel (bag, sword, axe, figure) | LibZ | `TabStrip` (bundle, iron sword, iron axe, armor stand) | ✅ placement (0.1.32, re-measured 0.1.33): every Aged capture floats the strip on the 21 rows above the 200x215 panel, the selected tab only taller so it merges into the panel edge, which is what `TabStrip.draw`/`clicked` do and what `ScreensTourGameTests` asserts; 🟡 the art is a legal stand-in for GPL tab art, silhouettes match the capture. **0.1.44**, after executing LibZ 1.0.3's own `DrawTabHelper`/`TabRegistry` against stub classes and measuring `Inventory.png` at exactly 6.000x: the sheet is byte-identical to `libz:textures/gui/icons.png` (sha256 equal) and the geometry, pitch, icon offset and unselected hit rects all match, so **the rebuild is kept and the mod is not ported** - nothing consumes its registration API (every LibZ-dependent mod in the pack is already ours or unported) and two of its mixin targets no longer exist on 26.2. Three gaps closed in passing: a **5th tab for a non-empty equipped backpack** (its icon is the backpack itself, which Aged shows too), **tooltip titles now the reference's own translatable text** (Crafting / Skills / Jobs / Party / Backpack, replacing our literals with `[K]`-style key hints), and the unselected **first** tab's hit box corrected from 25 rows to LibZ's 21 |
| Left accessory column (5 slots) | **Trinkets** + **BackSlot**/addon + **Inmis** | Trinkets Updated fork (data-driven slots), BackSlot back+belt slots (G / Shift+G, HUD + avatar rendering) with the BackSlot Addon shield/sword/lantern renders, Inmis backpacks (B, chest slot, on-back) with the Addon's 3D backpack models and Trinkets-slot support | ✅ 0.1.27: ports done 0.1.24 (Trinkets, Inmis, BackSlot + Addon) and the Inmis Addon rebuilt in-tree (3D models, Trinkets slot, Aged config keys); the LibZ tab strip is our own clean-room `TabStrip` (no 26.2 LibZ build exists; see 5.7) |
| Extra slots beside the player preview | **BackSlot** (back slot 41 + belt slot 42 only) and the right-hand column is **Trinkets**' own accessory grid | BackSlot back+belt slots, Trinkets Updated accessory slots | ✅ both already shipped (0.1.24/0.1.27). The old ❌ row was mis-attributed: BackSlot 1.2.15's `PlayerScreenHandlerMixin` only adds slots 41/42, so nothing weapon/bow-specific needs porting |
| `Lv. N` label on the player preview | LevelZ `inventorySkillLevel` (posX 0 / posY 62, 0.6 scale, white) | `InventoryScreenButtonMixin` draws `Lv. <overall>` | ✅ 2026-09-26; overall level = min(30, sum/12) like LevelZ (was a raw sum, so it showed Lv. 360) |
| Guidebook in the starter hotbar | Lavender `aged_guide_book` | rendered as missing texture; the vendored jar lacked the book entirely | ✅ fixed 2026-09-24 (item definition + refreshed vendored jar); re-verify with client gametests |
| Starter hotbar: bread x4, apple x4, book, bottle, campfire | Aged capture | the same five items, same hotbar slots | ✅ 0.1.30. The capture's food is not a spawn bonus: Aged's `welcomescreen` mod shows a welcome screen on the first join and its **Start** button runs five `/item replace entity @s hotbar.N` commands - `hotbar.0` bread x4, `hotbar.1` apples x4, `hotbar.4` the Lavender guide book, `hotbar.7` a `purified_water` potion, `hotbar.8` a campfire. We rebuilt that flow (`hearthwind-client` `WelcomeScreen` + `hearthwind-survival` `StarterKit`) and match the slots, counts and items; the one deviation is the potion id (Aged names `minecraft:purified_water`, we register `dehydration:purified_water`) |
| Welcome screen on the first join | **Welcomescreen** + the `aged_welcome_screen` paxi datapack (title, three text blocks, the 256x256 pack image left of centre, `Start` at the top right) | `WelcomeScreen` (same layout and texts) | ✅ 0.1.30, with two deliberate differences: the image is the Hearthwind title art we own (Aged's `aged:textures/pack.png` is not redistributable) and there is no Discord button (Hearthwind has no invite to link) |

**Screens** (details in `docs/AGED_UI_PARITY.md`): the panel chrome matches
the reference captures as of 0.1.33 - every info panel is 200x215 with the
LibZ tabs floating on the 21 rows above it, and the Jobs screen's rows and
two-column card order are the measured ones. (0.1.32 shipped a 200x236 panel
with a "20 px black tab band" that a careful re-measurement of the captures
showed to be the floating tab strip misread as a band; 0.1.33 restores the
measured chrome pixel-for-pixel.) The Level screen has the
attribute slide-out and the restriction rail (mining/crafting) opening
`SkillRestrictionScreen` lists; SkillInfoScreen shows per-skill bonuses and
per-level unlock icons; hub scroll/slider and the remaining bonus text are
still open, and the Jobs screen still uses vanilla item icons where Aged has
14x14 per-job textures. The EMI recipe sidebar arrives with the EMI port.

**FancyMenu menus:** Aged's FancyMenu configuration ships
(`config/fancymenu/**`, incl. the sound and universal layouts), but **not**
its title-screen layout. That layout pointed its background at
`aged:textures/main_menu_background_with_aged.png` with a fallback on
`aged:textures/main_menu_background.png`, and the only copy of those files in
the pack was `overrides/resources/aged/textures/` - which a launcher drops
into the instance as a plain `resources/` folder that the game never loads.
Every `aged:` reference the layout made was therefore unresolvable, and
FancyMenu painted the missing-texture checkerboard over the whole main menu
("purple and black blocks" was the report). 0.1.40 deletes that layout and
the dead 30 MB folder; Hearthwind's own title screen, drawn by
`HearthwindTitleScreenMixin` out of `assets/hearthwind/textures/gui/title/`
inside the client jar, now owns the menu, and the universal layout's
background points at the same file. Two guards keep it that way:
`tools/validate_menu_assets.py` fails the build if any FancyMenu
`[source:location]` reference fails to resolve or if an `overrides/resources/`
folder reappears, and the screens tour asserts the menu art resolves before
capturing `tour_title`. **Do not re-add `fancymenu` to `CGT_EXCLUDE_MODS`** -
excluding it is exactly how this shipped uncaught for eight releases, because
the mod that draws the menu was not even loaded during testing. FancyMenu's
one-time welcome popup appears in a brand-new game dir, which is the
behaviour we want; the screens tour runs with it enabled.

### 5.8 The Hearthwind guidebook

Aged ships `aged:aged_guide_book` (Lavender, loaded through Paxi from
`config/paxi/resourcepacks/aged_guide_book`): **12 categories, about 62
entries** (start, character, hydration, nutrition, temperature, produce,
storage, tools and weapons, armors, battle, archeology, ...) plus
multiblock structure previews. Ours: `hearthwind:hearthwind_guide_book`
(`validate_guidebook.py`: 11 categories, 48 entries), with a vanilla
written-book fallback when Lavender is absent.

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

### 5.11 Approved deviations from Aged

**There are none.** The section exists so the count is stated rather than
implied, and so adding one is a deliberate act.

The FancyMenu first-run panel was investigated as a candidate deviation and
turned out not to be one. It is a picture-in-picture window drawn over the main
menu, and Aged's own `config/fancymenu/options.txt` suppresses it by shipping
`modpack_mode = 'true'`: FancyMenu's `MixinGui` opens the panel only when
`showWelcomeScreen && !modpackMode && screen instanceof TitleScreen`, so a pack
in modpack mode is assumed to have greeted its players already. We ship the same
file, so we are at parity. What made the panel visible in testing was
FancyMenu regenerating its own config in a fresh game directory (writing
`modpack_mode = false`), which is a test-environment artefact, not pack
behaviour. `FirstRunMenuGuard` re-asserts the shipped values at runtime for that
reason, and `ScreensTourGameTests` fails if `modpack_mode` is not on.

**Parity is the default.** No deviation is permitted without asking the user
first, and one that turns out not to be a deviation is not recorded as one.

**Functional parity pass (0.1.46 onward).** The user set the order explicitly:
functional mechanics parity first, polish only after it is exhausted. Each
remaining mechanic was measured against the reference pack's own config and
datapack rather than assumed, and the first pass found two real divergences and
one invented table:

- **Steel** was three iron and one coal smelted for 600 ticks for half an
  experience point. The reference is two iron and two coal over 5200 ticks for
  six experience, so the pack's mid-game metal was eight times too cheap. The
  recipe now matches, and `steelBlastingMatchesTheReferenceCost` pins all four
  numbers.
- **Per-crop season rates**: all 62 of the reference's per-crop files are now
  ported verbatim (47 copied, 15 already identical). Twenty of ours were
  invented - smooth 0.8/1.2/1.2/0.3 patterns - and the tests that covered them
  were green against those inventions. They now assert the reference's own
  values, and one of them, tomato, does not peak where we said it did.
- **Downed/revive was a rewrite, not a port.** Aged's `revive.json5` sets only
  three of revive 1.0.7's fifteen keys, so the behaviour comes from the mod's
  constructor defaults, which were read out of its bytecode rather than guessed.
  Our rebuild had invented a 60-second bleedout that killed the player and
  dropped their inventory, a 3-second revive channel, and a 6 HP revive. Aged
  has **no bleedout at all** (`timer` defaults to `-1`, and the server tick
  hook returns immediately at bytecode offset 27), revives on a **single
  click** of the downed player's own button (an ally's crouch + empty hand only
  arms it), and revives to **2 HP** with a 600-tick aftermath effect. The user
  chose full parity, plus all three of the smaller details (crouch to arm,
  death coordinates, the 0.3 deg/tick death-camera spin). The countdown UI is
  gone with the timer it counted.
- **The temperature manager was regressed to upstream defaults.** `docs/AGED_PARITY.md`
  had recorded two "deliberate deviations" here; both were false, and they were
  hiding a data bug. Our model already accumulates integer deltas against the
  same band-quantised rows as `TemperatureAspects.tickPlayerEnvironment`, and
  acclimatization was always applied - but commit `3ddfe4794` had replaced Aged's
  world-datapack manager file with EnvironmentZ 2.0.8's stock one, so the values
  actually being applied were `1600/-15` instead of Aged's `1680/-20`, the
  thermometer icons turned at +-3 instead of +-2, and iced armour cost -5/-6/-3
  instead of -4 everywhere. Restored in 0.1.47, hard-coded fallbacks included.
- **The purified bowl was safer than Aged's.** Dehydration's `ItemInit`
  constructs `water_bowl` at offset 300 and `purified_water_bowl` at offset 328,
  both passing `hasThirstChance = true`, so a bowl of *purified* water still has
  a 40% chance of leaving you Thirsty. We had made ours safe; it now rolls like
  the dirty one.
- **Three audit rows were wrong about the reference.** The water bowl and milk
  bucket roll thirst at amplifier **0**, not 2 - the `iconst_2` in the bytecode
  is the `idiv` divisor for the duration, and the amplifier is the following
  `iconst_0`. Those two rows were already at parity; the 🟡 markers were
  spurious and are gone.
- **The last two functional water rows are ported (0.1.48).** Both were read out
  of Dehydration's mixins rather than assumed. `WaterFluidMixin` overrides
  `FlowableFluid.spreadTo` so that purified water writes the cell it flows into
  directly, skipping `LiquidBlockContainer.placeLiquid` - which re-checks
  `canBeReplacedWith` and refuses liquid-into-liquid, so without the override a
  purified stream simply halts on the first vanilla water it meets. 26.2 hands
  `spreadTo` the new `FluidState` and already ends in
  `setBlock(pos, target.createLegacyBlock(), 3)`, so that part is one line.
  Two more were needed, and the first two attempts at this row were wrong
  because of them:
  - **`WaterFluid#canBeReplacedWith`** is `direction == DOWN &&
    !other.is(FluidTags.WATER)`, and purified water is deliberately in
    `FluidTags.WATER` (so it counts as water for sipping, bowls, flasks and
    buckets). The flow is therefore refused *before* `spreadTo` is ever
    reached. This is the reference's other handler - its `matchesTypeMixin` -
    which 26.2 relocated into this method, so
    `WaterFluidPurifiedMixin` reinstates it.
  - **`WaterFluid#createLegacyBlock` hardcodes `Blocks.WATER`.** Our fluid
    extends `WaterFluid`, so it inherited that, and every time purified water
    placed itself in the world it wrote *vanilla* water. Purified water could
    not physically exist as a block, and pouring a purified bucket handed the
    player normal water. The reference's `PurifiedWaterFluid#method_15790`
    overrides exactly this; that override was the missing piece, and it is why
    the displacement test kept reporting vanilla water in the cell.
  `CauldronBehaviorMixin` is a single
  `map.put(PURIFIED_BUCKET, FILL_WITH_WATER)` at the tail of vanilla's
  bucket-behaviour registration, so the purified bucket reuses the water
  bucket's row verbatim and fills a **vanilla** cauldron. 26.2 renamed that map
  to `CauldronInteraction.Dispatcher`, split it by what the cauldron holds, and
  made `put` package-private, so the row goes on `CauldronInteractions.EMPTY`
  (the dispatcher a plain cauldron consults) through a one-method invoker
  mixin. Both are gametested against real server ticks and the real
  `useItemOn` path respectively.

With those two closed, every **functional** row in the Dehydration audit is
ported. The two rows still marked ❌ are polish, not mechanics: thirst droplet
tooltips and the four custom sound events (we use vanilla substitutes). The
functional-parity work therefore moves on to AdditionZ's `villager_gender` -
which needs the user's approval, because 26.2 has no `VillagerBreedTask` and
opposite-sex breeding without the female-villager texture swap is itself a
deviation - and then the 61-mod port queue.

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
4. ~~Port Aged's config overrides for mods we already ship (section 4 list),
   migrating keys where the 26.2 version renamed them.~~ **Done 0.1.41.**
   All 40 of Aged's config files were compared against ours key by key. The 24
   that belong to mods we rebuilt already match exactly (audited against the
   Java field initializers, which is what a fresh install writes - not the
   generated JSON, which was stale), and the 16 for third-party mods are now
   shipped verbatim: `adventurez`, `lootr`, `modmenu`, `moreculling`,
   `sparsestructures`, `immersive_aircraft`, `immersive_armors`,
   `sodium-options`, `smallships-common`, `DistantHorizons`. `backslot.json`
   already carried Aged's five values. Four are deliberately not ported, with
   reasons recorded in `conversion/overrides/config`: `cameraoverhaul` (the
   26.2 config was restructured into sections, so Aged's keys no longer exist
   and shipping them would be dead config), `logbegone` (26.2 has no external
   config file at all), `couplings` (player progress, not a setting), and
   `backslotaddon`/`jobsaddon` (those mods are not shipped).

**Startup chat (0.1.41).** Joining a world used to bury the pack's own first
instructions under two dozen mods announcing themselves, so the client's system
chat is filtered for the first 30 seconds of a session: Hearthwind's own lines
always pass, other players' chat is untouched, and the window closes itself so
nothing is lost later. `StartupChatFilter` + a `ClientPacketListener.handleSystemChat`
inject, pinned by `ChatFilterGameTests`.

**W1: First-impression and HUD parity (what a player sees in the first 10 minutes).**
Adopt Overflowing Bars; port Time & Wind and move seasons to tick length;
add the `Lv. N` preview label; match the tab icons; re-verify the
guidebook in the starter kit (done 0.1.30: the starter hotbar is Aged's
five welcome-screen commands, and `WelcomeScreen` rebuilds the welcome
screen that grants them); the Comfort card is decided - deleted, because Aged
has no hydration or temperature panel (0.1.34); the Hearthwind
guidebook (section 5.8). Then port the
accessory stack (Trinkets, BackSlot + addon, Inmis + addon), because it
defines the inventory silhouette. Screens: the panel chrome and the floating tab strip now match the captures
(done 0.1.32, corrected 0.1.33: 200x215 panels, floating LibZ tabs,
the Jobs screen's measured rows and multi-job summary). The Level screen's rail
and `SkillRestrictionScreen` were already shipped and the tour screenshots
(`tour_skills`, `tour_skills_attributes`, `tour_skill_restrictions_mining`,
`tour_skill_restrictions_crafting`) prove it; that W1 line was stale and is
retired on 0.1.35. What is still open on these screens is cosmetic: Aged's
GPL sprite art for the skill/tab icons, the hub scroll slider, and the full
per-level bonus text. Every item lands with a client-gametest screenshot next
to the Aged reference.

**W1b: Version drift review (section 5.9)** for shipped mods, gameplay-heavy first.

**W2: Adopt the mods with official 26.2 builds.** Audited against the built
mrpacks on 0.1.35 - **this workstream is essentially done**, so the list below
is history, not a queue. Shipping in the 0.1.34 client pack: `sodium`, `iris`,
`fancymenu`, `melody`, `distanthorizons`, `cameraoverhaul`, `moreculling`,
`smooth-swapping`, `smooth-scrolling`, `entityculling`, `immediatelyfast`,
`badoptimizations`, `konkrete`, `overflowing-bars`, `rsls`,
`sound-physics-remastered`, `default-options`, `entity-model-features`,
`skinlayers3d` (3dskinlayers' new name), `presence-footsteps`,
`spawn-animations`; in the server pack too: `stoneworks`, `neruina`,
`puzzles` + `puzzles-lib`, `backslot`, `inmis`, `trinkets`. The rest of the
original list was never an Aged mod at all (satin, indium, noisium, bobby,
zoomify, satin-style perf mods) or has no 26.2 build to adopt
(`tooltipfix` stops at 1.19; `better-archaeology`, `backslotaddon` and
`inmisaddon` are not on Modrinth and stay in the 63-entry W4 allowlist).
`first-person-model` and `3dskinlayers` are covered by name drift, not a
missing jar. Re-run `python3 conversion/scripts/aged_parity_diff.py` before
quoting any of this; it is the only source of truth here.

**W3: Close parity gaps in the Hearthwind rebuilds (section 5.3).**
Highest gameplay impact first: jobs curve, 3 concurrent jobs and switch
cooldown (done 0.1.28 - all three were already live; 0.1.28 added the
missing earn paths: anvil/smithing, brewer, builder placement); RPGDifficulty
caps and boss scaling (done 0.1.29: armor 2x cap, exclusion list, overworld-only
steps, adult livestock, boss path, and the 30% jitter plus the
5%/10% speed and big zombies - done 0.1.37, all three rolled in
rpgdifficulty's order with no early return so a spawn-adjacent mob still
rolls - and the payout keys done 0.1.38: extra XP scaling, doubled loot
rolls, and the general damage multiplier that upstream misleadingly calls
creeperExplosionFactor); steel ratio (done 0.1.19:
3 iron + 1 coal @600t); LevelZ craft/smithing/brewing gate enforcement
(verified already enforced); dirty-water duration 200t (verified at
parity); seasonal bonemeal (done 0.1.18). Every fix lands with a gametest
pinned to Aged's value.

**W4: Port queue (section 5.2), rewritten 0.1.44.** The old order had
drifted: it still listed `inmis`, `backslot` and `trinkets` (all shipped) plus
`bento-box`, `lootbeams` and `villagertradefix`, which are not in the queue at
all. Current state, measured against the Modrinth API on 2026-09-30: the queue
holds **62 entries and every one is a port - not one has a 26.2 Fabric build**,
so there is nothing left to adopt from the W2 list. Three were already finished
and had simply never been dropped from the allowlist (`inmis` shipped,
`backslotaddon` rebuilt in-tree, `Dungeon Now Loading` ported as `dungeonz`),
and `time-and-wind-ct` joined them when the audit proved the port already
exists.

Suggested order:

1. **AdditionZ** - the largest single gameplay change in the queue for one
   line: `baby_to_adult_time` makes every animal a baby for 3.5 real hours.
   Then campfire rain extinguish, phantom tick time, the spawner cap and the
   iron-golem cap. Study in `docs/AGED_HYDRATION.md` and
   `.tmp/azstudy/ADDITIONZ-STUDY.md`.
2. **Tier A (4)**: `fbp-renewed`, `fishing-real`, `moonlight`, `surveyor` -
   all Fabric 26.1.x, no 26.2 build.
3. **Gameplay content**: `medievalweapons`, `smitherz`, `voidz`, `travelerz`,
   `antique-atlas` + `surveyor`, `another-furniture`, `connectiblechains`,
   `niftycarts`, `villager-transportation`, `smarterfarmers`,
   `fishing-real`, `treechop`, `hearths`, `immersive-snow`,
   `immersive-thunder`, `immersive-ui`, `grass-overhaul`, `nameplate`,
   `particular`, `creeperoverhaul`, `endermanoverhaul`, `spirder-caves`,
   `sushi_bar`, `astrocraft`, `amarite`, `DEUF`, `dawner`/`voidz` family.
4. **Client-only feel**: `tooltipfix`, `seamless-loading-screen`,
   `Shut Up GL Error`, `borderless-mining`, `DripSounds`,
   `Dungeon Now Loading`, `loadmyresources`, `modelfix`,
   `GeckoLibIrisCompat`, `translucencyfix`, `libz` (**keep the rebuild** -
   0.1.44 verified it pixel-parity and found no consumer for its API).
5. **Libraries and addons that are dead weight alone**: `library-fabric`,
   `euphonium`, `bbb`, `Nimble`, `indium`, `noisium-fabric`,
   `modernfix-fabric`, `lmft`, the whole EMI family, `extendeddrawersaddon`,
   `inmisaddon`, `backslotaddon`.

Each is a per-mod team decision; record it before starting. Re-run
`python3 conversion/scripts/aged_parity_diff.py` before quoting any of this. (`welcomescreen` left the queue in
0.1.30: rebuilt in-tree as the welcome screen plus the five starter-loadout
commands.)

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
| `endrem` | Ported in-house by hearthwind-world (0.1.19 item/art/lang parity; the `ancient_portal_frame` block mechanic and code-driven acquisition remain a tracked deviation) |
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
