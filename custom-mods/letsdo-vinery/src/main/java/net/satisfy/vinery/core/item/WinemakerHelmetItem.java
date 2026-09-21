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

public class WinemakerHelmetItem extends Item {
    private final ArmorType type;
    private final Identifier hatTexture;

    public WinemakerHelmetItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, Properties properties, Identifier hatTexture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.type = type;
        this.hatTexture = hatTexture;
    }

    public Identifier getHatTexture() {
        return hatTexture;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext ctx, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag context) {
        if (Minecraft.getInstance().level!= null && Minecraft.getInstance().level.isClientSide()) {
            ArmorRegistryClient.appendToolTip(tooltip); // TODO client batch: consumer form
        }
    }
}
