# Feature parity matrix - Hearthwind (26.2 rebuild) vs original Aged 1.20.1 pack

Living document. Status per system the original pack shipped; goal is
parity first, deliberate improvement where noted. UPDATE WITH EVERY
GAMEPLAY COMMIT.

Legend: ✅ done · 🟡 partial · ❌ missing · ➖ deliberately not carried
over (with reason)

## Rebuild-class systems (custom mods replacing original mods)

| Original mod | System | Corpus / config | Status | Notes / improvement |
|---|---|---|---|---|
| dehydration | Thirst | `data/dehydration`, `config/hearthwind_survival.json` | ✅ | Hydration attachment `dehydration:hydration`, sprint/effect drain, 10 HUD droplets + flask icon, food hydration corpus (43 foods across 12 tiers) |
| environmentz | Temperature | `data/environmentz`, same config | ✅ | Biome drift, season offsets, heat/cold blocks, shelter bonus (+50%), insulation items, vertical thermometer HUD |
| nutritionz | Diet (5 groups) | `nutritionz:*` item tags, same config | ✅ | 5 nutrient groups (fruits, vegetables, grains, proteins, sugars), decay, deficiency debuffs, balanced bonus hearts, NutrientsScreen (`N` key / tab) |
| spoiledz | Spoilage | `spoiledz:perishable_items` + `non_spoiling_items`, same config | ✅ | Inventory & container food spoilage with hot-biome multiplier and non-spoiling exemptions |
| levelz | Skills x12 | `data/levelz` ~400 files → `data/hearthwind_skills/gates` | ✅ | 12 skills, 3-heart start progression (+0.5 heart/level), 750+ break/use/craft gates, triangular XP curves, skill capstones/procs |
| rpg-difficulty | Distance mob scaling | `config/hearthwind_skills.json` mobScaling | ✅ | Distance mob scaling past grace radius, capped scaling |
| jobs-addon | Jobs x8 | `data/jobsaddon` → `data/hearthwind_jobs` (8 jobs), `config/hearthwind_jobs.json` | ✅ | 8 jobs (miner, farmer, fisher, warrior, smither, brewer, builder, lumberjack), `/job join/leave/info` commands, job ladders, Age gating |
| party-addon | Parties & Shared XP | `PartyManager`, `PartyCommand` | ✅ | `/party create/invite/accept/leave` commands, party shared XP range distribution |
| earlystage | Primitive start | `data/earlystage` (sieve drops, flint/steel recipes) | ✅ | Surface rock & flint mounds, sieve mechanics, knapping start, 3 beginner deaths forgiveness, steel economy |
| tiered | Random gear tiers | `data/tiered` 199 files | ✅ | 199 affixes + equipment reforge recipes loaded into `TierRegistry` |
| fabric-seasons + seasonhud + crop-growth-modifier | Seasons & crops | `hearthwind-world`, `config/hearthwind_world.json` | ✅ | 18-day seasons, top-left SeasonHUD widget (`[Icon] Season, Day N/18`), 15 per-crop growth multipliers, winter snow layering |
| revive | Downed & Revive | `ReviveManager`, `hearthwind-survival` | ✅ | 60s bleedout crawl state, call for help, 3s team revive channel |
| let's do family | Agriculture suite | `hearthwind-flora` | ✅ | Farm & Charm, Vinery, Candlelight, Meadow, HerbalBrews, Brewery, Nether Vinery crops and stations |

## Contrib Ports (Vendored 26.2 Builds)

| Mod | Port Source | Status | Features |
|---|---|---|---|
| `yungs-api` + 5 overhauls | `contrib/yungs/` | ✅ | YUNG's Better Nether Fortresses, End Island, Desert/Jungle Temples, Ocean Monuments |
| `gardens-of-the-dead` | `contrib/gardens-of-the-dead/` | ✅ | Nether overhauls: Soulblight Forest & Whistling Woods biomes, flora, wood sets |
| `natures_spirit` | `conversion/vendored/` | ✅ | Diverse Overworld biomes, blooming canopies, Kaolin clay, diet integration |

## Deliberately not carried over

| Item | Reason |
|---|---|
| time-and-wind (day length) | dropped upstream; vanilla cycle kept |
| Client-only mods (EMI suite, antique atlas …) | server-first policy; revisit as companion bundle |
| duplicate storage / agriculture mods | de-kludge: keep one best system (e.g. single sieve, single farm progression) |

