package net.satisfy.farm_and_charm.core.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.farm_and_charm.core.registry.StorageTypeRegistry;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class WindowSillBlock extends StorageBlock {
    private static final Supplier<VoxelShape> voxelShapeSupplier = () -> {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0, 0, 0.4375, 1, 0.125, 1), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0625, 0.125, 0.5, 0.4375, 0.5, 0.875), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.53125, 0.125, 0.53125, 0.90625, 0.5, 0.90625), BooleanOp.OR);
        return shape;
    };
    public static final Map<Direction, VoxelShape> SHAPE = Util.make(new HashMap<>(), map -> {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            map.put(direction, GeneralUtil.rotateShape(Direction.NORTH, direction, voxelShapeSupplier.get()));
        }
    });

    public WindowSillBlock(Properties settings) {
        super(settings);
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean canInsertStack(ItemStack stack) {
        return (stack.getItem() instanceof net.minecraft.world.item.BlockItem _bi && _bi.getBlock().defaultBlockState().is(net.minecraft.tags.BlockTags.SMALL_FLOWERS));
    }

    @Override
    public Identifier type() {
        return StorageTypeRegistry.WINDOW_SILL;
    }

    @Override
    public Direction[] unAllowedDirections() {
        return new Direction[]{Direction.DOWN};
    }

    @Override
    public int getSection(Float x, Float y) {
        return x < 0.5F ? 0 : 1;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos neighborPos = facing == Direction.DOWN ? pos.above() : pos.relative(facing.getOpposite());
        BlockState neighborState = world.getBlockState(neighborPos);
        if (neighborState.is(Blocks.GLASS_PANE) || neighborState.getBlock() instanceof IronBarsBlock) {
            return true;
        }
        if (facing == Direction.DOWN) {
            return neighborState.isFaceSturdy(world, neighborPos, Direction.DOWN);
        } else if (facing == Direction.UP) {
            return false;
        } else {
            return neighborState.isFaceSturdy(world, neighborPos, facing);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(world, pos)) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, LevelReader _level, ScheduledTickAccess _ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource _rand) {
        if (!state.canSurvive(_level, pos)) {
            _ticks.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, _level, _ticks, pos, direction, neighborPos, neighborState, _rand);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

}
