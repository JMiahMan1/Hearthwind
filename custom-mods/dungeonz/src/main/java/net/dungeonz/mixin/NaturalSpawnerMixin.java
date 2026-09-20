package net.dungeonz.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.DimensionInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    // 26.x: per-world setSpawnSettings is gone (GameRules are server-global),
    // so natural spawns are suppressed by cancelling the spawner per chunk.
    // Custom dungeon spawners use their own logic and are unaffected.
    @Inject(method = "spawnForChunk", at = @At("HEAD"), cancellable = true)
    private static void spawnForChunkMixin(ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnState state, List<MobCategory> spawningCategories, CallbackInfo info) {
        if (level.dimension() == DimensionInit.DUNGEON_WORLD) {
            info.cancel();
        }
    }

}
