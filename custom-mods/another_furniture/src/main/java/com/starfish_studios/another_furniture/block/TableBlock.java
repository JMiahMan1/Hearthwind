package com.starfish_studios.another_furniture.block;

import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.registry.AFBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;

public class TableBlock extends Block implements SimpleWaterloggedBlock {
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty LEG1 = ModBlockStateProperties.LEG_1;
   public static final BooleanProperty LEG2 = ModBlockStateProperties.LEG_2;
   public static final BooleanProperty LEG3 = ModBlockStateProperties.LEG_3;
   public static final BooleanProperty LEG4 = ModBlockStateProperties.LEG_4;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty UPDATE = ModBlockStateProperties.UPDATE;
   protected static final VoxelShape TOP = Block.box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape LEG_1 = Block.box(14.0, 0.0, 0.0, 16.0, 14.0, 2.0);
   protected static final VoxelShape LEG_2 = Block.box(14.0, 0.0, 14.0, 16.0, 14.0, 16.0);
   protected static final VoxelShape LEG_3 = Block.box(0.0, 0.0, 14.0, 2.0, 14.0, 16.0);
   protected static final VoxelShape LEG_4 = Block.box(0.0, 0.0, 0.0, 2.0, 14.0, 2.0);
   protected static final VoxelShape[] SHAPES = new VoxelShape[]{
      TOP,
      Shapes.or(TOP, LEG_1),
      Shapes.or(TOP, LEG_2),
      Shapes.or(TOP, new VoxelShape[]{LEG_1, LEG_2}),
      Shapes.or(TOP, LEG_3),
      Shapes.or(TOP, new VoxelShape[]{LEG_1, LEG_3}),
      Shapes.or(TOP, new VoxelShape[]{LEG_2, LEG_3}),
      Shapes.or(TOP, new VoxelShape[]{LEG_1, LEG_2, LEG_3}),
      Shapes.or(TOP, LEG_4),
      Shapes.or(TOP, new VoxelShape[]{LEG_1, LEG_4}),
      Shapes.or(TOP, new VoxelShape[]{LEG_2, LEG_4}),
      Shapes.or(TOP, new VoxelShape[]{LEG_1, LEG_2, LEG_4}),
      Shapes.or(TOP, new VoxelShape[]{LEG_3, LEG_4}),
      Shapes.or(TOP, new VoxelShape[]{LEG_1, LEG_3, LEG_4}),
      Shapes.or(TOP, new VoxelShape[]{LEG_2, LEG_3, LEG_4}),
      Shapes.or(TOP, new VoxelShape[]{LEG_1, LEG_2, LEG_3, LEG_4})
   };

   public TableBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any())
                           .setValue(FACING, Direction.NORTH))
                        .setValue(LEG1, true))
                     .setValue(LEG2, true))
                  .setValue(LEG3, true))
               .setValue(LEG4, true))
            .setValue(WATERLOGGED, false)
      );
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      int shape = 0;
      if ((Boolean)state.getValue(LEG1)) {
         shape++;
      }

      if ((Boolean)state.getValue(LEG2)) {
         shape += 2;
      }

      if ((Boolean)state.getValue(LEG3)) {
         shape += 4;
      }

      if ((Boolean)state.getValue(LEG4)) {
         shape += 8;
      }

      return SHAPES[shape];
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
      BlockState state = (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()))
         .setValue(WATERLOGGED, waterlogged);
      return this.getConnections(state, context.getLevel(), context.getClickedPos());
   }

   public BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
   ) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         tickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      return this.getConnections(state, level, currentPos);
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{FACING, LEG1, LEG2, LEG3, LEG4, UPDATE, WATERLOGGED});
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      boolean leg1 = (Boolean)state.getValue(LEG1);
      boolean leg2 = (Boolean)state.getValue(LEG2);
      boolean leg3 = (Boolean)state.getValue(LEG3);
      boolean leg4 = (Boolean)state.getValue(LEG4);

      return switch (rotation) {
         case NONE -> (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
         case CLOCKWISE_90 -> (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.setValue(
                        FACING, rotation.rotate((Direction)state.getValue(FACING))
                     ))
                     .setValue(LEG1, leg4))
                  .setValue(LEG2, leg1))
               .setValue(LEG3, leg2))
            .setValue(LEG4, leg3);
         case CLOCKWISE_180 -> (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.setValue(
                        FACING, rotation.rotate((Direction)state.getValue(FACING))
                     ))
                     .setValue(LEG1, leg3))
                  .setValue(LEG2, leg4))
               .setValue(LEG3, leg1))
            .setValue(LEG4, leg2);
         case COUNTERCLOCKWISE_90 -> (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.setValue(
                        FACING, rotation.rotate((Direction)state.getValue(FACING))
                     ))
                     .setValue(LEG1, leg2))
                  .setValue(LEG2, leg3))
               .setValue(LEG3, leg4))
            .setValue(LEG4, leg1);
         default -> throw new MatchException(null, null);
      };
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
      return false;
   }

   public BlockState getConnections(BlockState state, LevelReader level, BlockPos pos) {
      boolean n = this.validConnection(level.getBlockState(pos.north()));
      boolean e = this.validConnection(level.getBlockState(pos.east()));
      boolean s = this.validConnection(level.getBlockState(pos.south()));
      boolean w = this.validConnection(level.getBlockState(pos.west()));
      boolean leg1 = !n && !e || n && e && !this.validConnection(level.getBlockState(pos.north().east()));
      boolean leg2 = !e && !s || e && s && !this.validConnection(level.getBlockState(pos.south().east()));
      boolean leg3 = !s && !w || s && w && !this.validConnection(level.getBlockState(pos.south().west()));
      boolean leg4 = !n && !w || n && w && !this.validConnection(level.getBlockState(pos.north().west()));
      boolean update = ((n ? 1 : 0) + (e ? 1 : 0) + (s ? 1 : 0) + (w ? 1 : 0)) % 2 == 0;
      return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.setValue(LEG1, leg1)).setValue(LEG2, leg2)).setValue(LEG3, leg3))
            .setValue(LEG4, leg4))
         .setValue(UPDATE, update);
   }

   public boolean validConnection(BlockState state) {
      return state.is(AFBlockTags.TABLES_CONNECTABLE);
   }
}
