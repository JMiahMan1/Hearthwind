# Villages & Pillages 26.2 Patch

Upstream: https://github.com/Faboslav/villages-and-pillages (tag 5.8.0, MC 1.21.1) and https://modrinth.com/mod/villages-and-pillages
Port: Standalone 26.2-native port in `custom-mods/villagesandpillages/` (2026-09-01)

Fabric: `fabricloader *` (0.19.3), `fabric-api *` (0.158.0+26.2), `minecraft >=26.2` (`fabric.mod.json:27`)

Build: `./gradlew :villagesandpillages:build --no-daemon` `BUILD SUCCESSFUL` (verified 2026-09-01)
Tests: Gametest via `custom-mods/tools/run_gametests.sh` (included in 224+26 suite)

Note: No RecipeSerializer changes needed (no custom recipes); 26.2 API was mostly `Identifier` vs `ResourceLocation` and `BlockEntityType` etc., already handled in port.
