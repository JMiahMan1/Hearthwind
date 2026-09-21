package net.satisfy.farm_and_charm.core.item.food;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.*;

public class EffectFoodHelper {

    private static float saturationModifierOf(FoodProperties base) {
        if (base.nutrition() <= 0) return 0.0f;
        return base.saturation() / (base.nutrition() * 2.0f);
    }

    private static java.util.List<net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect> consumeFx(ItemStack stack) {
        var c = stack.get(DataComponents.CONSUMABLE);
        if (c == null) return java.util.List.of();
        var out = new java.util.ArrayList<net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect>();
        for (var fx : c.onConsumeEffects()) if (fx instanceof net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect ae) out.add(ae);
        return out;
    }

    private static void writeFx(ItemStack stack, java.util.List<net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect> kept, boolean fast) {
        var old = stack.get(DataComponents.CONSUMABLE);
        var b = net.minecraft.world.item.component.Consumable.builder();
        if (fast || (old != null && old.consumeSeconds() == 0.8f)) b.consumeSeconds(0.8F);
        for (var ae : kept) b.onConsume(ae);
        stack.set(DataComponents.CONSUMABLE, b.build());
    }

    public static void addEffect(ItemStack stack, Pair<MobEffectInstance, Float> effect) {
        var kept = new java.util.ArrayList<>(consumeFx(stack));
        var target = effect.getFirst().getEffect();
        for (var ae : kept) for (var inst : ae.effects()) {
            if (inst.getEffect() == target) return;
        }
        kept.add(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(effect.getFirst(), effect.getSecond()));
        writeFx(stack, kept, false);
    }

    private static void rebuildWithout(ItemStack stack) {
        var kept = new java.util.ArrayList<net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect>();
        for (var ae : consumeFx(stack)) {
            var insts = new java.util.ArrayList<MobEffectInstance>();
            for (var inst : ae.effects()) if (inst.getEffect() != MobEffects.HUNGER) insts.add(inst);
            if (!insts.isEmpty()) kept.add(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(insts, ae.probability()));
        }
        writeFx(stack, kept, false);
    }

    private static void removeHungerEffect(ItemStack stack) {
        rebuildWithout(stack);
    }

    private static void removeRawChickenEffects(ItemStack stack) {
        if (stack.getItem() != Items.CHICKEN) return;
        removeHungerEffect(stack);
    }

    public static List<Pair<MobEffectInstance, Float>> getEffects(ItemStack stack) {
        removeHungerEffect(stack);
        removeRawChickenEffects(stack);
        var out = Lists.<Pair<MobEffectInstance, Float>>newArrayList();
        if (stack.getItem() instanceof PotionItem) {
            var pc = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            pc.potion().ifPresent(p -> {
                for (var e : p.value().getEffects()) out.add(new Pair<>(e, 1.0f));
            });
            for (var e : pc.customEffects()) out.add(new Pair<>(e, 1.0f));
            return out;
        }
        for (var ae : consumeFx(stack)) for (var inst : ae.effects()) out.add(new Pair<>(inst, ae.probability()));
        return out;
    }

    public static void applyEffects(ItemStack stack) {
        var pc = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (pc.hasEffects()) return;
        // effects already live on CONSUMABLE; nothing to migrate.
    }

    public static CompoundTag createNbt(short id, Pair<MobEffectInstance, Float> effect) {
        CompoundTag nbtCompound = new CompoundTag();
        nbtCompound.putShort("id", id);
        nbtCompound.putInt("duration", effect.getFirst().getDuration());
        nbtCompound.putInt("amplifier", effect.getFirst().getAmplifier());
        nbtCompound.putFloat("chance", effect.getSecond());
        return nbtCompound;
    }

    public static List<Pair<MobEffectInstance, Float>> fromNbt(ListTag list) {
        List<Pair<MobEffectInstance, Float>> effects = Lists.newArrayList();
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag nbtCompound = list.getCompoundOrEmpty(i);
            MobEffect _byId = BuiltInRegistries.MOB_EFFECT.byId(nbtCompound.getShortOr("id", (short) 0));
            Optional<Holder<MobEffect>> effect = _byId == null ? Optional.empty() : Optional.of(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(_byId));
            effect.ifPresent(mobEffectReference -> effects.add(new Pair<>(new MobEffectInstance(mobEffectReference, nbtCompound.getIntOr("duration", 0), nbtCompound.getIntOr("amplifier", 0)), nbtCompound.getFloatOr("chance", 1.0F))));
        }
        return effects;
    }

    public static ItemStack setStage(ItemStack stack, int stage) {
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(java.util.List.of((float) stage), java.util.List.of(), java.util.List.of(), java.util.List.of()));
        return stack;
    }

    public static int getStage(ItemStack stack) {
        var cmd = stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY);
        Float f = cmd.getFloat(0);
        return f == null ? 0 : f.intValue();
    }

    public static void getTooltip(ItemStack stack, Item.TooltipContext tooltipContext, java.util.function.Consumer<Component> tooltip) {
        List<MobEffectInstance> effects = new java.util.ArrayList<>();
        List<Float> probs = new java.util.ArrayList<>();
        for (var ae : consumeFx(stack)) for (var inst : ae.effects()) { effects.add(inst); probs.add(ae.probability()); }
        List<Pair<Holder<Attribute>, AttributeModifier>> attrs = Lists.newArrayList();
        if (effects.isEmpty()) {
            tooltip.accept(Component.translatable("effect.none").withStyle(ChatFormatting.GRAY));
            return;
        }
        for (int _i = 0; _i < effects.size(); _i++) {
            MobEffectInstance pe = effects.get(_i);
            float _prob = probs.get(_i);
            MutableComponent name = Component.translatable(pe.getDescriptionId());
            MobEffect eff = pe.getEffect().value();
            eff.createModifiers(pe.getAmplifier(), (h, base) -> {
                AttributeModifier m = new AttributeModifier(base.id(), base.amount() * (double)(pe.getAmplifier() + 1), base.operation());
                attrs.add(new Pair<>(h, m));
            });
            if (pe.getDuration() > 20) {
                name = Component.translatable("potion.withDuration", name, MobEffectUtil.formatDuration(pe, _prob, tooltipContext.tickRate()));
            }
            tooltip.accept(name.withStyle(eff.getCategory().getTooltipFormatting()));
        }
        if (!attrs.isEmpty()) {
            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            for (Pair<Holder<Attribute>, AttributeModifier> pair : attrs) {
                AttributeModifier m = pair.getSecond();
                double amt = m.amount();
                double shown = (m.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || m.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) ? amt * 100.0 : amt;
                if (amt > 0.0) {
                    tooltip.accept(Component.translatable("attribute.modifier.plus." + m.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(shown), Component.translatable(pair.getFirst().value().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                } else if (amt < 0.0) {
                    tooltip.accept(Component.translatable("attribute.modifier.take." + m.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-shown), Component.translatable(pair.getFirst().value().getDescriptionId())).withStyle(ChatFormatting.RED));
                }
            }
        }
    }

    public static Map<Holder<MobEffect>, MobEffectInstance> bestEffects(Iterable<Pair<MobEffectInstance, Float>> effects) {
        Map<Holder<MobEffect>, MobEffectInstance> best = new HashMap<>();
        for (Pair<MobEffectInstance, Float> p : effects) {
            MobEffectInstance cur = p.getFirst();
            Holder<MobEffect> key = cur.getEffect();
            MobEffectInstance prev = best.get(key);
            if (prev == null || cur.getAmplifier() > prev.getAmplifier() || (cur.getAmplifier() == prev.getAmplifier() && cur.getDuration() > prev.getDuration())) {
                best.put(key, cur);
            }
        }
        return best;
    }

    public static List<MobEffectInstance> collectMergedSortedEffects(Iterable<ItemStack> stacks) {
        List<Pair<MobEffectInstance, Float>> collected = new ArrayList<>();
        for (ItemStack in : stacks) if (!in.isEmpty()) collected.addAll(getEffects(in));
        Map<Holder<MobEffect>, MobEffectInstance> best = bestEffects(collected);
        List<MobEffectInstance> sorted = new ArrayList<>(best.values());
        sorted.sort(Comparator.comparingInt(a -> BuiltInRegistries.MOB_EFFECT.getId(a.getEffect().value())));
        return sorted;
    }

    public static List<MobEffectInstance> collectMergedSortedEffects(Container container, int from, int to) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = from; i <= to; i++) stacks.add(container.getItem(i));
        return collectMergedSortedEffects(stacks);
    }
}