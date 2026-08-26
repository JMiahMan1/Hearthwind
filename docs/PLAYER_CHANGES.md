# How Hearthwind differs from vanilla Minecraft

Player-facing summary of everything Hearthwind changes or adds on top of
vanilla. **Keep this updated with every gameplay commit** - it is the
contract of what the pack does. Grown from the Aged fork; server-side only.

## Survival needs (replaces Dehydration + Environmentz)

Vanilla Minecraft only tracks hunger. Hearthwind adds:

| System | What you see | Rules |
|---|---|---|
| **Thirst** | 10 blue droplets above hunger (client HUD) or overlay warnings on vanilla | **Scale** `dehydration:hydration` 0..20, `dehydration:hydration` not `copyOnDeath`. **Tick** 2s (`TICK_INTERVAL 40`). **Drain** `baseDrain 0.025` (~13m empty) ×2 sprint +0.05 per `thirst` amp. **Warnings** 12 yellow, 6 gold, 3 red; **regen** stops <6, **damage** 1/4s at 0. **Drink** `water_bowl` +6 (50% thirst 15s, green tainted swamp texture) / `purified` +6 clean (clear blue) / `hot_water_bowl` & `hot_purified` (boiling red bubbles, `CustomData hot_until` 30s, red name + "Steaming hot!" tooltip) scalds if drunk hot: 2 dmg +1s fire + thirst, only +3; wait 30s to cool (no burn). **Cold** `cold_water_bowl`/`cold_purified` (aqua icy, from snowy ≤0.15 base / winter / with `ice_pack`) gives -1.5 temp +60s "cooled" dampening (-1.5 target) vs normal -0.7 +30s; hot gives none. **Obtain** bowl on water source → `water_bowl` / `hot` (desert noon/target≥7, gold hint) / `cold` (snowy/winter/ice, aqua hint); bowl on `water_cauldron` → `purified` or `hot_purified` (if over lit campfire) or `cold_purified` (if snowy) and drains 1 level; empty-hand on water → +1 dirty sip (90% thirst 20s, 0.6 exhaustion, 3s cooldown, "barely helps" - tedious). **Purify** `water_bowl` → `purified` via campfire. All in `config/hearthwind_survival.json:thirst`; HUD wobbles ≤6. |
| **Body temperature** | "You feel very cold/hot", "Extreme temperature!" overlay, freeze/heat damage | Scale -10..10, `environmentz:temperature` drifts 0.05/s (0.1 per 2s) toward `targetFor(biome)` = `(base-0.6)*6.5` (-9..9; plains 1.3, desert 9, frozen -8.5). `warm` armor +1.2/piece, `non_affecting` -8%/piece, `ice_pack`/`insolating` ±1.5, rain -1, water -2, fire 9.5, Y>128 +1, Y<0 -1.5. **Cold** water drunk (-1.5 +60s "cooled" -1.5 target) and normal (-0.7 +30s) dampens heat; hot gives none and scalds. At **-8** freeze 1 dmg/4s, **+7** exhaustion 0.02/2s, **+9** burn 1 dmg/4s (4s cooldown). Warnings 6 "very cold/hot", 9 "Extreme temperature!". Desert noon hits 9 in ~90s from 0, so midday forces shade/cold water/ice. |
| **Diet** | Deficiency debuffs / bonus hearts | Five food groups (fruit, vegetables, grains, proteins, sugars) tracked separately; each decays over hours. Neglect a group and you suffer its debuff (e.g., no grains = slowness). Keep ALL groups above half for bonus absorption hearts. |
| **Food spoilage** | Food slowly rots in your inventory | Perishable meats/fish/produce randomly rot into rotten flesh over time - faster in hot biomes. Honey, teas and wines never spoil. Cooked food still spoils; plan expeditions accordingly. |

All numbers live in `config/hearthwind_survival.json` - server admins can retune
everything without updates (diet groups themselves are datapack tags under `nutritionz:`).

### Thirst - first day and first week

**First 10 minutes (Day 0):** Spawn 20/20, drain ~1/40s (13m) so time to learn. Punch 2-3 trees → 12 planks → 3-4 `minecraft:bowl` (4 bowls per craft). Find water. Bowl on source → `water_bowl` (green tainted, `BOTTLE_FILL`) or `hot` (desert noon, gold hint) or `cold` (snowy/winter/ice, aqua hint). No bowl? Empty-hand on water → +1 dirty sip (90% thirst 20s, 0.6 exhaustion, 3s cooldown, "barely helps" - deliberately tedious, craft a bowl). Drink bowl +6 (50% thirst if dirty, clean if purified). Stay >6 for regen.

**First campfire (Day 0-1):** Gather 3 sticks + 3 logs + 1 coal (or charcoal: smelt a log). Place `minecraft:campfire` → put `water_bowl` on it (campfire_cooking) → `purified_water_bowl` (+6 clean, no effect). Make 6-8 purified and keep them in your hotbar; campfire re-lights easily.

**First week (Days 1-7):** Keep a water source next to your base (dig a 1×1 pit, fill with bucket later, but bowls work without a bucket). Never sprint when below 6 unless you must - sprint drains ×2. Diet starts decaying (0.02/s per group, ~14 h to empty) and spoilage starts rolling (0.002 per 10s, ×2 in hot biomes) so you also start hunting diverse food and cooking in batches. Thirst stays a daily chore until you have a well and a stack of purified bowls - it never goes away, just gets routine.

HUD: 10 blue droplets above hunger (8px apart, 10px above food, wobbles when ≤6). Vanilla clients see the same warnings as yellow/gold/red action-bar text. `BowlWaterFillHandler` and `WaterBowlItem` are server-authoritative; payload `hearthwind:thirst` syncs to `hearthwind-client` for the HUD, vanilla ignores it.

## Skills (replaces LevelZ)

Twelve skills level up as you play (max level 30):

- **Mining** - mine stone-type blocks. Levels raise mining speed.
- **Farming** - harvest crops, cull livestock.
- **Stamina** - dig dirt/sand-type blocks.
- **Strength** - melee kills. Levels add attack damage.
- **Archery** - bow/crossbow/trident kills.
- **Health** - passive levels add max HP.
- **Defense** - levels add armor points.
- **Agility** - levels add movement speed.
- **Luck** - levels add luck.
- **Smithing / Alchemy / Trade** - unlock-based crafting tiers (see the
  skill unlock lists in-game; e.g., iron gear needs smithing 14).

XP costs grow each level (~30 XP per current level). Admins tune curve,
XP rates and bonuses in `config/hearthwind_skills.json`.

### Skill gates

Progression is enforced, not just cosmetic:

- **Mining gates**: stone needs mining 5, mud bricks 1, deepslate 18,
  obsidian 25, ancient debris 27 ... (full list from the original pack's
  tuning; unknown/modded blocks simply aren't gated).
- **Use gates**: furnace/anvil/smithing table need smithing,
  brewing stand & cauldrons need alchemy, smoker/beehive/composter need
  farming, cartography table agility 8, grindstone strength 15,
  lectern/loom stamina, beacon luck 30.
- Try anyway and you get an action-bar hint ("You need smithing level
  3 to use this") instead of a silent failure.

## Jobs (replaces jobs-addon)

Eight optional professions - miner, farmer, fisher, warrior, smither,
brewer, builder, lumberjack. Join one at a time; level it by doing its
work (mining the right ores, placing the right blocks, catching fish …).
XP curve is `pointsPerLevel * L` per level (default 100, tuned in
`config/hearthwind_jobs.json`). Each job's level tracks unlock its corpus
actions (e.g., smither: iron at lower levels, netherite at high). Leaving
a job clears progress. Job-restricted recipes and bonus rewards are next
(see `docs/FEATURE_PARITY.md`).

## Dangerous frontier (replaces RPG Difficulty)

Monsters get stronger the farther they spawn from world spawn:
+2 HP and +0.5 damage every 1000 blocks past a 500-block safe radius,
capped after 20 steps. The wilds are genuinely dangerous; early bases
near spawn are meaningfully safer. Configurable under `mobScaling` in
`config/hearthwind_skills.json`.

## Primitive start (replaces EarlyStage)

You punch stone for **rocks** (not cobblestone), knap **flint tools**
before any wood tier is viable, and use ore-piece recipes (9 pieces ↔ 1
ingot). Flint tool repairs use `flint_tool_repair` tag. **Steel** is the
next tier (steel ingot/nugget/block, crafted from iron + coal per the
migrated recipes). Sieve, knapping minigame and beginner-death forgiveness
remain planned (`hearthwind-primitive` - partial, see FEATURE_PARITY).

## Seasons (replaces fabric-seasons)

Four seasons rotate every `daysPerSeason` (21) MC days: **spring, summer,
autumn, winter**. Each applies a temperature drift offset and a crop growth
multiplier (tuned in `config/hearthwind_world.json`). Hook into thirst/
temperature and crop growth is wiring next; for now `Season.fromWorldTime()`
is the source of truth (`hearthwind-world` - partial).

## Coming next

- Job-restricted recipe gating + bonus rewards (commands already live via `/job`)
- Primitive: sieve (`earlystage:sieve_drops/aged_drops.json`), full tiered affixes
- Wire seasons into crops & survival temperature; river currents & ocean swells (`ideas/rivers-and-waves.md`)
- Client companion mod: HUD bars for thirst/diet/temperature/skills/jobs
