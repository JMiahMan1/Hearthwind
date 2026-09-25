# Aged 3.1.2 parity audit

**Audit date:** 2026-09-24  
**Primary reference:** downloaded Aged 3.1.2 `.mrpack` and its `modrinth.index.json` under `.tmp/aged-3.1.2/` (226 indexed files, including 212 mod jars).

This document is the source-of-truth reconciliation between the original Aged pack and the Hearthwind 26.2 rebuild. The original index contains filenames, not Modrinth project IDs, so filename/slug matching is necessarily followed by manual review for renamed mods and libraries.

## Current reconciliation

| Measure | Result |
|---|---:|
| Aged indexed files | 226 |
| Aged mod jars | 212 |
| Hearthwind manifest entries | 160 |
| Manifest `keep` | 118 |
| Manifest `rebuild` | 28 |
| Manifest `client-optional` | 9 |
| Manifest `drop` | 4 |
| Manifest `add` (26.2 exception) | 1 |
| Resolved exact/current 26.2 entries | 53 |
| Resolver `missing_for_target` | 68 |
| Resolver `api_error` | 12 |
| Generated upstream pack | 53 mods (50 server, 3 client-only) |

The 160 manifest entries are not 160 shipped mods. Unresolved Aged ports and client addons remain explicit records until their 26.2 implementations or intentional exceptions are verified. A `drop` action is **not** permission to remove an Aged feature: the current drop list was checked against the downloaded Aged jar index and contains only non-Aged additions (`terralith`, `tectonic`, `visuality`, and `waterfall-particles`). No Aged mod or Aged configuration is marked dropped. Any future Aged entry with a `drop` action is a release blocker until it is restored or has an explicit, documented parity decision.

## Hearthwind-native modules retained

The following are deliberate Hearthwind replacements and are not removed merely because they lack an Aged filename:

- `hearthwind-survival`, `hearthwind-skills`, `hearthwind-jobs`
- `hearthwind-world`, `hearthwind-primitive`, `hearthwind-client`
- `dungeonz`, `adventurez`, `fleshz`
- `lavender` and the Hearthwind guide-book content
- Hearthwind-native worldgen, water motion, jobs, skills, recipes, advancements, and gameplay data

These modules are judged against Aged behavior, not by blindly copying every old jar.

## Upstream exceptions and exclusions

| Component | Decision | Reason |
|---|---|---|
| `AmbientEnvironment` | Keep | Present in the Aged 3.1.2 index; 26.2 build resolves. |
| `Visuality` | Drop | Not present in the downloaded Aged index and not required by Hearthwind-native modules. |
| `Waterfall Particle` | Drop | Not present in the downloaded Aged index; `hearthwind-world` supplies its own water effects. |
| `Terralith` | Drop | Not present in the downloaded Aged index. Its biome/crop references remain optional compatibility data only. |
| `Tectonic` | Drop | Not present in the downloaded Aged index; do not ship a second terrain generator. |
| `Lithostitched` | Keep as 26.2 exception | Not in Aged, but the shipped Nature's Spirit `2.3.0+26.2` jar hard-requires it at runtime. |
| `C2ME` | Keep as 26.2 exception | Not in Aged, but required by the current 26.2 server world-generation path for stable multithreaded generation. |
| `supermartijn642s-core-lib` | Keep as dependency | Retained where current 26.2 builds require it; it is not claimed as an independent Aged gameplay feature. |

## Aged jars requiring manual alias/port review

The following Aged jar names were not matched confidently to a current manifest `file`/`slug` by the filename audit. They are not automatically removed: client libraries, renamed projects, and transitive dependencies must be checked before classification.

**Gameplay/content candidates (hardest first):** `Neruina`, `Stoneworks`, `BetterArcheology`, `VoidZ`, `Hearths`, `AdditionZ`, `TreeChop`, `Fishingreal`.

**Structure/content candidates:** `TLC`, `MNS`, `MES`, `Dungeons+`, `YUNG's` structure addons, `Desert Dungeon`, `Unnamed Desert`, `Better End Cities`, `Lukis Grand Capitals`, `Underground Worlds`, `Distant Horizons`.

**Ambience/audio candidates:** `DripSounds`, `ImmersiveThunder`, `Euphonium`, `PresenceFootsteps`, `Sound Physics Remastered`, `Sounds`.

**Client/UI/performance candidates:** `BadOptimizations`, `CameraOverhaul`, `Distant Horizons`, `FancyBlockParticles`, `ImmediatelyFast`, `ImmersiveUI`, `Indium`, `Iris`, `Sodium`, `EntityCulling`, `MoreCulling`, `Blur`, `Borderless Mining`, `DefaultOptions`, `Firstperson`, `LMFT`, `Melody`, `ModelFix`, `Nameplate`, `Particular`, `Rsls`, `Seamless Loading Screen`, `SkinLayers3D`, `Smooth Scrolling Refurbished`, `Smooth Swapping`, `Spawn Animations`, `TooltipFix`, `Translucency Fix`, `Welcome Screen`, and the advancement/EMI addons.

**Libraries/dependencies:** `Almanac`, `PuzzlesLib`, `Konkrete`, `LMFT`, `OctoLib`, `player-animation-lib`, `fabric-language-kotlin`, and any dependency that is required by a selected 26.2 port but is not itself an independent gameplay feature.

## Port order after the stage-1 gate

1. **Neruina** — largest unresolved Aged gameplay/library surface; requires source/API and 26.2 worldgen review.
2. **Stoneworks** — broad block/content family with material, tool, and recipe surface.
3. **BetterArcheology** — progression, excavation, loot, and world-feature integration.
4. **VoidZ / Hearths / AdditionZ** — entity and dimension-style content ports; resolve IDs and dependency overlap before implementation.
5. **TreeChop / Fishingreal** — smaller gameplay additions.
6. **Aged structure family** — TLC, MNS, MES, and YUNG addons after the core gameplay spine is stable.
7. **Aged ambience stack** — AmbientEnvironment is shipped; port DripSounds, ImmersiveThunder, Euphonium, PresenceFootsteps, Sound Physics, and Sounds only after server/client boot verification.
8. **Client look-and-feel stack** — Sodium/Iris/Visuality-class presentation only after gameplay parity is not blocked.

## Verification gate for a release

A GitHub version may be created only after:

- clean resolver/build output with no stale jars;
- fresh-world server reaches `Done` and forces a new chunk grid without broken-chunk errors;
- server container gametests pass;
- client container gametests pass with the required screenshots;
- Prism Full, Minimal, and Dev-Client receive the same verified pack;
- this audit and `docs/PLAYER_CHANGES.md` describe the shipped exceptions.

The first stage-1 smoke correctly found a missing runtime dependency: removing Lithostitched made Nature's Spirit fail before `Done`. The dependency was restored as an explicit 26.2 exception, and the pack was rebuilt.
