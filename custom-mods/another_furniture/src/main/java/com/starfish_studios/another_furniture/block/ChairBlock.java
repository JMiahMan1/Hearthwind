package com.starfish_studios.another_furniture.block;

import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.util.block.HammerableBlock;
import com.starfish_studios.another_furniture.util.block.ShapeUtil;
import com.starfish_studios.another_furniture.util.block.TuckableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
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

public class ChairBlock extends SeatBlock implements SimpleWaterloggedBlock, HammerableBlock, TuckableBlock {
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final IntegerProperty VARIANT = ModBlockStateProperties.VARIANT;
   protected static final VoxelShape SHAPE_NORTH = Shapes.or(
      Block.box(2.0, 0.0, 2.0, 14.0, 7.0, 14.0), Block.box(2.0, 7.0, 12.0, 14.0, 16.0, 14.0)
   );
   protected static final VoxelShape SHAPE_EAST = ShapeUtil.rotateShape(SHAPE_NORTH, Direction.EAST);
   protected static final VoxelShape SHAPE_SOUTH = ShapeUtil.rotateShape(SHAPE_NORTH, Direction.SOUTH);
   protected static final VoxelShape SHAPE_WEST = ShapeUtil.rotateShape(SHAPE_NORTH, Direction.WEST);
   protected static final VoxelShape SHAPE_NORTH_TUCKED = Shapes.or(
      Block.box(2.0, 7.0, 3.0, 14.0, 16.0, 5.0), Block.box(2.0, 0.0, 0.0, 14.0, 7.0, 5.0)
   );
   protected static final VoxelShape SHAPE_EAST_TUCKED = ShapeUtil.rotateShape(SHAPE_NORTH_TUCKED, Direction.EAST);
   protected static final VoxelShape SHAPE_SOUTH_TUCKED = ShapeUtil.rotateShape(SHAPE_NORTH_TUCKED, Direction.SOUTH);
   protected static final VoxelShape SHAPE_WEST_TUCKED = ShapeUtil.rotateShape(SHAPE_NORTH_TUCKED, Direction.WEST);
   protected static final VoxelShape[] SHAPES = new VoxelShape[]{
      SHAPE_SOUTH, SHAPE_WEST, SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH_TUCKED, SHAPE_WEST_TUCKED, SHAPE_NORTH_TUCKED, SHAPE_EAST_TUCKED
   };

   public ChairBlock(int defaultBackVariant, Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
                  .setValue(WATERLOGGED, false))
               .setValue(TUCKED, false))
            .setValue(VARIANT, defaultBackVariant)
      );
   }

   @Override
   public float seatHeight(BlockState state) {
      return 0.35F;
   }

   @Override
   public boolean isSittable(BlockState state) {
      return !(Boolean)state.getValue(TUCKED);
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
      int shape = ((Direction)state.getValue(FACING)).get2DDataValue();
      if ((Boolean)state.getValue(TUCKED)) {
         shape += 4;
      }

      return SHAPES[shape];
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

      return state.getValue(TUCKED) && level instanceof Level level1 && !TuckableBlock.canTuckUnderBlockInfront(state, level1, currentPos)
         ? (BlockState)state.setValue(TUCKED, false)
         : super.updateShape(state, level, tickAccess, currentPos, direction, neighborPos, neighborState, random);
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return this.tryHammerBlock(VARIANT, stack, state, level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return TuckableBlock.tryTuck(state, level, pos, player) ? InteractionResult.SUCCESS : super.useWithoutItem(state, level, pos, player, hitResult);
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{FACING, TUCKED, VARIANT, WATERLOGGED});
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
