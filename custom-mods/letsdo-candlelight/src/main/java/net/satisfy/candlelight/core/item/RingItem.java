package net.satisfy.candlelight.core.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.satisfy.candlelight.core.registry.ObjectRegistry;
import net.satisfy.farm_and_charm.core.registry.ArmorMaterialRegistry;

import java.util.List;

public class RingItem extends Item {
    public RingItem(Holder<ArmorMaterial> material, ArmorType type, Properties settings) {
        super(settings.humanoidArmor(material.value(), type));
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel serverLevel, Entity entity, EquipmentSlot slot) {
        if (!serverLevel.isClientSide()) {
            if (entity instanceof Player player) {
                this.checkForSet(player);
            }
        }
        super.inventoryTick(stack, serverLevel, entity, slot);
    }

    private void checkForSet(Player player) {
        if (this.hasRing(player)) {
            this.addStatusEffect(player, new MobEffectInstance(MobEffects.LUCK, 14 * 20, 1));
        }
    }

    private boolean hasRing(Player player) {
        if (player.getInventory().getItem(37).isEmpty()) return false;
        return player.getInventory().getItem(37).getItem() == ObjectRegistry.GOLD_RING.get();
    }

    private void addStatusEffect(Player player, MobEffectInstance mapStatusEffect) {
        boolean hasPlayerEffect = player.hasEffect(mapStatusEffect.getEffect());
        MobEffectInstance effect = player.getEffect(mapStatusEffect.getEffect());
        if (!hasPlayerEffect || effect != null && effect.getDuration() < 11 * 20) {
            player.addEffect(new MobEffectInstance(mapStatusEffect.getEffect(),
                    mapStatusEffect.getDuration(), mapStatusEffect.getAmplifier(), true, false, true));
        }
    }

}