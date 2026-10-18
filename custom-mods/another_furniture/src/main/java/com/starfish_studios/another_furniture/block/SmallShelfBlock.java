package com.starfish_studios.another_furniture.block;

import com.mojang.serialization.MapCodec;
import com.starfish_studios.another_furniture.block.entity.ShelfBlockEntity;
import com.starfish_studios.another_furniture.block.properties.HorizontalConnectionType;
import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.util.block.BlockPart;
import com.starfish_studios.another_furniture.util.block.ShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
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
import net.minecraft.server.level.ServerLevel;

public class SmallShelfBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<SmallShelfBlock> CODEC = simpleCodec(SmallShelfBlock::new);
   public static final net.minecraft.world.level.block.state.properties.EnumProperty<HorizontalConnectionType> TYPE = ModBlockStateProperties.HORIZONTAL_CONNECTION_TYPE;
   public static final net.minecraft.world.level.block.state.properties.EnumProperty<net.minecraft.world.level.block.state.properties.SlabType> HALF = ModBlockStateProperties.HALF;
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape TOP_AABB_NORTH = Block.box(0.0, 14.0, 8.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape BOTTOM_AABB_NORTH = Block.box(0.0, 6.0, 8.0, 16.0, 8.0, 16.0);
   protected static final VoxelShape DOUBLE_AABB_NORTH = Shapes.or(TOP_AABB_NORTH, BOTTOM_AABB_NORTH);
   protected static final VoxelShape TOP_AABB_EAST = ShapeUtil.rotateShape(TOP_AABB_NORTH, Direction.EAST);
   protected static final VoxelShape BOTTOM_AABB_EAST = ShapeUtil.rotateShape(BOTTOM_AABB_NORTH, Direction.EAST);
   protected static final VoxelShape DOUBLE_AABB_EAST = ShapeUtil.rotateShape(DOUBLE_AABB_NORTH, Direction.EAST);
   protected static final VoxelShape TOP_AABB_SOUTH = ShapeUtil.rotateShape(TOP_AABB_NORTH, Direction.SOUTH);
   protected static final VoxelShape BOTTOM_AABB_SOUTH = ShapeUtil.rotateShape(BOTTOM_AABB_NORTH, Direction.SOUTH);
   protected static final VoxelShape DOUBLE_AABB_SOUTH = ShapeUtil.rotateShape(DOUBLE_AABB_NORTH, Direction.SOUTH);
   protected static final VoxelShape TOP_AABB_WEST = ShapeUtil.rotateShape(TOP_AABB_NORTH, Direction.WEST);
   protected static final VoxelShape BOTTOM_AABB_WEST = ShapeUtil.rotateShape(BOTTOM_AABB_NORTH, Direction.WEST);
   protected static final VoxelShape DOUBLE_AABB_WEST = ShapeUtil.rotateShape(DOUBLE_AABB_NORTH, Direction.WEST);

   public MapCodec<SmallShelfBlock> codec() {
      return CODEC;
   }

   public SmallShelfBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
                  .setValue(TYPE, HorizontalConnectionType.SINGLE))
               .setValue(HALF, SlabType.BOTTOM))
            .setValue(WATERLOGGED, false)
      );
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
      ItemStack itemStack = useContext.getItemInHand();
      SlabType slabType = (SlabType)state.getValue(HALF);
      if (slabType == SlabType.DOUBLE || !itemStack.is(this.asItem())) {
         return false;
      } else if (!useContext.replacingClickedOnBlock()) {
         return true;
      } else {
         boolean bl = useContext.getClickLocation().y - useContext.getClickedPos().getY() > 0.5;
         Direction direction = useContext.getClickedFace();
         return slabType == SlabType.BOTTOM
            ? direction == Direction.UP || bl && direction.getAxis().isHorizontal()
            : direction == Direction.DOWN || !bl && direction.getAxis().isHorizontal();
      }
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (level.getBlockEntity(pos) instanceof ShelfBlockEntity shelfBE) {
         Direction facing = (Direction)state.getValue(FACING);
         int slot = BlockPart.get2D(pos, hitResult.getLocation(), Direction.UP, facing.getClockWise(), 2, 2);
         if (!stack.isEmpty()) {
            if (!level.isClientSide() && shelfBE.placeItem(player.getAbilities().instabuild ? stack.copy() : stack, slot)) {
               level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
               return InteractionResult.SUCCESS;
            } else {
               return InteractionResult.CONSUME;
            }
         } else {
            return shelfBE.removeItem(slot, player, level) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
         }
      } else {
         return InteractionResult.FAIL;
      }
   }

   public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
      if (!level.getBlockState(pos).is(state.getBlock())) {
         if (level.getBlockEntity(pos) instanceof ShelfBlockEntity shelfBE) {
            Containers.dropContents(level, pos, shelfBE.getItems());
         }

         super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      Direction direction = (Direction)state.getValue(FACING);
      HorizontalConnectionType type = (HorizontalConnectionType)state.getValue(TYPE);
      SlabType half = (SlabType)state.getValue(HALF);
      if (direction == Direction.NORTH) {
         return switch (half) {
            case TOP -> TOP_AABB_NORTH;
            case DOUBLE -> DOUBLE_AABB_NORTH;
            default -> BOTTOM_AABB_NORTH;
         };
      } else if (direction == Direction.EAST) {
         return switch (half) {
            case TOP -> TOP_AABB_EAST;
            case DOUBLE -> DOUBLE_AABB_EAST;
            default -> BOTTOM_AABB_EAST;
         };
      } else if (direction == Direction.SOUTH) {
         return switch (half) {
            case TOP -> TOP_AABB_SOUTH;
            case DOUBLE -> DOUBLE_AABB_SOUTH;
            default -> BOTTOM_AABB_SOUTH;
         };
      } else {
         return switch (half) {
            case TOP -> TOP_AABB_WEST;
            case DOUBLE -> DOUBLE_AABB_WEST;
            default -> BOTTOM_AABB_WEST;
         };
      }
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockPos clickedPos = context.getClickedPos();
      BlockState stateExisting = context.getLevel().getBlockState(clickedPos);
      if (stateExisting.is(this)) {
         return (BlockState)stateExisting.setValue(HALF, SlabType.DOUBLE);
      } else {
         boolean waterlogged = context.getLevel().getFluidState(clickedPos).getType() == Fluids.WATER;
         BlockState state = (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(HALF, SlabType.BOTTOM))
               .setValue(FACING, context.getHorizontalDirection().getOpposite()))
            .setValue(WATERLOGGED, waterlogged);
         Direction direction = context.getClickedFace();
         return direction == Direction.DOWN || direction != Direction.UP && context.getClickLocation().y - clickedPos.getY() > 0.5
            ? (BlockState)state.setValue(HALF, SlabType.TOP)
            : state;
      }
   }

   public BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
   ) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         tickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      BlockState above = level.getBlockState(currentPos.above());
      if (direction == Direction.UP
         && above.isFaceSturdy(level, currentPos, Direction.DOWN)
         && !above.getVisualShape(level, currentPos.above(), CollisionContext.empty()).isEmpty()
         && level.getBlockEntity(currentPos) instanceof ShelfBlockEntity shelfBE) {
         shelfBE.removeAllItems();
      }

      Direction facing = (Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING);
      Direction dirL = facing.getClockWise();
      Direction dirR = facing.getCounterClockWise();
      if (direction != dirL && direction != dirR) {
         return state;
      } else {
         BlockState stateL = level.getBlockState(currentPos.relative(dirL));
         BlockState stateR = level.getBlockState(currentPos.relative(dirR));
         boolean sideL = stateL.getBlock() instanceof SmallShelfBlock && stateL.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
         boolean sideR = stateR.getBlock() instanceof SmallShelfBlock && stateR.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
         HorizontalConnectionType type = sideL && sideR
            ? HorizontalConnectionType.MIDDLE
            : (sideR ? HorizontalConnectionType.LEFT : (sideL ? HorizontalConnectionType.RIGHT : HorizontalConnectionType.SINGLE));
         return (BlockState)state.setValue(TYPE, type);
      }
   }

   protected FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new ShelfBlockEntity(pos, state);
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{FACING, TYPE, HALF, WATERLOGGED});
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
      return false;
   }
}
