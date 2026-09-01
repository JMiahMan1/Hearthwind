# Mods status and roadmap from Aged 3.1.2 to Hearthwind (26.2)

**Policy: NO mod is ever dropped.** Every mod from Aged 3.1.2 is preserved through native rebuilds in `custom-mods/`, standalone 26.2 module ports, or active upstream 26.2 releases.

## Recently Shipped & Implemented (100% Verified on 26.2)

1. **Small Ships (`small-ships` / `ships`)**:
   - Standalone 26.2 Fabric port in [`custom-mods/smallships`](file:///Users/jeremiahsummers/Code/Hearthwind/custom-mods/smallships).
   - Fully functional Cogs, Brigs, Galleys, and Drakkars with shipboard cannons, mountable swivel guns, dyeable canvas sails, and container holds.
2. **Villages and Pillages (`villagesandpillages`)**:
   - Standalone 26.2 Fabric port in [`custom-mods/villagesandpillages`](file:///Users/jeremiahsummers/Code/Hearthwind/custom-mods/villagesandpillages).
   - Swamp Witch Villages with custom Jigsaw placement, structure processors, villager cages, and loot tables.
3. **Gardens of the Dead (`gardens-of-the-dead`)**:
   - Fully ported to Fabric 26.2 in `contrib/gardens-of-the-dead/`, vendored.
   - Soulblight Forest & Whistling Woods with native carvers, feature selectors, and TerraBlender surface rules.
4. **Nature's Spirit (`natures_spirit`)**:
   - Modern 26.2 release integrated and vendored. 60+ biomes, 18 wood sets, Kaolin clay, wild edibles integrated with 5-group nutrition.
5. **YUNG Suite (6 Mods)**:
   - `yungs-api`, `better-nether-fortresses`, `better-end-island`, `better-desert-temples`, `better-jungle-temples`, `better-ocean-monuments` on 26.2.
6. **Additional 26.x Native & Ported Mods**:
   - `the-lost-castle` (`tlc`), `medieval-buildings`, `birds-boids` & `boids`, `extended-drawers`, `scholar`, `chalk-colorful-addon`, `true-ending`, `supermartijn642configlib` & `supermartijn642corelib`.
7. **In-House Replacements (`custom-mods/`)**:
   - `hearthwind-survival`: Replaces `dehydration`, `environmentz`, `nutritionz`, `spoiledz`, and `revive` (teardrop thirst HUD, dirty water effect, authentic glass thermometer, 5 nutrient groups, spoilage, downed/revive).
   - `hearthwind-skills`: Replaces `levelz`, `rpgdifficulty` (12 skills, 3-heart start, 676+ gates, triangular curves, mob scaling).
   - `hearthwind-jobs`: Replaces `jobs-addon` (8 jobs, `/job` commands, job ladders, Age gating).
   - `hearthwind-primitive`: Replaces `earlystage`, `tiered`, `reciperemover` (sieve, knapping, surface rock/flint, equipment affixes).
   - `hearthwind-world`: Replaces `fabric-seasons`, `seasonhud`, `crop_growth_modifier`, `endrem`, `herdspanic`, `villager-transportation`.
   - `hearthwind-flora`: In-house implementation of the Let's Do agriculture suite (Farm & Charm, Vinery, Candlelight, Meadow, HerbalBrews, Brewery, Nether Vinery crops, blocks, and recipes).

---

## Active Watchlist & Upcoming Ports from Aged

The remaining upstream mods from Aged are tracked for 26.2 inclusion as ports or upstream releases become available:

### 1. High-Value Content & Immersion
- `creeperoverhaul` & `endermanoverhaul`: Biome-specific mob variants.
- `another_furniture`: Medieval home furniture (tables, chairs, lamps, curtains).
- `medievalweapons`: Daggers, spears, polearms, halberds (integrated with Better Combat).
- `lukis-grand-capitals` & `moogs-endless-structures`: Additional structure sets.
- `dungeon-now-loading`, `dungeons-plus`, `desert-dungeon`, `dungeonz`, `u-desert`, `underground-jungle`, `spider-caves`, `profundis`, `adventurez`: Dungeon structures and boss encounters.

### 2. Accessories & Specialized Utilities
- `trinkets`, `backslot`, `inmis`: Accessory and backpack systems.
- `antique-atlas`: In-game antique cartography atlas.
- `exposure`: Photography and image development.
- `chipped`: Decorative stone/wood variant crafting.
- `connectible-chains`, `barrels-bins-and-boxes`, `sushi-bar`, `grass-overhaul`, `fleshz`, `amarite`, `smitherz`, `travelerz`, `astrocraft`, `time-and-wind`.

---

## Pack Health
- **Gametests Passing**: **204 / 204 (100% Green)**
- **Dev Server Status**: **Done** on 26.2, RCON verified.

