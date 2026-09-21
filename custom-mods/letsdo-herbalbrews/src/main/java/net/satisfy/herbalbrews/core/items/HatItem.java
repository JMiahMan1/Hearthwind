package net.satisfy.herbalbrews.core.items;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.satisfy.herbalbrews.platform.PlatformHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class HatItem extends Item {
    private final ArmorType type;
    private final Identifier hatTexture;

    public HatItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, Properties properties, Identifier hatTexture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.type = type;
        this.hatTexture = hatTexture;
    }

    public Identifier getHatTexture() {
        return hatTexture;
    }

    public static void applyMagicResistance(LivingEntity entity, float damageAmount, boolean isMagic) {
        if (entity instanceof Player player) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.getItem() instanceof HatItem && isMagic && PlatformHelper.isHatDamageReductionEnabled()) {
                float reduction = PlatformHelper.getHatDamageReductionAmount() / 100f;
                float reducedDamage = damageAmount * (1 - reduction);
                entity.setHealth(entity.getHealth() + (damageAmount - reducedDamage));
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        if (PlatformHelper.isHatDamageReductionEnabled()) {
            int reductionAmount = PlatformHelper.getHatDamageReductionAmount();
            Component magicDamage = Component.translatable("tooltip.herbalbrews.magic_damage")
                    .setStyle(Style.EMPTY.withColor(TextColor.parseColor("#AA00FF").getOrThrow()));
            Component damageReduction = Component.translatable("tooltip.herbalbrews.damage_reduction", magicDamage, reductionAmount + "%");
            tooltip.accept(damageReduction);
        }
    }
}
