package net.satisfy.nethervinery.core.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class ImprovedHearthstoneEffect extends HearthstoneEffect {

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1));
        return super.applyEffectTick(level, entity, amplifier);
    }
}
