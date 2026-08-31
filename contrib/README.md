# Hearthwind Contrib Source & Port Directory

This directory tracks third-party mods that are modified, ported, or maintained for Hearthwind 26.2 when upstream authors have not yet published an official release.

## Portfolio of Contrib Ports

### 1. Nature's Spirit (`contrib/natures-spirit/`)
* **Upstream**: [Team-Hibiscus/NaturesSpirit](https://github.com/Team-Hibiscus/NaturesSpirit)
* **Base Branch**: `26.1.2` (PR #8 multiloader foundation)
* **Target Version**: Minecraft 26.2 (Fabric Loader)
* **Patches Applied**:
  - `SurfaceSystem.buildSurface` mixin descriptor updated for 26.2 (biomes `Set` parameter).
  - 108 tree `configured_feature` JSONs updated with 26.2 mandatory `below_trunk_provider`.
  - 26.2 Mojmap migrations (`EntityTypes`, `BlockEntityTypes`, `ColorCollection<Item>` dyes/terracotta/candles, advancement trigger packages).
  - `SurfaceRules` noise & biome condition source adaptations.
* **Build Artifact**: `conversion/vendored/natures_spirit-fabric-2.3.0+26.2.jar`
### 2. YUNG's Suite (`contrib/yungs/`)
* **Upstream**: [YUNG-GANG](https://github.com/YUNG-GANG)
* **Mods Included**:
  - `YUNGs-API` (v6.1.0)
  - `YUNGs-Better-Desert-Temples` (v5.1.0)
  - `YUNGs-Better-Fortresses` (v4.1.0)
  - `YUNGs-Better-Jungle-Temples` (v4.1.0)
  - `YUNGs-Better-Ocean-Monuments` (v5.1.0)
  - `YUNGs-Better-End-Island` (v4.1.0)
* **Target Version**: Minecraft 26.2 (Fabric Loader)
* **Patches Applied**:
  - Gradle wrapper 9.6.1 update for Java 25 compilation.
  - Maven signing bypass for local build artifact publishing.
  - Removed obsolete `BrushableBlockEntityMixin` for MC 26.2 in Ocean Monuments.
* **Build Artifacts**: `conversion/vendored/Yungs*.jar`
* **Automated Build**: Run `bash contrib/build_yungs.sh`

### 3. Gardens of the Dead (`contrib/gardens-of-the-dead/`)
* **Upstream**: [ochotonida/gardens-of-the-dead](https://github.com/ochotonida/gardens-of-the-dead)
* **Base Branch**: `1.21.x`
* **Target Version**: Minecraft 26.2 (Fabric Loader)
* **Patches Applied**:
  - Migrated `RootsBlock` and `FungusBlock` to 26.2 `NetherRootsBlock` and `NetherFungusBlock` with mandatory ground tags (`BlockTags.NYLIUM`).
  - `updateShape` modernized with `ScheduledTickAccess` and `RandomSource`.
  - `SurfaceRules.isBiome` ported to 26.2 `HolderGetter<Biome>`.
  - Client particles and block entity renderers (`StandingSignRenderer`, `ParticleProviderRegistry`, `BaseAshSmokeParticle`) ported for 26.2.
* **Build Artifact**: `conversion/vendored/gardens-of-the-dead-fabric-5.0.2+26.2.jar`
* **Automated Build**: Run `bash contrib/build_gardens_of_the_dead.sh`

### 4. The Lost Castle (`contrib/the-lost-castle/`)
* **Upstream**: TeamRemastered
* **Target Version**: Minecraft 26.2 (Fabric Loader)
* **Patches Applied**:
  - Migrated `StructureProcessor` registration in `TLCProcessors` from `Codec` lambda supplier to direct `MapCodec` in `BuiltInRegistries.STRUCTURE_PROCESSOR`.
  - Modernized `FoundationProcessor` to implement `StructureProcessor` interface.
* **Build Artifact**: `conversion/vendored/tlc-fabric-2.1.1+26.2.jar`

### 5. Birds Boids (`contrib/birds-boids/`)
* **Upstream**: Tomate0613 / DoubleKekse
* **Target Version**: Minecraft 26.2 (Fabric Loader)
* **Patches Applied**: Modernized `fabric.mod.json` environment and dependency constraints for 26.2.
* **Build Artifact**: `conversion/vendored/birds-boids-fabric-1.3.1+26.2.jar`

### 6. Medieval Buildings & True Ending
* **Target Version**: Minecraft 26.2 (Fabric Loader)
* **Build Artifacts**:
  - `conversion/vendored/medieval_buildings-fabric-1.2.0+26.2.jar`
  - `conversion/vendored/tru.e-ending-1.1.4d+26.2.jar`

## Delivery Flow
1. Contrib jars are built and placed into `conversion/vendored/`.
2. `conversion/scripts/build_pack.py` bundles them into the distribution `.mrpack` metadata and `dist/server/mods/`.
3. `custom-mods/tools/sync_test_clients.sh` automatically syncs vendored jars to test clients (Prism Launcher) and dev servers.
4. Continuous Integration (`.github/workflows/build-and-test.yml`) packages and verifies all vendored dependencies on every push.

