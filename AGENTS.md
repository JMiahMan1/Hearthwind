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
| `docs/RELEASE_1.0_PARITY.md` | **1.0.0 governing plan**: Aged 3.1.2 mod-by-mod parity ledger, workstreams, decisions |
| `docs/PROJECT_DIRECTION.md` | Post-1.0 strategy: fork → standalone: phases, asset provenance rules, borrow board |
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
   After every milestone build, refresh the Prism physical-test installs:
   `cd custom-mods && bash tools/update_prism.sh` (updates Hearthwind-Full,
   Hearthwind-Minimal, Hearthwind-Dev-Client in place; `--deploy-new <inst>`
   to ship a newly built module).
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
#    (257 server gametests green: survival + skills + jobs + primitive + world + smallships;
#    client gametests PASS: nutrients + screens tour + diet + mining gate arc + pack-server connect + biome temp)
```

CONTAINER MANDATE: never run game/client tests on the host. Build on the
host (`./gradlew build`), then run the container wrappers (they stage
host-built jars + sources into /tmp/cgthearthwind-stage — Docker Desktop
cannot bind-mount /Users paths, and its file sharing intermittently serves
empty dirs, so staging goes through a seeded named volume `cgtvol`):

```bash
cd custom-mods && bash tools/run_gametests_container.sh [--keep-server]
cd custom-mods && bash tools/run_client_gametests_container.sh [--keep-dir]
# -> same harnesses inside pinned linux images (java 26); screenshots copy
#    back to .tmp/shots/cgt/. Stage script: tools/stage_container_tests.sh.
#    Server suite needs custom-mods/.gradle/loom-cache (merged jar for the
#    mixin-shadow test) - staged automatically. Set JAVA_HOME explicitly in
#    containers (run_gametests.sh defaults to a Cellar path).
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
#    Every fabric-client-gametest entrypoint class must call
#    takeScreenshot at least once (10 classes / 21 calls as of 0.1.1).
#    waitFor* TIMEOUTS ARE TICKS (20/s) - use minutes, not seconds.
#    PackServerConnectGameTests MUST be the LAST client entrypoint: closing
#    its dedicated server exits the JVM (code 0), silently skipping later tests.
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

- Block entities: constants are in `BlockEntityTypes` (plural). A modded block
  reusing a vanilla block entity (e.g. `new BarrelBlock(...)`) must be added via
  `((FabricBlockEntityType) BlockEntityTypes.BARREL).addValidBlock(block)`, or
  placing it crashes ("Invalid block entity minecraft:barrel").
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
- Item names: 26.x builds an item's translation key from the ITEM id
  (`item.<ns>.<id>`). A BlockItem no longer borrows its block's name
  unless the properties call `useBlockDescriptionPrefix()`. Ported mods
  whose lang only has `block.<ns>.<id>_block` show raw dotted keys. Every
  new item needs an `en_us` entry; `ItemNameGameTests` (client) fails on
  any raw key.
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

## 1.0.0 focus: Aged parity FIRST (read docs/RELEASE_1.0_PARITY.md)

Until 1.0.0 ships, every task is judged by ONE rule: **does it make
Hearthwind on 26.2 look and play like Aged 3.1.2?** Client and server
are both in scope.

- Reference = `.tmp/Aged-3.1.2.mrpack` (212 mods + agedaddition, configs,
  resource/shader packs, datapack). Match mods by Modrinth project id,
  never by guessed slug.
- Hearthwind modules that already replace Aged mods are the allowed
  exception. Record their deviations; don't expand them.
- No Aged mod is marked obsolete. Each missing mod gets a per-mod team
  decision (port / adopt upstream / rebuild), recorded in the plan.
- Small bug fixes are fine anytime. Mod ports and new systems follow the
  workstream order in the plan.
- Do NOT start post-1.0 items (Ages enforcement beyond Aged's gates,
  Create/Mechanical Age, water motion, Terralith/Tectonic, dedupe audit,
  26.3 bump). They are parked in plan section 7.
- Newer mod versions are fine: note where they deviate from Aged's version
  and fix the deviation on the current version where possible.
- The guidebook (`assets/hearthwind/lavender/`) must never state something the
  game does not do. Change book and mechanic together, and run
  `python3 custom-mods/tools/validate_guidebook.py`. Checklist:
  `docs/GUIDEBOOK_PARITY.md`.
- Never claim "parity" or "everything available is deployed" without the
  Aged-index diff (plan section 9) backing it.

### Priority order (details and ledger in docs/RELEASE_1.0_PARITY.md)

- **W0 Truth:** re-key `mods-manifest.json` to Aged project ids, remove
  stale vendored duplicates (architectury 21.0.7, supermartijn642corelib
  1.1.24a, modmenu 20.0.1), add a CI Aged-parity diff, carry over Aged's
  config overrides for mods we already ship.
- **W1 First impression / HUD:** Overflowing Bars, Time & Wind day
  length (seasons in ticks), `Lv. N` preview label, tab icons, the
  medieval `hearthwind_guide_book` rewrite (plan section 5.8), then the Trinkets/BackSlot/Inmis accessory slots and
  the remaining Level/Restriction screens. Each change ships with a client
  gametest screenshot next to the Aged reference (plan section 5.7).
- **W2 Adopt:** the 30 Aged mods that already have official 26.2 Fabric
  builds (server/both batch, then the client stack: Sodium, Iris,
  FancyMenu, DH, ...).
- **W3 Rebuild gaps:** jobs curve/cooldown/multi-job, RPGDifficulty caps,
  steel ratio, LevelZ craft-gate enforcement, dirty-water duration,
  seasonal bonemeal.
- **W4 Port queue:** 65 mods + surveyor WIP, tier A (Fabric 26.1.x
  upstream) first.
- **W5 Look and feel:** Aged resource packs and shaders.
- **W6 Release gate:** CI parity diff green, all gametests green, server
  boots from the built mrpack, client play test.
- **Vendored jars must match their builds.** After rebuilding any
  `custom-mods` port that ships from `conversion/vendored/`, copy the plain
  jar over the vendored one. A stale lavender jar shipped with no guidebook.

### Post-1.0 direction (parked, from docs/PROJECT_DIRECTION.md)

Realism, earned unlocks, harder frontier, one best, slow tech (Ages
Stranded to Mechanical). This remains the long-term north star. It does
not drive 1.0.0 work.

### Module status (shipped; parity gaps are in the plan section 5.3)

- `hearthwind-survival`: thirst, temperature, 5-group diet, spoilage
  (inventory + containers), downed/revive.
- `hearthwind-skills`: LevelZ-parity skills, 649 corpus gates, procs,
  distance mob scaling, parties.
- `hearthwind-jobs`: 8 jobs with corpus ladders, `/job` commands. Job
  select is SILENT like Aged (`JobsManager.employJob/quitJob` send no
  chat); level-up chat in `awardIfMatch` is kept.
- `hearthwind-primitive`: earlystage rock/flint/sieve, crafting rock,
  beginner forgiveness, tiered affixes, RecipeRemover list, AgedAddition
  items + coal_piece fuel.
- `hearthwind-world`: seasons (21 days, Aged value), per-crop season
  multipliers, winter breeding block, HerdPanic, End Remastered eyes,
  fauna.
- `hearthwind-client`: HUD, Nutrients/Skills/Jobs/Party screens, SeasonHud.
- `dungeonz`: ported; 23 server gametests. Remaining: jigsaw generation
  path, criteria/loot asserts.
- Hygiene: remove `.tmp-test-server/` and `.tmp/` scratch at task end;
  rerun `resolve_deps.py --mc <latest>` periodically as a watchlist.

## Scratch-file policy (MANDATORY)

ALL generated files (logs, screenshots, test scripts, compiled helpers,
classpath dumps, scenario JSONs, crash dumps, jar/zip extraction dirs,
javap dumps) stay INSIDE the project in `.tmp/` (git-ignored). NEVER write
scratch to `/tmp`, `/var/folders/...`, or any absolute path outside the
repo - macOS temp dirs are invisible to code review, survive across
sessions as litter, and get purged at random. BEFORE running any shell
command, re-read it: if any path starts with `/tmp/`, `/var/folders/`, or
`cd /tmp`, STOP and rewrite it under `./.tmp/`. This includes `unzip -d`,
`mkdir`, output redirects (`> /tmp/...`), and `CGT_STAGE_DIR` overrides.

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
