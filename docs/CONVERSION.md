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
3. **Custom replacement mods** (Fabric, server-side, data-driven):
   | Mod | Replaces | Scope |
   |---|---|---|
   | `aged-survival` | dehydration, environmentz, nutritionz, spoiledz | thirst, temperature, diet, spoilage |
   | `aged-skills` | levelz (+jobs/party later), rpgdifficulty | skills/attributes to 30, mob scaling |
   | `aged-primitive` | earlystage, tiered, reciperemover, autotag | knapping start, recipe gating via datapack |
   | `aged-world` | fabric-seasons, seasonhud, crop_growth_modifier | seasons-lite crop/temperature effects |

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
not-yet-rebuilt mod items — vanilla fallbacks apply and the same files act as
the rebuild spec for `aged-*`.

## Current readiness (26.2)

41/57 entries resolve (including auto-resolved transitive dependencies —
the resolver walks Modrinth required-dep chains itself). Near-misses on
26.1 / 26.1.1 (YUNG's suite, End Remastered, Medieval Buildings, ModernFix,
Kiwi, The Lost Castle) auto-resolve as authors publish newer builds — just
rerun `resolve_deps.py`. Stalled-at-1.21.x entries (antique atlas, exposure,
herdspanic, log-begone) need watchlist monitoring or replacement.

## Roadmap

1. [x] Feasibility study + support matrix
2. [x] Fork, curated manifest, resolver/builder scripts
3. [x] Server datapack migration (native pack, replaces paxi)
4. [x] Boot test: local fabric 26.2 server reaches `Done` with full mod set + datapack
5. [x] Custom mod skeletons (`custom-mods/`) build on Java 25 / loom 1.17 and load on a dedicated 26.2 server
6. [ ] Gameplay systems per module (thirst/temperature/diet/spoilage -> skills -> primitive -> seasons), using the migrated datapack as tuning spec
7. [ ] Snapshot watchlist CI (nightly `--mc <latest snapshot>` probe)

## License

MIT, matching upstream. Upstream textures/configs under `resources/`,
`configs/` remain courtesy of xR4YM0ND and contributors.
