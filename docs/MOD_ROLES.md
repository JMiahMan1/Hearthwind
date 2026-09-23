# Mod roles & 26.2 compatibility

Every mod Aged 3.1.2 shipped (212 files -> 151 curated entries in
`conversion/curated/mods-manifest.json`), what it does for gameplay, our
disposition for the 26.x server rebuild, and its current 26.2 resolution
status. Regenerate statuses with
`python3 conversion/scripts/resolve_deps.py` (writes
`conversion/build/resolved.json`); this document explains the *why*.

Status snapshot (resolver run against target `26.2`, 2026-09-22 —
`conversion/build/readiness-report.json`: ready 57 / total 138, missing 66):

| Disposition | Count | 26.2 status |
|---|---|---|
| keep | 122 (manifest) | Modrinth resolves 57; **28 of the 66 "missing" are already solved** (vendored jar or `custom-mods` port). True open set for parity: see `docs/NOT_IMPLEMENTED.md`. |
| rebuild (our modules/datapacks) | 28 (manifest groups + entries) | shipped or planned in-house (`hearthwind-*`, `letsdo-*`, datapack) |
| client-optional | 7 | packaged for the client bundle only (EMI family still missing upstream 26.2) |
| add (beyond Aged) | 1 | c2me, resolves |

Local solutions the resolver does not see (still Modrinth-max ≤1.21.x):
lavender, logbegone, pockets, couplings, entitycollisionfpsfix,
memoryleakfix, async-locator, passable-foliage, athena, chipped, exposure,
dungeonz, smallships, villagesandpillages, YUNG×6, gardens-of-the-dead,
natures-spirit, tlc, medieval-buildings, true-ending, birds-boids, kiwi,
arrp. In-house: endrem eyes, HerdPanic.

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

## 3. Kept, still not on Modrinth 26.2 — but often already local

Resolver `missing` is **not** the same as "not in the pack." As of
2026-09-22, 28 of 66 misses are vendored or ported under `custom-mods/`.
Only the second table is genuine remaining port work for Aged parity.

### Already solved locally (resolver still lists them missing)

| Mod(s) | How we ship it |
|---|---|
| YUNG api + 5 overhauls (6) | `conversion/vendored/Yungs*-26.2-*.jar` |
| the-lost-castle, medieval-buildings, true-ending, birds-boids | vendored 26.2 jars |
| natures-spirit, gardens-of-the-dead | vendored 26.2 jars |
| athena, chipped, exposure, dungeonz, smallships, villagesandpillages | `custom-mods/*` ports, wired in `settings.gradle` |
| lavender, logbegone, pockets, couplings, entitycollisionfpsfix, memoryleakfix, async-locator, passable-foliage | `custom-mods/*` ports + Prism deploy |
| kiwi, arrp | vendored (needed as libs) |
| endrem, herdspanic | in-house: `hearthwind-world` `endrem/` + `HerdPanic` |
| DEUF / entity-collision-fps-fix | superseded by `entitycollisionfpsfix` port |

### True remaining (port / rebuild / watchlist)

See the full grouped table in **`docs/NOT_IMPLEMENTED.md`**. Highlights:

| Cluster | Mods | Impact while absent |
|---|---|---|
| Structures wave | dungeon-now-loading **DEFER** (177-class); yungs-better-desert-temples waitlist | destinations already: YUNG/DungeonZ + UndergroundWorlds/Dungeons+/Moogs/Profundis/lukis + desert-dungeon/unnamed-desert/better-end-cities |
| Mobs / adventure | adventurez, fleshz, creeper-overhaul, enderman-overhaul | fewer ambient/boss variants (astrocraft **held**) |
| Accessory slots | trinkets + inmis + backslot cluster | vanilla bundles only; port as ONE best |
| Gear / smith | medievalweapons, amarite, smitherz/libz, travelerz | weapon identity lives in tiered affixes |
| Furniture / transport | another-furniture, grass-overhaul, niftycarts, villager-transportation | tighter palette; villager transport may already be partially in world |
| Exploration | antique-atlas-4 | interim: vanilla explorer maps |
| Surveyor | `surveyor` | TEMP-OFF in `settings.gradle` (~324 compile errors) |
| Perf / libs | modernfix, noisium, moonlight | c2me/lithium cover most of it today |
| Client-optional | EMI family (6) | client bundle only; never server-required |

Watchlist: rerun `resolve_deps.py --mc <latest>` periodically; any
upstream 26.2 publish returns automatically.

## 4. Dropped (78) and why

Full machine-readable list in `conversion/curated/mods-manifest.json`.
Grouped:

- **Ambient mobs/worldgen stuck on 1.20.1-1.21.1** (natures-spirit,
  gardens-of-the-dead, naturalist, creeper-overhaul,
  enderman-overhaul, boids + birds-boids-addon, adventurez, fleshz,
  astrocraft): worldgen-churn heavy; ports would fight our biome picks.
  Effect: fewer ambient species - acceptable, tonal loss only.
- **Structure mods superseded / adopted** (spider-caves +
  underground-jungle → UndergroundWorlds; dungeons-plus / mns / mes /
  profundis / lukis / desert-dungeon / unnamed-desert(u_desert) /
  betterendcitiesvanilla adopted or ported 2026-09-22; dungeon-now-loading
  **DEFER** (177-class yarn, empty source); true-ending,
  villages-and-pillages, moogs-endless): overlap - we keep ONE
  structure suite per niche (Yungs/DnT/UndergroundWorlds/Moogs/Profundis
  + data addons). De-kludge: count per need goes down.
- **Let's Do family (all 12)**: food/crop ambience (bakery, brewery,
  vinery, candlelight, meadow, farm-charm, herbal-brews + do-api,
  moonlight). Duplicates diet/farming identity we own;
  ~1.21.1-stuck. Farming depth comes from seasons + diet + jobs.
  **athena**: DONE (port compiles, boots); no longer blocks.
- **Decor/transport bloat** (another-furniture, barrels-bins-and-boxes,
  chalk-colorful-addon, connectible-chains, villager-transportation,
  immersive-aircraft, extended-drawers + addon, grass-overhaul):
  furniture/recipe-count inflation or tonal break (aircraft). Effect:
  tighter block palette, craft count stays meaningful.
- **Gear/accessory slots** (medievalweapons, amarite, inmis(+addon),
  backslot(+addon), trinkets, revive): APIs stalled pre-1.21.2;
  weapon identity moves into aged-primitive tiers + tiered affixes;
  backpacks stay vanilla (bundle rework); revive waits for co-op demand.
- **Z-series** (smitherz, travelerz, libz): not yet ported — smithing upgrades
  fold into primitive/skills; travelerz covered by vanilla locator bar.
  Still on the Wave-3 rebuild-or-fork list (`NOT_IMPLEMENTED.md`).
- **Infrastructure handled in-house or already ported**: paxi → native
  world datapack; time-and-wind → vanilla cycle; lavender / logbegone /
  pockets / couplings / async-locator / memoryleakfix / DEUF → local
  `custom-mods` ports (2026-09-22); arrp / supermartijn libs / kiwi →
  vendored; surveyor → TEMP-OFF WIP; MRU / OctoLib → only if a
  dependent returns.

Net effect on players: same survival arc, tighter content set, one
system per need (one sieve, one food pipeline, one structure suite per
tier), and any mod above can return untouched once a 26.2 build exists.
