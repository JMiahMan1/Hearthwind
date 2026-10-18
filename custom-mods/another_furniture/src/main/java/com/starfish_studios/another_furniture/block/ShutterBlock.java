package com.starfish_studios.another_furniture.block;

import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.block.properties.VerticalConnectionType;
import com.starfish_studios.another_furniture.util.block.BlockPart;
import com.starfish_studios.another_furniture.util.block.HammerableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;

public class ShutterBlock extends Block implements SimpleWaterloggedBlock, HammerableBlock {
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final net.minecraft.world.level.block.state.properties.EnumProperty<VerticalConnectionType> VERTICAL = ModBlockStateProperties.VERTICAL_CONNECTION_TYPE;
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   public static final net.minecraft.world.level.block.state.properties.EnumProperty<net.minecraft.world.level.block.state.properties.DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final IntegerProperty VARIANT = ModBlockStateProperties.VARIANT;
   protected static final VoxelShape EAST = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
   protected static final VoxelShape WEST = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
   protected static final VoxelShape NORTH = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape[] SHAPES = new VoxelShape[]{SOUTH, WEST, NORTH, EAST};

   public ShutterBlock(int defaultVariant, Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any())
                           .setValue(FACING, Direction.NORTH))
                        .setValue(VERTICAL, VerticalConnectionType.SINGLE))
                     .setValue(OPEN, false))
                  .setValue(VARIANT, defaultVariant))
               .setValue(POWERED, false))
            .setValue(WATERLOGGED, false)
      );
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      int shape = ((Direction)state.getValue(FACING)).get2DDataValue()
         + (state.getValue(OPEN) ? (state.getValue(HINGE) == DoorHingeSide.LEFT ? 3 : 1) : 0);
      return SHAPES[shape % 4];
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction facing = context.getHorizontalDirection().getOpposite();
      BlockState state = (BlockState)this.defaultBlockState().setValue(FACING, facing);
      Level level = context.getLevel();
      BlockPos clickedPos = context.getClickedPos();
      Vec3 clickLocation = context.getClickLocation();
      state = (BlockState)state.setValue(
         HINGE, BlockPart.get1D(clickedPos, clickLocation, facing.getClockWise(), 2) == 0 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT
      );
      boolean powered = level.hasNeighborSignal(clickedPos);
      return (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(OPEN, powered)).setValue(POWERED, powered))
            .setValue(VERTICAL, this.getType(state, level.getBlockState(clickedPos.above()), level.getBlockState(clickedPos.below()))))
         .setValue(WATERLOGGED, level.getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if (!level.isClientSide()) {
         boolean powered = level.hasNeighborSignal(pos);
         if (powered != (Boolean)state.getValue(POWERED)) {
            if ((Boolean)state.getValue(OPEN) != powered) {
               state = (BlockState)state.setValue(OPEN, powered);
               this.toggleShutters((BlockState)state.setValue(OPEN, !powered), level, pos, null);
            }

            state = (BlockState)state.setValue(POWERED, powered);
            if ((Boolean)state.getValue(WATERLOGGED)) {
               level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
         }

         VerticalConnectionType type = this.getType(state, level.getBlockState(pos.above()), level.getBlockState(pos.below()));
         state = (BlockState)state.setValue(VERTICAL, type);
         level.setBlock(pos, state, 3);
      }
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return this.tryHammerBlock(VARIANT, stack, state, level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return this.toggleShutters(state, level, pos, player);
   }

   public InteractionResult toggleShutters(BlockState state, Level level, BlockPos pos, Player player) {
      state = (BlockState)state.cycle(OPEN);
      level.setBlock(pos, state, 3);
      if (player == null || !player.isCrouching()) {
         this.toggleShutters(state, level, pos, (Boolean)state.getValue(OPEN));
      }

      level.playSound(null, pos, shutterSound((Boolean)state.getValue(OPEN)), SoundSource.BLOCKS, 1.0F, 1.0F);
      if ((Boolean)state.getValue(WATERLOGGED)) {
         level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
   }

   public void toggleShutters(BlockState state, Level level, BlockPos pos, boolean open) {
      BlockState updateState = state;
      BlockPos updatePos = pos;
      if (state.getValue(VERTICAL) == VerticalConnectionType.MIDDLE || state.getValue(VERTICAL) == VerticalConnectionType.BOTTOM) {
         int heightUp = level.getMaxY() - pos.getY();

         for (int i = 0; i < heightUp; i++) {
            BlockState above = level.getBlockState(updatePos.above());
            if (!above.is(state.getBlock())
               || above.getValue(FACING) != updateState.getValue(FACING)
               || above.getValue(HINGE) != updateState.getValue(HINGE)
               || (Boolean)above.getValue(OPEN) == open) {
               break;
            }

            updateState = above;
            updatePos = updatePos.above();
            level.setBlock(updatePos, (BlockState)above.setValue(OPEN, open), 3);
         }
      }

      if (state.getValue(VERTICAL) == VerticalConnectionType.MIDDLE || state.getValue(VERTICAL) == VerticalConnectionType.TOP) {
         updateState = state;
         updatePos = pos;
         int heightDown = level.getMinY() - pos.getY();
         heightDown = heightDown < 0 ? -heightDown : heightDown;

         for (int i = 0; i < heightDown; i++) {
            BlockState below = level.getBlockState(updatePos.below());
            if (!below.is(state.getBlock())
               || below.getValue(FACING) != updateState.getValue(FACING)
               || below.getValue(HINGE) != updateState.getValue(HINGE)
               || (Boolean)below.getValue(OPEN) == open) {
               break;
            }

            updateState = below;
            updatePos = updatePos.below();
            level.setBlock(updatePos, (BlockState)below.setValue(OPEN, open), 3);
         }
      }
   }

   public static SoundEvent shutterSound(boolean open) {
      return open ? SoundEvents.WOODEN_TRAPDOOR_OPEN : SoundEvents.WOODEN_TRAPDOOR_CLOSE;
   }

   public VerticalConnectionType getType(BlockState state, BlockState above, BlockState below) {
      boolean up = this.isConnected(state, above);
      boolean down = this.isConnected(state, below);
      if (up && down) {
         return VerticalConnectionType.MIDDLE;
      } else if (up) {
         return VerticalConnectionType.BOTTOM;
      } else {
         return down ? VerticalConnectionType.TOP : VerticalConnectionType.SINGLE;
      }
   }

   public boolean isConnected(BlockState state, BlockState other) {
      return other.is(state.getBlock())
         && other.getValue(FACING) == state.getValue(FACING)
         && other.getValue(OPEN) == state.getValue(OPEN)
         && other.getValue(HINGE) == state.getValue(HINGE)
         && ((Integer)other.getValue(VARIANT)).equals(state.getValue(VARIANT));
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{FACING, VERTICAL, OPEN, HINGE, VARIANT, POWERED, WATERLOGGED});
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
   ) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         tickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      return super.updateShape(state, level, tickAccess, currentPos, direction, neighborPos, neighborState, random);
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }
}
