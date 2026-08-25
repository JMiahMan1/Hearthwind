# Feature parity matrix - Hearthwind (26.2 rebuild) vs original Aged 1.20.1 pack

Living document. Status per system the original pack shipped; goal is
parity first, deliberate improvement where noted. UPDATE WITH EVERY
GAMEPLAY COMMIT.

Legend: ✅ done · 🟡 partial · ❌ missing · ➖ deliberately not carried
over (with reason)

## Rebuild-class systems (custom mods replacing original mods)

| Original mod | System | Corpus / config | Status | Notes / improvement |
|---|---|---|---|---|
| dehydration | Thirst | `data/dehydration`, `config/hearthwind_survival.json` | ✅ | hydration attachment `dehydration:hydration`, sprint/effect drain, regen floor, zero damage; water/purified bowls |
| environmentz | Temperature | `data/environmentz`, same config | ✅ | biome drift, warm/neutral armor tags, insulation/ice items, freeze/heat damage |
| nutritionz | Diet (5 groups) | `nutritionz:*` item tags, same config | ✅ v1 | decay, deficiency debuffs, balanced-diet absorption bonus. Improvement: datapack-tunable group tags |
| spoiledz | Spoilage | `spoiledz:perishable_items` + `non_spoiling_items`, same config | ✅ v1 | player inventories only - **containers/chests TODO** |
| levelz | Skills x12 | `data/levelz` ~400 files → `data/hearthwind_skills/gates` | ✅ v1 | XP curve, attributes (`hearthwind_skills:<skill>`), mob scaling, break/use gates. Missing: **crafting denial for gated items** (smithing tiers), entity gates (husbandry/trade), HUD client |
| rpg-difficulty | Distance mob scaling | `config/hearthwind_skills.json` mobScaling | ✅ | +2 HP/+0.5 dmg /1000 blocks past 500 grace, cap 20 |
| jobs-addon | Jobs x8 | `data/jobsaddon` → `data/aged_jobs/jobs` (8 jobs), `config/hearthwind_jobs.json` | 🟡 partial | runtime: join/leave/info via `/job` command (with suggestions), per-player `hearthwind_jobs:state`, block-break/kill hooks, 4 gametests. Missing: **job-restricted recipes via gates, bonus rewards** |
| earlystage | Primitive start | `data/earlystage` (sieve drops, flint/steel era recipes) | 🟡 partial | flint tools + rock, stone->rock loot, ore-piece recipes, **steel ingot/nugget/block + assets** shipped. Missing: knapping minigame, sieve block `earlystage:sieve_drops/aged_drops.json`, beginner-death forgiveness (`beginnerDeathCount:3`), tier integration |
| tiered | Random gear tiers | `data/tiered` 199 files (weighted attribute rolls, styles) | ❌ | loot/crafted gear rolls affix tiers (common→…) |
| party-addon | Parties (shared XP?) | `data/jobsaddon` adjacency | ❌ | low priority; needs design decision (co-op ages idea from Genesis study fits here) |
| fabric-seasons + seasonhud + crop-growth-modifier | Seasons & crops | `hearthwind-world`, `config/hearthwind_world.json` | 🟡 partial | **seasons-lite shipped**: 4 seasons over `daysPerSeason` (21) MC days, `Season.fromWorldTime()`, temp offsets + crop multipliers per season, config-driven. Missing: crop growth hook wiring, greenhouse logic, world-time sync |

| recipe-remover | Cut-mod recipe cleanup | migration pipeline | 🟡 | dropped-mod recipes removed by migration; dedicated pass pending |
| autotag | Tag normalization | - | 🟡 | handled ad hoc in migration scripts |

## Deliberately not carried over

| Item | Reason |
|---|---|
| time-and-wind (day length) | dropped upstream; vanilla cycle kept |
| Client-only mods (EMI suite, modmenu, antique atlas, exposure …) | server-first policy; revisit as companion bundle |
| trinkets/backslot/inmis (accessory slots) | API-heavy client surface; needs dedicated design |
| small-ships/immersive-aircraft | heavy content mods stuck pre-26.2; watchlist |

## Keep-class mods on watchlist (resolver, not code)

Stalled pre-26.2 and auto-resolving when authors publish:
antique-atlas-4, exposure, herdspanic, log-begone, medieval-buildings,
the-lost-castle, kiwi, modernfix … (rerun `resolve_deps.py --mc`).

## Suggested implementation order (updated 2026-08-25)

1. **hearthwind-jobs: remaining gates** - job-restricted recipes via same
   gate mechanism as skills + bonus rewards. Commands + XP hooks already live (🟡).
2. **hearthwind-primitive: sieve + tiered integration** - finish the "friction" arc:
   knapping minigame, sieve block from `earlystage:sieve_drops/aged_drops.json`,
   beginner forgiveness, then `tiered` affix system. Steel tier now shipped.
3. **tiered** - random gear affixes; needs crafted/looted item tagging
   (ItemEvents / loot modification).
4. **hearthwind-world: wire crop & climate** - crop growth multiplier hook
   (use `Season.cropMultiplier` in a CropGrowthCallback) + temperature offset
   consumed by survival (`Season.tempOffset`). Seasons-lite core already shipped (🟡).
5. **Client companion** - HUD bars (thirst/diet/temp/skill/jobs), spoilage in
   containers, live-client verification via client-gametest harness.
