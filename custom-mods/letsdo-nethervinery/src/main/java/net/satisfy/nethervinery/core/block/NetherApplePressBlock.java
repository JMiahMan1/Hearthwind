package net.satisfy.nethervinery.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.vinery.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class NetherApplePressBlock extends Block {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = EnumProperty.create("half", DoubleBlockHalf.class);
	public static final Map<Direction, VoxelShape> TOP_SHAPES = new HashMap<>();
	public static final Map<Direction, VoxelShape> BOTTOM_SHAPES = new HashMap<>();

	public NetherApplePressBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF);
	}

	@Override
	public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
		if (!world.isClientSide()) {
			DoubleBlockHalf half = state.getValue(HALF);
			BlockPos otherPartPos = (half == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();
			BlockState otherPartState = world.getBlockState(otherPartPos);

			if (otherPartState.getBlock() == this && otherPartState.getValue(HALF) != half) {
				world.setBlock(otherPartPos, Blocks.AIR.defaultBlockState(), 35);
				world.levelEvent(2001, otherPartPos, Block.getId(otherPartState));
			}
		}
		super.affectNeighborsAfterRemoval(state, world, pos, isMoving);
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if (!player.isCreative()) {
			DoubleBlockHalf half = state.getValue(HALF);
			BlockPos otherPartPos = (half == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();
			BlockState otherPartState = world.getBlockState(otherPartPos);
            if (otherPartState.getBlock() == this) {
                otherPartState.getValue(HALF);
            }
        }
		return super.playerWillDestroy(world, pos, state, player);
	}

	@Override
	public @NotNull RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockGetter world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		if (pos.getY() < world.getMaxY() - 1 && world.getBlockState(pos.above()).canBeReplaced(ctx)) {
			return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(HALF, DoubleBlockHalf.LOWER);
		}
		return null;
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
			BlockPos upperPos = pos.above();
			BlockState upperState = state.setValue(HALF, DoubleBlockHalf.UPPER);
			world.setBlock(upperPos, upperState, 3);
		}
	}

	public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Direction facing = state.getValue(FACING);
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			return TOP_SHAPES.get(facing);
		} else {
			return BOTTOM_SHAPES.get(facing);
		}
	}

	static {
		Supplier<VoxelShape> topShapeSupplier = NetherApplePressBlock::makeTopShape;
		Supplier<VoxelShape> bottomShapeSupplier = NetherApplePressBlock::makeBottomShape;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			TOP_SHAPES.put(direction, GeneralUtil.rotateShape(Direction.NORTH, direction, topShapeSupplier.get()));
			BOTTOM_SHAPES.put(direction, GeneralUtil.rotateShape(Direction.NORTH, direction, bottomShapeSupplier.get()));
		}
	}

	private static VoxelShape makeTopShape() {
		VoxelShape shape = Shapes.empty();
		shape = Shapes.join(shape, Shapes.box(0.46875, 0.3125, 0.46875, 0.53125, 0.8125, 0.53125), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.375, 0.8125, 0.375, 0.625, 0.875, 0.625), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0, 0, 0.40625, 0.125, 0.625, 0.59375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.40625, 1, 0.625, 0.59375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.125, 0.5, 0.40625, 0.875, 0.625, 0.59375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.1875, 0, 0.1875, 0.8125, 0.3125, 0.8125), BooleanOp.OR);
		return shape;
	}

	private static VoxelShape makeBottomShape() {
		VoxelShape shape = Shapes.empty();
		shape = Shapes.join(shape, Shapes.box(0.125, 0.3125, 0.46875, 0.875, 0.4375, 0.53125), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0, 0, 0.0625, 0.125, 0.125, 0.9375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.0625, 1, 0.125, 0.9375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0, 0.125, 0.40625, 0.125, 1, 0.59375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.875, 0.125, 0.40625, 1, 1, 0.59375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.1875, 0.5625, 0.1875, 0.8125, 1, 0.8125), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.125, 0.4375, 0.125, 0.875, 0.5625, 0.875), BooleanOp.OR);
		return shape;
	}
}
