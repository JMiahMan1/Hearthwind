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
  - **Campfire**: For immediate shelter heating against freezing conditions and cooking raw perishables.
- **Health Skill Scaling**:
  Leveling up the **Health** skill unlocks +0.5 heart (+1.0 HP) per level:
  - Level 0: 3 Hearts (6.0 HP)
  - Level 14: 10 Hearts (20.0 HP - vanilla standard)
  - Level 30: 18 Hearts (36.0 HP - end-game powerhouse)
- **Attribute Modifiers**:
  Transient modifiers keyed `hearthwind_skills:<skill>` for Strength (attack damage), Agility (speed), Defense (armor), Mining (dig speed), and Luck.

## Survival needs (replaces Dehydration + EnvironmentZ + NutritionZ)

Vanilla Minecraft only tracks hunger. Hearthwind adds:

| System | What you see | Rules |
|---|---|---|
| **Thirst** | 10 blue teardrops in the 9-px band immediately above hunger, right-aligned to hotbar right edge, with a 13×13 glass flask icon on the left. Drinking dirty water shifts droplets from blue to murky green for the duration of the thirst debuff. | **Scale** `dehydration:hydration` 0..20. **Drain** `baseDrain 0.025` (~13m empty) ×2 sprint +0.05 per `thirst` amp. **Drink** the leather flask (+4 per sip), and **eating or drinking any catalogued food now rehydrates you**: melon slice +1, glow berries and chorus fruit +2, stews +3, apple +4, golden apple +6, milk bucket +8 - 38 foods and drinks are catalogued across 12 tiers (`config/hearthwind_survival.json`: `thirst.useHydrationCorpus`, `hydrationCorpusScale`). Empty-hand on water gives +0.5 dirty sip with a 3s cooldown and inflicts temporary `dehydration:thirst` (green teardrop HUD). |
| **Body temperature** | Bottom-anchored 16×32 authentic EnvironmentZ glass thermometer & reservoir to the right of hotbar + 12×12 "F" unit box + trend chevron | Scale -10..10. Drifts toward a biome + season target, then the environment adds: **lit** campfires, furnaces, blast furnaces, smokers and magma blocks warm you (+3 adjacent, +2 one block away, +1 two blocks), snow and ice cool you (ice -3 adjacent); at most two of any one source count. **Shelter pays** - a fire in an enclosed room gets a +50% bonus, while standing under a roof costs -1 (shade). Altitude, wetness (soaked -6 / rain -3), worn armor (+1, insulated +3, iced -4) and day/night all come from the same tuning tables. At **-8** freeze damage, at **+9** heat exhaustion & burn damage. |
| **Diet & Nutrition** | 5 nutrient groups (Fruits `#E54016`, Vegetables `#F1910C`, Grains `#F0DE1A`, Proteins `#64CA0C`, Sugars `#99916E`) in a 176×166 vanilla-grey panel (`N` key or tab) | Five segmented 140×5 bars at 24 GUI px pitch. Each group decays over time. Neglecting a group inflicts deficiency debuffs (no fruits = mining fatigue, no grains = slowness, no proteins/vegetables = weakness). Balanced diet grants regeneration & healing saturation. |
| **Food Spoilage** | Food slowly rots in inventory & containers | Perishable meats, fish, and produce rot into rotten flesh over time - twice as fast in hot biomes. Sealed teas, alcohol, and honey never spoil. |
| **Downed & Revive** | 60s bleedout state upon lethal damage | Downed players crawl and call for help; teammates can channel for 3s to revive them at 3 hearts. |

## Skills & Content Gates (LevelZ corpus, read from the datapack)

Gates are loaded from the migrated LevelZ corpus in the world datapack
(`data/levelz/**`) at server start, so the tuning is editable without a
rebuild. **676 gates are active**: 303 mining, 162 smithing, 148 crafting, 23 brewing, 16 block-use, 12 item-use, 12 entity.

- **Mining Gates**: Mud Bricks (1), Sandstone (2), Bricks (3), **Stone and Cobblestone (5)**, Diorite (6), Andesite (8), Granite (10), Terracotta (11), **Iron Ore (13)**, Deepslate (18), **Diamond (21)**, Obsidian (25), Netherite (27). Breaking a gated block shows the skill and level you need.
- **Earning your first Mining levels**: every pickaxe-mineable block is gated, so the loose **surface rocks and flint** you pick up are the tier-0 mining activity — breaking them is what raises Mining from 0. You also start with 2 skill points to spend as soon as you join.
- **Use Gates**: Furnaces, Anvils, Smithing Tables (Smithing), Brewing Stands & Cauldrons (Alchemy), Smokers & Beehives (Farming), Grindstones (Strength), Cartography Tables (Agility).
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
- Crafting is gated by **skill** levels, not jobs.
- Respects Age technology gating (e.g., Smither & Brewer unlock in Iron Age+).

## Flora, Crops, Agriculture & Wildlife (Complete Aged Parity)

- **Wild Crops & Farming (`hearthwind-flora` & Let's Do Family)**:
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
  - Per-crop seasonal growth multipliers (15 crop types loaded from `seasons/crop/*.json`).
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
- **Inventory Tab Strip**:
  - 4 tabs above vanilla `#C6C6C6` panels (Inventory Bag, Skills Tablet, Jobs Clipboard, Nutrients Apple) at 25 GUI px pitch.

## Biomes, Nether Exploration & Overworld Frontiers

- **Gardens of the Dead (Nether Overhaul)**:
  - **Soulblight Forest**: Eerie nether forest biome dense with Soulblight stems, hyphae, blightwart blocks, soulblight sprouts, and hanging/standing glowing soul spores.
  - **Whistling Woods**: Spooky nether bamboo forest featuring hollow whistling cane, red blistercrown blooms, and tall blistercrown stalks.
  - **New Wood Sets & Mosaic**: Soulblight brown wood and Whistlecane crimson bamboo planks, doors, trapdoors, fences, signs, and decorative Whistlecane Mosaics.
  - **Unique Spores & Flora**: Harvesting crops and soul spores provides brewing and composting resources.
- **Nature's Spirit (Overworld Flora & Biomes)**:
  - Diverse array of natural biomes, blooming canopies, and regional soil varieties (Kaolin clay).
  - All wild fruits, vegetables, grains, and nuts from Nature's Spirit are fully integrated with the 5-group Diet & Nutrition system.
- **YUNG's Dungeon & Fortress Overhauls**:
  - Massive architectural overhauls for Nether Fortresses, End Islands, Desert Temples, Jungle Temples, and Ocean Monuments.
- **Villages & Pillages**:
  - Witch Villages in swamp biomes with custom Jigsaw structures, villager cages, potion brewing stations, and unique loot chests.
- **Sailable Ships & Ocean Voyages (Small Ships)**:
  - Multi-tier craftable watercraft: **Cog**, **Brigg**, **Galley**, and **Drakkar**.
  - Functional shipboard cannons, mountable swivel guns, dyeable canvas sails, and onboard storage holds.

