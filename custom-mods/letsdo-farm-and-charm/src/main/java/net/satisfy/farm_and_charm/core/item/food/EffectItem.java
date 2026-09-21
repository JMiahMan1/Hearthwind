package net.satisfy.farm_and_charm.core.item.food;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class EffectItem extends Item {
    private final boolean returnBowl;

    public EffectItem(Properties properties, int duration, boolean returnBowl) {
        super(properties);
        this.returnBowl = returnBowl;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        List<net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect> list2 = new java.util.ArrayList<>();
        var _consumable = itemStack.get(DataComponents.CONSUMABLE);
        if (_consumable != null) for (var _fx : _consumable.onConsumeEffects()) if (_fx instanceof net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect _ae) for (var _inst : _ae.effects()) list2.add(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(_inst, _ae.probability()));
        List<Pair<Holder<Attribute>, AttributeModifier>> list3 = Lists.newArrayList();
        if (list2.isEmpty()) {
            tooltip.accept(Component.translatable("effect.none").withStyle(ChatFormatting.GRAY));
        } else {
            for (var _aeLoop : list2) { for (net.minecraft.world.effect.MobEffectInstance statusEffectInstance : _aeLoop.effects()) {
                MutableComponent mutableText = Component.translatable(statusEffectInstance.getDescriptionId());
                MobEffectInstance statusEffect = statusEffectInstance;

                statusEffect.getEffect().value().createModifiers(statusEffect.getAmplifier(), (holderx, attributeModifierx) -> {
                    AttributeModifier entityAttributeModifier = new AttributeModifier(
                            attributeModifierx.id(),
                            attributeModifierx.amount() * (double)(statusEffect.getAmplifier() + 1),
                            attributeModifierx.operation()
                    );
                    list3.add(new Pair<>(holderx, entityAttributeModifier));
                });
                if (statusEffectInstance.getDuration() > 20) {
                    mutableText = Component.translatable(
                            "potion.withDuration",
                            mutableText, MobEffectUtil.formatDuration(statusEffect, _aeLoop.probability(), tooltipContext.tickRate()));
                }

                tooltip.accept(mutableText.withStyle(statusEffect.getEffect().value().getCategory().getTooltipFormatting())); }
            }
        }

        if (!list3.isEmpty()) {
            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for (Pair<Holder<Attribute>, AttributeModifier> pair : list3) {
                AttributeModifier entityAttributeModifier3 = pair.getSecond();
                double d = entityAttributeModifier3.amount();
                double e;
                if (entityAttributeModifier3.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && entityAttributeModifier3.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    e = entityAttributeModifier3.amount();
                } else {
                    e = entityAttributeModifier3.amount() * 100.0;
                }

                if (d > 0.0) {
                    tooltip.accept(
                            Component.translatable(
                                            "attribute.modifier.plus." + entityAttributeModifier3.operation().id(),
                                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e), Component.translatable(pair.getFirst().value().getDescriptionId()))
                                    .withStyle(ChatFormatting.BLUE)
                    );
                } else if (d < 0.0) {
                    e *= -1.0;
                    tooltip.accept(
                            Component.translatable(
                                            "attribute.modifier.take." + entityAttributeModifier3.operation().id(),
                                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e), Component.translatable(pair.getFirst().value().getDescriptionId()))
                                    .withStyle(ChatFormatting.RED)
                    );
                }
            }
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        ItemStack eaten = itemStack.getOrDefault(net.minecraft.core.component.DataComponents.CONSUMABLE, net.minecraft.world.item.component.Consumable.builder().build()).onConsume(level, livingEntity, itemStack);
        if (this.returnBowl) {
            return GeneralUtil.convertStackAfterFinishUsing(livingEntity, eaten, Items.BOWL, this);
        }
        return eaten;
    }
}
