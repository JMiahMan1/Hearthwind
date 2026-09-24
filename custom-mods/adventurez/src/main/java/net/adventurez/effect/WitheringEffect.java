package net.adventurez.effect;

import java.util.function.BiConsumer;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WitheringEffect extends MobEffect {

    private static final AttributeModifier WITHERING = new AttributeModifier(Identifier.fromNamespaceAndPath("adventurez", "withering"), -0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    public WitheringEffect(MobEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        entity.hurt(entity.damageSources().wither(), 0.5F);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 80 == 0;
    }

    @Override
    public void createModifiers(int amplifier, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        consumer.accept(Attributes.MOVEMENT_SPEED, WITHERING);
    }

}
