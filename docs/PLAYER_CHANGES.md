# How Hearthwind differs from vanilla Minecraft

Player-facing summary of everything Hearthwind changes or adds on top of
vanilla. **Keep this updated with every gameplay commit** - it is the
contract of what the pack does. Grown from the Aged fork; server-side only.

## Survival needs (replaces Dehydration + Environmentz)

Vanilla Minecraft only tracks hunger. Hearthwind adds:

| System | What you see | Rules |
|---|---|---|
| **Thirst** | Overlay warnings ("You are getting thirsty") | Drains constantly, 2x while sprinting; below 3/20 bars natural health regen stops; at zero you take starvation-style damage. Drink from water bowls, purify water to avoid the thirst effect. |
| **Body temperature** | "You feel very cold/hot", freeze/heat damage | Drifts toward your biome's climate (deserts scorch, snowy peaks freeze). Wolf & wanderer armor, insulation items and ice packs bias you toward comfort; rain and deep underground shift it too. |
| **Diet** | Deficiency debuffs / bonus hearts | Five food groups (fruit, vegetables, grains, proteins, sugars) tracked separately; each decays over hours. Neglect a group and you suffer its debuff (e.g., no grains = slowness). Keep ALL groups above half for bonus absorption hearts. |
| **Food spoilage** | Food slowly rots in your inventory | Perishable meats/fish/produce randomly rot into rotten flesh over time - faster in hot biomes. Honey, teas and wines never spoil. Cooked food still spoils; plan expeditions accordingly. |

All numbers live in `config/hearthwind_survival.json` - server admins can retune
everything without updates (diet groups themselves are datapack tags under `nutritionz:`).

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
