# Guidebook parity checklist: Aged's book vs Hearthwind's game

Aged 3.1.2 ships `aged:aged_guide_book` (11 categories, 55 entries, 3
multiblock structures) in `config/paxi/resourcepacks/aged_guide_book`. Each
entry states a mechanic. This file checks every one of those claims against
Hearthwind's code (2026-09-24, from code-level fact sheets), so the guidebook
can serve as a mechanic-by-mechanic parity test.

Status: ✅ parity, 🟡 differs, ❌ missing in Hearthwind, ➖ mod not ported yet.
"Book" says where the Hearthwind guide covers it.

The Hearthwind guide (`hearthwind:hearthwind_guide_book`) only describes what
the game does today. When a ❌ row is fixed, add or extend its entry in the
same change. `custom-mods/tools/validate_guidebook.py` checks recipes, links,
items and initials mechanically.

## Start

| Aged entry | Aged says | Hearthwind | Status | Book |
|---|---|---|---|---|
| gather | sticks, rocks, flint, wild crops lie around; leaves give sticks | same; rock heaps have sizes, shovel cycles them | ✅ | first_days/gathering |
| crafting_rock | 2 rocks; hit it with a rock several times | 2 rocks; 2 hits; wears out after 80 | ✅ | first_days/crafting_rock |
| flint_tools | wood tools restricted; flint tools from rock | flint tools on crafting rock; gating is by **skills** (stone needs Mining 5), not by wood tools | 🟡 | first_days/flint_tools |
| lit_campfire | campfires unlit; light with bark or flint and steel; campfire takes bark | same | ✅ | first_days/bark_and_fire |
| early_protection | wooden shield | same | ✅ | first_days/early_protection |

## Hydration

| Aged entry | Aged says | Hearthwind | Status | Book |
|---|---|---|---|---|
| drink | sneak + hold right-click on still water; high dirty chance | same (50%, halved in rivers); uses up the source | ✅ | water/drinking_by_hand |
| campfire | put water bottles on a lit campfire to purify | same (Dehydration boil: 1000 ticks, purified bottle potion) | ✅ | water/purifying |
| campfire_cauldron | copper cauldron over a campfire boils water; bottles/flasks/buckets | a vanilla water cauldron over fire purifies **flasks only**; no campfire cauldron block | ❌ | water/purifying (flask part only) |
| copper_cauldron | collects rain, acts as purified water | **not implemented** | ❌ | (none yet) |
| bamboo_pump | pump fills bottles/flasks | **not implemented** | ❌ | (none yet) |
| furnace | smelt water bucket to purified bucket | same | ✅ | water/purifying |
| utilities | glass bottles, 5 flask tiers | same (2/3/4/5/6 sips) | ✅ | water/flasks |

## Nutrition

| Aged entry | Aged says | Hearthwind | Status | Book |
|---|---|---|---|---|
| nutrients | 5 nutrients (carbs, protein, fat, vitamins, minerals), max 300; buff at 90-100%, debuff at 0-10% | same model. Aged's own NutritionZ data gave buffs on deficiency (a bug in Aged too); fixed to match Aged's book: deficiencies mirror the bonuses | ✅ (fixed 2026-09-24) | food/nutrients |
| gathering | wild crops and trees across biomes | Let's Do wild crops ship | ✅ | first_days/gathering (brief) |
| seasons | 4 seasons, **14 days**, one day 30 minutes; crops faster/slower/none | 4 seasons, **21 days** of 20 minutes (same ticks, 504000); crops per season | 🟡 until Time & Wind is ported | seasons/* |
| spoiling | food spoils after obtained; compost it | slow random rot to rotten flesh, hot biomes x2, containers too | 🟡 different model | food/spoilage |

## Temperature

| Aged entry | Aged says | Hearthwind | Status | Book |
|---|---|---|---|---|
| body_temperature | 5 stages, effects; wet, items, armour | same model and values | ✅ | heat_and_cold/body_temperature |
| environment_temperature | biome, season, time, height, dimension, blocks | same | ✅ | heat_and_cold/the_air |
| surrounding_blocks | heat/cold blocks, radius 3, lit state | same; line of sight; no indoor bonus (Aged has none either, checked in bytecode) | ✅ | heat_and_cold/fire_and_frost |
| utilities | heating stones, ice pack, held items | same | ✅ | heat_and_cold/warm_and_cool_things |

## Armour

| Aged entry | Aged says | Hearthwind | Status | Book |
|---|---|---|---|---|
| standard / thick / thin | normal +1, thick (leather, wolf, fur-lined) x2, thin none; wanderer cools | same (+1 / +3 / 0 / -1) | ✅ | heat_and_cold/clothing |
| special | immersive armors sets and bonuses | immersive_armors ships | ✅ (not described yet) | (todo) |

## Character

| Aged entry | Aged says | Hearthwind | Status | Book |
|---|---|---|---|---|
| skills | K or 2nd tab; 12 skills; restrictions | same; 649 gates | ✅ | soul/skills |
| jobs | 3 jobs, 20 min cooldown, max 150; XP: lumberjack logs, miner ores, farmer crops **+ craft/smoke food**, warrior kills, builder **place blocks**, smither **anvil/smithing/furnace**, fisher **fishing**, brewer | 3 jobs, 1-day cooldown; XP **only** from breaking ladder blocks and killing ladder mobs | ❌ builder/fisher/smither/brewer/farmer-cooking XP | soul/jobs (describes break/kill only) |
| party | share vanilla + LevelZ XP with online members in the same dimension, +5% per player | orb XP pools at the leader and splits evenly among members in the leader's world. Aged's code never applied the +5% (display only); kept as Aged, flagged for review | ✅ (fixed 2026-09-24) | soul/party |

## Produce

| Aged entry | Aged says | Hearthwind | Status | Book |
|---|---|---|---|---|
| sieve | sieve, block list, redstone sieve | same drop table (byte-equal) | ✅ | crafts/sieve |
| ore | furnace smelts only nuggets; blast furnace for ores | same | ✅ | crafts/metals |
| leather | rotten leather, prepared hide, rack | Aged Paxi recipes restored (1 slab → 3 racks, 4 flesh + 4 bone meal, 2 hide + 2 sugar; 12000/18000 tick drying); fleshz 1.6.1 variant racks kept as extra | ✅ | crafts/leather |
| workbenches | woodcutter, stonecutter, chipped benches | same | ✅ | crafts/workbenches |
| furniture | Another Furniture | not ported | ➖ | (none) |
| amarite | amarite geode ritual | not ported | ➖ | (none) |

## Tools and weapons

| Aged entry | Aged says | Hearthwind | Status | Book |
|---|---|---|---|---|
| tiers | 6 rarities x 4 variants; reroll at anvil with tool at 100% durability + material + shard | 5 rolling rarities (unique never rolls); anvil reforge with material, 2 levels | 🟡 (no durability rule, no shard) | crafts/tool_quality |
| gems | SmitherZ gems in slots by rarity | SmitherZ not ported | ➖ | (none) |
| variants | Medieval Weapons | not ported | ➖ | (none) |

## Storage

| Aged entry | Aged says | Hearthwind | Status | Book |
|---|---|---|---|---|
| drawers / drawer_upgrades | Extended Drawers blocks and upgrades | Extended Drawers ships | ✅ (not described yet) | (todo) |
| backpacks | Inmis backpacks in a trinket slot, Health-gated | stand-in `inmis:` items in hearthwind-world; Inmis/Trinkets not ported | ➖ | (none) |

## Battle

| Aged entry | Aged says | Hearthwind | Status | Book |
|---|---|---|---|---|
| dungeonz | compass, calibrate at cartography table, portal | same | ✅ | perils/dungeons |
| blackstone_golem / the_eye / void_shadow | AdventureZ bosses | AdventureZ ported | ✅ (not described yet) | (todo, verify stats) |
| chaos_spawner | Dungeon Now Loading boss | DnL deferred | ➖ | (none) |
| ender_dragon | True Ending dragon phases | True Ending vendored | ✅ (not described yet) | (todo) |

## Archeology

| Aged entry | Aged says | Hearthwind | Status | Book |
|---|---|---|---|---|
| artifacts / fossils / suspicious / totems | Better Archeology | has an official 26.2 build, not yet added (plan 5.1) | ➖ | (none) |

## Hearthwind-only chapters (beyond Aged)

first_days/felling_trees, first_days/buckets, water/thirst, water/hydrating_food,
food/cooking, seasons/farming_the_year, crafts/steel, perils/the_wild,
perils/downed, perils/beasts, ages/the_ages.

## Fix queue (❌ rows), in order

1. ~~Diet deficiency bug~~ done.
2. Job XP sources: builder place, fisher catch, smither anvil/smithing/furnace output, farmer craft/smoke food, brewer brew.
3. ~~Party XP sharing~~ done.
4. Aged water chain: purify bottles on a lit campfire; copper (rain) cauldron; campfire cauldron; bamboo pump.
5. End Remastered eye sources (not a book entry in Aged, but the eyes are unobtainable).
6. Guide entries for Extended Drawers, Immersive Armors sets, AdventureZ bosses, True Ending.
