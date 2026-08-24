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

`custom-mods/aged-survival` ships headless gametests
(`AgedSurvivalGameTests`, fabric-gametest entrypoint). Run them all:

```bash
cd custom-mods && bash tools/run_gametests.sh [--keep-server]
# -> builds, boots a throwaway 26.2 server, runs every @GameTest,
#    prints "gametests: N/M passed", exits nonzero on failure
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

1. **Finish survival** (`custom-mods/aged-survival`) — v1 SHIPPED
   (verified boot + RCON on 26.2):
   - Diet module DONE: five `nutritionz:` item tags (fruits, vegetables,
     grains, proteins, sugars), nutrients attachment (0..100) with decay,
     deficiency debuffs (fruit->mining fatigue, vegetables/proteins->
     weakness, grains->slowness), balanced diet -> refreshed absorption
     bonus hearts. Eat hook = mixin on `Consumable#onConsume`
     (`ConsumableConsumeMixin`). NOT yet play-verified with a live client.
   - Spoilage module DONE: `spoiledz:perishable_items` tag rots stack
     items into rotten flesh on a random check interval; migrated
     `spoiledz:non_spoiling_items` tag respected as exemption; hot biomes
     double the chance. Inventory-only in v1 (containers TODO).
   - Temperature/thirst polish DONE: all tunables now in
     `config/aged_survival.json` (auto-created with defaults).
   - Remaining: client-side HUD bars for hydration/diet, container
     spoilage, in-game eat-hook verification.
2. **Skills** (`aged-skills`): levelz parity XP->levels (max 30) with
   attribute bonuses (healthBase 6, +1 HP/level etc.), mob scaling by
   distance-from-spawn (rpgdifficulty parity); big reference corpus in
   `data/levelz/` (~400 files) of the migrated datapack.
3. **Primitive upgrades** (`aged-primitive`): knapping minigame,
   sieve block using `earlystage:sieve_drops/aged_drops.json` spec,
   beginner-death forgiveness (`beginnerDeathCount: 3`), steel tier
   items + recipes.
4. **World** (`aged-world`): seasons-lite (4 seasons, crop multipliers,
   temperature hook consumed by survival module); water motion system per
   `ideas/rivers-and-waves.md` (river currents, ocean swell, foam, tides
   -> later visible wave surfaces via optional client companion/shaders;
   Tectonic/Terralith decision at next version bump).
5. **Datapack noise shrink**: each shipped item set reduces the
   remaining non-fatal loot/recipe parse warnings; re-census via
   `grep "Couldn't parse" bootN.log`.
6. **Watchlist**: periodically rerun `resolve_deps.py --mc <latest>`;
   YUNG suite/endrem/etc. return automatically as authors publish.
   Water/worldgen candidates tracked in `ideas/rivers-and-waves.md`
   (tectonic + terralith both ship 26.2 builds; adopt at next bump).
7. **Snapshot CI probe**: nightly resolver run against newest snapshot.
8. **Real art**: replace generated placeholder textures/models.
9. **Cleanup discipline**: remove `.tmp-test-server/`,
   `/tmp/opencode/*` scratch at task end.

## Verification checklist per feature

- `./gradlew build` green (Java 25, loom 1.17-SNAPSHOT)
- `bash tools/run_gametests.sh` all green (add tests for new logic)
- Boot reaches `Done`; our namespaces absent from parse-error greps
- RCON spot checks: summon item by id, apply effects, loot spawn
- `ruff check tools/` for python tooling changes (install once via
  `sudo dnf install -y ruff`; not yet present on this host)
