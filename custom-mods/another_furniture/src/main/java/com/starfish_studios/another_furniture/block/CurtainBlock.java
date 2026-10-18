package com.starfish_studios.another_furniture.block;

import com.starfish_studios.another_furniture.block.properties.HorizontalConnectionType;
import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.registry.AFSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;

public class CurtainBlock extends Block implements SimpleWaterloggedBlock {
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final net.minecraft.world.level.block.state.properties.EnumProperty<HorizontalConnectionType> HORIZONTAL_CONNECTION_TYPE = ModBlockStateProperties.HORIZONTAL_CONNECTION_TYPE;
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final EnumProperty<Direction> VERTICAL_CONNECTION_TYPE = ModBlockStateProperties.VERTICAL_CONNECTION_TYPE_UP_DOWN;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape NORTH = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape EAST = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
   protected static final VoxelShape SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
   protected static final VoxelShape WEST = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);

   public CurtainBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any())
                        .setValue(FACING, Direction.NORTH))
                     .setValue(HORIZONTAL_CONNECTION_TYPE, HorizontalConnectionType.SINGLE))
                  .setValue(VERTICAL_CONNECTION_TYPE, Direction.UP))
               .setValue(OPEN, false))
            .setValue(WATERLOGGED, false)
      );
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      HorizontalConnectionType type = (HorizontalConnectionType)state.getValue(HORIZONTAL_CONNECTION_TYPE);
      if ((Boolean)state.getValue(OPEN)
         && (type == HorizontalConnectionType.SINGLE || type == HorizontalConnectionType.MIDDLE)
         && state.getValue(VERTICAL_CONNECTION_TYPE) == Direction.DOWN) {
         return Shapes.empty();
      } else {
         return switch ((Direction)state.getValue(FACING)) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
         };
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return state.getValue(OPEN) ? Shapes.empty() : super.getCollisionShape(state, level, pos, context);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      if (level.getBlockState(pos.below()).canBeReplaced(context) && !level.isOutsideBuildHeight(pos.below())) {
         Direction facing = context.getClickedFace();
         BlockPos clickedPos = context.getClickedPos();
         Direction clickedFace = context.getClickedFace();
         BlockPos clickedFacingPos = clickedPos.relative(clickedFace);
         BlockState clickedFacingState = level.getBlockState(clickedFacingPos);
         if (clickedFacingState.getBlock() instanceof CurtainBlock) {
            Direction clickedFacingFace = (Direction)clickedFacingState.getValue(FACING);
            if (clickedFacingFace != clickedFace && clickedFacingFace.getOpposite() != clickedFace) {
               facing = clickedFacingFace;
            }
         }

         if (facing.getAxis().isVertical()) {
            facing = context.getHorizontalDirection().getOpposite();
         }

         BlockState stateL = level.getBlockState(pos.relative(facing.getClockWise()));
         BlockState stateR = level.getBlockState(pos.relative(facing.getCounterClockWise()));
         boolean sideL = stateL.getBlock() instanceof CurtainBlock
            && stateL.getValue(VERTICAL_CONNECTION_TYPE) == Direction.UP
            && stateL.getValue(FACING) == facing;
         boolean sideR = stateR.getBlock() instanceof CurtainBlock
            && stateR.getValue(VERTICAL_CONNECTION_TYPE) == Direction.UP
            && stateR.getValue(FACING) == facing;
         boolean open = sideL && sideR
            ? (Boolean)stateL.getValue(OPEN) || (Boolean)stateR.getValue(OPEN)
            : (sideR ? (Boolean)stateR.getValue(OPEN) : (sideL ? (Boolean)stateL.getValue(OPEN) : true));
         return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, facing)).setValue(OPEN, open))
            .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
      } else {
         return null;
      }
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide()) {
         BlockPos blockPos = pos.below();
         level.setBlock(
            blockPos,
            (BlockState)((BlockState)state.setValue(VERTICAL_CONNECTION_TYPE, Direction.DOWN))
               .setValue(WATERLOGGED, level.getFluidState(blockPos).getType() == Fluids.WATER),
            3
         );
         level.updateNeighborsAt(pos, Blocks.AIR, null);
         state.updateNeighbourShapes(level, pos, 3);
      }
   }

   public BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
   ) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         tickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      Direction facing = (Direction)state.getValue(FACING);
      Direction facing_vertical = (Direction)state.getValue(VERTICAL_CONNECTION_TYPE);
      if (direction.getAxis().isVertical()) {
         BlockState stateOpposite = level.getBlockState(currentPos.relative(facing_vertical.getOpposite()));
         if (!(stateOpposite.getBlock() instanceof CurtainBlock) || stateOpposite.getValue(VERTICAL_CONNECTION_TYPE) == facing_vertical) {
            return Blocks.AIR.defaultBlockState();
         }
      }

      if (direction != facing.getClockWise() && direction != facing.getCounterClockWise()) {
         return state;
      } else {
         BlockState stateL = level.getBlockState(currentPos.relative(facing.getClockWise()));
         BlockState stateR = level.getBlockState(currentPos.relative(facing.getCounterClockWise()));
         boolean sideL = stateL.getBlock() instanceof CurtainBlock
            && stateL.getValue(VERTICAL_CONNECTION_TYPE) == facing_vertical
            && stateL.getValue(FACING) == facing;
         boolean sideR = stateR.getBlock() instanceof CurtainBlock
            && stateR.getValue(VERTICAL_CONNECTION_TYPE) == facing_vertical
            && stateR.getValue(FACING) == facing;
         HorizontalConnectionType type = sideL && sideR
            ? HorizontalConnectionType.MIDDLE
            : (sideR ? HorizontalConnectionType.LEFT : (sideL ? HorizontalConnectionType.RIGHT : HorizontalConnectionType.SINGLE));
         return (BlockState)state.setValue(HORIZONTAL_CONNECTION_TYPE, type);
      }
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      if (state.getValue(VERTICAL_CONNECTION_TYPE) == Direction.UP) {
         this.toggleFromTop(state, level, pos);
      } else {
         this.toggleFromTop(level.getBlockState(pos.above()), level, pos.above());
      }

      level.playSound(null, pos, (SoundEvent)AFSoundEvents.CURTAIN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
      return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
   }

   public void toggleFromTop(BlockState state, Level level, BlockPos pos) {
      boolean open = !(Boolean)state.getValue(OPEN);
      Direction facing = (Direction)state.getValue(FACING);
      Direction relativeLeft = facing.getClockWise();
      Direction relativeRight = facing.getCounterClockWise();
      this.toggleVertical(state, level, pos, open);
      this.toggleHorizontal(state, level, pos, open, facing, relativeLeft);
      this.toggleHorizontal(state, level, pos, open, facing, relativeRight);
   }

   public void toggleHorizontal(BlockState state, Level level, BlockPos pos, boolean open, Direction facing, Direction dir) {
      BlockPos facingHorizontalPos = pos.relative(dir);
      BlockState facingHorizontalState = level.getBlockState(facingHorizontalPos);
      if (facingHorizontalState.getBlock() instanceof CurtainBlock
         && facingHorizontalState.getValue(FACING) == facing
         && state.getValue(VERTICAL_CONNECTION_TYPE) == facingHorizontalState.getValue(VERTICAL_CONNECTION_TYPE)) {
         this.toggleVertical(facingHorizontalState, level, facingHorizontalPos, open);
         this.toggleHorizontal(facingHorizontalState, level, facingHorizontalPos, open, facing, dir);
      }
   }

   public void toggleVertical(BlockState state, Level level, BlockPos pos, boolean open) {
      Direction facingVertical = (Direction)state.getValue(VERTICAL_CONNECTION_TYPE);
      BlockPos facingVerticalPos = pos.relative(facingVertical.getOpposite());
      BlockState facingVerticalState = level.getBlockState(facingVerticalPos);
      level.setBlockAndUpdate(pos, (BlockState)state.setValue(OPEN, open));
      if (facingVerticalState.getBlock() instanceof CurtainBlock
         && facingVertical == ((Direction)facingVerticalState.getValue(VERTICAL_CONNECTION_TYPE)).getOpposite()) {
         level.setBlockAndUpdate(facingVerticalPos, (BlockState)facingVerticalState.setValue(OPEN, open));
      }
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{FACING, HORIZONTAL_CONNECTION_TYPE, OPEN, VERTICAL_CONNECTION_TYPE, WATERLOGGED});
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }
}
