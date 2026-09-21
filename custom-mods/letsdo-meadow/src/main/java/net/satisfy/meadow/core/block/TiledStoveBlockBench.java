package net.satisfy.meadow.core.block;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.meadow.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;

public class TiledStoveBlockBench extends Block {
    public static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 4, 2, 4), Block.box(12, 0, 0, 16, 2, 4), Block.box(0, 0, 12, 4, 2, 16), Block.box(12, 0, 12, 16, 2, 16));
    public static final VoxelShape SHAPE_SMALL = Shapes.or(SHAPE, Block.box(0, 2, 0, 16, 6, 16));


    public TiledStoveBlockBench(Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE_SMALL;
    }

    @Override
    public @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = GeneralUtil.onUse(level, player, hand, hit, 0.1);
        if (result == InteractionResult.SUCCESS) {
            return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        InteractionResult result = GeneralUtil.onUse(level, player, InteractionHand.MAIN_HAND, hit, 0.1);
        if (result == InteractionResult.SUCCESS) {
            return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
        GeneralUtil.onStateReplaced(world, pos);
    }
}
