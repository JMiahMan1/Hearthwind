package net.satisfy.vinery.core.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.satisfy.vinery.core.registry.ArmorRegistryClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WinemakerChestItem extends Item {
    private final ArmorType type;
    private final Identifier chestplateTexture;

    public WinemakerChestItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, Properties properties, Identifier chestplateTexture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.type = type;
        this.chestplateTexture = chestplateTexture;
    }

    public Identifier getChestplateTexture() {
        return chestplateTexture;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.isClientSide()) {
            ArmorRegistryClient.appendToolTip(tooltip); // TODO client batch: consumer form
        }
    }
}
