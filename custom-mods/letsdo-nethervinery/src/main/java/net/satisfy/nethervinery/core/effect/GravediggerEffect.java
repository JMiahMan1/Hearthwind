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
import org.jetbrains.annotations.Nullable;

import java.util.Set;

// 26.2: InstantenousMobEffect is gone. Instant effects are plain MobEffect
// with isInstantaneous() + applyInstantaneousEffect (mirrors vinery TeleportEffect).
public class GravediggerEffect extends MobEffect {

    public GravediggerEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF69B4);
    }

    @Override
    public boolean isInstantaneous() {
        return true;
    }

    @Override
    public void applyInstantaneousEffect(ServerLevel level, @Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        teleportToDeath(target);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        teleportToDeath(livingEntity);
        return true;
    }

    private void teleportToDeath(LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            serverPlayer.getLastDeathLocation().ifPresent(deathLocation -> {
                ResourceKey<Level> deathDimension = deathLocation.dimension();
                ServerLevel targetLevel = serverPlayer.level().getServer().getLevel(deathDimension);
                if (targetLevel != null) {
                    BlockPos pos = deathLocation.pos();
                    serverPlayer.teleportTo(targetLevel, pos.getX(), pos.getY(), pos.getZ(),
                            Set.of(), Mth.wrapDegrees(serverPlayer.getYRot()), Mth.wrapDegrees(serverPlayer.getXRot()), true);
                }
            });
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration == 1;
    }
}
