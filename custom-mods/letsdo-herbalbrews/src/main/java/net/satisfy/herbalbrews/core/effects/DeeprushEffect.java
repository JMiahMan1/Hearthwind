package net.satisfy.herbalbrews.core.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class DeeprushEffect extends MobEffect {
    private static final int RADIUS = 10;

    public DeeprushEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00FF00);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && player.isAlive()) {
            Level world = player.level();
            List<Player> nearbyPlayers = world.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(RADIUS), this::isAffectedEntity);
            int playerCount = nearbyPlayers.size() - 1;
            if (playerCount < 0) playerCount = 0;
            int finalAmplifier = calculateFinalAmplifier(player.getBlockY(), playerCount);
            if (finalAmplifier > 0) {
                nearbyPlayers.stream()
                        .filter(p -> p.isAlive() && p != player && !p.isCreative())
                        .forEach(p -> applyDigSpeedEffect(p, finalAmplifier));
            }
        }
        return super.applyEffectTick(level, entity, amplifier);
    }

    private int calculateFinalAmplifier(int y, int playerCount) {
        int baseAmplifier = determineBaseAmplifierByYLevel(y);
        if (playerCount <= 0) return baseAmplifier;
        return Math.max(0, baseAmplifier / playerCount);
    }

    private int determineBaseAmplifierByYLevel(int y) {
        if (y >= 50) {
            return 0;
        } else if (y >= 30) {
            return 1;
        } else if (y >= 0) {
            return 2;
        } else if (y >= -20) {
            return 3;
        } else {
            return 4;
        }
    }

    private void applyDigSpeedEffect(Player player, int amplifier) {
        MobEffectInstance currentEffect = player.getEffect(MobEffects.HASTE);
        MobEffectInstance newEffect = new MobEffectInstance(MobEffects.HASTE, 200, amplifier, false, false, false);
        if (shouldUpdateEffect(currentEffect, newEffect)) {
            player.removeEffect(MobEffects.HASTE);
            player.addEffect(newEffect);
        }
    }

    private boolean shouldUpdateEffect(MobEffectInstance currentEffect, MobEffectInstance newEffect) {
        return currentEffect == null || currentEffect.getAmplifier() != newEffect.getAmplifier();
    }

    private boolean isAffectedEntity(Player player) {
        return player.isAlive() && !player.isCreative();
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int j) {
        return true;
    }
}
