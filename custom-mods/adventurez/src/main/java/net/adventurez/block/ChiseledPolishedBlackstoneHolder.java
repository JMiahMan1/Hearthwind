package net.adventurez.block;

import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Consumer;

import net.adventurez.init.BlockInit;
import net.adventurez.init.ConfigInit;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import net.adventurez.block.entity.ChiseledPolishedBlackstoneHolderEntity;
import net.adventurez.init.TagInit;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class ChiseledPolishedBlackstoneHolder extends Block implements EntityBlock {
    private static final VoxelShape SHAPE;

    public ChiseledPolishedBlackstoneHolder(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChiseledPolishedBlackstoneHolderEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return checkType(type, BlockInit.CHISELED_POLISHED_BLACKSTONE_HOLDER_ENTITY,
                level.isClientSide() ? ChiseledPolishedBlackstoneHolderEntity::clientTick : ChiseledPolishedBlackstoneHolderEntity::serverTick);
    }

    public void appendBlockHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (ConfigInit.CONFIG.allow_extra_tooltips) {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)) {
                tooltip.accept(Component.translatable("block.adventurez.chiseled_polished_blackstone_holder.tooltip"));
                tooltip.accept(Component.translatable("block.adventurez.chiseled_polished_blackstone_holder.tooltip2"));
                tooltip.accept(Component.translatable("block.adventurez.chiseled_polished_blackstone_holder.tooltip3"));
            } else {
                tooltip.accept(Component.translatable("item.adventurez.moreinfo.tooltip"));
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack itemStack = player.getMainHandItem();
        Container blockEntity = (Container) level.getBlockEntity(pos);
        ItemStack blockStack = blockEntity.getItem(0);
        if (blockStack.isEmpty()) {
            if ((itemStack.is(TagInit.HOLDER_ITEMS) || ConfigInit.CONFIG.allow_all_items_on_holder) && level.getBlockState(pos.above()).isAir()) {
                if (!level.isClientSide()) {
                    blockEntity.setItem(0, new ItemStack(itemStack.getItem(), 1));
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS_SERVER;
            } else {
                return InteractionResult.CONSUME;
            }
        } else {
            if (!level.isClientSide() && !player.getInventory().add(blockStack)) {
                player.drop(blockStack, false);
            }

            blockEntity.clearContent();
            return InteractionResult.SUCCESS_SERVER;
        }

    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }

    static {
        SHAPE = Shapes.or(box(0D, 0D, 0D, 16D, 14D, 16D), box(0D, 14D, 0D, 16D, 16D, 3D), box(0D, 14D, 13D, 16D, 16D, 16D),
                box(13D, 14D, 3D, 16D, 16D, 13D), box(0D, 14D, 3D, 3D, 16D, 13D));
    }

}
