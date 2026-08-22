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

## Current readiness (26.2)

35/52 curated entries resolve exactly on 26.2 today. Near-misses on 26.1 /
26.1.1 (YUNG's suite, End Remastered, Medieval Buildings, ModernFix, Kiwi,
The Lost Castle) auto-resolve as authors publish newer builds — just rerun
`resolve_deps.py`. Stalled-at-1.21.x entries (antique atlas, exposure,
herdspanic, log-begone, playeranimator, noisium) need watchlist monitoring or
replacement.

## Roadmap

1. [x] Feasibility study + support matrix
2. [x] Fork, curated manifest, resolver/builder scripts
3. [ ] Custom mod skeletons (`custom-mods/`) with parity configs from 1.20.1 pack
4. [ ] Native world datapack (ore-piece recipes etc., replacing paxi)
5. [ ] Spin up local fabric 26.2 server, boot test with resolved set
6. [ ] Snapshot watchlist CI (nightly `--mc <latest snapshot>` probe)

## License

MIT, matching upstream. Upstream textures/configs under `resources/`,
`configs/` remain courtesy of xR4YM0ND and contributors.
