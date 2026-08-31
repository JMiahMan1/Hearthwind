# Aged parity: gameplay + look & feel

Reference: Aged 3.1.2 (MC 1.20.1, 227 jars) extracted at
`.tmp/aged-ref/extract/`. Hearthwind = 26.2 rebuild.

Scope note (per user): **splash/title screen** and **drinking water with
hand** are intentional deviations — everything else targets parity.

---

## 1. Can the Aged datapack be ported to 26.2? — YES (one silent bug fixed)

`conversion/datapacks/aged-server/` is the migrated corpus: **802 files,
pack_format 107**. Status verified live on the dev server:

- Loads as `file/aged-server (world)` — **zero parse errors** on boot.
- Ore→piece economy verified end-to-end via RCON `loot ... mine`:
  `iron_ore → agedaddition:raw_iron_nugget ×6`, `coal_ore → coal_piece`,
  `diamond_ore → diamond_piece ×3`, `copper_ore → raw_copper_nugget ×21`;
  silk touch still drops the ore block; fortune 3 applies `ore_drops`.

### The bug that made it look unportable

The dev world had **no datapack installed** (`Missing data pack
file/aged-server`) — the whole corpus was inert during every earlier test.
After installing it, one real 26.2 incompatibility surfaced:

1.20.1 item predicates carried enchantments inline; 26.x moved them behind
a component map. The old shape does **not** error — unknown keys are
ignored, so `match_tool` (silk touch) matched *every* tool and ores dropped
themselves instead of pieces.

```jsonc
// 1.20.1 (silently wrong on 26.2)
"predicate": {"enchantments": [{"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}
// 26.2 (correct)
"predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}
```

Fixed in `conversion/scripts/migrate_datapack.py` so regeneration stays
deterministic. Three classes of 1.20.1 -> 26.2 drift are rewritten during
migration (`fix_item_predicate` for component-map enchantment predicates,
`fix_loot_functions` for `looting_enchant` ->
`enchanted_count_increase` and `random_chance_with_looting` ->
`random_chance_with_enchanted_bonus`, plus EntityPredicate `type` ->
`entity_type` and `type_specific` drops); 99 loot-table fixes total.
Loot entries referencing mods we do not ship (e.g. naturalist, which made
the fishing table drop nothing) are pruned, and Aged's own dead
`blasting_extra` steel recipe is skipped in favour of our
`earlystage:steel_ingot_from_blasting`. The script
also no longer depends on a `/tmp` path (scratch-policy violation); it
defaults to `.tmp/aged-ref/extract/...`.

Remaining ids flagged as "unused by vanilla 26.2" (`random_chance_with_looting`,
`looting_enchant`) still resolve and load cleanly — no action needed.

### Port status matrix

| Corpus | Files | Ported | Consumed by | Verdict |
|---|---|---|---|---|
| `minecraft/loot_table` (ore→piece) | 49 | ✅ | vanilla | **ACTIVE — verified** |
| `minecraft/recipe`, `aged/recipe` | 70 | ✅ | vanilla | ACTIVE |
| `minecraft/tags`, `aged/tags`, `c`, `spoiledz` | ~20 | ✅ | vanilla + survival | ACTIVE |
| `tiered` (affixes/reforge) | 199 | ✅ | hearthwind-primitive `TierRegistry` | **ACTIVE — done** |
| `earlystage/sieve_drops` | 1 | ✅ | primitive `SieveBlock` | ACTIVE (byte-equal to Aged) |
| `levelz` (skill gates) | 400 | ✅ | **hearthwind-skills `SkillGates`** | **ACTIVE — 649 gates live** |
| `minecraft/seasons/crop` | 15 | ✅ | hearthwind-world `SeasonCrops` | **ACTIVE — 15 crops, per-season** |
| `environmentz` (temp model) | 9 | ✅ | survival `EnvironmentCorpus` | **ACTIVE — 17 blocks / 3 items / 3 tables** |
| `dehydration/hydration_items` | 1 | ✅ | survival `HydrationCorpus` | **ACTIVE — 35 items / 12 tiers** |
| `jobsaddon` (job ladders + restricted) | 14 | ✅ | jobs `JobCorpus` | **ACTIVE — 8 ladders, 103 restricted** |
| `minecraft/worldgen` | 20 | ✅ | vanilla (15 foreign dropped) | active, foreign ore-gen lost |

**So: the datapack ports. The work that remains is writing readers into our
base mods for the five inert corpora** (§4 priority list).

---

## 2. Gameplay comparison

Legend: ✅ parity · 🟡 partial/different tuning · ❌ missing

### Survival needs

| System | Aged | Hearthwind | Status |
|---|---|---|---|
| Thirst | Dehydration: `hydrating_factor 2.0`, sip 0.5/300 t, dirty flask 0.3/200 t, potion 0.15, **130 hydration items in 13 tiers** | `baseDrainPerSecond 0.025`, sip 0.5/300 t, flask quench 4.0, dirty 0.3/**600 t**, **35 catalogued hydration items across 12 tiers** | 🟡 dirty duration differs; only 35 of 130 catalogue because we do not ship the lets-do food mods yet |
| Temperature | EnvironmentZ: bands −6/−2/+2/+6, acclimatization ±10/±20, **104 heating blocks, 203 cooling**, biome/day/night/armor/wet/height modifiers | corpus-driven: block/item heat + dimension rows (day/night, armor, soaked, wett, shadow, height, sweat) on top of a continuous biome base | 🟡 parity on sources; two deliberate deviations (below) |
| Diet | NutritionZ is nearly inert (2 item ids); "diet" = food variety | 5 nutrient groups, decay 0.02/s, deficiency debuffs, balanced bonus hearts | ✅ superset (intentional) |
| Spoilage | `seasonSpoilage 8`, 69 non-spoiling (alcohol/tea) | interval 200, chance 0.002, hot ×2, **container spoilage**, non-spoiling tag | ✅ superset |
| Downed/revive | — (no such mod) | 60 s bleedout, 3 s channel, revive at 6 HP | ✅ extra (Hearthwind-only) |

### Progression

| System | Aged | Hearthwind | Status |
|---|---|---|---|
| Skills | LevelZ: max 30, start 2 pts, xp 25×1.6ⁿ, 12 skills, **400 gate files**, procs (crit 3%, double-dmg, reflect 5%, survive 50%, twin 20%) | max 30, base 30/level, 12 skills, **649 gates from the corpus**, procs live | ✅ gates + procs |
| Jobs | 8 jobs, max level 150, 100×1.6ⁿ, 3 jobs at once, 1-day switch cooldown, **per-level content ladders (miner→iron 7/diamond 20, smither→steel 8, builder 533 blocks)**, 103 restricted recipes | 8 jobs, 100 pts/level, Age gating, `/job` commands, **8 ladders read from the corpus (XP = content tier)** | 🟡 no exponential job curve, no cooldown/multi-job |
| Affixes | Tiered: 199 files, rarities 50/35/15/8/3/0 | **same 199 files + reforge** | ✅ |
| Mob scaling | RPGDifficulty: distance 300/200, caps hp 4×/dmg 3×/prot 2×/speed 1.8×, special zombies, boss scaling | grace 300, step 200, +1 hp/+0.3 dmg per step, max 60 steps | 🟡 no caps/protection/speed/boss |
| Primitive | earlystage: rock/flint, crafting rock (2 hits/80 wear), beginner deaths 3, sieve, **steel = 2 iron + 2 coal @5200 t** | same hits/wear/deaths, sieve drops byte-equal, steel = **1 iron + 2 coal** | 🟡 steel recipe ratio differs |
| Recipe removal | 95 recipes removed (all ore smelting, all cooked food, flint_and_steel, steel blasting) | **73 ore/tech removed** (vanilla ore smelting gone, ore-piece blasting is the route); 21 cooking removals loaded but off until stoves work | 🟡 cooking path intentionally still open |
| SmitherZ gems, FleshZ tanning, VoidZ boss respawn, AdditionZ (mob aging/phantoms/spawners) | present | absent | ❌ |

### World

| System | Aged | Hearthwind | Status |
|---|---|---|---|
| Seasons | 21 days, temp + snow melt, **per-crop multipliers** (wheat 0.5/1.5/1.0/**0.0**), no underground growth, no winter breeding | 21 days, temp offsets + one per-season multiplier (0.4–1.2) | 🟡 **per-crop table unused** |
| Ambience/sound | AmbientEnvironment, DripSounds, PresenceFootsteps, Sounds, Sound Physics, ImmersiveThunder, Euphonium | none | ❌ (big "feel" gap) |

---

## 3. Look & feel comparison

Aged's rule: **no global UI retexture** — panels stay vanilla `#C6C6C6`;
the feel comes from additions. Everything is measured at GUI scale 3
(640×360 GUI space) from `aged-hud-reference.png`.

| Element | Aged | Hearthwind | Status |
|---|---|---|---|
| Thirst | 10 teardrops, `#1AAFE7`/`#0E86CA`, right-aligned in the band **directly above hunger** | droplets above hunger, shift up when air bubbles show | ✅ |
| Temperature | vertical 7×27 tube **right of hotbar** + 12×12 unit box + trend arrow, `#1D2946` outline | TempHud right of hotbar | 🟡 missing unit box + trend arrow |
| Season | top-left (2,2), 9×9 icon + "Season, Day N/M" one string, season-tinted (spring `#FFA3BB`) | SeasonHud top-left "Winter, Day 14/21" | 🟡 no icon, no per-season tint |
| Panels | vanilla grey, 4 **item-icon** tabs ~20×22 with 14×14 icons, selected brighter+taller | icon tab strip (apple/bottle/campfire/sword/axe), jobs card grid, dark text | ✅ close |
| Nutrients | 5 rows, **segmented** 140×5 bars, red→amber→yellow→green over near-black track | 5 bars, right-aligned values | 🟡 bars not segmented |
| Inventory | tabs merged onto the inventory + left button column + player preview | standalone screens (`N`, buttons) | ❌ epic |
| Fonts | vanilla everywhere (SeasonHUD ships 9×9 bitmap season icons) | vanilla | ✅ |

FancyMenu in Aged owns **only** the title screen + menu backgrounds — the
in-game HUD is not FancyMenu. Our HUD is therefore the right architecture.

---

## 4. Priority: what gets written into the base mods

1. **levelz gate loader — DONE** (hearthwind-skills `SkillGates`): reads the
   ported corpus from the world datapack at `SERVER_STARTING` and falls back
   to the bundled digest when the pack is absent. All seven categories load:
   284 mining, 16 block-use, 12 item-use, 140 crafting, 162 smithing,
   23 brewing, 12 entity (**649 gates**, verified on the dev server). Aged's
   progression ladder is live: stone/cobblestone 5, diorite 6, andesite 8,
   granite 10, iron ore 13, deepslate 18, diamond 21, obsidian 25,
   netherite 27. Ids from mods we don't ship are skipped, and files merge
   rather than wipe — levelz's `"replace": true` overrides its own built-in
   defaults, which we don't have (eleven `entity/farming_0` files carry it).
   Known follow-ups: `crafting`/`smithing`/`brewing` gates are loaded and
   queryable but not yet enforced at the menu, and `ClientSkillGates` still
   reads the bundled digest for tooltips, so its hints can drift until gate
   data is pushed from the server (priority 10).
2. **Season crop loader — DONE** (hearthwind-world `SeasonCrops`): reads the
   15 `minecraft/seasons/crop/*.json` files at `SERVER_STARTING` and applies
   per-crop multipliers to wheat, carrots, potatoes, beetroots, melon/pumpkin
   stem, cocoa, sweet berries and all 6 saplings (wheat 0.5/1.5/1.0/**0.0**,
   spruce 0.5/0.1/1.0/1.5, cherry 1.5/1.0/0.5/0.0…). A 0.0 multiplier cancels
   the growth tick outright, so winter genuinely halts wheat and oak saplings
   instead of merely slowing them; uncatalogued crops fall back to the
   per-season config defaults. Two bugs fixed on the way: the old single
   multiplier was **inverted** (`bound * multiplier` made slow seasons grow
   *faster*), and winter breeding was never blocked — `Animal` now refuses to
   breed in winter (config `animalsBreedInWinter`, default false).
   **World gametests were never registered**: `hearthwind-world` had no
   `fabric-gametest` entrypoint, so all 18 world tests were silently skipped in
   every "144/144" run. Now registered → **166/166**. Two of the newly-running
   tests were themselves wrong: a tautology (`mixinClass != null`, which also
   crashes on Mixin-stripped classes) and a season expectation that
   contradicted `Season.fromDay`; both replaced with real assertions.
   Left at vanilla speed: bonemeal (its growth roll goes through
   `Mth.nextInt`, not `RandomSource.nextInt`).
3. **EnvironmentZ data loader** — **DONE**. `EnvironmentCorpus` reads
   `environmentz/environment_blocks` (17 resolvable blocks: campfire,
   furnace, blast furnace, smoker, magma block + snow/ice),
   `environment_items` (3) and `manager` (overworld/nether/end tables,
   thermometer bands −6/−2/+2/+6). Block heat follows the reference
   semantics: `heatBlockRadius` 3 volume scan, the optional boolean property
   must be TRUE (a **lit** furnace warms you, a cold one does not), line of
   sight is required, at most `max_count` blocks of one type contribute
   (campfire max_count 2), and the value falls off with distance
   (campfire +3 / +2 / +1). Being enclosed adds `roomHeatFactor` × the heat
   sum, so fires indoors matter far more. Chipped-compat entries (~97% of
   the corpus) are skipped since we don't ship Chipped.
   Two **deliberate deviations**, recorded here so they are choices and not
   accidents: our body temperature drifts toward a continuous biome target
   (Aged accumulates integer deltas against band-quantised rows), and the
   corpus acclimatization values (thresholds 180/1680) live on that integer
   scale, so they are loaded and exposed but not applied.
4. **~~Dehydration hydration-items loader~~ DONE** (hearthwind-survival) —
   `HydrationCorpus` reads `data/dehydration/hydration_items/*.json`; every
   catalogued food or drink now restores its tier in hydration points on
   consumption (melon 1, apple 4, golden apple 6, milk bucket 8 …). 35 of
   Aged's ~130 items resolve today; the rest arrive with the lets-do food
   content. Tunable via `thirst.useHydrationCorpus` / `hydrationCorpusScale`.
   Not ported: the bad-potion thirst roll (`potion_bad_thirst_chance 0.15`).
5. **~~Jobs content ladders~~ DONE** (hearthwind-jobs `JobCorpus`) — reads the
   8 `jobsaddon/*_job.json` ladders and the 103-entry restricted recipe list
   from the datapack at `SERVER_STARTING`. Job XP now pays the CONTENT TIER
   like the reference: iron ore pays 7 as a miner, diamond 20, off-ladder work
   pays the flat `xpPerAction`. Two deliberate deviations found by reading the
   reference jar: restricted recipes only **deny crafting XP** (they are never
   uncraftable), and job ladders **gate nothing** - crafting denial in Aged
   comes from LevelZ skill gates - so `jobCraftGating` now defaults to false
   (it used to block e.g. iron ingots for anyone who wasn't a miner).
   Still missing: the exponential job curve (150 levels, 100×1.6ⁿ), holding
   three jobs at once, and the 1-day job-switch cooldown.
6. **~~RecipeRemover parity~~ DONE** (hearthwind-primitive `RecipeRemovals`)
   — 73 ore/tech recipes are stripped from the recipe map as it is installed
   (mixin on `RecipeManager#apply`, so crafting, furnaces and the recipe book
   all lose them on both sides). The list lives in
   `data/earlystage/recipe_removals/aged.json` (packs can add files), and the
   point is the ore-piece economy: ores drop pieces, and ingots come from the
   corpus' slower blasting recipes instead of vanilla 200-tick smelting.
   `flint_and_steel` is removed too, so fire needs steel. The 21 cooking
   removals are loaded but **off by default** (`removeCookedFoodRecipes`) —
   upstream forces cooking onto stoves, and our stoves are not playable yet,
   so flipping it on today would leave no way to cook food. Our own
   `earlystage:steel_ingot_from_blasting` is deliberately excluded from the
   list (upstream deleted a differently-named recipe for the same purpose).
7. **Skill procs — DONE.** `SkillProcs` (hearthwind-skills) implements the
   levelz capstones: crit chance scales with luck (+20% damage on a crit),
   double damage is a max-strength capstone (3%), dodge (agility, 10%),
   reflect (defense, 5% via thorns damage back at the attacker), cheat death
   (luck, 50% → 1 HP + regen II + absorption) and twin babies from breeding
   (farming, 20%). Fall damage soaks 0.25 per agility level (scales, no
   capstone). All chances live in `config/hearthwind_skills.json`
   (`procs.*`); procs that only fire at max level are gated by
   `capstonesRequireMaxLevel`. Two deliberate deviations: a crit is a flat
   +20% bonus rather than forcing the vanilla 1.5× crit path, and archery
   procs (bow extra damage / double shot) are deferred.
8. **HUD polish** — thermometer unit box + trend arrow, season icon + tint,
   segmented nutrient bars; inventory-anchored tabs (bigger epic).
9. **Ambience mods** — evaluate 26.2 builds for the sound stack
   (see docs/DROPPED_78_STUDY.md; we never drop, so these stay on the
   watchlist until ported).
10. **Gate hint sync** — push the resolved gate map to the client on join so
    `ClientSkillGates` tooltips match the server's corpus instead of the
    bundled digest (keeps the client presentation-only, server-authoritative).

Mods that cannot be ported as jars are already mapped to in-house modules in
`conversion/curated/mods-manifest.json` (`rebuild` = 15 entries: dehydration,
environmentz, nutritionz, spoiledz → survival; levelz, jobsaddon, partyaddon,
rpgdifficulty → skills; earlystage, tiered, reciperemover, autotag →
primitive; fabric-seasons, seasonhud, crop_growth_modifier → world).
Per-mod 26.2 availability is tracked in docs/NOT_IMPLEMENTED.md and
docs/PATCH_PORT_STUDY.md.

---

## 5. Reproducing the verification

```bash
# regenerate + install the corpus
python3 conversion/scripts/migrate_datapack.py         # defaults to .tmp/aged-ref/...
cp -R conversion/datapacks/aged-server dev-server/world/datapacks/
# restart dev server (Popen start_new_session pattern), then:
python3 custom-mods/tools/rcon.py 127.0.0.1 25575 agedtest "datapack list"   # must show file/aged-server
python3 custom-mods/tools/rcon.py 127.0.0.1 25575 agedtest \
  "loot spawn 9 72 9 mine 9 70 9 minecraft:diamond_pickaxe"   # -> raw_iron_nugget (with iron_ore placed)
```
