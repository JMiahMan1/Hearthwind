# How Hearthwind differs from vanilla Minecraft

Player-facing summary of everything Hearthwind changes or adds on top of
vanilla. **Keep this updated with every gameplay commit** - it is the
contract of what the pack does. Grown from the Aged fork; server-side only.

## Progression & Starting Health (replaces LevelZ Health progression)

- **Starting Health (3 Hearts / 6.0 Max HP)**:
  Players begin their journey with only **3 Hearts (6.0 HP)**. Surviving the early game requires caution and preparation.
- **Starter Kit & Guidebook**:
  On first world spawn, every player is automatically granted:
  - **Hearthwind Survival Guide**: An in-game written book containing comprehensive survival rules, temperature guides, nutrient details, Age 0 rock gathering, skill gates, and job instructions. (Use `/guide` or `/guidebook` anytime to receive a replacement copy).
  - **Glass Bottle**: For collecting water and managing early hydration.
  - **Campfire**: For immediate shelter heating against freezing conditions and cooking raw perishables. Campfires **place unlit** - strike them yourself.
- **Health Skill Scaling**:
  Leveling up the **Health** skill unlocks +0.5 heart (+1.0 HP) per level:
  - Level 0: 3 Hearts (6.0 HP)
  - Level 14: 10 Hearts (20.0 HP - vanilla standard)
  - Level 30: 18 Hearts (36.0 HP - end-game powerhouse)
- **Attribute Modifiers**:
  Transient modifiers keyed `hearthwind_skills:<skill>` for Strength (attack damage), Agility (speed), Defense (armor), Mining (dig speed), and Luck.
- **Ages advance via advancements**: the `hearthwind:age/age0..5` chain is awarded automatically when its criteria are met (Age 0 starts with rock+flint in inventory; each later Age builds on the previous milestone). Age 5 (Mechanical) also requires **smithing 20 + builder job 3** before the advancement can complete - craft a rail once the gates are open (`PlayerAdvancementTracker`). `/job age` remains a debug override.
- **Beginner death forgiveness**: your first `beginnerDeathCount` lethal deaths (default 3, `config/hearthwind_primitive.json`) keep your inventory. The counter **resets when you finish eating food** or **sleep in a bed**. After the budget is spent, deaths drop items normally.
- **Wooden shield (earlystage)**: off-hand wooden shield with vanilla-style `BLOCKS_ATTACKS` (90% reduction, 3 damage threshold, durability cost). Axes (and other disable-blocking attackers) put it on a **100-tick** disable cooldown.

## Survival needs (replaces Dehydration + EnvironmentZ + NutritionZ)

Vanilla Minecraft only tracks hunger. Hearthwind adds:

| System | What you see | Rules |
|---|---|---|
| **Thirst** | 10 blue teardrops in the 9-px band immediately above hunger, right-aligned to hotbar right edge, with a 13×13 glass flask icon on the left. Drinking dirty water shifts droplets from blue to murky green for the duration of the thirst debuff. | **Scale** `dehydration:hydration` 0..20. **Drain** `baseDrain 0.025` (~13m empty) ×2 sprint +0.05 per `thirst` amp. **Drink** the leather flask (+4 per sip), and **eating or drinking any catalogued food now rehydrates you**: melon slice +1, glow berries and chorus fruit +2, stews +3, apple +4, golden apple +6, milk bucket +8 - 38 foods and drinks are catalogued across 12 tiers (`config/hearthwind_survival.json`: `thirst.useHydrationCorpus`, `hydrationCorpusScale`). Empty-hand on still water while sneaking: hold right-click ~4s to drink +1 with gulp sounds, may inflict temporary `dehydration:thirst` (green teardrop HUD, halved chance in rivers), and consumes the source block. **Purified water** (`dehydration:purified_water` fluid/block/bucket — smelt a water bucket to get one) never inflicts thirst. |
| **Body temperature** | Bottom-anchored 13×13 body-status icon at (screen centre - 7, bottom - 52) + 16×32 glass thermometer at (centre + 95, bottom - 32); no unit box or trend arrow | Faithful EnvironmentZ 2.0.8 port (data values mirror the version Aged 3.1.2 ships). **Body temperature** is an integer **-2400..+2400** recomputed once per 10 ticks: bands -2400 very cold / -1800 freezing / -240 comfortable / +240 hot / +1800 overheating / +2400. **Thermometer** reads a separate -6..+6 value driven only by climate, shade, height and nearby heat. **Seasons** shift both body and thermometer (winter -3, summer +2, spring +0.5, autumn 0; `daysPerSeason = 18`). **Heat sources** (lava 4/3/2/1, campfire, soul campfire, fire, soul fire, magma, lava cauldron, lit furnace/blast furnace/smoker: 3/2/1/0; torch/soul torch 1 at 0) only count within 3 blocks with a clear line of sight, at most `max_count` of each; snow/ice cool -3/-2/-1. **Worn armor** +1/piece, insulated (polar bear fur, wolf/leather) +3/piece, iced chainmail-style armor -5 and wears off one charge per calculation; **warm armor +3 takes precedence** over its +1. **Wetness 0..200**: water +100, rain +1, dries -1 per calculation, soaked (≥180) costs -6 and any wet costs -3. **Shade** (no sky) -1. **Height**: +2 below y=0, +1 below y=30, 0 up to y=120, -1 above, -2 above y=190. **Acclimatization** pulls you back to comfort (±10/±15 at ±180/±1600), and cold/heat resistance+protection pools (max 600, from items and effects) soak the incoming delta before your body changes. **Debuffs** by band: cold -8% speed, freezing -25% speed / -20% attack speed, hot -12% attack damage, overheating -30% attack damage / -20% attack speed. At **-2400** you take 1 freezing damage; at **+2400** you gain 0.07 exhaustion. |
| **Diet & Nutrition** | Five nutrients - **Carbs, Protein, Fat, Vitamins, Minerals** (NutritionZ 1.0.11 parity) - in the original 176×142 nutrients panel on the `N` key or the 9×9 inventory tab; **hold Shift** on any catalogued food for its nutrition tooltip | Each nutrient is an integer `0..300` starting at 150. Eating/drinking adds the item's positive values from the full Aged NutritionZ corpus (vanilla + bakery/brewery/candlelight/farm_and_charm/herbalbrews/meadow/vinery/nethervinery/dehydration/naturalist/natures_spirit/adventurez compat). Losing a hunger point to exhaustion decays all five by 1. At **≤30** the datapack's negative effects fire, at **≥270** the positive ones (long status effects + attribute modifiers): low vitamins = Weakness, high vitamins = Regeneration, high carbs = +attack/move speed, high protein = +damage/knockback, high fat = +armor, high minerals = Haste. Hover the left/right ends of a bar to preview its effects. |
| **Food Spoilage** | Food slowly rots in inventory & containers | Perishable meats, fish, and produce rot into rotten flesh over time - twice as fast in hot biomes. Sealed teas, alcohol, and honey never spoil. **Tanning**: 4 rotten flesh craft into 1 leather. |
| **Sobriety** (default ON) | No beer, wine, whiskey, or mead anywhere: no recipes, no creative-tab entries | `config/hearthwind_survival.json`: `sobriety.removeAlcohol` (default `true`). Alcohol content lives in `vinery_alcohol` / `brewery_alcohol` built-in packs that only load when the flag is off. Sober replacements always brew: vinery grape juices + apple juice + honey cordial + apple cider; brewery **sassafras root beer** (sugar + sweet berries + hops, hearty), **small beer** (wheat + hops, restorative), **kvass** (bread + sugar + yeast, restorative), **coffee** (cocoa + sugar + grain, energizing). HerbalBrews teas/coffees are naturally alcohol-free. |
| **Downed & Revive** | 60s bleedout state upon lethal damage | Downed players crawl and call for help; teammates can channel for 3s to revive them at 3 hearts. |

## Skills & Content Gates (LevelZ corpus, read from the datapack)

Gates are loaded from the migrated LevelZ corpus in the world datapack
(`data/levelz/**`) at server start, so the tuning is editable without a
rebuild. **676 gates are active**: 303 mining, 162 smithing, 148 crafting, 23 brewing, 16 block-use, 12 item-use, 12 entity.

- **Mining Gates**: Mud Bricks (1), Sandstone (2), Bricks (3), **Stone and Cobblestone (5)**, Diorite (6), Andesite (8), Granite (10), Terracotta (11), **Iron Ore (13)**, Deepslate (18), **Diamond (21)**, Obsidian (25), Netherite (27). Breaking a gated block shows the skill and level you need.
- **Earning your first Mining levels**: every pickaxe-mineable block is gated, so the loose **surface rocks and flint** you pick up are the tier-0 mining activity — breaking them is what raises Mining from 0. You also start with 2 skill points to spend as soon as you join.
- **Surface rock & flint spawning (earlystage parity)**: mounds generate only in forest/hill/mountain/river biomes plus mushroom fields and stony shores, on bare dirt or stone with open sky — not in oceans, deserts, or on grass. Look for bare-dirt patches, not open plains.
- **Use Gates**: Furnaces, Anvils, Smithing Tables (Smithing), Brewing Stands & Cauldrons (Alchemy), Smokers & Beehives (Farming), Grindstones (Strength), Cartography Tables (Agility).
- **Crafting Gates**: gated results never appear in the crafting output (golden pickaxe needs Mining 8, diamond armor needs Defense 24) - denied crafts show the skill and level you need.
- **Item & Entity Gates**: Certain items need a skill level to use, and breeding/taming livestock is gated behind Farming and Agility.

### Skill capstones (LevelZ procs)

Mastering a skill past the level curve unlocks a passive perk. Chances and bonuses are configurable in `config/hearthwind_skills.json` (`procs.*`); unlocked at the maximum skill level (30):

- **Luck — Critical strikes**: every luck level adds 1% crit chance; a crit deals +20% damage.
- **Strength — Double damage**: at max level, 3% of melee hits deal double.
- **Agility — Evasion**: at max level, 10% of incoming attacks miss entirely. Agility also soaks 0.25 fall damage per level.
- **Defense — Retribution**: at max level, 5% of hits reflect their damage back at whatever attacked you.
- **Luck — Cheat death**: at max level, 50% of otherwise-lethal hits leave you on 1 HP with Regeneration and Absorption instead.
- **Farming — Twins**: at max level, 20% of animal breedings produce two babies instead of one.

## Jobs (replaces jobs-addon)

Eight optional professions (Miner, Farmer, Fisher, Warrior, Smither, Brewer, Builder, Lumberjack):
- Join and manage via `/job join <job>`, `/job leave`, `/job info`.
- Earn XP by performing trade-specific tasks following the job ladder.
- Crafting is gated by **skill** levels always; per-job recipe gating is opt-in (`jobCraftGating`, default off).
- Respects Age technology gating: **Smither & Brewer unlock at Copper Age (Age 2)** - joining earlier fails, and their level-up bonus items are withheld until Age 2. Full quest/Age design: `docs/QUESTS_AND_AGES.md` (no quest mods by design; guide book + gates instead).

## Flora, Crops, Agriculture & Wildlife (Complete Aged Parity)

- **Wild Crops & Farming (Let's Do Family, `letsdo-*`)**:
  - **Farm & Charm**: Wild Barley, Wild Corn, Wild Strawberries, Wild Onions, Wild Garlic scattered in Overworld biomes. Harvesting yields seeds and produce for flour, dough, oatmeal, ribs, and soup. Crafting stations (Silo, Roaster, Butter Churn, Plow, Supply Cart).
  - **Vinery**: Grape varieties (Red, White, Taiga, Savanna, Jungle) and seeds for wine making. Fermentation Barrels, Grapevine Pots, Apple Press, and Dark Cherry wood.
  - **Candlelight**: Tomatoes, Lettuce, Broccoli, and seeds. Cooking Pan and Pot for multi-ingredient meals (pasta, lasagna, beef tartare).
  - **Meadow**: High-altitude alpine wildflowers (Edelweiss, Alpine Poppy, Gentian, Delphinium, Fire Lily, Saxifrage, Eriophorum). Cheese making with Wooden Cauldrons, Cheese Forms, and Aging Racks.
  - **HerbalBrews**: Wild Herbs (Lavender, Wild Coffee, Wild Yerba Mate, Wild Rooibos, Hibiscus) and tea leaves for brewed hot and cold beverages in Tea Kettles.
  - **Brewery**: Wild Hops, Hops Seeds, and grains brewed into beers, whiskey, and vodka in Brew Kettles and Beer Barrels.
  - **Nether Vinery**: Crimson and Warped nether grapes and ghast wine brewing.
- **Leaves & Early-Game Stick Foraging**:
  - Punching or right-clicking leaves with an empty hand forages sticks (60% chance with 1s cooldown).
  - Breaking leaves with bare hands or tools drops sticks reliably (50% drop rate).
- **Passable Foliage (walk through leaves)**:
  - Leaves have no collision: players, mobs and pathfinding treat every `#minecraft:leaves` block as open air, so you can walk, fall and jump straight through canopies.
  - Pushing through leaves slows you slightly (90% speed each way by default), cushions long falls (no damage for the first 20 blocks, half damage after), and rustles leaf sounds as you move.
  - **Leaf Walker** boot enchantment (enchanted-book + leaves shapeless recipe): wearers can stand on top of leaves again instead of sinking through.
  - Tunables in `config/passablefoliage.json` (speed/fall-damage multipliers, player-only mode, head-hitter mode that blocks entry from below).
- **Wildlife & Fauna Dynamics (`hearthwind-world`)**:
  - **Superb Steeds**: Multi-tier steed breeds, pack mules, draft horses, donkeys, and functional carts.
  - **Waterfowl**: Ducks and waterfowl inhabit rivers, swamps, and shorelines; can be fed seeds and kelp for feathers and breeding.
  - **Aquatic Life & Ice Spawning**: Bass and catfish spawn across all rivers, oceans, and lakes, including frozen waters directly beneath ice, frosted ice, and packed ice sheets.
  - **Herd Panic**: Attacking one animal causes nearby herd members to panic and stampede together.
  - **Villager Leashing**: Villagers can be attached to leads for organized relocation and transport.

## World, Seasons & Water Dynamics

- **18-Day Seasons (SeasonHUD parity)**:
  - Season duration set to **18 days** (`daysPerSeason = 18`).
  - Top-left HUD widget at GUI `(2, 2)` displaying 9×9 season icon + single-line formatted text: `"[Icon] Season, Day N/18"`.
  - Season text tinting: Spring `#FFA3BB`, Summer `#FEE92A`, Autumn `#BC5E27`, Winter `#E0FCFC`.
  - Per-crop seasonal growth multipliers (37 crop types loaded from `seasons/crop/*.json`: 15 vanilla + tomato/lettuce/strawberry/corn/onion/oats/barley, tea/coffee/rooibos/yerba-mate, all grape bushes, hops — frost-tender crops stall in winter, hardy oats/barley creep, grapes peak in fall).
- **River Currents & Ocean Swell**:
  - Gentle downhill river flow and oceanic tidal wave swell.
  - Directional splash, surface bubbles, and bubble pops (`ParticleTypes.SPLASH` / `ParticleTypes.BUBBLE_POP` / `ParticleTypes.BUBBLE`) visibly stream in the direction of the water current.
- **Winter Snow Layering**:
  - Gradual multi-layer snow accumulation during winter on ground and leaves.

## Visual Inventory & UI Look & Feel

- **Main Menu (Aged 3.1.2 Parity)**:
  - Custom HearthWind panoramic start screen with cabin and autumnal breeze.
  - Left-aligned button stack at `x = width / 9` with authentic hover tints (`#EEDAC3`, `#A1B8B5`, `#6AA7BA`, `#BFA8BF`, `#EB9484`).
  - Top-right 20×20 icon buttons (Discord, Modrinth, Language, Accessibility).
- **Inventory Tab Strip (Aged 3.1.2 parity)**:
  - The four LibZ tabs (Inventory Bag / Skills / Jobs / Party) sit above every
    aged panel at the modpack's exact geometry: 25 GUI px pitch, 24 px wide
    raised-selected / lowered-unselected backgrounds from the bundled LibZ
    sheet, vanilla-item tab icons, hover tooltips. Clicking a tab switches
    screens; the bag tab returns to the vanilla inventory.
- **Skills screen (`K`)**:
  - Rebuilt to the Aged LevelZ layout: 200x215 panel, "&lt;Name&gt; Skills"
    title, live player model preview, six attribute readouts (health, defense,
    agility, strength, stamina, luck) in a 3x2 grid, "Level N / Points N"
    line, segmented XP bar with "Xp n / next" (Aged 25 + 1.6L curve), a "?"
    help page, and twelve skill rows (two columns of six) with [+] buttons
    that spend one experience level per press.
- **Jobs screen (`J`)**:
  - Rebuilt to the Aged JobsAddon layout: 200x215 panel, "&lt;Name&gt; Jobs"
    title, "Job Cooldown: MM:SS" and employed summary, then eight job cards
    (icon slot, name, centred "Lv. N", segmented XP bar). **You may now hold
    up to 3 jobs at once** (Aged `employedJobs`), job XP is kept per job even
    after leaving, and changing jobs starts the Aged **20-minute change
    cooldown** (24000 ticks). Joining/leaving uses the same `/job` command
    path as before.
- **Party screen (`P`)**:
  - Rebuilt on the shared Aged panel: left column lists party members with
    player heads, distance and health bars; right column shows party name,
    member count, role, PvP state and the PvP toggle / Leave / Disband
    buttons. Invitations are not modelled by our party subsystem, so
    PartyAddon's invitation column is intentionally omitted.

## Biomes, Nether Exploration & Overworld Frontiers

- **Gardens of the Dead (Nether Overhaul)**:
  - **Soulblight Forest**: Eerie nether forest biome dense with Soulblight stems, hyphae, blightwart blocks, soulblight sprouts, and hanging/standing glowing soul spores.
  - **Whistling Woods**: Spooky nether bamboo forest featuring hollow whistling cane, red blistercrown blooms, and tall blistercrown stalks.
  - **New Wood Sets & Mosaic**: Soulblight brown wood and Whistlecane crimson bamboo planks, doors, trapdoors, fences, signs, and decorative Whistlecane Mosaics.
  - **Unique Spores & Flora**: Harvesting crops and soul spores provides brewing and composting resources.
- **Nature's Spirit (Overworld Flora & Biomes)**:
  - Diverse array of natural biomes, blooming canopies, and regional soil varieties (Kaolin clay).
  - All wild fruits, vegetables, grains, and nuts from Nature's Spirit ship with NutritionZ Carbs/Protein/Fat/Vitamins/Minerals values.
- **YUNG's Dungeon & Fortress Overhauls**:
  - Massive architectural overhauls for Nether Fortresses, End Islands, Desert Temples, Jungle Temples, and Ocean Monuments.
- **Villages & Pillages**:
  - Witch Villages in swamp biomes with custom Jigsaw structures, villager cages, potion brewing stations, and unique loot chests.
- **Sailable Ships & Ocean Voyages (Small Ships)**:
  - Multi-tier craftable watercraft: **Cog**, **Brigg**, **Galley**, and **Drakkar**.
  - Functional shipboard cannons, mountable swivel guns, dyeable canvas sails, and onboard storage holds.

### 26.2 Port Status
- Verified 2026-09-11: `./gradlew build` green; server gametests **257/257 passed** (container); client gametests **PASS** (container, 14 screenshots).
- `letsdo-*` family builds green in-tree (candlelight/meadow API errors resolved since the last audit).
  - Wave-1 contrib ports (2026-09-12): Athena done (0 compile errors, all 39 classes + CTM rendering complete, `settings.gradle` wired, ready for deploy). Chipped done (0 compile errors, full build green, boot-smoke verified: server boots to `Done`, RCON responds).
  - Local Aged-dep ports (2026-09-22): lavender, logbegone, pockets, couplings, entitycollisionfpsfix, memoryleakfix, async-locator, passable-foliage — build green, container gametests **296/296**, vendored, deployed to all three Prism instances (`update_prism.sh` + `verify_prism`). Commit `ba4677096`.
  - Remaining for complete Aged parity: grouped table in `docs/NOT_IMPLEMENTED.md`.
- Mechanical Age (Create wind/water wheels, smithing 18 / builder 3 preview): not started - Create is not in the pack yet, so there is nothing to gate.

