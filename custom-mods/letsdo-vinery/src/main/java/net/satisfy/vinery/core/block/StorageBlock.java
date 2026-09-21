package net.satisfy.vinery.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.vinery.core.block.entity.StorageBlockEntity;
import net.satisfy.vinery.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class StorageBlock extends FacingBlock implements EntityBlock {

    public static final SoundEvent event = SoundEvents.WOOD_PLACE;

    public StorageBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack stack = player.getMainHandItem();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof StorageBlockEntity shelfBlockEntity) {
            Optional<Pair<Float, Float>> optional = GeneralUtil.getRelativeHitCoordinatesForBlockFace(hit, state.getValue(FACING), unAllowedDirections());
            if (optional.isEmpty()) {
                return InteractionResult.PASS;
            } else {
                Pair<Float, Float> ff = optional.get();
                int i = getSection(ff.getFirst(), ff.getSecond());
                if (i == Integer.MIN_VALUE) {
                    return InteractionResult.PASS;
                }
                if (!shelfBlockEntity.getInventory().get(i).isEmpty()) {
                    remove(world, pos, player, shelfBlockEntity, i);
                    return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
                } else {
                    if (!stack.isEmpty() && canInsertStack(stack)) {
                        add(world, pos, player, shelfBlockEntity, stack, i);
                        return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
                    } else {
                        return InteractionResult.CONSUME;
                    }
                }
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    public void add(Level level, BlockPos blockPos, Player player, StorageBlockEntity shelfBlockEntity, ItemStack itemStack, int i) {
        if (!level.isClientSide()) {
            SoundEvent soundEvent = getAddSound(level, blockPos, player, i);
            shelfBlockEntity.setStack(i, itemStack.split(1));
            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player.isCreative()) {
                itemStack.grow(1);
            }
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
        }
    }
    public void remove(Level level, BlockPos blockPos, Player player, StorageBlockEntity shelfBlockEntity, int i) {
        if (!level.isClientSide()) {
            ItemStack itemStack = shelfBlockEntity.removeStack(i);
            SoundEvent soundEvent = getRemoveSound(level, blockPos, player, i);
            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getInventory().add(itemStack)) {
                player.drop(itemStack, false);
            }
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
        }
    }

    public SoundEvent getRemoveSound(Level level, BlockPos blockPos, Player player,  int i){
        return event;
    }

    public SoundEvent getAddSound(Level level, BlockPos blockPos, Player player,  int i){
        return event;
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel world, BlockPos pos, boolean moved) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof StorageBlockEntity shelf) {
                Containers.dropContents(world, pos, shelf.getInventory());

                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public abstract int size();

    public abstract Identifier type();

    public abstract Direction[] unAllowedDirections();

    public abstract boolean canInsertStack(ItemStack stack);

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageBlockEntity(pos, state, size());
    }
    public abstract int getSection(Float x, Float y);
}
