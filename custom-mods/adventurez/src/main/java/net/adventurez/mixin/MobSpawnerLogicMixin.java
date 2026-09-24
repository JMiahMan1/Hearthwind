package net.adventurez.mixin;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.adventurez.entity.BlazeGuardianEntity;
import net.adventurez.init.ConfigInit;
import net.adventurez.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

@Mixin(BaseSpawner.class)
public class MobSpawnerLogicMixin {

    @Shadow
    private int spawnDelay = 20;

    @Shadow
    @Nullable
    private SpawnData nextSpawnData;

    @Unique
    private boolean spawnGuardian = false;

    @Inject(method = "setEntityId", at = @At("HEAD"))
    private void setEntityId(EntityType<?> type, @Nullable Level level, RandomSource random, BlockPos pos, CallbackInfo info) {
        if (type != null && type.equals(EntityTypes.BLAZE)) {
            this.spawnGuardian = true;
        }
    }

    @Inject(method = "load", at = @At("HEAD"))
    private void loadMixin(@Nullable Level level, BlockPos pos, ValueInput input, CallbackInfo info) {
        this.spawnGuardian = input.getBooleanOr("SpawnGuardian", false);
    }

    @Inject(method = "save", at = @At("HEAD"))
    private void saveMixin(ValueOutput output, CallbackInfo info) {
        output.putBoolean("SpawnGuardian", this.spawnGuardian);
    }

    @Inject(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;spawnDelay:I", ordinal = 1))
    private void serverTick(ServerLevel level, BlockPos pos, CallbackInfo info) {
        if (this.spawnDelay == 2 && ConfigInit.CONFIG.allow_guardian_spawner_spawn) {
            if (this.spawnGuardian) {
                BlazeGuardianEntity blazeGuardianEntity = EntityInit.BLAZE_GUARDIAN.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                if (blazeGuardianEntity != null) {
                    blazeGuardianEntity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
                    int randomCheck = level.getRandom().nextInt(3);
                    blazeGuardianEntity.setPos(pos.north(level.getRandom().nextInt(3) - 1).east(randomCheck - 1 == 0 ? -1 : randomCheck).getX() + 0.5D,
                            pos.north(level.getRandom().nextInt(3) - 1).east(randomCheck - 1 == 0 ? -1 : randomCheck).getY(),
                            pos.north(level.getRandom().nextInt(3) - 1).east(randomCheck - 1 == 0 ? -1 : randomCheck).getZ() + 0.5D);
                    blazeGuardianEntity.setYRot(level.getRandom().nextFloat() * 360F);
                    blazeGuardianEntity.setXRot(0.0F);
                    level.addFreshEntity(blazeGuardianEntity);
                    this.spawnDelay = 600;
                    this.spawnGuardian = false;
                }
            } else if (this.nextSpawnData != null) {
                String blazeId = this.nextSpawnData.getEntityToSpawn().getString("id").orElse("");
                if (blazeId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityTypes.BLAZE).toString())
                        && !level.getEntitiesOfClass(BlazeGuardianEntity.class, new AABB(pos).inflate(16D), EntitySelector.NO_SPECTATORS).isEmpty()) {
                    this.spawnDelay = 600;
                }
            }
        }
    }
}
