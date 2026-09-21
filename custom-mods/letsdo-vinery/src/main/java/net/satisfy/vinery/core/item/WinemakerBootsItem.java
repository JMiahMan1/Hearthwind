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

public class WinemakerBootsItem extends Item {
    private final ArmorType type;
    private final Identifier bootsTexture;

    public WinemakerBootsItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, Properties properties, Identifier bootsTexture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.type = type;
        this.bootsTexture = bootsTexture;
    }

    public Identifier getBootsTexture() {
        return bootsTexture;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> list, @NotNull TooltipFlag tooltipFlag) {
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.isClientSide()) {
            ArmorRegistryClient.appendToolTip(list);
        }
    }
}
