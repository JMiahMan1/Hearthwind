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
   **Reading the resolver's choice.** `pick_version` ranks
   `(fabric?, no-forge?, filename-does-not-name-a-newer-MC,
   exclusive-to-target-MC, date_published, -len(game_versions))`. The two
   traps, both hit in 0.1.54:
   - **`game_versions` cannot be the safety gate.** Authors extend one
     rolling build list, so a 26.2-compatible build also claims 26.3 (and
     snapshots). Every upgrade available to us claims 26.3. "Claims only
     26.2" also fails, because those authors publish no exclusive build at
     all. **The build FILENAME is the only signal authors actually set** -
     `almanac-fabric-26.2-1.26.9.1.jar` is safe,
     `spawnanimations-v1.11.6-mc1.17-26.3.9-mod.jar` is not. `mc1.21+`
     reads as BELOW 26.2. Before shipping an "upgrade", read the filename.
   - **Never let narrowness outrank recency.** `-len(game_versions)` above
     `date_published` silently pins to older builds, because a newer
     release claims *more* versions.
2. **Custom mods**: `cd custom-mods && ./gradlew build`. Copy the plain
   jar into a test server (NEVER the `-sources` jar - its unexpanded
   fabric.mod.json poisons logs with `${version}` warnings).
   After every milestone build, refresh the Prism physical-test installs:
   `cd custom-mods && bash tools/update_prism.sh` (rebuilds
   Hearthwind-Full, Hearthwind-Minimal, Hearthwind-Dev-Client from the
   built `HearthwindClient-*.mrpack`: index jars, `overrides/mods`, and
   `overrides/` config/datapacks, pruning anything that left the pack;
   exceptions live in `tools/prism_keep.txt`). Instances must always
   match what a user gets by importing the release mrpack - never hand
   copy jars into them.
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
#    (server gametests green: survival + skills + jobs + primitive + world + dungeonz
#     + smallships;
#    client gametests PASS: nutrients + screens tour + diet + mining gate arc + pack-server connect + biome temp)
```

CONTAINER MANDATE: never run game/client tests on the host. Build on the
host (`./gradlew build`), then run the container wrappers. They stage
host-built jars + sources into `custom-mods/.tmp/cgthearthwind-stage` and
**bind-mount that tree** - no named volume, no `docker cp`. (The old
`cgtvol` + `docker cp` seeding was a workaround for Docker Desktop file
sharing; measured 0.1.57, bind mounts serve the whole 56k-file stage fine,
and `docker cp` never deletes, which silently kept removed jars alive.)

```bash
cd custom-mods && bash tools/run_gametests_container.sh [--keep-server]
cd custom-mods && bash tools/run_client_gametests_container.sh [--keep-dir]
# -> same harnesses inside pinned linux images (java 25); screenshots copy
#    back to .tmp/shots/cgt/. Stage script: tools/stage_container_tests.sh.
#    Server suite needs custom-mods/.gradle/loom-cache (merged jar for the
#    mixin-shadow test) - staged automatically. Set JAVA_HOME explicitly in
#    containers (run_gametests.sh defaults to a Cellar path).
#    Both wrappers forward CGT_EXCLUDE_MODS but DEFAULT IT TO EMPTY, so a
#    bare `run_client_gametests_container.sh` loads DistantHorizons and
#    deadlocks (see the DH trap below). Always invoke it as
#    CGT_EXCLUDE_MODS=DistantHorizons bash tools/run_client_gametests_container.sh
```

**The images are Java 25, not 26, for a reason.** MC 26.2's version manifest
declares `java_version` 25 and Prism auto-provisions the matching runtime, so
25 is what a real player runs; `update_prism.sh` actively repairs any Java 26
pin. Testing on 26 proved nothing about the runtime users actually get. Keep
both Dockerfiles and CI's `java-version` at 25.

**Every container run must clean up after itself, and so must you.** Docker on
this host is Colima, whose QEMU VM holds **8 GB of RAM** and whose image/volume
leftovers reached **29 GB** on disk. All three wrappers now start the VM if it
is down, and on exit call `tools/cleanup_container_tests.sh`, which removes the
Hearthwind images and test volumes and stops the VM *only if that run started
it*. `CGT_KEEP=1` skips cleanup for a debugging session; `--keep-images` and
`--stop-vm` are available on the cleanup script directly. This is not hygiene
theatre: a Colima VM left up for a day drove this 32 GB machine into swap
exhaustion (11.4 GB swap used, 19-30 MB physical free), and the Minecraft
client then died with **SIGSEGV in the C2 compiler** - `Chunk::next_chop` on a
garbage pointer, because native allocation could not be satisfied.
`hs_err_pid22780` and `hs_err_pid58980` were both misdiagnosed as a JIT bug or
DistantHorizons native corruption before the `Memory:` line in the reports
showed the machine had no memory left.

**When a client dies with a random-looking JVM SIGSEGV, read `Memory:` in the
`hs_err_pid*.log` and run `sysctl vm.swapusage` before blaming Java.** The
frames name whichever code happened to allocate next; the cause may be the
host. Also remember `opencode` itself can hold 8-10 GB. The Colima VM's disk
(`~/.colima`, ~21 GB after cleanup) also holds other projects' images - never
`colima delete` without checking `docker images` first.

**DistantHorizons is the one mod in this pack that crashes a real client, and
it is excluded from every client test.** Four `hs_err` reports agree on the
shape: pid35899 died *inside* DH's own bundled native
(`Java_dh_1sqlite_core_NativeDB_deserialize` in `libsqlitejdbc.dylib`);
pid22780 and pid58980 died in `Chunk::next_chop` while compiling entirely
different methods (`SingleVariant::emitQuads`, then `OreFeature::doPlace`)
and pid66728 in `Arena::destruct_contents` with an empty compile task. Arena
ALLOC and arena FREE integrity failures in different places, plus a direct
native crash, is the signature of native memory corruption - and DH is the
only mod here that ships native libraries (39 of them: sqlite, zstd).
Measured: with DH removed the same client runs indefinitely; with it loaded
it has died from ~2 to ~5 minutes in, every time. **Do not burn a session
assuming it is the graphics stack or the JVM version** - and do not "fix" it
by editing `conversion/overrides/config/DistantHorizons.toml`:
that file is byte-identical to Aged's by design, and `chunkGeneratorMode`
(which DH 3.x defaults to `FEATURES` and its own log calls unsafe with our
dungeonz chunk generator) does not exist in Aged's 2.2.1 schema, so setting
it breaks the parity we already have. One 13-minute survival on
`PRE_EXISTING_ONLY` was followed by a 2-minute crash on a default config, so
that hypothesis is NOT confirmed. Until this is understood, run the suite
with `CGT_EXCLUDE_MODS=DistantHorizons` and treat the mod as a known crash
source for AMD/macOS OpenGL 4.1 hosts.

**The stage script must carry `conversion/overrides/config`.** Config the pack
ships only reaches a container test if `stage_container_tests.sh` rsyncs it;
otherwise `run_client_gametests.sh`'s `cp -R` silently copies nothing and the
suite boots every mod on **stock defaults** while still reporting PASS. That
was true for the FancyMenu staging until 0.1.55 - so every client run before
then verified the menu with mod defaults, not the shipped layout. When a mod
reads a config file we ship, confirm the file is present in the game dir
(`ls custom-mods/.tmp/cgthearthwind-stage/repo/custom-mods/.tmp/cgt-game/config`
- the bind mount means it is a plain host path) AND read back **what the mod
wrote** after the run: a wrong schema is discarded and regenerated at defaults
without any error. That read-back is what caught Aged's flat `{"volume": 55}`
being thrown away by PresenceFootsteps 1.13.3, which had nested it. Keep
`--keep-dir` for any run whose point is inspecting a config; without it the
game dir is deleted at the end.

**Resource and shader packs must be staged too.** `stage_container_tests.sh`
carries `conversion/overrides/resourcepacks/` and `conversion/vendored/resourcepacks/`
into the container, and `run_client_gametests.sh` copies them into the game dir.
Without this the client suite boots with no file-based resource packs at all.
A pack whose `pack_format` predates the game's is silently dropped from the
enabled list, so a pack copied from a 1.20.1 pack without updating its
`pack.mcmeta` would load in no test and in no install. 26.2 uses resource pack
format **64** (`PackFormat.lastPreMinorVersion(CLIENT_RESOURCES)`) and datapack
format **81** (`SERVER_DATA`).

**Stale jars used to survive in a volume; bind mounts removed that class of
bug.** The old `cgtvol` seed used `docker cp`, which overwrites and adds but
never deletes, so a jar removed from `conversion/build/dist/*/mods/` kept
running from the previous run - reverting an adoption pruned the jar from the
staged dist, every verifier went green, and the next boot still died on the
exact jar that had been removed. The wrappers now bind-mount the staged tree
directly, so what the container sees is always exactly what the repo staged in
that run. If a boot still fails for something you are certain you removed,
re-run `tools/stage_container_tests.sh` and check the stage before believing
the code.

**DefaultOptions cannot be verified by the client suite.** The harness
deliberately pre-writes a complete `$WORK/options.txt` for the gametest API, so
DefaultOptions finds every option already set and applies nothing. Its defaults
are still shipped, but the suite proves only that the file parses - treat
"DefaultOptions applied" as unverified in-container, not as covered.

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
#    That entrypoint boots a REAL dedicated server INSIDE the client JVM, and
#    fabric gives it a hardcoded 10-second boot window
#    (DedicatedServerImplUtil#start -> serverFuture.get(10, SECONDS), not a
#    property). 150+ mods cannot boot in 10s, so it failed on every slow host
#    while CI passed. Our mixin raises the literal to 420s (see
#    DedicatedServerBootWindowMixin - @Pseudo + a string target, because
#    fabric-client-gametest-api-v1 is only in the TEST pack). The same test
#    also needs max-tick-time=-1 (fabric's server shares the client's JVM and
#    the client's texture-atlas uploads trip the vanilla server watchdog),
#    a settle between boot attempts, and a halt of the server fabric leaks
#    when the window expires. And CGT_TIMEOUT only reaches the suite when the
#    CONTAINER wrapper forwards it - exporting it on the host does nothing.
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
- Client gametests with DistantHorizons loaded deadlock between
  entrypoints: after the first test world closes, DH's
  `Closing all [N] databases...` never returns and the harness times out.
  Run the client suite with `CGT_EXCLUDE_MODS=DistantHorizons` (isolation
  switch in `run_client_gametests.sh`); DH is not our code and the pack
  still ships it. Third-party chipped tests run first (3 workbench
  screenshots) - a run that stops after `0002_chipped_*` means the next
  entrypoint never started.
- 26.2 renamed ids silently drop data files: a recipe referencing the
  removed `minecraft:chain` item only logs `Couldn't parse data file`
  and vanishes (no crafting entry). The client harness parse gate covers
  our namespaces and `dehydrationRecipesLoad` asserts every
  dehydration recipe still resolves; model `block/chain` is likewise
  `block/iron_chain` now.
- **Memory pressure makes this flake fire almost every time.** Measured
  0.1.57: with the host at 20 MB free and swap 17 GB/18.4 GB used (opencode
  ~10 GB plus a Colima VM that had come back up at 5 GB), the suite failed
  7 and then 9 tests across two runs; stopping the VM (free RAM 0.02 -> 4.38
  GB) and rerunning the SAME tree gave 413/413. So before believing a
  gametest failure - or blaming the change you just made - check
  `vm_stat`/`sysctl vm.swapusage` and `pgrep -fl qemu-system`. This is the
  same starvation that produced the client SIGSEGVs above.
- Headless gametests occasionally fail with `Cannot invoke
  "it.unimi.dsi.fastutil.ints.IntArrayList.getInt(int)" because
  "this.wrapped" is null` inside
  `GameTestHelper.makeMockServerPlayerInLevel` -> `PlayerList.placeNewPlayer`
  -> `ChunkMap.addEntity`. That is a fastutil 8.5.18 iterator/rehash race
  against async player-data loading, not a mod bug - `gh run rerun <id>
  --failed` clears it (seen once with 8 jobs tests, once with 3 dungeonz
  tests, once with 9 jobs + 3 primitive tree-felling tests; all pass on
  rerun).
- A `boot-smoke` failure reading
  `Failed to load datapacks, can't proceed with server load` +
  `Caused by: java.lang.NullPointerException: Cannot read field "left"
  because "r" is null` inside
  `ResourceManagerRegistryLoadTask.lambda$load$2` /
  `ParallelMapTransform$Container.applyOperation` is a PARALLEL REGISTRY-LOAD
  RACE, not a broken datapack: a genuinely malformed JSON file reports
  `Couldn't parse data file` instead. It has killed `boot-smoke` (and, in
  the same run, the `client-gametest` 1200 s timeout) with no code change
  in sight - `gh run rerun <id> --failed` cleared it. Confirm locally
  before rerunning: boot `conversion/build/dist/server` (42s on a warm
  host) and replay CI's four probes - `Done (`, no `Feature order
  cycle`, no `Error upgrading chunk`, `PROBE_ITEM_OK`, `PROBE_COG_OK`.
  A local boot that reaches `Done` with every probe green means the
  failure was the runner, not the pack.

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
- **PARITY IS THE DEFAULT. Never take a deviation from Aged unless the user has
  approved that specific deviation.** If our behaviour differs from Aged's,
  match Aged - do not "improve" it, do not drop a mod Aged ships, and do not
  quietly pick the nicer option. When a deviation is genuinely wanted, ask
  first, then record it in `docs/RELEASE_1.0_PARITY.md` with the reason, and say
  so in `docs/PLAYER_CHANGES.md`. Every mod Aged ships stays in the pack; if its
  behaviour is wrong for us, configure it, suppress it from our client, or
  rebuild it in-tree - but the mod itself stays. **There are currently ZERO
  approved deviations** (`docs/RELEASE_1.0_PARITY.md` section 5.11). The
  FancyMenu first-run panel was investigated as one and is not: Aged's own
  `options.txt` ships `modpack_mode = 'true'`, and FancyMenu's `MixinGui` opens
  the welcome panel only when `showWelcomeScreen && !modpackMode && screen
  instanceof TitleScreen`, so modpack mode suppresses it by design and we are
  already at parity. The panel was only ever visible in our gametest, which
  creates a fresh game directory where FancyMenu rewrites its own config to
  `modpack_mode = false`; `FirstRunMenuGuard` re-asserts the shipped values every
  tick and `ScreensTourGameTests` fails if `modpack_mode` is not on.
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
  (inventory + containers), campfire water purification (1000-tick boil,
  frozen on a dark fire) and the Potion of Hydration brewing chain.
  Downed/revive is a port of revive 1.0.7 at full parity (0.1.47): **no
  bleedout timer** (Aged's `timer` is -1), crouch + non-potion hand ARMS the
  Revive button, one click revives at **2 HP** with a 600-tick
  `revive:aftermath`. The temperature corpus carries **Aged's** manager
  numbers, not EnvironmentZ's upstream defaults. Purified water is a real
  fluid: it displaces vanilla water as it flows (the reference's
  `WaterFluidMixin.spreadTo`, ported as a plain override because our fluid
  already extends 26.2 `WaterFluid` and 26.2's signature already receives the
  new `FluidState`), and the purified bucket fills a vanilla cauldron to
  LEVEL 3 - the reference's `CauldronBehaviorMixin`, which on 26.2 needs an
  invoker mixin because `CauldronInteraction.Dispatcher#put` is
  package-private. Both fluid behaviours needed a line that is easy to miss:
  26.2's `WaterFluid#canBeReplacedWith` refuses any fluid in
  `FluidTags.WATER` (which purified water is in) and
  `WaterFluid#createLegacyBlock` hardcodes `Blocks.WATER`, so a fluid that
  merely extends `WaterFluid` writes VANILLA water. Both are overridden.
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
- `hearthwind-client`: HUD, Nutrients/Skills/Jobs/Party screens, SeasonHud,
  WelcomeScreen, and the shared 200x215 panel chrome (LibZ tabs floating on the
  21 rows above the panel) measured pixel-for-pixel off the Aged captures.
  The old `SurvivalInfoScreen` meter cards were DELETED in 0.1.34: Aged has no
  hydration/temperature panel (Dehydration and EnvironmentZ ship no screen and
  no keybind), so for parity nothing binds one. Aged's own stat panel -
  NutritionZ's 176x142 diet screen - is a pixel-exact rebuild, opened by the
  9x9 button at the inventory's top right.
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
- Client suite run with FancyMenu LOADED (`CGT_EXCLUDE_MODS=DistantHorizons`
  only). FancyMenu draws the main menu, so excluding it hides exactly the
  class of bug where a GUI config points at a texture the game cannot
  resolve - that is how the "purple and black blocks" menu shipped through
  0.1.32-0.1.39 with every suite green. The container wrapper only excludes
  DistantHorizons; do not add fancymenu back to the exclude list.
- `python3 custom-mods/tools/validate_menu_assets.py` (run by the client
  harness too) whenever a FancyMenu config or a `conversion/overrides/`
  asset folder changes.
- `python3 custom-mods/tools/audit_pack_assets.py` for a PACK-WIDE asset
  audit - it indexes every model, texture, blockstate and item definition
  in the vanilla jar and all 141 client jars and reports what is genuinely
  absent. It is a DIAGNOSTIC, not a gate: 44 upstream references in mods we
  do not build are still open. The gate half lives in `validate_models.py`,
  which now scans EVERY model we ship for un-namespaced texture references -
  1.20.1 read a bare `block/x` in a mod model as `minecraft:block/x` and 26.2
  reads it as `<that mod>:block/x`, which broke 173 references across 98 files
  in nine vendored mods in 0.1.53. Same checkerboard as the 0.1.39 main menu,
  so it is worth checking whenever a port touches model JSON.
- `ruff check tools/` for python tooling changes (install once via
  `sudo dnf install -y ruff`; not yet present on this host)
