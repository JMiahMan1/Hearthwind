# AGENTS.md - Handbook for AI/human contributors

Modern-Minecraft (26.x) server-focused rebuild of the Aged modpack
(fork of [xR4YM0ND/Aged](https://github.com/xR4YM0ND/Aged), MIT). Working
branch: `server-26.2`. The project identity is its own; **do not prefix
commit titles with "Aged"** - use plain conventional subjects
(`feat(survival): ...`, `docs: ...`).

## Repo map

| Path | Purpose |
|---|---|
| `conversion/build.conf.json` | Single source of truth: target MC, loader, datapack pack_format |
| `conversion/scripts/resolve_deps.py` | Manifest -> Modrinth resolution incl. recursive transitive deps |
| `conversion/scripts/build_pack.py` | Builds `.mrpack` + materializes `dist/server/` (mods + world datapack) |
| `conversion/scripts/migrate_datapack.py` | Ports original paxi datapack to native 26.x world datapack |
| `conversion/datapacks/hearthwind/` | Generated migrated datapack (committed; deterministic) |
| `conversion/curated/mods-manifest.json` | Every upstream mod classified keep/rebuild/drop/client-optional |
| `custom-mods/` | Gradle multi-module Fabric workspace (survival, skills, primitive, world) |
| `custom-mods/tools/gen_placeholder_assets.py` | Placeholder models/textures/lang/equipment generator |
| `custom-mods/tools/rcon.py` | Minimal Source-RCON client for headless verification |
| `docs/CONVERSION.md` | Feasibility study, strategy, verified-state writeups |
| `docs/PROJECT_DIRECTION.md` | Fork → standalone strategy: phases, asset provenance rules, borrow board |
| `docs/INSTALL.md` | Install instructions (players/admins/devs) + packaging flow |
| `docs/PLAYER_CHANGES.md` | Player-facing list of gameplay differences from vanilla; UPDATE WITH EVERY GAMEPLAY COMMIT |
| `.github/workflows/build-and-test.yml` | GHA: build + headless gametests on push; optional pack boot-smoke on dispatch |

## CI (GitHub Actions)

`build-and-test.yml` runs the full gradle build + gametest suite on GH
ubuntu runners (7 GB - no memory gymnastics needed there, but keep
--no-daemon). Artifacts: mod jars + JUnit XML report. A `boot-smoke`
job (workflow_dispatch) resolves the full pack and boots a real server
expecting `Done`.

How far CI can go:

- Headless gametests + boot/RCON smoke tests: fully supported (current).
- Automated CLIENT testing: `fabric-client-gametest-api-v1` drives a REAL
  client under xvfb on runners (movement, clicks, inventory, screenshots,
  assertions). Heavy (~4 GB, minutes per scenario) - adopt when we ship
  client-side code worth testing.
- INTERACTIVE human sessions: also possible despite runners having no
  inbound ports - everything tunnels outbound:
  1. workflow boots pack server (and optionally a client under xvfb),
  2. expose via playit.gg / ngrok / tailscale (outbound-only agents),
  3. humans connect from their own machines for as long as the job lives
     (6 h cap per job; re-dispatch to renew).
  Guardrails if we ever enable this: gate behind workflow_dispatch +
  environment approval, never print tunnel tokens in logs, use a
  dedicated offline-mode test world.

## Golden workflow

1. **Version bumps**: edit only `conversion/build.conf.json`
   (`targets.minecraft`; after a bump re-check `datapack.pack_format`
   from the new server jar's `version.json`, field
   `pack_version.data_major`; 26.2 = 107). Then run
   `python3 conversion/scripts/resolve_deps.py`, review the readiness
   report, then `build_pack.py --server-dir`.
2. **Custom mods**: `cd custom-mods && ./gradlew build`. Copy the plain
   jar into a test server (NEVER the `-sources` jar - its unexpanded
   fabric.mod.json poisons logs with `${version}` warnings).
3. **Every change ships verified**: boot test + RCON checks. No "should
   work" claims.

## Testing harness (gametests - preferred)

`custom-mods/hearthwind-survival`, `hearthwind-skills` and
`hearthwind-jobs` ship headless gametests
(`HearthwindSurvivalGameTests`, `HearthwindSkillsGameTests`,
`HearthwindJobsGameTests`, fabric-gametest entrypoints). Run them all:

```bash
cd custom-mods && bash tools/run_gametests.sh [--keep-server]
# -> builds all modules, boots a throwaway 26.2 server, runs every @GameTest,
#    prints "gametests: N/M passed", exits nonzero on failure
#    (204 server gametests green: survival + skills + jobs + primitive + world + flora + smallships;
#    client gametests PASS: nutrients + screens tour + diet + mining gate arc + pack-server connect + biome temp)
```

REAL-CLIENT gametests (fabric-client-gametest-api-v1, headless, no
window/mouse takeover - runs under xvfb in docker or CI):

```bash
cd custom-mods && bash tools/run_client_gametests_docker.sh
#    (or CGT_XVFB=1 bash tools/run_client_gametests.sh on linux)
# -> one client boot, then every registered 'fabric-client-gametest'
#    entrypoint class runs in sequence: nutrients screen, screens tour
#    (inventory/N/K/J/P), diet loop (eat apple -> server nutrient assert),
#    mining loop (job join -> mine -> skills+jobs XP asserts), dedicated
#    pack-server connect (registry negotiation), desert temperature.
#    Screenshots land in .tmp/shots/cgt/; exit code is the verdict.
#    waitFor* TIMEOUTS ARE TICKS (20/s) - use minutes, not seconds.
```

Gotchas learned the hard way:

- The maven `fabric-api` jar is THIN (no nested modules) - the runner
  fetches `fabric-gametest-api-v1` explicitly into `mods/`. Without it
  `-Dfabric-api.gametest=true` silently does nothing.
- fabric's v1 `@GameTest` methods are INSTANCE methods on the entrypoint
  class, and the class needs a PUBLIC constructor; vanilla's
  `net.minecraft.gametest.framework.GameTest` annotation is a DIFFERENT
  annotation that will not register anything.
- loom 1.17 removed `modImplementation`; use the `modCompileClasspath`
  configuration. Child build.gradles can't call loom DSL at all (plugin
  applied via root `subprojects {}`) - module deps go in the root file
  inside `afterEvaluate`.
- Manual equivalent: `java -Dfabric-api.gametest=true
  -Dfabric-api.gametest.report-file=report.xml -jar fabric-server.jar
  nogui` runs tests and exits; parse report.xml with
  `tools/parse_gametest_report.py`.
- New logic should land WITH a gametest: extract pure-logic cores
  (Entity/Container params, no ServerPlayer-only APIs) so they are
  testable without a client.

## Boot-test loop (headless)

```bash
cp custom-mods/<mod>/build/libs/<mod>-<mc>+x.jar .tmp-test-server/mods/
cd .tmp-test-server
(setsid timeout 170 /usr/bin/java -Xmx3G -jar fabric-server.jar nogui > bootN.log 2>&1 < /dev/null &)
sleep ~72   # then:
grep -c "Done (" bootN.log          # must be 1
grep "Couldn't parse" bootN.log     # must not list our namespaces
```

RCON (already enabled: port 25575, password `agedtest`):

```bash
python3 ../custom-mods/tools/rcon.py 127.0.0.1 25575 agedtest "summon item ~ ~ ~ {Item:{id:\"ns:item\",count:1}}"
```

### Hard-won traps (do not relearn these)

- `pgrep -f "fabric-server.jar"` matches its own bash command line - use
  `pgrep -f "[f]abric-server.jar"`.
- Launch with `(setsid timeout NNN java ... &)` double-fork; plain
  `nohup ... & disown` hangs the tool shell.
- Never relaunch while a previous instance is still shutting down -
  `session.lock` collisions crash boot (`DirectoryLock$LockException`).
- Summoned entities vanish instantly with no players online (modern MC
  has no spawn chunks). `/forceload add -16 -16 31 31` keeps chunks
  alive for entity/effect checks.
- Stale jars copied into `mods/` have caused false failures - after
  resource edits, REBUILD before recopying.
- NEVER replace mod jars under a RUNNING server - lazy class loading then
  reads a mix of old/new jar bytes; first touch of the swapped class dies
  with `ExceptionInInitializerError` and every later use is poisoned
  (`NoClassDefFoundError: Could not initialize class X`) until restart.
  Restart the server after every jar deploy, then verify.
- RCON probes: `/forceload add` takes BLOCK coords (chunk = coord/16,
  logged as `Marked chunk [x, z]`); summon into unloaded chunks silently
  discards the entity ("Summoned new X" prints anyway). Probe inside the
  forceloaded chunk with `execute positioned <x> <y> <z> run ...`, and
  assert existence via `execute if entity ... run say MARKER` + log grep
  (the RCON protocol cannot signal command failure).
- JDT/LSP phantom Java errors happen; gradle build is the authority.
- RCON properties are the VANILLA names: `enable-rcon=true`,
  `rcon.port`, `rcon.password`. Fabric-style `rcon.enabled` lines are
  ignored (server rewrites server.properties and RCON stays off).
- Servers pause after 60s with no players (`pause-when-empty-seconds`,
  default 60; set `-1` in test servers) - tick loops and RCON stop
  answering while paused; do RCON checks right after `Done`.
- This build host has ~8 GB RAM with no swap and heavy baseline usage
  (elasticsearch/clamd). Gradle daemon heap is capped at `-Xmx1G` in
  custom-mods/gradle.properties; run builds with
  `--no-daemon --max-workers=2`, test servers with `-Xmx768M`.

## 26.x API cheat sheet (verified on 26.2)

- Entity constants live in `net.minecraft.world.entity.EntityTypes`
  (plural), not `EntityType`. Loot tables: entity via
  `entityType.getDefaultLootTable()` (`Optional<ResourceKey<LootTable>>`),
  blocks via `Blocks.X.getLootTable()`.
- Tools: no PickaxeItem/SwordItem classes - plain `new Item(props)` plus
  `props.pickaxe(ToolMaterial, speed, dmg)` / axe / shovel / hoe / sword;
  `ToolMaterial` is a record; gate tiers with tags like
  `BlockTags.INCORRECT_FOR_WOODEN_TOOL`.
- `Item.Properties.setId(ResourceKey<Item>)` is MANDATORY before
  construction (else intrusive-holder freeze crash at registry close).
- `Item.use` returns sealed `InteractionResult` (SUCCESS_SERVER /
  CONSUME / FAIL / PASS), not `InteractionResultHolder`. Messages:
  `sendSystemMessage` / `sendOverlayMessage(Component)`.
- Armor: `props.humanoidArmor(ArmorMaterial, ArmorType)`; ArmorMaterial
  record needs defense map, equip sound holder, repair `TagKey<Item>`,
  and `ResourceKey<EquipmentAsset>`; asset JSON at
  `assets/<ns>/equipment/<asset>.json` (layers humanoid +
  humanoid_leggings), textures under
  `textures/entity/equipment/humanoid/<asset>[_leggings].png`.
- Recipes/tags: flat strings (`"#tag"`, `"item"`); shaped-pattern key
  may NOT contain `' '` (reserved empty-cell symbol).
- Food is the 1.21.2+ component system: there is NO `Player.eat` /
  `FoodProperties.getNutrition` item method. `ItemStack# FOOD` data
  component lives at `net.minecraft.core.component.DataComponents.FOOD`
  (record `nutrition()`/`saturation()`); consumption runs through
  `net.minecraft.world.item.component.Consumable#onConsume(Level,
  LivingEntity, ItemStack)` - mixin THAT for "finished eating" hooks.
- Vanilla effect holder constants: `MobEffects.MINING_FATIGUE`,
  `MobEffects.SLOWNESS`, `MobEffects.WEAKNESS`, `MobEffects.ABSORPTION`
  (no DIG_SLOWDOWN/MOVEMENT_SLOWNESS names in 26.x mojmap).
- Fabric data attachments: `AttachmentRegistry.<T>builder()
  .persistent(codec).copyOnDeath().buildAndRegister(id)`; access with
  `player.getAttached(...)` / `setAttached(...)`.
- Register custom items under ORIGINAL upstream namespaces (earlystage,
  agedaddition, dehydration, environmentz, levelz, tiered, ...) so the
  ~800 migrated tuning files activate unchanged.
- Blocks: `DirectionProperty` is GONE in 26.2 - `BlockStateProperties.
  HORIZONTAL_FACING` is an `EnumProperty<Direction>`. Block overrides:
  `updateShape(state, LevelReader, ScheduledTickAccess, pos, dir,
  neighborPos, neighborState, RandomSource)`, `useItemOn(...) ->
  InteractionResult`, `canSurvive(state, LevelReader, pos)`,
  `rotate/mirror` standard. Custom enums need `StringRepresentable`.
- GUI text colors are STRICT ARGB: `0x3F3F3F` renders INVISIBLE (alpha
  0x00) - always `0xFF3F3F3F` style. `GuiGraphicsExtractor.text(Font,
  String, int x, int y, int color)`.
- Screen hit-test helpers must take PANEL-RELATIVE coords (like vanilla
  `isPointWithinBounds(5,5,...)`) - passing absolute `this.x+5` into a
  helper that subtracts `this.x` double-offsets the region (this exact
  bug broke the nutrients back-arrow).
- Inventory widgets: anchor to `@Shadow leftPos/topPos` (mixins on
  InventoryScreen can extend AbstractContainerScreen to reach them).
  NEVER recompute `(width-176)/2` - the recipe book shifts leftPos by
  +71 and the drawn/clicked regions diverge.
- macOS host has NO `setsid`/GNU `timeout`: launch test servers with
  `(nohup java ... > log 2>&1 < /dev/null &)` from the server dir.
- cliclick: `kp:` is unreliable for LETTER keys in game - use `t:`
  (`type`). Held left-click mining: `rhold X Y --ms N --button left`.

## Overarching principles (from docs/PROJECT_DIRECTION.md North star)

Every task is judged against **realism → earned unlock → harder frontier → one best → slow tech**:
- Make it as close to reality as possible without being miserable (costs for magic, physical crafting).
- More resources = more unlocks, but the world gets harder in lockstep (distance + aggregate power scaling, capped 20).
- All mods must play well together - delete overlap, keep one best (single sieve, single storage, single farm system).
- Technology arrives in Ages (Stranded → Camp → Copper → Iron/Steel → Mechanical), gated by skills/jobs/advancements, hard-fought not creative.

## Next steps (priority order - slow-tech, no kludge)

1. **Survival** (`custom-mods/hearthwind-survival`) - v1 SHIPPED
   (verified boot + RCON on 26.2, 8/8 gametests green):
   - Diet: five `nutritionz:` item tags (fruits, vegetables,
     grains, proteins, sugars), nutrients attachment (0..100) with decay,
     deficiency debuffs (fruit->mining fatigue, vegetables/proteins->
     weakness, grains->slowness), balanced diet -> refreshed absorption
     bonus hearts. Eat hook = mixin on `Consumable#onConsume`
     (`ConsumableConsumeMixin`). NOT yet play-verified with a live client.
   - Spoilage: `spoiledz:perishable_items` tag rots stack
     items into rotten flesh on a random check interval; migrated
     `spoiledz:non_spoiling_items` tag respected as exemption; hot biomes
     double the chance. Inventory-only in v1 (containers TODO).
   - Temperature/thirst: all tunables now in
     `config/hearthwind_survival.json` (auto-created with defaults).
   - Remaining: client-side HUD bars for hydration/diet, container
     spoilage, in-game eat-hook verification.
2. **Skills** (`hearthwind-skills`) - v1 SHIPPED (7/7 gametests green):
   12 levelz-parity skills (farming/mining/smithing/strength/agility/
   defense/health/stamina/luck/archery/alchemy/trade), XP attachment
   under `levelz:` namespace, triangular XP curve (baseXpPerLevel * N
   per level, max 30), attribute bonuses as transient modifiers keyed
   `hearthwind_skills:<skill>` (health/strength/agility/defense/mining/luck),
   XP hooks on block break (crops->farming, pickaxe->mining,
   shovel->stamina) and kills (melee->strength, bow/crossbow/trident->
   archery, animals->farming). All tunables in `config/hearthwind_skills.json`.
   Skill break/use gates from merged `data/levelz` corpus are live (mining 1..27, use gates on 17 stations).
    - Remaining vs original: crafting denial for gated items (smithing
      tiers), entity/husbandry gates, client HUD (companion mod).
3. **Jobs** (`hearthwind-jobs`, jobs-addon parity) - 🟡 partial, 4/4 gametests green:
   8 jobs (fisher/miner/farmer/warrior/smither/brewer/builder/lumberjack), per-player job attachment `hearthwind_jobs:state`, level math `pointsPerLevel` (default 100), XP hooks on block break / entity kill via `JobState.awardIfMatch`, **`/job join/leave/info` commands** shipped; config `config/hearthwind_jobs.json`.
   Remaining: job-restricted recipe gating (reuses gate infra), bonus rewards - must respect **Age 2+** before smither/brewer unlocks.
4. **Primitive Ages 0→3** (`hearthwind-primitive`) - 🟡 partial: **faithful earlystage rock+flint port shipped** (surface `earlystage:rock` 4 variants / `earlystage:flint` 2 variants × facing, weighted_state_provider worldgen in Aged's biome tag, 1-hit mounds drop rock/flint, shovel right-click cycles variant, stonecutter rocks_from_stone + shaped cobblestone_from_rock, original earlystage MIT models/textures - they render vanilla stone); flint tools, ore pieces, steel ingot/nugget/block + assets shipped. **Removed the invented stone->rock/gravel loot hooks** (Aged keeps vanilla drops). Next: Age 1 Sieve (`earlystage:sieve_drops/aged_drops.json` as the ONE sieve, tanning 4 flesh→leather as datapack recipe, no duplicate Prospector Bench), knapping minigame on `crafting_rock`, beginner-death forgiveness (`beginnerDeathCount: 3`), full `tiered` affix system. Steel stays gated behind `mining 7`+`smithing 14` (Iron Age).
   - Client: **NutrientsScreen + inventory tab SHIPPED and live-verified** (apple tab top-left anchored to `leftPos/topPos`, N key, back arrow, E close; NutritionZ MIT crops for panel/bars/arrow).
5. **World Ages 1→5** (`hearthwind-world`) - 🟡 partial: **seasons-lite shipped** (4 seasons over `daysPerSeason` 21, `Season.fromWorldTime()`, temp offsets + crop multipliers per season, `config/hearthwind_world.json`); next wiring crop growth + temperature hook, then **Age-gated Mechanical preview** (Create wind/water wheel after `smithing 18`/`builder 3`, full Create only at Mechanical Age). Water motion per `ideas/rivers-and-waves.md` (river currents, ocean swell, foam, tides -> later visible wave surfaces via optional client companion/shaders; Tectonic vs Terralith pick ONE).
6. **De-kludge audit** (new): before adding any tech, dedupe overlap - `grep` `mods-manifest.json` for duplicate storage (`Sophisticated Backpacks` vs `Iron Chests` → keep best), duplicate farming (`Let's Do` vs `Farmer's Delight` → keep one), duplicate sieving (keep `earlystage:sieve`, drop Homesteads `Prospector's Bench`). Count per need must go down.
7. **Datapack noise shrink**: each shipped item set reduces the
   remaining non-fatal loot/recipe parse warnings; re-census via
   `grep "Couldn't parse" bootN.log`.
8. **Watchlist**: periodically rerun `resolve_deps.py --mc <latest>`;
   YUNG suite/endrem/etc. return automatically as authors publish.
   Water/worldgen candidates tracked in `ideas/rivers-and-waves.md`
   (tectonic + terralith both ship 26.2 builds; pick ONE at next bump).
   Genesis/Genesis Framework studied for design ideas only (advancement
   -wrapped gating, instruction toasts, ordered age chains) - rebuild
   in-house, see `ideas/genesis-comparison.md`; neither mod adopted.
9. **Snapshot CI probe**: nightly resolver run against newest snapshot.
10. **Real art**: replace generated placeholder textures/models.
11. **Cleanup discipline**: remove `.tmp-test-server/` and all
    `.tmp/` scratch at task end.

## Scratch-file policy (MANDATORY)

ALL generated files (logs, screenshots, test scripts, compiled helpers,
classpath dumps, scenario JSONs, crash dumps) stay INSIDE the project in
`.tmp/` (git-ignored). NEVER write scratch to `/tmp`, `/var/folders/...`,
or any absolute path outside the repo - macOS temp dirs are invisible to
code review, survive across sessions as litter, and get purged at random.

```
.tmp/
  logs/    client-stdout.log, server_run.log, ...
  shots/   *.png screenshots from the live harness
  bin/     compiled helpers (cghold, winlist)
  *.json   test scenarios, mc_cp.txt classpath dumps
```

Tools must default to these paths (see `custom-mods/tools/client_harness.py`).
Before finishing any task: `git status` must show no untracked litter, and
nothing may remain in system temp dirs from this project.

## Verification checklist per feature

- `./gradlew build` green (Java 25, loom 1.17-SNAPSHOT)
- `bash tools/run_gametests.sh` all green (add tests for new logic)
- Boot reaches `Done`; our namespaces absent from parse-error greps
- RCON spot checks: summon item by id, apply effects, loot spawn
- `ruff check tools/` for python tooling changes (install once via
  `sudo dnf install -y ruff`; not yet present on this host)
