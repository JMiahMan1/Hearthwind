# Aged -> Modern Minecraft (26.x) Server Conversion

Fork of [xR4YM0ND/Aged](https://github.com/xR4YM0ND/Aged) (MIT) rebuilt as a
**server-focused** pack targeting modern Minecraft releases, with an easy bump
path to future releases and snapshots.

## Verdict of the feasibility study (2026-08-22)

- Target landscape: latest release `26.2` (2026-06-16), latest snapshot
  `26.3-snapshot-9` (2026-08-17).
- Loaders: **Fabric** supports both releases and snapshots. NeoForge tops out at
  release builds (`26.2.x`) and never targets snapshots.
- Full 1:1 port of Aged 3.1.2 (212 mods / 184 server-required) is **not**
  feasible today: only ~34% of server mods have exact `26.2` builds; ~10% sit on
  older `26.x`; the rest are stuck at `1.21.x` or lower.
- The pack's *identity* mods (earlystage, dehydration, environmentz, nutritionz,
  spoiledz, levelz + addons, fabric-seasons, ...) are all pre-1.21.2. These are
  small, mechanic-focused mods that we **rebuild ourselves** (see
  `custom-mods/`).

## Strategy

1. **Baseline `26.2`, bump-friendly.** All versions live in one file:
   `conversion/build.conf.json`. Changing `targets.minecraft` and re-running
   `resolve_deps.py` re-resolves every dependency for the new target (release or
   snapshot) and reports exactly what is missing.
2. **Curated manifest.** Every mod from 3.1.2 is classified in
   `conversion/curated/mods-manifest.json`: `keep` / `rebuild` / `drop` /
   `client-optional`, each with a reason. Efficiency audit highlights:
   - ~28 client-only visual/perf mods ship as an optional companion download,
     not in the server index.
   - The "Let's Do" food family (10 mods, all stalled ~1.21.1) is cut for phase
     1; ambience biome/mob packs trimmed; redundant QOL replaced by vanilla
     features or datapacks.
3. **Custom replacement mods** (Fabric, server-side, data-driven - now `hearthwind-*`):
   | Mod | Replaces | Scope |
   |---|---|---|
   | `hearthwind-survival` | dehydration, environmentz, nutritionz, spoiledz | thirst, temperature, diet, spoilage |
   | `hearthwind-skills` | levelz, rpgdifficulty | skills/attributes to 30, mob scaling, gates |
   | `hearthwind-jobs` | jobs-addon (8 jobs) + party stub | job levels, XP tracks, per-job unlocks |
   | `hearthwind-primitive` | earlystage, tiered (part), reciperemover, autotag | knapping start, recipe gating via datapack |
   | `hearthwind-world` | fabric-seasons, seasonhud, crop_growth_modifier | seasons-lite crop/temperature effects |

## Toolchain

```bash
python3 conversion/scripts/resolve_deps.py                 # readiness for configured target
python3 conversion/scripts/resolve_deps.py --mc 26.3-snapshot-9   # probe any snapshot
python3 conversion/scripts/build_pack.py --server-dir      # mrpack + plain server mods/
```

Artifacts land in `conversion/build/dist/`. The generated `.mrpack` installs
through Modrinth App / Prism / ATLauncher; `dist/server/mods` can be dropped
onto a fabric-loader server directly.

## Native server datapack (replaces paxi)

`conversion/scripts/migrate_datapack.py` ports the original 1.20.1 paxi
datapack to a native world datapack shipped at
`world/datapacks/aged-server/` (embedded in the `.mrpack` overrides and
materialized by `build_pack.py --server-dir`). Migration passes:

- directory singularization (`loot_tables -> loot_table`, `tags/items ->
  tags/item`, ...) and the 1.21.2+ recipe schema (flat string ingredients,
  `result.id`);
- tag refs to uninstalled mods become `{"required": false}` so registry
  loading never fails and entries auto-activate once our custom mods ship;
- worldgen overrides referencing cut mods are dropped (vanilla takes over);
- `minecraft:uniform` int providers flattened for 26.x;
- `pack.mcmeta` is generated from `datapack.pack_format` in
  `build.conf.json`. After a Minecraft bump re-verify the number from the
  server jar's `version.json` (`pack_version.data_major`; 26.2 = 107).

**Verified**: fabric-loader 0.19.3 + 41 resolved mods on MC 26.2 reaches
`Done` with the migrated datapack installed. Remaining log warnings are
non-fatal parse notices for loot/recipe files that reference dropped or
not-yet-rebuilt mod items - vanilla fallbacks apply and the same files act as
the rebuild spec for `hearthwind-*`.

## Current readiness (26.2)

41/57 entries resolve (including auto-resolved transitive dependencies -
the resolver walks Modrinth required-dep chains itself). Near-misses on
26.1 / 26.1.1 (YUNG's suite, End Remastered, Medieval Buildings, ModernFix,
Kiwi, The Lost Castle) auto-resolve as authors publish newer builds - just
rerun `resolve_deps.py`. Stalled-at-1.21.x entries (antique atlas, exposure,
herdspanic, log-begone) need watchlist monitoring or replacement.

## Roadmap

1. [x] Feasibility study + support matrix
2. [x] Fork, curated manifest, resolver/builder scripts
3. [x] Server datapack migration (native pack, replaces paxi)
4. [x] Boot test: local fabric 26.2 server reaches `Done` with full mod set + datapack
5. [x] Custom mod skeletons (`custom-mods/`) build on Java 25 / loom 1.17 and load on a dedicated 26.2 server
 6. [~] Gameplay systems per module (thirst/temperature/diet/spoilage -> skills -> jobs -> primitive -> seasons), using the migrated datapack as tuning spec
    - [x] Thirst (dehydration parity): hydration attachment `dehydration:hydration` 0..20, tick every 2s, `baseDrain 0.025` (~13 min) ×2 sprint +0.05 per thirst level, regen stop <6, 1 dmg/4s at 0, warnings at 12/6/3, `WaterBowlItem` +6 (+50% thirst if dirty), `BowlWaterFillHandler` bowl-on-water → `water_bowl` + bare-hand sip +2 (75% thirst) for day-0, campfire `water_bowl → purified_water_bowl`, sync `hearthwind:thirst` → `hearthwind-client` 10-droplet HUD above hunger (3 sprites `hud/thirst_*`, `RenderPipelines.GUI_TEXTURED` blitSprite, wobble ≤6, vanilla action-bar fallback), `dehydration:thirst` effect (15s)
    - [x] Temperature (environmentz parity): biome-target drift, warm/neutral armor tags, insulation/ice items, freeze/heat damage; wolf & wanderer armor sets
    - [x] Diet v1 (nutritionz parity): five `nutritionz:*` item-group tags, decaying nutrient attachment per group, deficiency debuffs, balanced-diet absorption bonus; eat hook via `Consumable#onConsume` mixin
    - [x] Spoilage v1 (spoiledz parity): `spoiledz:perishable_items` rot to rotten flesh on random checks, `non_spoiling_items` exempt, hot-biome acceleration; player inventory only for now
    - [x] Config: all thirst/temperature/diet/spoilage tunables in `config/hearthwind_survival.json` (auto-generated defaults, `thirst.baseDrain 0.025` etc.)
    - [x] Client HUD thirst bar live-verified on Prism dev client (Prism 47 jars, dev-server 46, no black-bar stretch, 6/10 blue at 12.3 hydration - see 2026-08-25 screenshot)
7. [~] Skills module (levelz + rpgdifficulty parity)
   - [x] v1 core: 12 skills, XP attachment `levelz:xp` + triangular curve to 30,
     transient attribute modifiers `hearthwind_skills:<skill>`, block-break & kill XP hooks,
     `config/hearthwind_skills.json`; 7 gametests green
   - [x] mob scaling by distance-from-spawn (rpgdifficulty parity):
     monsters gain +2 HP/+0.5 dmg per 1000 blocks past a 500-block grace
     radius, capped at 20 steps; transient modifier, no double stacking;
     gametests for math + application
   - [x] unlock gating from migrated `data/levelz/*` corpus: break gates (mining 1..27, ~600 entries) + use gates (17 stations) live via `SkillGates`
8. [x] Jobs module (jobs-addon parity) - 4 gametests: 8 job defs from corpus, `hearthwind_jobs:state` attachment, `config/hearthwind_jobs.json`, block-break/kill hooks, **`/job join/leave/info` commands with tab-complete**
   - [ ] job-restricted recipe gating + bonus rewards
9. [~] Primitive upgrades: flint tools/rock loot + ore pieces + **steel ingot/nugget/block** ✅, knapping/sieve + full `tiered` affixes remaining
10. [~] Seasons-lite (hearthwind-world) - **shipped**: `Season` enum, `hearthwind_world.json` (daysPerSeason + temp offsets + crop multipliers), `HearthwindWorld.currentSeason(world)` hook ready; crop growth + temperature wiring next
11. [ ] Snapshot watchlist CI (nightly `--mc <latest snapshot>` probe)

### Survival v1 verified state (26.2) - update 2026-08-25

Verified headless with a minimal server (fabric-loader 0.19.3 +
fabric-api 0.158.0+26.2 + hearthwind-survival + hearthwind-skills + hearthwind-jobs):

- `./gradlew build --no-daemon --max-workers=2` green (Java 25, loom 1.17.19, 5 modules).
- Boot reaches `Done`; no parse errors mentioning `nutritionz`,
  `spoiledz`, or `hearthwind_survival` / `hearthwind_skills` / `hearthwind_jobs`.
- RCON summon of `dehydration:water_bowl`, `dehydration:purified_water_bowl`,
  `environmentz:wolf_pelt` succeeds.
- First boot materializes `config/hearthwind_survival.json` (and skills/jobs) with documented defaults.
- Known limitation: diet eat-hook and spoilage decay need a live client
  session to exercise end-to-end; both are plain server-tick logic and
  boot-verified only so far.

### Testing harness

`custom-mods/tools/run_gametests.sh` boots a throwaway dedicated server
with fabric-api's gametest harness (`-Dfabric-api.gametest=true`) and runs
every `@GameTest` suite headless - **19/19 green on 26.2** (8 survival
+ 7 skills + 4 jobs; config defaults, diet eat/decay/deficiency, spoilage
rot/exemption, thirst clamp, skill XP curve + mob scaling + gates, jobs
load + XP/level + max-cap). Diet/spoilage/thirst cores were refactored to
take `Entity` / `Container` parameters so they are testable without a
client. New gameplay logic should ship with a gametest; the boot+RCON loop
remains for integration checks the suite can't reach (item registration,
datapack parse noise).

## License

MIT, matching upstream. Upstream textures/configs under `resources/`,
`configs/` remain courtesy of xR4YM0ND and contributors.
