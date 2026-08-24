# Flowing Rivers & Ocean Waves — feasibility for Aged (server-first 26.x)

Status: DESIGN DRAFT (2026-08-24). Nothing implemented; owner `aged-world`
module (+ one watchlist entry). Written after checking Modrinth 26.2
availability.

## TL;DR

All three layers are ON THE ROADMAP — nothing is dropped; they ship in
phases ordered by what a dedicated server can do today:

1. **Phase 1 – gameplay water motion** (server-side, `aged-world`):
   directional river current + ocean swell pushing boats/swimmers, foam
   wash particles, tides. Fully within our control, gametestable.
2. **Phase 2 – terrain-quality rivers**: Tectonic / Terralith already
   publish 26.2 builds; adopt at the next pack version bump for deep
   carved rivers and real coastlines.
3. **Phase 3 – visible wave surfaces**: needs client rendering. Two
   supported paths, both kept open:
   - a small **client-optional companion mod** (`aged-world-client`)
     rendering animated wave crests/water distortion, built in the same
     Gradle workspace and shipped as a separate optional download; and/or
   - a documented **shader recommendation** (Iris + Complementary) whose
     wave settings are tuned to match our swell timing.
   The pack stays fully playable without either; with them, Phase 1's
   physical swells line up with what you see.

## Design: water motion system (`aged-world`)

## Design: water motion system (`aged-world`)

### River current

- Compute a cheap **flow direction** per water column: downhill vector of
  the surrounding heightmap sampled every N blocks (rivers always descend;
  flat sections get a deterministic noise-derived drift so they never feel
  dead).
- Cache per-chunk in a data attachment or an in-memory map keyed by
  `SectionPos`; recompute lazily on first touch, drop on chunk unload.
- Apply to entities in flowing water each tick (or 4-tick interval):
  `setDeltaMovement` horizontal nudge scaled by depth & boat/passenger
  state. Boats should feel it strongly, swimmers moderately, waders little.
- Config tunables in the shared `aged_survival.json`-style config file
  (strength, max speed clamp, whitelist dimensions).

### Ocean swell & waves (gameplay layer)

- Periodic **swell pulse** (~30–45 s cycle, phase from world seed so all
  players see consistent timing): gentle push toward nearest shore
  (approximated by sampling heightmap gradient over a few blocks) plus a
  small vertical bob for boats/swimmers.
- **Foam wash**: when the swell peaks near shore, send burst particles
  (`minecraft:splash` / `minecraft:cloud`, low counts) — server-sent
  particles are visible to all clients without any client mod.
- Optional later: rare **storm surge** events tied to the seasons/weather
  module (higher swell amplitude during storms).

### Tides

- Slow global multiplier on current/swell strength (e.g. 0.6..1.4 over a
  multi-day sine), exposed to the survival temperature/thirst hooks if we
  ever want rain/tide interplay. Cheap, pure math, gametestable.

## Testing strategy (fits the new harness)

- Pure logic extracted into static helpers taking `(LevelAccessor-like
  inputs, positions)` so `AgedSurvivalGameTests`-style instance tests can
  assert: flow direction points downhill; swell pushes entities seaward;
  clamps hold at config extremes; tide function is continuous/sinusoidal.
- Integration: boot+RCON summon a boat in forceloaded chunks, sample its
  position drift after N ticks.

## Upstream watchlist (resolver)

| Mod | 26.2 status | Role |
|---|---|---|
| tectonic | 10 builds | terrain/river carving candidate |
| terralith | 6 builds | biome variety candidate |
| river-redux | none | historical "flowing rivers"; watch or skip |

Decision needed before adding either worldgen mod: existing worlds become
incompatible at generation borders; pick at pack version bump time, not
mid-cycle.

## Phase sequencing & constraints (not feature cuts)

- **Phase 1** has no blockers and lands with the `aged-world` module.
- **Phase 2** waits for the next pack version bump: worldgen mods change
  generation borders, so existing worlds must not be invalidated
  mid-cycle. Tectonic (10 builds on 26.2) vs Terralith (6 builds) is a
  realism-fit decision to make then — both stay on the watchlist until
  that decision.
- **Phase 3** is deferred only by dependency order (it tunes to Phase 1's
  swell timing); the companion client mod slots into the existing Gradle
  workspace as a new module, and the shader recommendation ships in the
  pack README. Server pack policy (`include_client_only_in_server_pack =
  false`) is untouched: both paths are optional add-ons distributed
  alongside, not inside, the server pack.
- True finite-volume water simulation stays off the table for perf
  reasons **for now**; if the host budget grows, a chunk-limited version
  (rivers only) can revisit it without design changes.
