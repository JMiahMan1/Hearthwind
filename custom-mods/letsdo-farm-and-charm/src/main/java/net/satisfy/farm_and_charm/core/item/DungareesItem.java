package net.satisfy.farm_and_charm.core.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DungareesItem extends Item {
    private final Identifier leggingsTexture;

    public DungareesItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, Properties properties, Identifier leggingsTexture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.leggingsTexture = leggingsTexture;
    }

    public Identifier getLeggingsTexture() {
        return leggingsTexture;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext,
            @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("tooltip.farm_and_charm.dungarees_1").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.accept(Component.translatable("tooltip.farm_and_charm.dungarees_2").withStyle(ChatFormatting.BLUE));
        tooltip.accept(Component.translatable("tooltip.farm_and_charm.dungarees_3").withStyle(ChatFormatting.BLUE));
    }
}
