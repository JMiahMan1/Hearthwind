package net.satisfy.brewery.core.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.satisfy.farm_and_charm.core.block.FacingBlock;

import java.util.function.Consumer;

public class BagBlock extends FacingBlock {

    public BagBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    public void appendBlockHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, net.minecraft.world.item.component.TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
    }
}