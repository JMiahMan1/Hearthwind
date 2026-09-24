package net.adventurez.item;

import java.util.function.Consumer;

import net.adventurez.init.ConfigInit;
import net.adventurez.init.ItemInit;
import net.adventurez.item.component.GildedActivationComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class GildedNetheriteArmor extends Item {

    public GildedNetheriteArmor(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, components, tooltipFlag);
        if (ConfigInit.CONFIG.allow_extra_tooltips) {
            components.accept(Component.translatable("item.adventurez.moreinfo.tooltip"));
            if (tooltipFlag.isAdvanced()) {
                components.accept(Component.translatable("item.adventurez.gilded_netherite_armor.tooltip"));
                components.accept(Component.translatable("item.adventurez.gilded_netherite_armor.tooltip2"));
                components.accept(Component.translatable("item.adventurez.gilded_netherite_armor.tooltip3"));
                components.accept(Component.translatable("item.adventurez.gilded_netherite_armor.tooltip4"));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (stack.getItem() == ItemInit.GILDED_NETHERITE_CHESTPLATE) {
            GildedActivationComponent component = stack.getOrDefault(ItemInit.GILDED_DATA, GildedActivationComponent.DEFAULT);
            long time = level.getGameTime();
            if (component.activated() && component.time() + (ConfigInit.CONFIG.gilded_netherite_armor_effect_duration * 2) < (int) time) {
                stack.set(ItemInit.GILDED_DATA, new GildedActivationComponent(false, component.time(), component.visuals()));
            }
            if (component.activated() && component.time() + ConfigInit.CONFIG.gilded_netherite_armor_effect_duration < (int) time) {
                entity.clearFire();
                stack.set(ItemInit.GILDED_DATA, new GildedActivationComponent(component.activated(), component.time(), false));
            }
        }
    }

    public static void activateStoneGolemArmor(Player player, ItemStack stack) {
        if (!stack.is(ItemInit.GILDED_NETHERITE_CHESTPLATE)) {
            return;
        }

        GildedActivationComponent component = stack.getOrDefault(ItemInit.GILDED_DATA, GildedActivationComponent.DEFAULT);
        if (!component.activated()) {
            stack.set(ItemInit.GILDED_DATA, new GildedActivationComponent(true, (int) player.level().getGameTime(), true));
            if (!player.level().isClientSide()) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, ConfigInit.CONFIG.gilded_netherite_armor_effect_duration, 0, false, false, false));
            }
            player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
        }
    }

    public static boolean isStoneGolemArmorActive(ItemStack stack) {
        if (!stack.is(ItemInit.GILDED_NETHERITE_CHESTPLATE)) {
            return false;
        }
        GildedActivationComponent component = stack.getOrDefault(ItemInit.GILDED_DATA, GildedActivationComponent.DEFAULT);
        return component.activated() && component.visuals();
    }

    public static boolean fullGolemArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.GILDED_NETHERITE_HELMET)
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.GILDED_NETHERITE_CHESTPLATE)
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ItemInit.GILDED_NETHERITE_LEGGINGS)
                && player.getItemBySlot(EquipmentSlot.FEET).is(ItemInit.GILDED_NETHERITE_BOOTS);
    }
}
