package net.satisfy.meadow.core.block;

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
import net.satisfy.meadow.core.block.entity.StorageBlockEntity;
import net.satisfy.meadow.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class StorageBlock extends FacingBlock implements EntityBlock {

    public StorageBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof StorageBlockEntity storageEntity) {
            Optional<Pair<Float, Float>> hitCoordinates = GeneralUtil.getRelativeHitCoordinatesForBlockFace(hit, state.getValue(FACING), this.unAllowedDirections());
            if (hitCoordinates.isEmpty()) {
                return InteractionResult.PASS;
            }

            Pair<Float, Float> coordinates = hitCoordinates.get();
            int section = this.getSection(coordinates.getFirst(), coordinates.getSecond());
            if (section == Integer.MIN_VALUE) {
                return InteractionResult.PASS;
            }

            if (!storageEntity.getInventory().get(section).isEmpty()) {
                this.remove(world, pos, player, storageEntity, section);
                return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
            }

            if (!stack.isEmpty() && this.canInsertStack(stack)) {
                this.add(world, pos, player, storageEntity, stack, section);
                return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public void add(Level level, BlockPos pos, Player player, StorageBlockEntity storageEntity, ItemStack itemStack, int index) {
        if (!level.isClientSide()) {
            SoundEvent sound = SoundEvents.WOOD_PLACE;
            storageEntity.setStack(index, itemStack.split(1));
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player.isCreative()) {
                itemStack.grow(1);
            }

            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    public void remove(Level level, BlockPos pos, Player player, StorageBlockEntity storageEntity, int index) {
        if (!level.isClientSide()) {
            ItemStack removedStack = storageEntity.removeStack(index);
            SoundEvent sound = SoundEvents.WOOD_BREAK;
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getInventory().add(removedStack)) {
                player.drop(removedStack, false);
            }

            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (true) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof StorageBlockEntity storageEntity) {
                if (world instanceof ServerLevel serverLevel) {
                    Containers.dropContents(serverLevel, pos, storageEntity.getInventory());
                }

                world.updateNeighbourForOutputSignal(pos, this);
            }

            super.affectNeighborsAfterRemoval(state, world, pos, moved);
        }
    }

    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public abstract int size();

    public abstract Identifier type();

    public abstract Direction[] unAllowedDirections();

    public abstract boolean canInsertStack(ItemStack stack);

    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageBlockEntity(pos, state, this.size());
    }

    public abstract int getSection(Float x, Float y);
}