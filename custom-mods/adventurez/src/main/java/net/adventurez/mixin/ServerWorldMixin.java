package net.adventurez.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.adventurez.entity.SummonerEntity;
import net.adventurez.init.ConfigInit;
import net.adventurez.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin {

    @Inject(method = "tickThunder", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LightningBolt;setVisualOnly(Z)V", shift = At.Shift.AFTER))
    public void tickThunder(LevelChunk chunk, CallbackInfo info) {
        int summonerSpawnChance = ConfigInit.CONFIG.summoner_thunder_spawn_chance;
        if (summonerSpawnChance != 0) {
            int spawnChanceInt = ((ServerLevel) (Object) this).getRandom().nextInt(summonerSpawnChance) + 1;
            if (spawnChanceInt == 1) {
                ServerLevel level = (ServerLevel) (Object) this;
                ChunkPos chunkPos = chunk.getPos();
                int minX = chunkPos.getMinBlockX();
                int minZ = chunkPos.getMinBlockZ();
                BlockPos blockPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, level.getBlockRandomPos(minX, 0, minZ, 15));
                if (SpawnPlacements.isSpawnPositionOk(EntityInit.SUMMONER, level, blockPos)
                        && SpawnPlacements.checkSpawnRules(EntityInit.SUMMONER, level, EntitySpawnReason.EVENT, blockPos, level.getRandom())) {
                    SummonerEntity summonerEntity = EntityInit.SUMMONER.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                    if (summonerEntity != null) {
                        summonerEntity.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        summonerEntity.finalizeSpawn(level, level.getCurrentDifficultyAt(blockPos), EntitySpawnReason.EVENT, null);
                        level.addFreshEntity(summonerEntity);
                        summonerEntity.spawnAnim();
                    }
                }
            }
        }
    }

}
