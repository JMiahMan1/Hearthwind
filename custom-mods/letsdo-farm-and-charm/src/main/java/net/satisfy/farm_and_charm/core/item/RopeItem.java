package net.satisfy.farm_and_charm.core.item;

import dev.architectury.event.events.client.ClientTooltipEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.farm_and_charm.core.block.entity.RopeKnotBlockEntity;
import net.satisfy.farm_and_charm.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Objects;

public class RopeItem extends BlockItem {
    public RopeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState clicked = level.getBlockState(pos);

        if (clicked.getBlock() instanceof FenceBlock) {
            if (!level.isClientSide()) {
                BlockState knot = ObjectRegistry.ROPE_KNOT.get().defaultBlockState();
                if (level.setBlock(pos, knot, 3)) {
                    level.playSound(ctx.getPlayer(), pos, SoundEvents.LEAD_TIED, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (level.getBlockEntity(pos) instanceof RopeKnotBlockEntity be) {
                        be.setHeldBlock(clicked);
                        be.setChanged();
                    }
                    if (!Objects.requireNonNull(ctx.getPlayer()).getAbilities().instabuild) {
                        ctx.getItemInHand().shrink(1);
                    }
                }
            }
            return (level.isClientSide() ? InteractionResult.SUCCESS_SERVER : InteractionResult.SUCCESS);
        }

        return super.useOn(ctx);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        int earthy = 0xFFD966;
        int gold = 0xFFD700;

        if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT)) {
            tooltip.accept(Component.translatable("tooltip.farm_and_charm.rope.info_0")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
        } else {
            tooltip.accept(Component.translatable(
                    "tooltip.farm_and_charm.tooltip_information.hold",
                    Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gold)))
            ).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
        }
    }
}
