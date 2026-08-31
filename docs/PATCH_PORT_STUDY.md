# Patch-port study: 16 kept mods without official 26.2 builds

Status snapshot 2026-08-29 via `resolve_deps.py` + Modrinth API
(raw data: `.tmp/modrinth_status.json`). All 16 are classified `keep` in
`conversion/curated/mods-manifest.json` but have no official 26.2 release yet,
so the resolver leaves them out of the pack index.

**Policy (user directive, 2026-08-29): NO mod is ever dropped.** Every mod
here is either already built locally, gets a fork port when upstream stalls,
or gets an equivalent replacement that keeps the feature alive. "Drop" is not
an available disposition anywhere in this study.

## Already solved: YUNG suite patched + rebuilt locally (6 of 16)

Documented in `docs/YUNG_API_EVALUATION.md` (2026-08-28). We do not wait for
upstream — we build Yungs for 26.2 ourselves:

- All six YUNG-GANG repos built from the preferred community 26.2 port PRs
  (MentalCokuntus #109/#50/#16/#38; jojo-chaechae #24/#75 where those are the
  only ports), every API change verified against the real 26.2 mapped jar.
- Boot-smoked on a fresh 26.2 server: `Done`, 0 errors, 4/5 structure types
  force-placed through the full yungsapi processor chain (End Island is
  code-driven worldgen; validated by clean boot + dimension registration).
- The six jars now live permanently in `conversion/vendored/` and
  `build_pack.py --server-dir` copies them into the materialized
  server/client mods dirs alongside resolved Modrinth jars. YungsApi is also
  installed in mavenLocal (`26.2-Common/Fabric-6.1.0`) for dependent builds.

| Mod | Local artifact |
|---|---|
| YungsApi | `YungsApi-26.2-Fabric-6.1.0.jar` |
| YungsBetterDesertTemples | `YungsBetterDesertTemples-26.2-Fabric-5.1.0.jar` |
| YungsBetterEndIsland | `YungsBetterEndIsland-26.2-Fabric-4.1.0.jar` |
| YungsBetterJungleTemples | `YungsBetterJungleTemples-26.2-Fabric-4.1.0.jar` |
| YungsBetterNetherFortresses | `YungsBetterNetherFortresses-26.2-Fabric-4.1.0.jar` |
| YungsBetterOceanMonuments | `YungsBetterOceanMonuments-26.2-Fabric-5.1.0.jar` |

Standing rules: re-run the YUNG clone→build→boot loop whenever the upstream
PRs move; switch to official artifacts if YUNG-GANG publishes 26.2 releases
(retire our forks, never the mods).

## Auto-resolve watchlist (6): authors actively publishing 26.1.x

These ship `26.1.x` builds and are actively maintained. Path: rerun
`resolve_deps.py` each bump; if a mod is still missing one full bump cycle
later, fork and rebuild for 26.2 (same treatment the YUNG suite got).

| Mod | Max stable | Updated | Notes |
|---|---|---|---|
| Kiwi | 26.1.2 | 2026-08-29 | Very active; only needed if a kept mod depends on it — re-audit dependency need |
| endrem | 26.1.2 | 2026-07-25 | Eyes quest = early-game direction |
| medieval_buildings | 26.1.2 | 2026-04-20 | |
| modernfix | 26.1.2 | 2026-08-24 | Verify still useful alongside lithium on 26.2 (overlap audit, not a drop) |
| tlc (The Lost Castle) | 26.1.2 | 2026-05-10 | |

## Stalled upstream — we carry the port (4)

| Mod | Max stable | Last update | We build for 26.2 how |
|---|---|---|---|
| antique-atlas | 1.21.1 | 2026-01-05 | Exploration identity item. Fork and port (AtlasScreen is the heavy part). Interim: equivalent exploration surface (vanilla explorer maps + cartographer trades, or a hearthwind-client map panel) keeps the feature alive while the port lands. |
| exposure | 1.21.1 | 2026-06-09 | Photography. Fork + port (camera item, photo renderer, clapper UI). Author still active 2026-06 — ping upstream first. |
| herdspanic | 1.21.1 | 2024-09-10 | Small mixin mod (panic AI); fork + port is day-scale. |
| log-begone | 1.21.1 | 2024-08-08 | Fork + port (log-filter mixin/config). Equivalent log4j2 filter in the server distribution is the interim; mod returns once ported. |
| noisium | 1.21.6 | 2025-06-24 | Worldgen perf fork + port; re-baseline against 26.2 vanilla first (vanilla optimized noise paths) and port only the remaining delta. |

## Re-evaluation triggers

1. Any official `26.2` release → resolver picks it up; retire the vendored jar.
2. A watchlist mod stalls one full bump cycle → start its fork port.
3. Worldgen/chunk-gen profiling pain → prioritize the noisium port.
4. Exploration milestone (Age-gated maps) → prioritize the antique-atlas port.
