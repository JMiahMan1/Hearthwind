package com.starfish_studios.another_furniture.block;

import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.block.properties.SofaType;
import com.starfish_studios.another_furniture.util.block.ShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;

public class SofaBlock extends SeatBlock implements SimpleWaterloggedBlock {
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final net.minecraft.world.level.block.state.properties.EnumProperty<SofaType> TYPE = ModBlockStateProperties.SOFA_TYPE;
   public static final VoxelShape SEAT_BASE = Block.box(0.0, 3.0, 0.0, 16.0, 7.0, 16.0);
   public static final VoxelShape SEAT_BACK_SINGLE = Block.box(2.0, 7.0, 12.0, 14.0, 16.0, 16.0);
   public static final VoxelShape SEAT_BACK_MIDDLE = Block.box(0.0, 7.0, 12.0, 16.0, 16.0, 16.0);
   public static final VoxelShape SEAT_BACK_LEFT = Block.box(0.0, 7.0, 12.0, 14.0, 16.0, 16.0);
   public static final VoxelShape SEAT_BACK_RIGHT = Block.box(2.0, 7.0, 12.0, 16.0, 16.0, 16.0);
   public static final VoxelShape LEG_BL = Block.box(14.0, 0.0, 14.0, 16.0, 3.0, 16.0);
   public static final VoxelShape LEG_BR = Block.box(0.0, 0.0, 14.0, 2.0, 3.0, 16.0);
   public static final VoxelShape LEG_FR = Block.box(0.0, 0.0, 0.0, 2.0, 3.0, 2.0);
   public static final VoxelShape LEG_FL = Block.box(14.0, 0.0, 0.0, 16.0, 3.0, 2.0);
   public static final VoxelShape ARM_L = Block.box(14.0, 7.0, 0.0, 16.0, 12.0, 16.0);
   public static final VoxelShape ARM_R = Block.box(0.0, 7.0, 0.0, 2.0, 12.0, 16.0);
   public static final VoxelShape SINGLE = Shapes.or(SEAT_BASE, new VoxelShape[]{SEAT_BACK_SINGLE, ARM_L, ARM_R, LEG_BL, LEG_BR, LEG_FL, LEG_FR});
   public static final VoxelShape SINGLE_EAST = ShapeUtil.rotateShape(SINGLE, Direction.EAST);
   public static final VoxelShape SINGLE_SOUTH = ShapeUtil.rotateShape(SINGLE, Direction.SOUTH);
   public static final VoxelShape SINGLE_WEST = ShapeUtil.rotateShape(SINGLE, Direction.WEST);
   public static final VoxelShape MIDDLE = Shapes.or(SEAT_BASE, SEAT_BACK_MIDDLE);
   public static final VoxelShape MIDDLE_EAST = ShapeUtil.rotateShape(MIDDLE, Direction.EAST);
   public static final VoxelShape MIDDLE_SOUTH = ShapeUtil.rotateShape(MIDDLE, Direction.SOUTH);
   public static final VoxelShape MIDDLE_WEST = ShapeUtil.rotateShape(MIDDLE, Direction.WEST);
   public static final VoxelShape OUTER = Shapes.or(SEAT_BASE, new VoxelShape[]{LEG_BL, LEG_FR, Block.box(12.0, 6.0, 12.0, 16.0, 16.0, 16.0)});
   public static final VoxelShape OUTER_EAST = ShapeUtil.rotateShape(OUTER, Direction.EAST);
   public static final VoxelShape OUTER_SOUTH = ShapeUtil.rotateShape(OUTER, Direction.SOUTH);
   public static final VoxelShape OUTER_WEST = ShapeUtil.rotateShape(OUTER, Direction.WEST);
   public static final VoxelShape INNER = Shapes.or(
      SEAT_BASE, new VoxelShape[]{SEAT_BACK_MIDDLE, LEG_BL, LEG_FR, Block.box(12.0, 7.0, 0.0, 16.0, 16.0, 12.0)}
   );
   public static final VoxelShape INNER_EAST = ShapeUtil.rotateShape(INNER, Direction.EAST);
   public static final VoxelShape INNER_SOUTH = ShapeUtil.rotateShape(INNER, Direction.SOUTH);
   public static final VoxelShape INNER_WEST = ShapeUtil.rotateShape(INNER, Direction.WEST);
   public static final VoxelShape RIGHT = Shapes.or(SEAT_BASE, new VoxelShape[]{SEAT_BACK_RIGHT, ARM_R, LEG_BR, LEG_FR});
   public static final VoxelShape RIGHT_EAST = ShapeUtil.rotateShape(RIGHT, Direction.EAST);
   public static final VoxelShape RIGHT_SOUTH = ShapeUtil.rotateShape(RIGHT, Direction.SOUTH);
   public static final VoxelShape RIGHT_WEST = ShapeUtil.rotateShape(RIGHT, Direction.WEST);
   public static final VoxelShape LEFT = Shapes.or(SEAT_BASE, new VoxelShape[]{SEAT_BACK_LEFT, ARM_L, LEG_BL, LEG_FL});
   public static final VoxelShape LEFT_EAST = ShapeUtil.rotateShape(LEFT, Direction.EAST);
   public static final VoxelShape LEFT_SOUTH = ShapeUtil.rotateShape(LEFT, Direction.SOUTH);
   public static final VoxelShape LEFT_WEST = ShapeUtil.rotateShape(LEFT, Direction.WEST);

   public SofaBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(WATERLOGGED, false)
      );
   }

   @Override
   public float seatHeight(BlockState state) {
      return 0.35F;
   }

   @Override
   public BlockPos primaryDismountLocation(Level level, BlockState state, BlockPos pos) {
      Direction facing = (Direction)state.getValue(FACING);
      BlockState stateRelative = level.getBlockState(pos.relative(facing));
      if (stateRelative.getBlock() instanceof SofaBlock) {
         Direction facingRelative = (Direction)stateRelative.getValue(FACING);
         if (facing != facingRelative) {
            return pos.relative(facing).relative(facingRelative);
         }
      }

      return pos.relative(facing);
   }

   @Override
   public float setRiderRotation(BlockState state, Entity entity) {
      float corner = switch ((SofaType)state.getValue(TYPE)) {
         case INNER_LEFT, OUTER_LEFT -> -45.0F;
         case INNER_RIGHT, OUTER_RIGHT -> 45.0F;
         default -> 0.0F;
      };
      return ((Direction)state.getValue(FACING)).toYRot() + corner;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockPos blockPos = context.getClickedPos();
      FluidState fluidState = context.getLevel().getFluidState(blockPos);
      BlockState blockState = (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()))
         .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
      return (BlockState)blockState.setValue(TYPE, getConnection(blockState, context.getLevel(), blockPos));
   }

   public BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
   ) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         tickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      return direction.getAxis().isHorizontal() && level instanceof Level level1
         ? (BlockState)state.setValue(TYPE, getConnection(state, level1, currentPos))
         : super.updateShape(state, level, tickAccess, currentPos, direction, neighborPos, neighborState, random);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      Direction facing = (Direction)state.getValue(FACING);
      SofaType shape = (SofaType)state.getValue(TYPE);

      return switch (shape) {
         case INNER_LEFT -> {
            switch (facing) {
               case EAST:
                  yield INNER_EAST;
               case SOUTH:
                  yield INNER_SOUTH;
               case WEST:
                  yield INNER_WEST;
               default:
                  yield INNER;
            }
         }
         case OUTER_LEFT -> {
            switch (facing) {
               case EAST:
                  yield OUTER_EAST;
               case SOUTH:
                  yield OUTER_SOUTH;
               case WEST:
                  yield OUTER_WEST;
               default:
                  yield OUTER;
            }
         }
         case INNER_RIGHT -> {
            switch (facing) {
               case EAST:
                  yield INNER_SOUTH;
               case SOUTH:
                  yield INNER_WEST;
               case WEST:
                  yield INNER;
               default:
                  yield INNER_EAST;
            }
         }
         case OUTER_RIGHT -> {
            switch (facing) {
               case EAST:
                  yield OUTER_SOUTH;
               case SOUTH:
                  yield OUTER_WEST;
               case WEST:
                  yield OUTER;
               default:
                  yield OUTER_EAST;
            }
         }
         case SINGLE -> {
            switch (facing) {
               case EAST:
                  yield SINGLE_EAST;
               case SOUTH:
                  yield SINGLE_SOUTH;
               case WEST:
                  yield SINGLE_WEST;
               default:
                  yield SINGLE;
            }
         }
         case MIDDLE -> {
            switch (facing) {
               case EAST:
                  yield MIDDLE_EAST;
               case SOUTH:
                  yield MIDDLE_SOUTH;
               case WEST:
                  yield MIDDLE_WEST;
               default:
                  yield MIDDLE;
            }
         }
         case LEFT -> {
            switch (facing) {
               case EAST:
                  yield LEFT_EAST;
               case SOUTH:
                  yield LEFT_SOUTH;
               case WEST:
                  yield LEFT_WEST;
               default:
                  yield LEFT;
            }
         }
         case RIGHT -> {
            switch (facing) {
               case EAST:
                  yield RIGHT_EAST;
               case SOUTH:
                  yield RIGHT_SOUTH;
               case WEST:
                  yield RIGHT_WEST;
               default:
                  yield RIGHT;
            }
         }
      };
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{FACING, TYPE, WATERLOGGED});
   }

   public static SofaType getConnection(BlockState state, Level level, BlockPos pos) {
      Direction facing = (Direction)state.getValue(FACING);
      BlockState state1 = level.getBlockState(pos.relative(facing));
      Direction dir1;
      if (!(state1.getBlock() instanceof SofaBlock)
         || (dir1 = (Direction)state1.getValue(FACING)).getAxis() == ((Direction)state.getValue(FACING)).getAxis()
         || !isDifferentOrientation(state, level, pos, dir1.getOpposite())) {
         BlockState state2 = level.getBlockState(pos.relative(facing.getOpposite()));
         Direction dir2;
         if (!(state2.getBlock() instanceof SofaBlock)
            || (dir2 = (Direction)state2.getValue(FACING)).getAxis() == ((Direction)state.getValue(FACING)).getAxis()
            || !isDifferentOrientation(state, level, pos, dir2)) {
            boolean left = canConnect(level, pos, ((Direction)state.getValue(FACING)).getCounterClockWise());
            boolean right = canConnect(level, pos, ((Direction)state.getValue(FACING)).getClockWise());
            if (left && right) {
               return SofaType.MIDDLE;
            } else if (left) {
               return SofaType.LEFT;
            } else {
               return right ? SofaType.RIGHT : SofaType.SINGLE;
            }
         } else {
            return dir2 == facing.getCounterClockWise() ? SofaType.OUTER_LEFT : SofaType.OUTER_RIGHT;
         }
      } else {
         return dir1 == facing.getCounterClockWise() ? SofaType.INNER_LEFT : SofaType.INNER_RIGHT;
      }
   }

   public static boolean canConnect(Level level, BlockPos pos, Direction direction) {
      BlockState state = level.getBlockState(pos.relative(direction));
      return state.getBlock() instanceof SofaBlock;
   }

   public static boolean isDifferentOrientation(BlockState state, Level level, BlockPos pos, Direction dir) {
      BlockState blockState = level.getBlockState(pos.relative(dir));
      return !(blockState.getBlock() instanceof SofaBlock) || blockState.getValue(FACING) != state.getValue(FACING);
   }

   @Override
   protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
      return false;
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      Direction direction = (Direction)state.getValue(FACING);
      SofaType type = (SofaType)state.getValue(TYPE);
      switch (mirror) {
         case LEFT_RIGHT:
            if (direction.getAxis() == Axis.Z) {
               return switch (type) {
                  case INNER_LEFT -> (BlockState)state.rotate(Rotation.CLOCKWISE_180).setValue(TYPE, SofaType.INNER_RIGHT);
                  case OUTER_LEFT -> (BlockState)state.rotate(Rotation.CLOCKWISE_180).setValue(TYPE, SofaType.OUTER_RIGHT);
                  case INNER_RIGHT -> (BlockState)state.rotate(Rotation.CLOCKWISE_180).setValue(TYPE, SofaType.INNER_LEFT);
                  case OUTER_RIGHT -> (BlockState)state.rotate(Rotation.CLOCKWISE_180).setValue(TYPE, SofaType.OUTER_LEFT);
                  default -> state.rotate(Rotation.CLOCKWISE_180);
               };
            }
            break;
         case FRONT_BACK:
            if (direction.getAxis() == Axis.X) {
               return switch (type) {
                  case INNER_LEFT -> (BlockState)state.rotate(Rotation.CLOCKWISE_180).setValue(TYPE, SofaType.INNER_LEFT);
                  case OUTER_LEFT -> (BlockState)state.rotate(Rotation.CLOCKWISE_180).setValue(TYPE, SofaType.OUTER_RIGHT);
                  case INNER_RIGHT -> (BlockState)state.rotate(Rotation.CLOCKWISE_180).setValue(TYPE, SofaType.INNER_RIGHT);
                  case OUTER_RIGHT -> (BlockState)state.rotate(Rotation.CLOCKWISE_180).setValue(TYPE, SofaType.OUTER_LEFT);
                  default -> state.rotate(Rotation.CLOCKWISE_180);
               };
            }
      }

      return super.mirror(state, mirror);
   }
}
