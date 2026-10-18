package com.starfish_studios.another_furniture.block;

import com.starfish_studios.another_furniture.block.properties.HorizontalConnectionType;
import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.util.block.HammerableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;

public class BenchBlock extends SeatBlock implements SimpleWaterloggedBlock, HammerableBlock {
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final net.minecraft.world.level.block.state.properties.EnumProperty<HorizontalConnectionType> CONNECTION_TYPE = ModBlockStateProperties.HORIZONTAL_CONNECTION_TYPE_1;
   public static final net.minecraft.world.level.block.state.properties.EnumProperty<HorizontalConnectionType> BACK_TYPE = ModBlockStateProperties.HORIZONTAL_CONNECTION_TYPE_2;
   public static final BooleanProperty BACK = ModBlockStateProperties.BACK;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape AABB_Z = Block.box(0.0, 0.0, 2.0, 16.0, 7.0, 14.0);
   protected static final VoxelShape AABB_X = Block.box(2.0, 0.0, 0.0, 14.0, 7.0, 16.0);
   protected static final VoxelShape AABB_EAST = Shapes.or(AABB_X, Block.box(2.0, 7.0, 0.0, 4.0, 16.0, 16.0));
   protected static final VoxelShape AABB_WEST = Shapes.or(AABB_X, Block.box(12.0, 7.0, 0.0, 14.0, 16.0, 16.0));
   protected static final VoxelShape AABB_SOUTH = Shapes.or(AABB_Z, Block.box(0.0, 7.0, 2.0, 16.0, 16.0, 4.0));
   protected static final VoxelShape AABB_NORTH = Shapes.or(AABB_Z, Block.box(0.0, 7.0, 12.0, 16.0, 16.0, 14.0));

   public BenchBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any())
                        .setValue(FACING, Direction.NORTH))
                     .setValue(CONNECTION_TYPE, HorizontalConnectionType.SINGLE))
                  .setValue(BACK_TYPE, HorizontalConnectionType.SINGLE))
               .setValue(BACK, true))
            .setValue(WATERLOGGED, false)
      );
   }

   @Override
   public float seatHeight(BlockState state) {
      return 0.35F;
   }

   @Override
   public BlockPos primaryDismountLocation(Level level, BlockState state, BlockPos pos) {
      return pos.relative((Direction)state.getValue(FACING));
   }

   @Override
   public float setRiderRotation(BlockState state, Entity entity) {
      return ((Direction)state.getValue(FACING)).toYRot();
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      Direction facing = (Direction)state.getValue(FACING);
      if ((Boolean)state.getValue(BACK)) {
         return switch (facing) {
            case EAST -> AABB_EAST;
            case SOUTH -> AABB_SOUTH;
            case WEST -> AABB_WEST;
            default -> AABB_NORTH;
         };
      } else {
         return facing.getAxis() == Axis.Z ? AABB_Z : AABB_X;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())).setValue(WATERLOGGED, waterlogged);
   }

   public BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
   ) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         tickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      Direction facing = (Direction)state.getValue(FACING);
      if (direction != facing.getClockWise() && direction != facing.getCounterClockWise()) {
         return state;
      } else {
         BlockState l_state = level.getBlockState(currentPos.relative(facing.getClockWise()));
         BlockState r_state = level.getBlockState(currentPos.relative(facing.getCounterClockWise()));
         boolean l_side = l_state.getBlock() instanceof BenchBlock
            && (l_state.getValue(FACING) == facing || l_state.getValue(FACING) == facing.getOpposite());
         boolean r_side = r_state.getBlock() instanceof BenchBlock
            && (r_state.getValue(FACING) == facing || r_state.getValue(FACING) == facing.getOpposite());
         boolean back_l_side = l_state.getBlock() instanceof BenchBlock && l_state.getValue(FACING) == facing && (Boolean)l_state.getValue(BACK);
         boolean back_r_side = r_state.getBlock() instanceof BenchBlock && r_state.getValue(FACING) == facing && (Boolean)r_state.getValue(BACK);
         HorizontalConnectionType connection_type = l_side && r_side
            ? HorizontalConnectionType.MIDDLE
            : (r_side ? HorizontalConnectionType.LEFT : (l_side ? HorizontalConnectionType.RIGHT : HorizontalConnectionType.SINGLE));
         HorizontalConnectionType back_type = back_l_side && back_r_side
            ? HorizontalConnectionType.MIDDLE
            : (back_r_side ? HorizontalConnectionType.LEFT : (back_l_side ? HorizontalConnectionType.RIGHT : HorizontalConnectionType.SINGLE));
         return (BlockState)((BlockState)state.setValue(CONNECTION_TYPE, connection_type)).setValue(BACK_TYPE, back_type);
      }
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return this.tryHammerBlock(BACK, stack, state, level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return super.useWithoutItem(state, level, pos, player, hitResult);
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{FACING, CONNECTION_TYPE, BACK_TYPE, BACK, WATERLOGGED});
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   @Override
   protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
      return false;
   }
}
