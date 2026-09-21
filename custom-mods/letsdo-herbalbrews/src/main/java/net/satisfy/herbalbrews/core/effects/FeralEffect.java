package net.satisfy.herbalbrews.core.effects;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class FeralEffect extends MobEffect {

    public FeralEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00FF00);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, Identifier.withDefaultNamespace("effect.strength"), 3.0F, AttributeModifier.Operation.ADD_VALUE);
        addAttributeModifier(Attributes.MAX_HEALTH, Identifier.withDefaultNamespace("effect.health_boost"), 4.0, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && player.isAlive()) {
            Level world = player.level();
            double radius = 10.0;
            List<Player> nearbyPlayers = world.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(radius));
            int playerCount = nearbyPlayers.size() - 1;
            if (playerCount < 0) playerCount = 0;
            int finalAmplifier = calculateFinalAmplifier(playerCount);
            if (finalAmplifier > 0) {
                addAttributeModifiers(entity.getAttributes(), finalAmplifier);
            }
        }
        return super.applyEffectTick(level, entity, amplifier);
    }

    private int calculateFinalAmplifier(int playerCount) {
        float baseAmplifier = 1.0f;
        float reductionPerPlayer = 0.1f;
        float calculatedAmplifier = baseAmplifier - (playerCount * reductionPerPlayer);
        if (calculatedAmplifier < 0) {
            calculatedAmplifier = 0;
        }
        int finalAmplifier = Math.round(calculatedAmplifier);
        return Math.max(0, Math.min(finalAmplifier, 3));
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int j) {
        return true;
    }
}
