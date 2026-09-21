package net.satisfy.nethervinery.core.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class NetheriteEffect extends MobEffect {

    public NetheriteEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7E4C3C);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        entity.invulnerableTime = Integer.MAX_VALUE;
        return true;
    }

    // 26.2: removeAttributeModifiers(LivingEntity, AttributeMap, int) is now
    // removeAttributeModifiers(AttributeMap); effect-end cleanup moves to onMobRemoved.
    @Override
    public void onMobRemoved(ServerLevel level, LivingEntity entity, int amplifier, Entity.RemovalReason reason) {
        entity.invulnerableTime = 0;
        super.onMobRemoved(level, entity, amplifier, reason);
    }
}
