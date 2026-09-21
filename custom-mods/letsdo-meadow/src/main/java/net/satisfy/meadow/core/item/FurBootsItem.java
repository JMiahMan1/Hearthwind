package net.satisfy.meadow.core.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.satisfy.meadow.core.registry.ArmorRegistry;
import org.jetbrains.annotations.NotNull;

public class FurBootsItem extends Item {
    private final ArmorType type;
    private final Identifier bootsTexture;

    public FurBootsItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, Properties properties, Identifier bootsTexture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.type = type;
        this.bootsTexture = bootsTexture;
    }

    public Identifier getBootsTexture() {
        return bootsTexture;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext ctx, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        ArmorRegistry.appendToolTip(tooltip);
    }
}
