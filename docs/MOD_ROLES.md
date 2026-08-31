# Mod roles & 26.2 compatibility

Every mod Aged 3.1.2 shipped (212 files -> 151 curated entries in
`conversion/curated/mods-manifest.json`), what it does for gameplay, our
disposition for the 26.x server rebuild, and its current 26.2 resolution
status. Regenerate statuses with
`python3 conversion/scripts/resolve_deps.py` (writes
`conversion/build/resolved.json`); this document explains the *why*.

Status snapshot (resolver run against target `26.2`):

| Disposition | Count | 26.2 status |
|---|---|---|
| keep | 50 | 34 resolve (`ok:exact`), 16 missing for target |
| rebuild (our modules/datapacks) | 15 | n/a - shipped or planned in-house |
| client-optional | 7 | packaged for the client bundle only |
| drop | 78 | not shipped (reasons below) |
| add (beyond Aged) | 1 | c2me, resolves |

Auto-added transitive dependencies (6): fabric-language-kotlin (for
fzzy-config), almanac (for letmedespawn), player-animation-library (for
better-combat), strawberrylib (for superb-steeds), modmenu +
placeholder-api (client-side, see client-optional).

---

## 1. Survival core - rebuilt in-house (15 mods -> our code)

These ARE the pack's identity. Upstream jars do not support 26.x and are
GPL/MIT code we re-implement server-authoritatively under our own
namespaces (upstream namespaces kept only where migrated datapack tuning
expects them, e.g. `dehydration:`, `levelz:` attachment ids).

| Aged mod | Role in gameplay | Replaced by |
|---|---|---|
| dehydration | Thirst bar, dirty-water risk, flask drinking | `hearthwind-survival` (hydration attachment, ThirstHud droplets, flask feature) |
| environmentz | Body temperature, hot/cold biomes, thermometer | `hearthwind-survival` (TempSyncPayload, TempHud) |
| nutritionz | Fruit/veg/grain/protein/sugar diet + deficiency debuffs | `hearthwind-survival` diet system + NutrientsScreen |
| spoiledz | Food rot in inventories, hot-biome acceleration | `hearthwind-survival` spoilage loop |
| levelz | 12 skills to 30, XP curve, attribute bonuses, gates | `hearthwind-skills` |
| jobsaddon | 8 jobs, job XP, job-gated content | `hearthwind-jobs` |
| partyaddon | Parties for job/party sharing | `hearthwind-jobs` phase 2 (deferred until multiplayer demand) |
| tiered | Weapon/tool affixes on loot | `hearthwind-primitive` (planned full affix system) |
| rpgdifficulty | Distance-based mob scaling | `hearthwind-skills`/survival rule (trivial server-side) |
| earlystage | Rocks/flint knapping start, sieve, primitive tools | `hearthwind-primitive` (rock+flint port SHIPPED) |
| fabric-seasons | 4-season calendar driving crops/temp | `hearthwind-world` seasons-lite (SeasonSyncPayload live) |
| seasonhud | Season widget HUD | `hearthwind-client` SeasonHud |
| crop-growth-modifier | Seasonal crop speed | `hearthwind-world` crop multiplier mixin |
| reciperemover | Trim vanilla recipes | migration datapack (recipe removals) |
| autotag | Tag fixes | migration datapack (tags) |

Gameplay effect: identical intent, ours is data-driven and
server-authoritative so vanilla clients can still connect (action-bar
fallbacks, no HUD).

## 2. Shipping now - 26.2-ready keeps (34)

### Structures & worldgen
| Mod | Role in gameplay |
|---|---|
| dungeons-and-taverns (+ ancient-city, pillager-outpost, stronghold overhauls) | Repaints vanilla structure loot/layout into Aged's medieval tone; exploration reward loop |
| formations / formations-overworld / formations-nether | Smaller vanilla-style structure garnish; makes surface travel interesting |
| hopo-better-mineshaft, hopo-better-underwater-ruins | Mineshafts and ocean ruins become loot destinations (ore pieces, flask ingredients) |
| sparsestructures | Thins vanilla structures so the above read as special |
| lootr | Per-player loot chests - critical for a hosted server with multiple players (no loot racing) |
| terrablender | Biome-region API the retained worldgen mods sit on |

### Combat & equipment
| Mod | Role in gameplay |
|---|---|
| better-combat (+ player-animation-library) | Combo melee swings/animates combat; makes slow-tech melee feel deliberate |
| combat-roll | Dodge roll with cooldown - survival skill expression in fights |
| immersive-armors | Craftable medieval armor sets between leather and iron |

### QOL & flavor
| Mod | Role in gameplay |
|---|---|
| appleskin | Shows hunger/saturation on hover - pairs with diet system |
| chalk | Mark caves while exploring (wayfinding without minimap) |
| crawl | Crawling through 1-block gaps - cave density feels fair |
| jump-over-fences | Vault fences - travel QOL |
| superb-steeds (+ strawberrylib) | Horse breeding/variants - realism travel tier |

### Performance & libraries
| Mod | Role |
|---|---|
| fabric-api | Platform |
| lithium, ferrite-core, letmedespawn (+ almanac), c2me (our add) | Tick/memory/worldgen performance; c2me multithreads worldgen (standard for hosted servers) |
| geckolib, resourceful-lib, resourceful-config, tcdcommons, balm, cloth-config, architectury-api, forge-config-api-port, owo-lib, fzzy-config (+ fabric-language-kotlin) | Libraries required by the mods above; invisible to players |

### Client-optional (7 + 2 auto)
EMI + emi-loot/emi-ores/emi-enchanting/emiffect/emitrades and modmenu
(+ placeholder-api): recipe/lookup HUD. Shipped in the client bundle,
never required on the server; vanilla players unaffected.

## 3. Kept but NOT yet 26.2-compatible (16) - gameplay impact

| Mod | Aged role | Max stable | Impact while absent |
|---|---|---|---|
| yungs-api + better-desert-temples/end-island/jungle-temples/nether-fortresses/ocean-monuments (6) | Replaces 6 vanilla structure families with larger, loot-rich versions | 26.1.1 | Vanilla structures ship instead: fewer/blander destinations; flask/ore-piece loot economy slightly thinner. Return automatically when YUNG publishes 26.2 |
| the-lost-castle | Big rogue-lite castle structure, mid-game combat spike | 26.1 | One fewer "dungeon boss" destination |
| medieval-buildings | Medieval village houses spawn in vanilla villages | 26.1 | Villages stay vanilla-looking; cosmetic |
| endrem | End eyes progression + End structures (Endergate) | 26.1 | Late-game End arc reduced to vanilla paths |
| herdspanic | Herd AI: animals flock/flee realistically | 1.21.1 | Hunting = vanilla passive mobs; early food slightly easier |
| antique-atlas-4 | Paper-style map item (map room without maps) | 1.21.1 | Use vanilla maps/cartography until port |
| exposure | In-game photographs | 1.21.1 | Pure flavor, no systems impact |
| modernfix | Startup/memory fixes | 26.1 | Longer boots, higher RAM; revisit at pack-boot-smoke time |
| noisium | Faster worldgen noise | 1.21.6 | Slower chunk gen; c2me covers most of it |
| log-begone | Suppresses log spam | 1.21.1 | Noisier logs only |
| kiwi | Library for some structure mods | 26.1 | No direct impact (needed only by dropped/mods) |

Watchlist: rerun `resolve_deps.py --mc <latest>` periodically; every
entry above returns automatically once authors publish 26.2 builds.

## 4. Dropped (78) and why

Full machine-readable list in `conversion/curated/mods-manifest.json`.
Grouped:

- **Ambient mobs/worldgen stuck on 1.20.1-1.21.1** (natures-spirit,
  gardens-of-the-dead, naturalist, creeper-overhaul,
  enderman-overhaul, boids + birds-boids-addon, adventurez, fleshz,
  astrocraft): worldgen-churn heavy; ports would fight our biome picks.
  Effect: fewer ambient species - acceptable, tonal loss only.
- **Structure mods superseded** (dungeon-now-loading, dungeons-plus,
  desert-dungeon, dungeonz, u-desert, underground-jungle, spider-caves,
  true-ending, vanilla-end-city-overhaul, profundis,
  lukis-grand-capitals, villages-and-pillages, mns,
  moogs-endless-structures): overlap - we keep ONE structure suite per
  niche (Yungs/DnT/formations/hopo). De-kludge: count per need goes down.
- **Let's Do family (all 12)**: food/crop ambience (bakery, brewery,
  vinery, candlelight, meadow, farm-charm, herbal-brews + do-api,
  moonlight, athena). Duplicates diet/farming identity we own;
  ~1.21.1-stuck. Farming depth comes from seasons + diet + jobs.
- **Decor/transport bloat** (another-furniture, chipped,
  barrels-bins-and-boxes, chalk-colorful-addon, connectible-chains,
  villager-transportation, small-ships, ships, niftycarts,
  immersive-aircraft, extended-drawers + addon, grass-overhaul):
  furniture/recipe-count inflation or tonal break (aircraft). Effect:
  tighter block palette, craft count stays meaningful.
- **Gear/accessory slots** (medievalweapons, amarite, inmis(+addon),
  backslot(+addon), trinkets, revive): APIs stalled pre-1.21.2;
  weapon identity moves into aged-primitive tiers + tiered affixes;
  backpacks stay vanilla (bundle rework); revive waits for co-op demand.
- **Z-series replaced** (smitherz, travelerz, libz): smithing upgrades
  fold into primitive/skills; travelerz covered by vanilla locator bar.
- **Obsolete infrastructure** (paxi, async-locator, memoryleakfix,
  DEUF_Refabricated, arrp, MRU, OctoLib,
  supermartijn642configlib/corelib, lavender, surveyor, time-and-wind):
  we ship the migrated datapack natively, run 26.x, or the dependent
  mod is dropped.

Net effect on players: same survival arc, tighter content set, one
system per need (one sieve, one food pipeline, one structure suite per
tier), and any mod above can return untouched once a 26.2 build exists.
