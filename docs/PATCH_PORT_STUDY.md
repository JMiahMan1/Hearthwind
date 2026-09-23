# Patch-port study: kept mods without official 26.2 builds

Status snapshot 2026-08-29 via `resolve_deps.py` + Modrinth API
(raw data: `.tmp/modrinth_status.json`); local-port status refreshed
2026-09-22. Mods classified `keep` in
`conversion/curated/mods-manifest.json` with no official 26.2 release are
omitted from the resolver index — many are now satisfied by vendored jars
or `custom-mods` ports (see tables below). Full remaining open set:
`docs/NOT_IMPLEMENTED.md`.

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

## Auto-resolve watchlist (5): authors actively publishing 26.1.x

These ship `26.1.x` builds and are actively maintained. Path: rerun
`resolve_deps.py` each bump; if a mod is still missing one full bump cycle
later, fork and rebuild for 26.2 (same treatment the YUNG suite got).

| Mod | Max stable | Updated | Notes |
|---|---|---|---|
| Kiwi | 26.1.2 | 2026-08-29 | Very active; only needed if a kept mod depends on it — re-audit dependency need |
| medieval_buildings | 26.1.2 | 2026-04-20 | |
| modernfix | 26.1.2 | 2026-08-24 | Verify still useful alongside lithium on 26.2 (overlap audit, not a drop) |
| tlc (The Lost Castle) | 26.1.2 | 2026-05-10 | |

endrem (upstream max 26.1.2, updated 2026-07-25) left the watchlist: eyes
quest is progression-critical, so manifest action is `rebuild` — fork and
port for 26.2 at the eyes-quest milestone; do not wait on the resolver.

## Ported locally: custom-mods 1.20.1 → 26.2 (2026-09-22)

Six mods no longer wait on upstream — sources live under `custom-mods/`,
built `*-26.2+0.1.0.jar` artifacts vendored in `conversion/vendored/`, each
boot-smoke verified (`Done`, 0 fail greps):

| Mod | Module | Notes |
|---|---|---|
| logbegone | `custom-mods/logbegone` | log-filter port |
| entitycollisionfpsfix | `custom-mods/entitycollisionfpsfix` | collision perf port |
| pockets | `custom-mods/pockets` | `PocketUtil` widened to `Player` |
| couplings | `custom-mods/couplings` | door/fence/trapdoor coupling; no fabric networking |
| memoryleakfix | `custom-mods/memoryleakfix` | slimmed: biome temp ThreadLocal + client crosshair target only (rest obsolete on 26.2) |
| async-locator | `custom-mods/async-locator` | slimmed: locate command / exploration maps / ender eye / dolphin treasure; dropped ServiceLoader+SparkConfig (plain properties), villager TreasureMapForEmeralds (class gone on 26.2), SetNameFunction defer (CUSTOM_NAME component path); MAP_ID/CUSTOM_DATA/Holder\<MapDecorationType\> |
| lavender | `custom-mods/lavender` | guidebook API + inlined lavender-md; 13 mixins disabled (structure overlay, book renderer, translation injection — v1 stubs); lang flattened to plain strings; depends fabricloader/owo-lib (dropped stale `"fabric"` id — 26.x fabric-api id is `fabric-api`) |
| passable-foliage | `custom-mods/passable-foliage` | walk-through leaves (Aged PassableFoliage 8.2.1 parity); 26.1→26.2 nudge |

Queue open: next from the stalled-upstream / fork-port tables below
(antique-atlas, noisium, then `NOT_IMPLEMENTED.md` waves). All eight
ports above are vendored, boot-smoked, and deployed to Prism
(Hearthwind-Full / Minimal / Dev-Client) via `tools/update_prism.sh`
(glob includes these modules as of 2026-09-22).

## Stalled upstream — we carry the port

| Mod | Max stable | Last update | Status / plan |
|---|---|---|---|
| antique-atlas | 1.21.1 | 2026-01-05 | **OPEN.** Exploration identity item. Fork and port (AtlasScreen is the heavy part). Interim: vanilla explorer maps + cartographer trades. |
| exposure | 1.21.1 | 2026-06-09 | **DONE** — `custom-mods/exposure` port shipped + wired. |
| herdspanic | 1.21.1 | 2024-09-10 | **DONE in-house** — `hearthwind-world` `HerdPanic.java`. |
| noisium | 1.21.6 | 2025-06-24 | **OPEN.** Worldgen perf fork; re-baseline against 26.2 vanilla noise paths first, port only the remaining delta. |

Moved to ported section above: log-begone, lavender, pockets, couplings,
async-locator, memoryleakfix, passable-foliage, DEUF→entitycollisionfpsfix.

## Re-evaluation triggers

1. Any official `26.2` release → resolver picks it up; retire the vendored jar.
2. A watchlist mod stalls one full bump cycle → start its fork port.
3. Worldgen/chunk-gen profiling pain → prioritize the noisium port.
4. Exploration milestone (Age-gated maps) → prioritize the antique-atlas port.
