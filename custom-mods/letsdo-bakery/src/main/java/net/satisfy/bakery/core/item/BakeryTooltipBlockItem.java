package net.satisfy.bakery.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

// 26.2: Block.appendHoverText does not exist (tooltips are Item-only).
// Full parity: block tooltip bodies stay on the block classes as
// appendBlockHoverText and are dispatched from here, like farm_and_charm.
public class BakeryTooltipBlockItem extends BlockItem {
    public BakeryTooltipBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext,
            @NotNull net.minecraft.world.item.component.TooltipDisplay display,
            @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        Block block = getBlock();
        if (block instanceof net.satisfy.bakery.core.block.BreadBox b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.bakery.core.block.CakeDisplayBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.bakery.core.block.CakeStandBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.bakery.core.block.WallDisplayBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.bakery.core.block.StreetSignBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.bakery.core.block.BreadBasketBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.bakery.core.block.BakerStationBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.bakery.core.block.BrickSinkBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.bakery.core.block.CupcakeDisplayBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.bakery.core.block.cake.PieBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.bakery.core.block.CompletionistBannerBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        if (block instanceof net.satisfy.bakery.core.block.SmallCookingPotBlock b) { b.appendBlockHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag); return; }
        super.appendHoverText(itemStack, tooltipContext, display, tooltip, tooltipFlag);
    }
}
