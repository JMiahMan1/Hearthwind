package net.satisfy.bakery.core.block;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.bakery.core.registry.ObjectRegistry;
import net.satisfy.farm_and_charm.core.block.FacingBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BakerStationBlock extends FacingBlock {
    public BakerStationBlock(Properties settings) {
        super(settings);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.isEmptyBlock(pos.above());
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (!world.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            if (itemStack.is(ObjectRegistry.CAKE_DOUGH.get())) {
                BlockPos blockAbove = pos.above();
                if (world.isEmptyBlock(blockAbove)) {
                    world.setBlock(blockAbove, ObjectRegistry.BLANK_CAKE.get().defaultBlockState(), 3);
                    world.playSound(null, pos, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    world.levelEvent(2001, blockAbove, Block.getId(ObjectRegistry.BLANK_CAKE.get().defaultBlockState()));
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            } else {
                player.sendOverlayMessage(Component.translatable("tooltip.bakery.baker_station.interaction"));
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

    public void appendBlockHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        int icingRed = 0xE3A6A0;
        int gold = 0xFFD700;

        if (!com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT)) {
            Component key = Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gold)));
            tooltip.accept(Component.translatable("tooltip.farm_and_charm.tooltip_information.hold", key).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(icingRed))));
            return;
        }

        tooltip.accept(Component.translatable("tooltip.bakery.baker_station.info_0").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(icingRed))));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("tooltip.bakery.baker_station.info_1").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(icingRed))));
        tooltip.accept(Component.translatable("tooltip.bakery.baker_station.info_2").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(icingRed))));
    }
}