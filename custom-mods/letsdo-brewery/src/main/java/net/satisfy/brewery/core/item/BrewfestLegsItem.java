package net.satisfy.brewery.core.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.satisfy.brewery.core.registry.ArmorRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BrewfestLegsItem extends Item {
    private final ArmorType type;
    private final Identifier leggingsTexture;

    public BrewfestLegsItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, Properties properties, Identifier leggingsTexture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.type = type;
        this.leggingsTexture = leggingsTexture;
    }

    public Identifier getLeggingsTexture() {
        return leggingsTexture;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        // 26.2: ArmorRegistry still takes a List; bridge until it moves to the consumer form.
        ArrayList<Component> lines = new ArrayList<>();
        ArmorRegistry.appendToolTip(lines);
        lines.forEach(tooltip);
    }
}
