package net.satisfy.herbalbrews.core.items;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DrinkBlockItem extends BlockItem {

    public DrinkBlockItem(Block block, Properties settings) {
        super(block, settings.stacksTo(16));
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        return ItemUtils.startUsingInstantly(level, player, interactionHand);
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        if (!Objects.requireNonNull(context.getPlayer()).isCrouching()) {
            return null;
        }
        BlockState blockState = this.getBlock().getStateForPlacement(context);
        return blockState != null && this.canPlace(context, blockState) ? blockState : null;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        ItemStack result = super.finishUsingItem(stack, world, user);

        PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (potionContents.hasEffects()) {
            potionContents.forEachEffect(user::addEffect, 1.0F);
        }

        return result;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext tooltipContext, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        Map<Identifier, MobEffectInstance> combined = new LinkedHashMap<>();

        PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (potionContents.hasEffects()) {
            potionContents.forEachEffect(inst -> {
                Identifier id = BuiltInRegistries.MOB_EFFECT.getKey(inst.getEffect().value());
                MobEffectInstance prev = combined.get(id);
                if (prev == null) {
                    combined.put(id, inst);
                } else {
                    int amplifier = Math.max(prev.getAmplifier(), inst.getAmplifier());
                    int duration = Math.max(prev.getDuration(), inst.getDuration());
                    boolean ambient = prev.isAmbient() || inst.isAmbient();
                    boolean visible = prev.isVisible() || inst.isVisible();
                    boolean icon = prev.showIcon() || inst.showIcon();
                    combined.put(id, new MobEffectInstance(prev.getEffect(), duration, amplifier, ambient, visible, icon));
                }
            }, 1.0F);
        }

        if (combined.isEmpty()) {
            tooltip.accept(Component.translatable("effect.none").withStyle(ChatFormatting.GRAY));
        } else {
            for (MobEffectInstance inst : combined.values()) {
                MutableComponent effectName = Component.translatable(inst.getDescriptionId());
                if (inst.getDuration() > 20) {
                    effectName = Component.translatable("potion.withDuration", effectName, MobEffectUtil.formatDuration(inst, 1.0f, tooltipContext.tickRate()));
                }
                tooltip.accept(effectName.withStyle(inst.getEffect().value().getCategory().getTooltipFormatting()));
            }
        }

        List<Pair<Attribute, AttributeModifier>> attributeModifiers = Lists.newArrayList();
        if (stack.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
            ItemAttributeModifiers itemAttributeModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            itemAttributeModifiers.modifiers().forEach(entry -> {
                Attribute attribute = entry.attribute().value();
                double amount = entry.modifier().amount();
                AttributeModifier modifier = new AttributeModifier(entry.modifier().id(), amount, entry.modifier().operation());
                attributeModifiers.add(new Pair<>(attribute, modifier));
            });
        }

        if (!attributeModifiers.isEmpty()) {
            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            for (Pair<Attribute, AttributeModifier> pair : attributeModifiers) {
                AttributeModifier modifier = pair.getSecond();
                double amount = modifier.amount();
                double displayAmount = (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                        ? amount * 100.0
                        : amount;

                if (amount > 0.0) {
                    tooltip.accept(Component.translatable(
                                    "attribute.modifier.plus." + modifier.operation().id(),
                                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(displayAmount),
                                    Component.translatable(pair.getFirst().getDescriptionId()))
                            .withStyle(ChatFormatting.BLUE));
                } else if (amount < 0.0) {
                    tooltip.accept(Component.translatable(
                                    "attribute.modifier.take." + modifier.operation().id(),
                                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-displayAmount),
                                    Component.translatable(pair.getFirst().getDescriptionId()))
                            .withStyle(ChatFormatting.RED));
                }
            }
        }

        tooltip.accept(Component.translatable("tooltip.herbalbrews.canbeplaced").withStyle(ChatFormatting.GRAY));
    }
}