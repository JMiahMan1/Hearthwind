package net.satisfy.nethervinery.core.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

// 26.2: InstantenousMobEffect is gone. Instant effects are plain MobEffect
// with isInstantaneous() + applyInstantaneousEffect (mirrors vinery TeleportEffect).
public class HearthstoneEffect extends MobEffect {

    public HearthstoneEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF69B4);
    }

    @Override
    public boolean isInstantaneous() {
        return true;
    }

    @Override
    public void applyInstantaneousEffect(ServerLevel level, @Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        teleportHome(target);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        teleportHome(livingEntity);
        return true;
    }

    private void teleportHome(LivingEntity livingEntity) {
        if (livingEntity.level().isClientSide() || !(livingEntity instanceof ServerPlayer serverPlayer)) {
            return;
        }
        // 26.2: getRespawnPosition()/getRespawnDimension() are gone;
        // respawn data lives on ServerPlayer.RespawnConfig.
        ServerPlayer.RespawnConfig respawnConfig = serverPlayer.getRespawnConfig();
        if (respawnConfig != null && respawnConfig.respawnData() != null) {
            ResourceKey<Level> respawnDimension = respawnConfig.respawnData().dimension();
            ServerLevel targetLevel = serverPlayer.level().getServer().getLevel(respawnDimension);
            BlockPos respawnPos = respawnConfig.respawnData().pos();
            if (targetLevel != null && (targetLevel.getBlockState(respawnPos).getBlock() instanceof BedBlock)) {
                Vec3 pos = Vec3.atBottomCenterOf(respawnPos);
                serverPlayer.teleportTo(targetLevel, pos.x, pos.y, pos.z, Set.of(), Mth.wrapDegrees(serverPlayer.getYRot()), Mth.wrapDegrees(serverPlayer.getXRot()), true);
            }
        } else {
            // 26.2: ServerLevel.getSharedSpawnPos() is gone; world spawn is getRespawnData().
            Vec3 pos = Vec3.atBottomCenterOf(serverPlayer.level().getRespawnData().pos());
            serverPlayer.teleportTo(pos.x, pos.y, pos.z);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration == 1;
    }
}
