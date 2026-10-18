package com.starfish_studios.another_furniture.block;

import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.registry.AFBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;

public class LampConnectorBlock extends Block implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty BASE = ModBlockStateProperties.BASE;
   protected static final VoxelShape AABB_NOT_BASE = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);
   protected static final VoxelShape AABB_BASE = Shapes.or(
      Block.box(5.0, 0.0, 5.0, 11.0, 2.0, 11.0), Block.box(7.0, 2.0, 7.0, 9.0, 16.0, 9.0)
   );
   private final DyeColor color;

   public LampConnectorBlock(DyeColor color, Properties properties) {
      super(properties);
      this.color = color;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(POWERED, false)).setValue(WATERLOGGED, false))
            .setValue(BASE, true)
      );
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return state.getValue(BASE) ? AABB_BASE : AABB_NOT_BASE;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState blockstate = this.defaultBlockState();
      if (level.hasNeighborSignal(pos)) {
         blockstate = (BlockState)blockstate.setValue(POWERED, true);
      }

      return (BlockState)blockstate.setValue(WATERLOGGED, waterlogged);
   }

   public BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
   ) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         tickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      if (direction == Direction.UP || direction == Direction.DOWN) {
         BlockState aState = level.getBlockState(currentPos.above());
         BlockState bState = level.getBlockState(currentPos.below());
         boolean aConnect = aState.getBlock() instanceof LampBlock lampBlock
               && lampBlock.getColor() == this.getColor()
               && aState.getValue(LampBlock.FACING) == Direction.UP
            || aState.getBlock() instanceof LampConnectorBlock connectorBlock && connectorBlock.getColor() == this.getColor();
         boolean bConnect = bState.getBlock() instanceof LampBlock lampBlockx
               && lampBlockx.getColor() == this.getColor()
               && bState.getValue(LampBlock.FACING) == Direction.UP
            || bState.getBlock() instanceof LampConnectorBlock connectorBlockx && connectorBlockx.getColor() == this.getColor();
         if (aConnect && !bConnect) {
            state = (BlockState)state.setValue(BASE, true);
         } else if (!aConnect && bConnect) {
            state = (BlockState)((BlockState)getLampByColor(this.color).defaultBlockState().setValue(BASE, false))
               .setValue(WATERLOGGED, (Boolean)state.getValue(WATERLOGGED));
         } else if (aConnect) {
            state = (BlockState)state.setValue(BASE, false);
         } else {
            state = (BlockState)((BlockState)getLampByColor(this.color).defaultBlockState().setValue(BASE, true))
               .setValue(WATERLOGGED, (Boolean)state.getValue(WATERLOGGED));
         }
      }

      return state;
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if (!level.isClientSide()) {
         BlockState below = level.getBlockState(pos.below());
         boolean powered = level.hasNeighborSignal(pos) || below.getBlock() instanceof LampConnectorBlock && (Boolean)below.getValue(POWERED);
         if (powered != (Boolean)state.getValue(POWERED)) {
            state = (BlockState)state.setValue(POWERED, powered);
         }

         level.setBlock(pos, state, 3);
      }
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{WATERLOGGED, POWERED, BASE});
   }

   public DyeColor getColor() {
      return this.color;
   }

   public static Block getLampByColor(DyeColor color) {
      return switch (color) {
         case WHITE -> (Block)AFBlocks.WHITE_LAMP.get();
         case ORANGE -> (Block)AFBlocks.ORANGE_LAMP.get();
         case MAGENTA -> (Block)AFBlocks.MAGENTA_LAMP.get();
         case LIGHT_BLUE -> (Block)AFBlocks.LIGHT_BLUE_LAMP.get();
         case YELLOW -> (Block)AFBlocks.YELLOW_LAMP.get();
         case LIME -> (Block)AFBlocks.LIME_LAMP.get();
         case PINK -> (Block)AFBlocks.PINK_LAMP.get();
         case GRAY -> (Block)AFBlocks.GRAY_LAMP.get();
         case LIGHT_GRAY -> (Block)AFBlocks.LIGHT_GRAY_LAMP.get();
         case CYAN -> (Block)AFBlocks.CYAN_LAMP.get();
         case PURPLE -> (Block)AFBlocks.PURPLE_LAMP.get();
         case BLUE -> (Block)AFBlocks.BLUE_LAMP.get();
         case BROWN -> (Block)AFBlocks.BROWN_LAMP.get();
         case GREEN -> (Block)AFBlocks.GREEN_LAMP.get();
         case RED -> (Block)AFBlocks.RED_LAMP.get();
         case BLACK -> (Block)AFBlocks.BLACK_LAMP.get();
         default -> throw new MatchException(null, null);
      };
   }

}
