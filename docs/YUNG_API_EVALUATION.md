# YUNGs-API / YUNG structure mods — 26.2 port evaluation

Date: 2026-08-28. Target: MC 26.2 Fabric (Hearthwind `server-26.2`).
Question: are the open upstream 26.2 PRs legit, and can we build on them
instead of waiting for official releases (or writing our own port)?

## TL;DR

**Yes.** All six YUNG-GANG repos have open 26.2 port PRs against the
`26.1.2` branches. We verified every API change against the real 26.2
mapped jar, built all six from PR branches on this host, and boot-smoked
a fresh server with the full suite: `Done`, **0 errors**, and 4/5
structure types force-placed through the full yungsapi processor chain
(the 5th, Better End Island, is code-driven worldgen — validated by a
clean boot and clean dimension registration on a fresh world).

**Recommended pick: the MentalCokuntus series (`Port to Minecraft 26.2`)**.
It is a strict superset of jojo-chaechae's series with two extra fixes and
better defaults. Where only jojo's PR exists (Ocean Monuments #24, End
Island #75), that PR builds and works fine.

## The PRs

| Repo | jojo-chaechae "Update to MC 26.2" | MentalCokuntus "Port to MC 26.2" |
|---|---|---|
| YUNGs-API | #108 (Jul 28) | **#109** (Aug 2) — preferred |
| YUNGs-Better-Desert-Temples | #49 | **#50** — preferred |
| YUNGs-Better-Jungle-Temples | #15 | **#16** — preferred |
| YUNGs-Better-Fortresses (nether) | #37 | **#38** — preferred |
| YUNGs-Better-Ocean-Monuments | #24 (only one) | — |
| YUNGs-Better-End-Island | #75 (only one) | — |

All are single-commit, base `26.1.2`, `mergeable_state: clean`.

### API verification (javap against minecraft-merged-deobf-26.2.jar)

Every vanilla-API claim in both PRs checks out:

1. `EntitySpawnRequest` record exists; ctor `(EntitySpawnReason, boolean)`;
   `EntityType.create(ValueInput, Level, EntitySpawnRequest)` overload exists.
2. `Identifier.fromNamespaceAndPath(...)` exists (26.2 renamed/refactored
   the old factory).
3. `CriterionTrigger` now lives at `net.minecraft.advancements.triggers`.
4. `BuiltInRegistries.STRUCTURE_PROCESSOR` is now
   `Registry<MapCodec<? extends StructureProcessor>>` — both PRs register
   the `MapCodec` directly (the old `StructureProcessorType` class still
   exists but the registry no longer stores it).
5. `StructureProcessor` is now an **interface** (`implements`, not `extends`).
6. `Blocks.BANNER` / `Blocks.WALL_BANNER` are now `ColorCollection<Block>`
   records. `ColorCollection.pick(DyeColor)` (#108) and the record accessor
   `.black()` (#109) both compile; #109 preserves the original
   wall-banner semantics, #108 silently switches to a floor banner.
7. `BlockTags.DEFAULT_IMMUNE_TO` exists — #109's new default for
   `AutoRegisterEntityType.Builder.immuneTo`. #108 instead invents a fake
   `yungsapi:none` tag (resolves empty = benign but sloppy).
8. `EntityType.Builder.immuneTo(TagKey<Block>)` — vanilla really did
   change `ImmutableSet<Block>` → `TagKey<Block>`, so the signature break
   in both PRs is forced, not invented.

### Why #109 over #108

- #109's author independently built #108: **fails at
  `:NeoForge:compileJava`** (`NeoForgeMod.NAMETAG_DISTANCE` was removed in
  NeoForge 26.2). We are Fabric-only so this wouldn't bite us directly,
  but it means #108 as-is breaks any NeoForge downstream and can't be
  merged upstream.
- #109 additionally adds a real bugfix: `BlockStateRandomizer` now
  accepts float accumulation drift up to `1.0E-5F` (JSON object iteration
  order can make probabilities that sum to exactly 1.0 exceed `1.0F`,
  which silently drops blocks from structures).
- Cleaner defaults (real vanilla tag vs fake tag; wall banner preserved).

## Local build results (this host, Java 26.0.2.1, gradle daemon)

| Repo @ PR | Build | Jar |
|---|---|---|
| YUNGs-API @ #109 | ✅ Common + Fabric (9m31s incl. loom setup) | `YungsApi-26.2-Fabric-6.1.0.jar` |
| Desert-Temples @ #50 | ✅ 30s (Common resolves API from mavenLocal) | `YungsBetterDesertTemples-26.2-Fabric-5.1.0.jar` |
| Jungle-Temples @ #16 | ✅ | `YungsBetterJungleTemples-26.2-Fabric-4.1.0.jar` |
| Fortresses @ #38 | ✅ | `YungsBetterNetherFortresses-26.2-Fabric-4.1.0.jar` |
| Ocean-Monuments @ #24 | ✅ 36s | `YungsBetterOceanMonuments-26.2-Fabric-5.1.0.jar` |
| End-Island @ #75 | ✅ 29s | `YungsBetterEndIsland-26.2-Fabric-4.1.0.jar` |

Two local build-system patches were needed (both in our scratch clones
only; candidates for upstream PRs):

1. **Gradle wrapper 9.2.0 → 9.6.1.** Gradle 9.2.0's embedded Groovy
   cannot run on a Java 26 host (`Unsupported class file major version
   70`). #108 already includes exactly this bump; #109 assumes a Java 25
   host. On hosts with Java 25 available, no bump needed.
2. **Conditional signing.** `buildSrc/.../multiloader-common.gradle`
   unconditionally `sign(publishing.publications)`, so
   `publishToMavenLocal` fails without the author's private signing
   keys. Guarded it with `if (project.hasProperty("signing.keyId"))`.

## Boot-smoke results (throwaway server, MC 26.2, fabric-api 0.158.0,
cloth-config 26.2.155)

- Fresh world, API + all 5 structure mods: **`Done (x.xxxs)`, 0
  ERROR/Exception lines.**
- `betterendisland`'s `bei_ExtraDragonFight` dimension registers cleanly
  on worlds created with the mod present. (Adding End Island to a world
  that predates it logs a benign `key missing: bei_ExtraDragonFight`
  datafix warning.)
- Force-placed structures via RCON (exercises template loading, all
  autoregistered structure processors, and `EntityProcessorMixinFabric`):
  - `betterdeserttemples:desert_temple` ✅ (~65k blocks verified by region fill count)
  - `betterjungletemples:jungle_temple` ✅
  - `betteroceanmonuments:ocean_monument` ✅
  - `betterfortresses:fortress` (nether) ✅
- Only warning-class noise: `Reference map 'yungsapi.refmap.json' could
  not be read` — benign (0 mixin apply errors; targets resolve).
- One non-fatal error observed only when force-placing a jungle temple
  at y=-60 (below build height) — artifact of the test placement, not of
  natural generation.

### Cloth-config dependency

The structure mods require `cloth-config2 >= 26.2.155` (modmenu
20.0.x on clients). Our pack already ships `cloth-config-26.2.155.jar`
→ satisfied. Structure mod server jars do NOT require modmenu.

## Strategy

1. **Adopt the PR branches.** Fork the six YUNG-GANG repos into our org
   at the picked PR heads (or carry the jar set in our pack build),
   until upstream merges/releases. Re-run
   `tools/test_yungs_api.sh` whenever the PRs move.
2. **Resolver wiring.** Treat these as "vendored 26.2 builds": either
   point `resolve_deps.py` at our forks, or vendor the six jars into the
   pack staging the same way generated jars are handled.
3. **Watch upstream.** When YUNG-GANG merges + publishes 26.2 releases
   to `maven.blamejared.com`, switch back to official artifacts and
   retire our forks.
4. **Guardrail.** `test_yungs_api.sh` automates the whole loop (clone →
   build → mavenLocal → boot → place-structure assertions) and must stay
   green before any YUNG jar enters the pack.

## Misc notes

- YUNG-GANG repo for nether fortresses is named `YUNGs-Better-Fortresses`
  (Modrinth slug `yungs-better-nether-fortresses`).
- `YUNGs-Better-End-Island` moved `yungnickyoung/…` → `YUNG-GANG/…`
  (redirects follow).
- Structure namespaces: `betterdeserttemples`, `betterjungletemples`,
  `betterfortresses`, `betteroceanmonuments`, `betterendisland`.
  End Island has NO structure JSON — worldgen is entirely code-driven
  (custom features + a `bei_ExtraDragonFight` dimension + block tags).
- Building only `:Fabric:build` skips NeoForge entirely; the Common
  subproject must still be published to mavenLocal for dependent mods'
  `:Common:compileJava`.
