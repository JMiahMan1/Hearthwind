# Custom Replacement Mods (Fabric, MC 26.2 — Hearthwind)

Server-side Fabric mods that rebuild Aged's core survival identity on modern
Minecraft. Each replaces upstream mods whose latest builds predate 26.2.
Values mirror the original pack's configs so gameplay parity is intentional
and reviewable. Internal ids are now `hearthwind_*` (display names
`Hearthwind: Survival/Skills/Primitive/World/Jobs`); data attachment
namespaces `levelz:`, `nutritionz:`, `spoiledz:` deliberately still mirror
originals for corpus compatibility (see `docs/PROJECT_DIRECTION.md` Phase B
namespace audit).

## Modules

| Module | Replaces | Status | Scope |
|---|---|---|---|
| `hearthwind-survival` | `dehydration`, `environmentz`, `nutritionz`, `spoiledz` | ✅ v1 shipped | Thirst (hydration attachment, sprint/effect drain, regen floor, zero damage), temperature (biome-target drift, warm/neutral armor, insulation/ice items, freeze/heat damage), diet (5 food groups, decay, deficiency debuffs, balanced-diet absorption bonus via `Consumable#onConsume` mixin), spoilage (perishable tag rot to rotten flesh, non-spoiling exempt, hot-biome multiplier). Config `config/hearthwind_survival.json`. 8/8 gametests green. |
| `hearthwind-skills` | `levelz` + `rpgdifficulty` | ✅ v1 shipped | 12 skills to 30 (triangular curve `baseXpPerLevel * L`, transient attribute modifiers `hearthwind_skills:<skill>`), mob scaling +2 HP/+0.5 dmg per 1000 blocks past 500 grace capped 20, break/use gates from migrated `data/levelz` corpus. Config `config/hearthwind_skills.json`. 7/7 gametests green. |
| `hearthwind-jobs` | `jobs-addon` (8 jobs) | 🟡 partial — commands live | Job defs from `data/aged_jobs/jobs`, per-player `hearthwind_jobs:state`, level math `pointsPerLevel * L`, XP hooks on block break / kill, **`/job join/leave/info` commands** (with suggestions). Config `config/hearthwind_jobs.json`. 4/4 gametests green. Remaining: job-restricted recipe gating, bonus rewards. |
| `hearthwind-primitive` | `earlystage`, `tiered` (part), `reciperemover`, `autotag` | 🟡 partial | Flint tools + rock, stone->rock loot, ore-piece recipes, flint tool recipes, **steel ingot/nugget/block + assets**. Remaining: knapping minigame, sieve `earlystage:sieve_drops/aged_drops.json`, beginner-death forgiveness, full `tiered` affix system. |
| `hearthwind-world` | `fabric-seasons`, `seasonhud`, `crop_growth_modifier` | 🟡 partial — seasons-lite shipped | 4 seasons over `daysPerSeason` (21) via `Season.fromWorldTime()`, temp offsets + crop multipliers per season (config `hearthwind_world.json`). Hook into survival temperature + crop growth next. Water motion per `ideas/rivers-and-waves.md` (Phase C). |

## Build system

One Gradle multi-module workspace targets Fabric API for MC `26.2`. The
`gradle.properties` `minecraft_version=` is the only place the game version
appears — bump it in lockstep with `conversion/build.conf.json`.

```bash
cd custom-mods
./gradlew build --no-daemon --max-workers=2          # all five modules -> hearthwind-*-26.2+0.1.0.jar
./gradlew :hearthwind-skills:build --no-daemon        # single module
bash tools/run_gametests.sh                           # headless gametests (all modules)
```

Jars land in `<module>/build/libs/`. Drop them into the server `mods/`
directory (never the `-sources` jar — unexpanded `fabric.mod.json` poisons
logs with `${version}` warnings).

**Verified:** `hearthwind-survival`, `hearthwind-skills`, `hearthwind-jobs`,
`hearthwind-primitive`, `hearthwind-world` all compile on Java 25 / loom
1.17.19 and load on a dedicated 26.2 server (build ✅ 2026-08-25,
19 gametests green: 8 survival + 7 skills + 4 jobs; primitive steel + world
seasons-lite incremental). Gameplay uses the migrated datapack
(`conversion/datapacks/aged-server/`) as tuning spec.
