package net.satisfy.brewery.core.block;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.brewery.core.registry.ObjectRegistry;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BigBarrelMainBlock extends BigBarrelBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF;
    private static final Supplier<VoxelShape> bottomVoxelShapeSupplier = () -> {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Shapes.box(0, 0, 0.1875, 0.875, 0.25, 0.4375));
        shape = Shapes.or(shape, Shapes.box(0, 0.25, 0, 0.875, 1, 1));
        return shape;
    };
    public static final Map<Direction, VoxelShape> BOTTOM_SHAPE = Util.make(new HashMap<>(), map -> {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            map.put(direction, GeneralUtil.rotateShape(Direction.NORTH, direction, bottomVoxelShapeSupplier.get()));
        }
    });
    private static final Supplier<VoxelShape> topVoxelShapeSupplier = () -> {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Shapes.box(0, 0, 0, 0.875, 1, 1));
        return shape;
    };
    public static final Map<Direction, VoxelShape> TOP_SHAPE = Util.make(new HashMap<>(), map -> {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            map.put(direction, GeneralUtil.rotateShape(Direction.NORTH, direction, topVoxelShapeSupplier.get()));
        }
    });

    static {
        HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    }

    public BigBarrelMainBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }


    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            level.setBlockAndUpdate(blockPos.above(), blockState.setValue(HALF, DoubleBlockHalf.UPPER));
        }
    }


    @Override
    public @NotNull BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess tickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource random) {
        DoubleBlockHalf doubleBlockHalf = blockState.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            return blockState2.is(this) && blockState2.getValue(HALF) != doubleBlockHalf ? blockState.setValue(FACING, blockState2.getValue(FACING)) : Blocks.AIR.defaultBlockState();
        } else {
            return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !blockState.canSurvive(level, blockPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockState, level, tickAccess, blockPos, direction, blockPos2, blockState2, random);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos blockPos, boolean moved) {
        Direction facing = blockState.getValue(FACING);
        BlockPos backPos = blockPos.relative(facing.getOpposite());
        BlockPos sidePos = blockPos.relative(facing.getCounterClockWise());
        BlockPos diagonalPos = sidePos.relative(facing.getOpposite());

        level.removeBlock(backPos, false);
        level.removeBlock(sidePos, false);
        level.removeBlock(diagonalPos, false);
        level.removeBlock(blockPos, false);

        super.affectNeighborsAfterRemoval(blockState, level, blockPos, moved);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level level = blockPlaceContext.getLevel();
        BlockPos mainPos = blockPlaceContext.getClickedPos();
        BlockState blockState = super.getStateForPlacement(blockPlaceContext);
        if (blockState == null) return null;
        Direction facing = blockState.getValue(FACING);
        BlockPos backPos = mainPos.relative(facing.getOpposite());
        BlockPos sidePos = mainPos.relative(facing.getCounterClockWise());
        BlockPos diagonalPos = sidePos.relative(facing.getOpposite());
        BlockPos topPos = diagonalPos.above();
        boolean placeable = canPlace(level, backPos, sidePos, diagonalPos, topPos);
        return placeable ? blockState : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
        if (level.isClientSide()) return;
        Direction facing = blockState.getValue(FACING);
        BlockPos backPos = blockPos.relative(facing.getOpposite());
        BlockPos sidePos = blockPos.relative(facing.getCounterClockWise());
        BlockPos diagonalPos = sidePos.relative(facing.getOpposite());
        BlockPos topPos = diagonalPos.above();
        if (!canPlace(level, backPos, sidePos, diagonalPos, topPos)) return;
        level.setBlock(backPos, ObjectRegistry.BARREL_MAIN_HEAD.get().defaultBlockState().setValue(FACING, facing), 3);
        level.setBlock(sidePos, ObjectRegistry.BARREL_RIGHT.get().defaultBlockState().setValue(FACING, facing), 3);
        level.setBlock(diagonalPos, ObjectRegistry.BARREL_HEAD_RIGHT.get().defaultBlockState().setValue(FACING, facing), 3);

    }

    private boolean canPlace(Level level, BlockPos... blockPoses) {
        for (BlockPos blockPos : blockPoses) {
            if (!level.getBlockState(blockPos).isAir()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        DoubleBlockHalf half = state.getValue(HALF);
        Direction facing = state.getValue(FACING);

        if (half == DoubleBlockHalf.LOWER) {
            return BOTTOM_SHAPE.get(facing);
        } else {
            return TOP_SHAPE.get(facing);
        }
    }

    public void appendBlockHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, net.minecraft.world.item.component.TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
    }
}
