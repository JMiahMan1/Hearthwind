# Feature parity matrix — Aged (26.2 rebuild) vs original 1.20.1 pack

Living document. Status per system the original pack shipped; goal is
parity first, deliberate improvement where noted. UPDATE WITH EVERY
GAMEPLAY COMMIT.

Legend: ✅ done · 🟡 partial · ❌ missing · ➖ deliberately not carried
over (with reason)

## Rebuild-class systems (custom mods replacing original mods)

| Original mod | System | Corpus | Status | Notes / improvement |
|---|---|---|---|---|
| dehydration | Thirst | `data/dehydration` | ✅ | hydration attachment, sprint/effect drain, regen floor, damage; water/purified bowls; config file |
| environmentz | Temperature | `data/environmentz` | ✅ | biome drift, armor insulation tags, warm/neutral items, freeze/heat damage; config |
| nutritionz | Diet (5 groups) | item-group tags | ✅ v1 | decay, deficiency debuffs, balanced-diet absorption bonus. Improvement over original: datapack-tunable group tags |
| spoiledz | Spoilage | non_spoiling tag | ✅ v1 | player inventories only — **containers/chests TODO** |
| levelz | Skills x12 | `data/levelz` 400 files | ✅ v1 | XP curve, attributes, mob scaling, break/use gates. Missing vs original: **crafting denial for gated items** (smithing tiers), entity gates (husbandry/trade), HUD client |
| rpg-difficulty | Distance mob scaling | — | ✅ | +2 HP/+0.5 dmg /1000 blocks past 500 grace, cap 20 |
| jobs-addon | Jobs x8 | `data/jobsaddon` (fisher/miner/farmer/warrior/smither/brewer/builder/lumberjack + restricted recipes) | ❌ | corpus defines job XP sources per level + job-restricted recipes |
| tiered | Random gear tiers | `data/tiered` 199 files (weighted attribute rolls, styles) | ❌ | loot/crafted gear rolls affix tiers (common→…) |
| earlystage | Primitive start | `data/earlystage` (sieve drops, flint/steel era recipes) | ❌ | knapping/sieve progression before stone tools |
| party-addon | Parties (shared XP?) | `data/jobsaddon` adjacency | ❌ | low priority; needs design decision (co-op ages idea from Genesis study fits here) |
| fabric-seasons + seasonhud + crop-growth-modifier | Seasons & crops | config-driven | ❌ | planned as aged-world module (roadmap); crop multipliers fold into it |
| recipe-remover | Cut-mod recipe cleanup | migration pipeline | 🟡 | dropped-mod recipes removed by migration; dedicated pass pending |
| autotag | Tag normalization | — | 🟡 | handled ad hoc in migration scripts |

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

## Suggested implementation order (post-skills)

1. **aged-jobs** — corpus-complete spec, reuses skill-event infra
   (block break / kill / craft hooks already exist). Includes
   job-restricted recipes via the same gate mechanism.
2. **earlystage primitive** — sieve drops + flint-era gating completes
   the "friction" story arc; pairs with Genesis-style ordered ages.
3. **tiered** — random gear affixes; needs crafted/looted item tagging
   (ItemEvents / loot modification).
4. **aged-world seasons-lite** — date attachment → season → crop growth
   multiplier + temperature offset hook into survival module.
5. **Client companion** — HUD bars (thirst/diet/temp/skill), spoilage in
   containers, live-client verification via client-gametest harness.
