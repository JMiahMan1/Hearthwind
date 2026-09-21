package net.satisfy.farm_and_charm.core.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class EffectJugItem extends Item {
    private final boolean returnBottle;

    public EffectJugItem(Properties properties, int duration, boolean returnBottle) {
        super(properties);
        this.returnBottle = returnBottle;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        ItemStack eaten = itemStack.getOrDefault(DataComponents.CONSUMABLE, net.minecraft.world.item.component.Consumable.builder().build()).onConsume(level, livingEntity, itemStack);
        if (this.returnBottle) {
            return GeneralUtil.convertStackAfterFinishUsing(livingEntity, eaten, Items.GLASS_BOTTLE, this);
        }
        return eaten;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag);

        List<net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect> possibleEffects = new java.util.ArrayList<>();
        var _consumable = itemStack.get(DataComponents.CONSUMABLE);
        if (_consumable != null) for (var _fx : _consumable.onConsumeEffects()) if (_fx instanceof net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect _ae) for (var _inst : _ae.effects()) possibleEffects.add(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(_inst, _ae.probability()));

        List<Pair<Holder<Attribute>, AttributeModifier>> attributeModifiers = Lists.newArrayList();

        if (possibleEffects.isEmpty()) {
            tooltip.accept(Component.translatable("effect.none").withStyle(ChatFormatting.GRAY));
        } else {
            for (var _aeLoop : possibleEffects) { for (net.minecraft.world.effect.MobEffectInstance possibleEffect : _aeLoop.effects()) {
                MutableComponent effectLine = Component.translatable(possibleEffect.getDescriptionId());
                MobEffect mobEffect = possibleEffect.getEffect().value();

                mobEffect.createModifiers(possibleEffect.getAmplifier(), (attributeHolder, baseModifier) -> {
                    AttributeModifier scaledModifier = new AttributeModifier(
                            baseModifier.id(),
                            baseModifier.amount() * (double) (possibleEffect.getAmplifier() + 1),
                            baseModifier.operation()
                    );
                    attributeModifiers.add(new Pair<>(attributeHolder, scaledModifier));
                });

                if (possibleEffect.getDuration() > 20) {
                    effectLine = Component.translatable(
                            "potion.withDuration",
                            effectLine,
                            MobEffectUtil.formatDuration(possibleEffect, _aeLoop.probability(), tooltipContext.tickRate())
                    );
                }

                tooltip.accept(effectLine.withStyle(mobEffect.getCategory().getTooltipFormatting())); }
            }
        }

        if (!attributeModifiers.isEmpty()) {
            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for (Pair<Holder<Attribute>, AttributeModifier> pair : attributeModifiers) {
                AttributeModifier modifier = pair.getSecond();
                double amount = modifier.amount();
                double displayValue;

                if (modifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && modifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    displayValue = amount;
                } else {
                    displayValue = amount * 100.0;
                }

                if (amount > 0.0) {
                    tooltip.accept(Component.translatable(
                            "attribute.modifier.plus." + modifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(displayValue),
                            Component.translatable(pair.getFirst().value().getDescriptionId())
                    ).withStyle(ChatFormatting.BLUE));
                } else if (amount < 0.0) {
                    displayValue *= -1.0;
                    tooltip.accept(Component.translatable(
                            "attribute.modifier.take." + modifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(displayValue),
                            Component.translatable(pair.getFirst().value().getDescriptionId())
                    ).withStyle(ChatFormatting.RED));
                }
            }
        }
    }
}
