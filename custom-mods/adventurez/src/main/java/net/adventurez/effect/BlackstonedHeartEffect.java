package net.adventurez.effect;

import java.util.function.BiConsumer;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BlackstonedHeartEffect extends MobEffect {

    private static final AttributeModifier BLACKSTONED = new AttributeModifier(Identifier.fromNamespaceAndPath("adventurez", "blackstoned"), 1.0D, AttributeModifier.Operation.ADD_VALUE);
    private static final AttributeModifier BLACKSTONED_HEALTH = new AttributeModifier(Identifier.fromNamespaceAndPath("adventurez", "blackstoned_health"), 4.0D, AttributeModifier.Operation.ADD_VALUE);

    public BlackstonedHeartEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void createModifiers(int amplifier, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        consumer.accept(Attributes.ATTACK_DAMAGE, BLACKSTONED);
        consumer.accept(Attributes.MAX_HEALTH, BLACKSTONED_HEALTH);
    }

    @Override
    public void onMobRemoved(ServerLevel level, LivingEntity entity, int amplifier, Entity.RemovalReason reason) {
        super.onMobRemoved(level, entity, amplifier, reason);
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

}
