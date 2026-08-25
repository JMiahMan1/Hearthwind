# AGENTS.md — Handbook for AI/human contributors

Modern-Minecraft (26.x) server-focused rebuild of the Aged modpack
(fork of [xR4YM0ND/Aged](https://github.com/xR4YM0ND/Aged), MIT). Working
branch: `server-26.2`. The project identity is its own; **do not prefix
commit titles with "Aged"** — use plain conventional subjects
(`feat(survival): ...`, `docs: ...`).

## Repo map

| Path | Purpose |
|---|---|
| `conversion/build.conf.json` | Single source of truth: target MC, loader, datapack pack_format |
| `conversion/scripts/resolve_deps.py` | Manifest -> Modrinth resolution incl. recursive transitive deps |
| `conversion/scripts/build_pack.py` | Builds `.mrpack` + materializes `dist/server/` (mods + world datapack) |
| `conversion/scripts/migrate_datapack.py` | Ports original paxi datapack to native 26.x world datapack |
| `conversion/datapacks/aged-server/` | Generated migrated datapack (committed; deterministic) |
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
ubuntu runners (7 GB — no memory gymnastics needed there, but keep
--no-daemon). Artifacts: mod jars + JUnit XML report. A `boot-smoke`
job (workflow_dispatch) resolves the full pack and boots a real server
expecting `Done`.

How far CI can go:

- Headless gametests + boot/RCON smoke tests: fully supported (current).
- Automated CLIENT testing: `fabric-client-gametest-api-v1` drives a REAL
  client under xvfb on runners (movement, clicks, inventory, screenshots,
  assertions). Heavy (~4 GB, minutes per scenario) — adopt when we ship
  client-side code worth testing.
- INTERACTIVE human sessions: also possible despite runners having no
  inbound ports — everything tunnels outbound:
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
   jar into a test server (NEVER the `-sources` jar — its unexpanded
   fabric.mod.json poisons logs with `${version}` warnings).
3. **Every change ships verified**: boot test + RCON checks. No "should
   work" claims.

## Testing harness (gametests — preferred)

`custom-mods/hearthwind-survival`, `hearthwind-skills` and
`hearthwind-jobs` ship headless gametests
(`HearthwindSurvivalGameTests`, `HearthwindSkillsGameTests`,
`HearthwindJobsGameTests`, fabric-gametest entrypoints). Run them all:

```bash
cd custom-mods && bash tools/run_gametests.sh [--keep-server]
# -> builds all modules, boots a throwaway 26.2 server, runs every @GameTest,
#    prints "gametests: N/M passed", exits nonzero on failure
#    (19 gametests green: 8 survival + 7 skills + 4 jobs)
```

Gotchas learned the hard way:

- The maven `fabric-api` jar is THIN (no nested modules) — the runner
  fetches `fabric-gametest-api-v1` explicitly into `mods/`. Without it
  `-Dfabric-api.gametest=true` silently does nothing.
- fabric's v1 `@GameTest` methods are INSTANCE methods on the entrypoint
  class, and the class needs a PUBLIC constructor; vanilla's
  `net.minecraft.gametest.framework.GameTest` annotation is a DIFFERENT
  annotation that will not register anything.
- loom 1.17 removed `modImplementation`; use the `modCompileClasspath`
  configuration. Child build.gradles can't call loom DSL at all (plugin
  applied via root `subprojects {}`) — module deps go in the root file
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

- `pgrep -f "fabric-server.jar"` matches its own bash command line — use
  `pgrep -f "[f]abric-server.jar"`.
- Launch with `(setsid timeout NNN java ... &)` double-fork; plain
  `nohup ... & disown` hangs the tool shell.
- Never relaunch while a previous instance is still shutting down —
  `session.lock` collisions crash boot (`DirectoryLock$LockException`).
- Summoned entities vanish instantly with no players online (modern MC
  has no spawn chunks). `/forceload add -16 -16 31 31` keeps chunks
  alive for entity/effect checks.
- Stale jars copied into `mods/` have caused false failures — after
  resource edits, REBUILD before recopying.
- JDT/LSP phantom Java errors happen; gradle build is the authority.
- RCON properties are the VANILLA names: `enable-rcon=true`,
  `rcon.port`, `rcon.password`. Fabric-style `rcon.enabled` lines are
  ignored (server rewrites server.properties and RCON stays off).
- Servers pause after 60s with no players (`pause-when-empty-seconds`,
  default 60; set `-1` in test servers) — tick loops and RCON stop
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
- Tools: no PickaxeItem/SwordItem classes — plain `new Item(props)` plus
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
  LivingEntity, ItemStack)` — mixin THAT for "finished eating" hooks.
- Vanilla effect holder constants: `MobEffects.MINING_FATIGUE`,
  `MobEffects.SLOWNESS`, `MobEffects.WEAKNESS`, `MobEffects.ABSORPTION`
  (no DIG_SLOWDOWN/MOVEMENT_SLOWNESS names in 26.x mojmap).
- Fabric data attachments: `AttachmentRegistry.<T>builder()
  .persistent(codec).copyOnDeath().buildAndRegister(id)`; access with
  `player.getAttached(...)` / `setAttached(...)`.
- Register custom items under ORIGINAL upstream namespaces (earlystage,
  agedaddition, dehydration, environmentz, levelz, tiered, ...) so the
  ~800 migrated tuning files activate unchanged.

## Next steps (priority order)

1. **Survival** (`custom-mods/hearthwind-survival`) — v1 SHIPPED
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
2. **Skills** (`hearthwind-skills`) — v1 SHIPPED (7/7 gametests green):
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
3. **Jobs** (`hearthwind-jobs`, jobs-addon parity) — 🟡 partial, 4/4 gametests green (new):
   8 jobs (fisher/miner/farmer/warrior/smither/brewer/builder/lumberjack), per-player job attachment `hearthwind_jobs:state`, level math `pointsPerLevel` (default 100), XP hooks on block break / entity kill via `JobState.awardIfMatch`, **`/job join/leave/info` commands** shipped; config `config/hearthwind_jobs.json`.
   Remaining: job-restricted recipe gating (reuses gate infra), bonus rewards. Parity: docs/FEATURE_PARITY.md 🟡 partial.
4. **Primitive** (`hearthwind-primitive`) — 🟡 partial: flint tools/rock item, stone->rock loot, ore pieces recipes, **steel ingot/nugget/block + assets** shipped; remaining: knapping minigame,
    sieve block using `earlystage:sieve_drops/aged_drops.json` spec,
    beginner-death forgiveness (`beginnerDeathCount: 3`), full `tiered` affix system.
5. **World** (`hearthwind-world`) — 🟡 partial: **seasons-lite shipped** (4 seasons over `daysPerSeason` 21, `Season.fromWorldTime()`, temp offsets + crop multipliers per season, `config/hearthwind_world.json`); wiring crop growth + temperature hook next. Water motion per `ideas/rivers-and-waves.md` (river currents, ocean swell, foam, tides -> later visible wave surfaces via optional client companion/shaders; Tectonic/Terralith decision at next version bump).
6. **Datapack noise shrink**: each shipped item set reduces the
   remaining non-fatal loot/recipe parse warnings; re-census via
   `grep "Couldn't parse" bootN.log`.
7. **Watchlist**: periodically rerun `resolve_deps.py --mc <latest>`;
   YUNG suite/endrem/etc. return automatically as authors publish.
   Water/worldgen candidates tracked in `ideas/rivers-and-waves.md`
   (tectonic + terralith both ship 26.2 builds; adopt at next bump).
   Genesis/Genesis Framework studied for design ideas only (advancement
   -wrapped gating, instruction toasts, ordered age chains) — rebuild
   in-house, see `ideas/genesis-comparison.md`; neither mod adopted.
8. **Snapshot CI probe**: nightly resolver run against newest snapshot.
9. **Real art**: replace generated placeholder textures/models.
10. **Cleanup discipline**: remove `.tmp-test-server/`,
    `/tmp/opencode/*` scratch at task end.

## Verification checklist per feature

- `./gradlew build` green (Java 25, loom 1.17-SNAPSHOT)
- `bash tools/run_gametests.sh` all green (add tests for new logic)
- Boot reaches `Done`; our namespaces absent from parse-error greps
- RCON spot checks: summon item by id, apply effects, loot spawn
- `ruff check tools/` for python tooling changes (install once via
  `sudo dnf install -y ruff`; not yet present on this host)
