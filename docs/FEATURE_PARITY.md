# Feature parity matrix - Hearthwind (26.2 rebuild) vs Aged 3.1.2 (MC 1.20.1)

> **1.0.0 scope (2026-09-24):** `docs/RELEASE_1.0_PARITY.md` governs 1.0.0 and wins wherever this file disagrees. Several ✅ rows here overstate parity (seasons; the RPGDifficulty row is now 🟡 with only the random roll and the special zombies open, and the jobs curve/cooldown row was corrected to 🟡 in 0.1.28). The gap list is in the plan's section 5.3.

Living document. Status per system the original pack shipped; goal is
parity first, deliberate improvement where noted. UPDATE WITH EVERY
GAMEPLAY COMMIT.

Legend: ✅ done · 🟡 partial · ❌ missing · ➖ deliberately not carried
over (with reason)

## Rebuild-class systems (custom mods replacing original mods)

| Original mod | System | Corpus / config | Status | Notes / improvement |
|---|---|---|---|---|
| dehydration | Thirst | `data/dehydration`, `config/hearthwind_survival.json` | ✅ | Hydration attachment `dehydration:hydration`, sprint/effect drain, 10 HUD droplets (Aged's exact row, no flask icon - upstream has none), food hydration corpus (105 of 115 catalogued ids resolve, across 12 tiers), campfire purification (0.1.33) and the three Aged brewing mixes + `dehydration:hydration` Potion of Hydration (0.1.35). **0.1.47**: the purified water bowl rolls thirst like the dirty one - Dehydration builds both with `hasThirstChance = true` |
| environmentz | Temperature | `data/environmentz`, same config | ✅ | Integer-delta body temperature recomputed every 11 ticks (EnvironmentZ's own model), season offsets, heat/cold blocks, shelter bonus (+50%), insulation items, vertical thermometer HUD. **0.1.47** restored Aged's manager numbers after commit `3ddfe4794` had reverted the file to EnvironmentZ's upstream defaults |
| nutritionz | Diet (5 groups) | `nutritionz:*` item tags, same config | ✅ | 5 nutrient groups (fruits, vegetables, grains, proteins, sugars), decay, deficiency debuffs, balanced bonus hearts, `NutrientsScreen` - a pixel-exact 176x142 rebuild of NutritionZ 1.0.11, opened by clicking the 9x9 button at the inventory's top right exactly like Aged (the `N` keybind is a Hearthwind addition) |
| spoiledz | Spoilage | `spoiledz:perishable_items` + `non_spoiling_items`, same config | ✅ | Inventory & container food spoilage with hot-biome multiplier and non-spoiling exemptions |
| levelz | Skills x12 | `data/levelz` ~400 files → `data/hearthwind_skills/gates` | ✅ | 12 skills, 3-heart start progression (+0.5 heart/level), 750+ break/use/craft gates, triangular XP curves, skill capstones/procs, and Aged's 2 starting skill points (`startPoints`, granted once on first join). 0.1.39: the gate loader reads levelz's `object` field, so the 55 block-gate files that hide their real id behind a `minecraft:custom_block` placeholder now load (block-use gates 16 -> 68 in the full pack). 0.1.44: that fallback was still restricted to block gates, leaving 39 item gates dropped by the same placeholder - including Alchemy 15's teleport potion and scroll, which point at AdditionZ ids we do not have and so still correctly drop. The rule is now simply "the file has an `object` field"; `entity` uses it 10x and the four skill categories never do |
| additionz | mob aging, phantoms, spawners, campfire rain | rebuilt in `hearthwind-survival` (`additionz/AdditionZParity` + 5 mixins) | ✅ | 0.1.45: the five gameplay keys ship with the reference's values - 252 000-tick baby time, 60-sample rain extinguish (~121 s), 144 000-tick phantoms, 8 iron golems, 20 spawner waves then a 10-minute lockout with sparks. Five keys are no-ops on 26.2 and are skipped; `villager_gender` is out of scope |
| libz | Info-panel tab system | `hearthwind-client` `TabStrip`, `HearthwindPanelScreen` | 🟡 | The LibZ **mod** is not shipped (no 26.2 build; W4 port queue). What ships is the clean-room reimplementation of its `DrawTabHelper` geometry (24 px tabs, 25 px pitch, 27/25/21 px heights, floating on the 21 rows above the panel) and the 200x215 panel chrome measured pixel-for-pixel off the Aged captures (0.1.32, re-measured 0.1.33), plus Aged's job card grid rows, two-column order and multi-job summary. 0.1.32 shipped a 200x236 "tab band" that a careful re-measurement showed to be a misreading of the floating strip; 0.1.33 restores the truth. Aged's per-tab art is GPL, so the tabs use vanilla item icons. **0.1.44**: executing LibZ 1.0.3's own renderer against stub classes and measuring `Inventory.png` at exactly 6.000x confirmed the sheet is byte-identical to `libz:textures/gui/icons.png` and the geometry, pitch, icon offset and hit rects all match, so the rebuild is **kept and the mod is not ported** (nothing consumes its registration API, and two of its mixin targets no longer exist on 26.2). Three gaps closed: a 5th tab for a non-empty equipped backpack, tooltip titles using the reference's own translatable text instead of our `[K]`-style literals, and the unselected first tab's hit box corrected from 25 rows to LibZ's 21 |
| rpg-difficulty | Distance mob scaling | `config/hearthwind_skills.json` mobScaling | ✅ 0.1.29 + 0.1.37 | One factor on health, damage and armor from distance (0.05/200 blocks) and height (0.1/25 blocks), separate caps 4×/3×/2×, warden + ender dragon excluded, steps only count in the overworld, adult livestock scale (babies don't), boss path (0.05/step + 0.3 per player within 128 blocks, 3× health cap). 0.1.37 added rpgdifficulty's three per-mob rolls: 30% chance of a ±3% health/damage jitter, 5% speed zombies (-10 HP, ×1.2 speed) and 10% big zombies (×0.7 speed, +10 HP, +2 damage, 1.3× hitbox and model). 0.1.38 added the payout keys: XP scaling with the health factor (capped 4×), a second loot roll (factor × 0.02, capped 2.0, half the stacks skipped), and the 1.1 damage multiplier upstream misleadingly names `creeperExplosionFactor` |
| jobs-addon | Jobs x8 | `data/jobsaddon` (kept), `config/hearthwind_jobs.json` | 🟡 0.1.28 | 8 jobs (miner, farmer, fisher, warrior, smither, brewer, builder, lumberjack), `/job join/leave/info`, corpus ladders, Age gating, 3 employed slots, 150 cap, 24000t cooldown, exponential curve; earn paths: break, kill, craft, furnace/smoker/blast, fish, anvil, smithing, place, brew. Missing: campfire cooking pays (26.2 campfire drops output from a playerless tick), recipe gating (`jobCraftGating`, off by default) |
| party-addon | Parties & Shared XP | `PartyManager`, `PartyCommand` | ✅ | `/party create/invite/accept/leave` commands, party shared XP range distribution |
| earlystage | Primitive start | `data/earlystage` (sieve drops, flint/steel recipes) | ✅ | Surface rock & flint mounds, sieve mechanics, knapping start, 3 beginner deaths forgiveness, steel economy **at the reference cost since 0.1.46: 2 iron + 2 coal, 5200 ticks, 6 xp** (it was 3 + 1, 600 ticks, 0.5 xp) |
| tiered | Random gear tiers | `data/tiered` 199 files | ✅ | 199 affixes + equipment reforge recipes loaded into `TierRegistry` |
| fabric-seasons + seasonhud + crop-growth-modifier | Seasons & crops | `hearthwind-world`, `config/hearthwind_world.json` | ✅ | 21-day seasons (Aged `seasons.json` 504000 ticks), top-left SeasonHUD widget (`[Icon] Season, Day N/21`), winter snow layering, and **all 62 of the reference's per-crop growth rates ported verbatim since 0.1.46** (the 20 mod-namespace values we had were invented; the four config scalars are now only a fallback) |
| revive | Downed & Revive | `revive/ReviveManager`, `hearthwind-survival` | ✅ | **Rewritten to the reference 0.1.47.** No bleedout timer (Aged's `timer` is -1), crouch + non-potion hand arms the Revive button, one click revives at 2 HP with a 600-tick `revive:aftermath`, death coordinates and a 0.3 deg/tick camera spin on the downed overlay |
| let's do family | Agriculture suite | `letsdo-*` (external repo) | ✅ | Farm & Charm, Vinery, Candlelight, Meadow, HerbalBrews, Brewery, Nether Vinery crops and stations |
| welcomescreen | First-join welcome screen + starter loadout | `hearthwind-client` `WelcomeScreen`, `hearthwind-survival` `StarterKit` | ✅ 0.1.30 | Aged's welcome screen (title, three text blocks, pack image, `Start` button) and the five `/item replace entity @s hotbar.N` commands it runs: bread x4, apples x4, guide book, purified water bottle, campfire - same slots as Aged. Escape starts the game too. Differences on purpose: our own title art, no Discord button, the knapping line dropped from the second text block (our crafting rock is a 3x3 grid) |

## Contrib Ports (Vendored 26.2 Builds)

| Mod | Port Source | Status | Features |
|---|---|---|---|
| `yungs-api` + 5 overhauls | `contrib/yungs/` | ✅ | YUNG's Better Nether Fortresses, End Island, Desert/Jungle Temples, Ocean Monuments |
| `gardens-of-the-dead` | `contrib/gardens-of-the-dead/` | ✅ | Nether overhauls: Soulblight Forest & Whistling Woods biomes, flora, wood sets |
| `natures_spirit` | `conversion/vendored/` | ✅ | Diverse Overworld biomes, blooming canopies, Kaolin clay, diet integration |

## custom-mods 26.2 fork-ports (2026-09-22)

| Mod | Module | Status |
|---|---|---|
| lavender (+ owo-lib) | `lavender` | ✅ guidebook API; Prism + vendored |
| logbegone / pockets / couplings / memoryleakfix / async-locator / passable-foliage | same-named modules | ✅ Prism + vendored (entitycollisionfpsfix dropped 0.1.4: not an Aged mod, no dependents) |
| athena / chipped / exposure / dungeonz / smallships / villagesandpillages | same-named modules | ✅ wired in `settings.gradle` |
| profundis | `profundis` | ✅ yarn→mojmap cave biomes, boot-green 26.2 |

## Adopted structure jars (vendored, 26.2 fabric)

| Mod | Status | Notes |
|---|---|---|
| undergroundworlds 3.2.1 | ✅ | replaces spider-caves + underground-jungle |
| dungeons+ 1.12.0 | ✅ | fabric adopt (was forge-only) |
| MoogsNether + MoogsEnd + StructureLib | ✅ | mns/mes 26.2 fabric family |
| lukis-grand-capitals | ✅ | datapack `conversion/datapacks/`, pack_format 107 |
| ForgeConfigAPIPort 26.2.1 | ✅ | dep for undergroundworlds |
| desert-dungeon 1.0.1+26.2 | ✅ | DungeonZ data addon (`desert-dungeon-dungeonz-addon`, MIT) |
| unnamed-desert 2.0.3+26.2 | ✅ | u_desert re-located (`unnamed-desert`, **ARR** — careful redistribution) |
| betterendcities-vanilla 1.21.3+26.2 | ✅ | end city NBT overrides (`better-end-cities-base`, MIT) |

**Remaining for complete Aged parity**: see `docs/NOT_IMPLEMENTED.md`
(true open set = resolver missing − 28 already-local).

## Deliberately not carried over

| Item | Reason |
|---|---|
| time-and-wind (day length) | dropped upstream; vanilla cycle kept |
| Client-only mods (EMI suite, antique atlas …) | server-first policy; revisit as companion bundle |
| duplicate storage / agriculture mods | de-kludge: keep one best system (e.g. single sieve, single farm progression) |

