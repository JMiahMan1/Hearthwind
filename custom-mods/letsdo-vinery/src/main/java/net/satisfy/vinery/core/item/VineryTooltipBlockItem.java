package net.satisfy.vinery.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

// 26.2: Block.appendHoverText does not exist (tooltips are Item-only).
// Block tooltip bodies stay on the block classes as appendBlockHoverText.
public class VineryTooltipBlockItem extends BlockItem {
    public VineryTooltipBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext,
            @NotNull net.minecraft.world.item.component.TooltipDisplay display,
            @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        Block block = getBlock();
        if (block instanceof net.satisfy.vinery.core.block.BigBottleStorageBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.vinery.core.block.FourBottleStorageBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.vinery.core.block.NineBottleStorageBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.vinery.core.block.CompletionistBannerBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        super.appendHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag);
    }
}
