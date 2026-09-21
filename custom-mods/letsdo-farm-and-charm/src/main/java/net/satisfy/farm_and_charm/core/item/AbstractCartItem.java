package net.satisfy.farm_and_charm.core.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractCartItem extends Item {
    protected AbstractCartItem(Properties properties) {
        super(properties);
    }

    protected abstract Entity createCartEntity(Level level);

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            Entity entity = this.createCartEntity(level);
            if (entity != null) {
                entity.setPos(context.getClickedPos().getX() + 0.5D, context.getClickedPos().getY() + 1.0D, context.getClickedPos().getZ() + 0.5D);
                level.addFreshEntity(entity);
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WOOD_PLACE, entity.getSoundSource(), 1.0F, 1.0F);
                context.getItemInHand().shrink(1);
            }
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS_SERVER : InteractionResult.SUCCESS);
    }

    protected String getCartTitleKey() {
        return "tooltip.farm_and_charm.cart.title";
    }

    protected void appendShiftDetails(java.util.function.Consumer<Component> tooltipComponents, int earthy) {
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        int earthy = 0xFFD966;
        int gold = 0xFFD700;

        tooltipComponents.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.empty());

        if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT)) {
            tooltipComponents.accept(Component.translatable(getCartTitleKey()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            tooltipComponents.accept(Component.translatable("tooltip.farm_and_charm.cart.action_attach_self").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
            tooltipComponents.accept(Component.translatable("tooltip.farm_and_charm.cart.action_detach").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
            tooltipComponents.accept(Component.empty());
            tooltipComponents.accept(Component.translatable("tooltip.farm_and_charm.cart.while_pulling").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
            appendShiftDetails(tooltipComponents, earthy);
        } else {
            Component key = Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gold)));
            tooltipComponents.accept(Component.translatable("tooltip.farm_and_charm.tooltip_information.hold", key).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
        }

        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}