package net.satisfy.meadow.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.satisfy.meadow.platform.PlatformHelper;

import java.util.function.Consumer;

public class CompletionistBannerItem extends BlockItem {
    public CompletionistBannerItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        if (PlatformHelper.shouldShowTooltip()) {
            int primary = 0xEDE6D6;
            int accent = 0xC8A873;
            tooltip.accept(Component.translatable("tooltip.meadow.banner.thankyou_1").withStyle(style -> style.withColor(TextColor.fromRgb(primary))));
            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("tooltip.meadow.banner.thankyou_2").withStyle(style -> style.withColor(TextColor.fromRgb(accent))));
            tooltip.accept(Component.translatable("tooltip.meadow.banner.thankyou_4").withStyle(style -> style.withColor(TextColor.fromRgb(accent))));
            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("tooltip.meadow.banner.thankyou_3").withStyle(style -> style.withColor(TextColor.fromRgb(primary))));
        }
    }
}
