# Custom Replacement Mods (Fabric, MC 26.x)

Server-side Fabric mods that rebuild Aged's core survival identity on modern
Minecraft. Each replaces upstream mods whose latest builds predate 1.21.2.
Values mirror the original pack's configs (see `conversion/overrides-ref/`
once copied) so gameplay parity is intentional and reviewable.

## Modules

### aged-survival
- **Thirst** (replaces `dehydration`): thirst bar driven by exhaustion;
  drinking unsafe water risks the thirst effect
  (`flask_dirty_thirst_chance: 0.3`, sip 0.5/15s — from dehydration.json5).
- **Temperature** (replaces `environmentz`): biome/altitude/light/wetness-driven
  temperature; overheating adds exhaustion (0.07/tick bucket), freezing damages.
- **Diet** (replaces `nutritionz`): five food groups; deficiency debuffs,
  bonus hearts at balance. Vitamin/mineral reference items configurable.
- **Spoilage** (replaces `spoiledz`): perishable foods rot through stages into
  rotten flesh / mold variants; preservation via smoking/cellar blocks later.

### aged-skills
- **Skills & attributes** (replaces `levelz`): XP → level (max 30), skill trees
  granting health/movement/attack/defense/luck/stamina bonuses; numbers lifted
  from levelz.json5 (`healthBase: 6`, per-level +1 HP etc.).
- **Mob scaling** (replaces `rpgdifficulty`): mob buffs scale with distance from
  world spawn.
- Phase 2: jobs & party XP sharing (jobsaddon/partyaddon).

### aged-primitive
- **Knapping start** (replaces `earlystage`): punch gravel for rocks → knap
  flint tools before any wood tier; `craftRockCraftHits: 2` feel.
- **Beginner deaths**: first N deaths are forgiven with a warning
  (`beginnerDeathCount: 3`).
- Ships its own **datapack**: ore-piece recipes, gated vanilla recipes
  (replaces reciperemover/autotag mods).

### aged-world
- **Seasons-lite** (replaces fabric-seasons + seasonhud): 4 seasons over N
  days; crop growth multipliers by season/biome-inside-greenhouse rules;
  temperature offset hook consumed by aged-survival.
- Server-authoritative; client HUD uses actionbar text (no client mod needed).

## Build system

One Gradle multi-module workspace targets Fabric API for MC `26.2`. The
`gradle.properties` `minecraft_version=` is the only place the game version
appears — bump it in lockstep with `conversion/build.conf.json`.

```bash
cd custom-mods
./gradlew build            # all four modules -> aged-*-26.2+0.1.0.jar
./gradlew :aged-skills:build   # single module
```

Jars land in `<module>/build/libs/`. Drop them into the server `mods/`
directory (or add to the pack via `conversion/curated/mods-manifest.json`
once they carry real features).

**Status:** skeletons build clean on Java 25 / loom 1.17 and load on a
dedicated 26.2 server (all four appear in the fabric mod list, boot reaches
`Done`). Gameplay systems are next per module, using the migrated datapack
(`conversion/datapacks/aged-server/`) as the tuning spec.
