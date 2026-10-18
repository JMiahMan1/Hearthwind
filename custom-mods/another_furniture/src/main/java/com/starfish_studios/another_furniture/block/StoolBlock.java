package com.starfish_studios.another_furniture.block;

import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.util.block.HammerableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;

public class StoolBlock extends SeatBlock implements SimpleWaterloggedBlock, HammerableBlock {
   protected static final VoxelShape SHAPE_HIGH = Block.box(0.0, 3.0, 0.0, 16.0, 8.0, 16.0);
   protected static final VoxelShape SHAPE_LOW = Block.box(0.0, 3.0, 0.0, 16.0, 7.0, 16.0);
   protected static final VoxelShape LEG1 = Block.box(14.0, 0.0, 14.0, 16.0, 3.0, 16.0);
   protected static final VoxelShape LEG2 = Block.box(0.0, 0.0, 14.0, 2.0, 3.0, 16.0);
   protected static final VoxelShape LEG3 = Block.box(0.0, 0.0, 0.0, 2.0, 3.0, 2.0);
   protected static final VoxelShape LEG4 = Block.box(14.0, 0.0, 0.0, 16.0, 3.0, 2.0);
   protected static final VoxelShape AABB_HIGH = Shapes.or(SHAPE_HIGH, new VoxelShape[]{LEG1, LEG2, LEG3, LEG4});
   protected static final VoxelShape AABB_LOW = Shapes.or(SHAPE_LOW, new VoxelShape[]{LEG1, LEG2, LEG3, LEG4});
   public static final BooleanProperty LOW = ModBlockStateProperties.LOW;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

   public StoolBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(WATERLOGGED, false)).setValue(LOW, false));
   }

   @Override
   public float seatHeight(BlockState state) {
      return state.getValue(LOW) ? 0.35F : 0.4F;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return state.getValue(LOW) ? AABB_LOW : AABB_HIGH;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
      return (BlockState)this.defaultBlockState().setValue(WATERLOGGED, waterlogged);
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

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return this.tryHammerBlock(LOW, stack, state, level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return super.useWithoutItem(state, level, pos, player, hitResult);
   }

   public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double dmg) {
      super.fallOn(level, state, pos, entity, dmg * 0.5);
   }

   
   
   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{LOW, WATERLOGGED});
   }

   @Override
   protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
      return false;
   }
   @Override
   public float getBounceRestitution() {
      return 0.66F;
   }

}
