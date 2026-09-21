package net.satisfy.farm_and_charm.core.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.satisfy.farm_and_charm.platform.PlatformHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CatFoodItem extends Item {
    public CatFoodItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        if (PlatformHelper.enableCatTamingChance()) {
            tooltip.accept(Component.translatable("tooltip.farm_and_charm.animal_fed_to_cat").withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.translatable("tooltip.farm_and_charm.cat_effect_1").withStyle(ChatFormatting.BLUE));
        }
    }
}

