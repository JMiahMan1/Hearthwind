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

public class ShelfBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<ShelfBlock> CODEC = simpleCodec(ShelfBlock::new);
   public static final net.minecraft.world.level.block.state.properties.EnumProperty<HorizontalConnectionType> TYPE = ModBlockStateProperties.HORIZONTAL_CONNECTION_TYPE;
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape TOP = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape NL = Block.box(12.0, 6.0, 6.0, 16.0, 14.0, 16.0);
   protected static final VoxelShape NR = Block.box(0.0, 6.0, 6.0, 4.0, 14.0, 16.0);
   protected static final VoxelShape T_NL = Shapes.or(TOP, NL);
   protected static final VoxelShape T_NR = Shapes.or(TOP, NR);
   protected static final VoxelShape T_NLR = Shapes.or(TOP, new VoxelShape[]{NL, NR});
   protected static final VoxelShape T_EL = ShapeUtil.rotateShape(T_NL, Direction.EAST);
   protected static final VoxelShape T_ER = ShapeUtil.rotateShape(T_NR, Direction.EAST);
   protected static final VoxelShape T_ELR = ShapeUtil.rotateShape(T_NLR, Direction.EAST);
   protected static final VoxelShape T_SL = ShapeUtil.rotateShape(T_NL, Direction.SOUTH);
   protected static final VoxelShape T_SR = ShapeUtil.rotateShape(T_NR, Direction.SOUTH);
   protected static final VoxelShape T_SLR = ShapeUtil.rotateShape(T_NLR, Direction.SOUTH);
   protected static final VoxelShape T_WL = ShapeUtil.rotateShape(T_NL, Direction.WEST);
   protected static final VoxelShape T_WR = ShapeUtil.rotateShape(T_NLR, Direction.WEST);
   protected static final VoxelShape T_WLR = ShapeUtil.rotateShape(T_NLR, Direction.WEST);

   public MapCodec<ShelfBlock> codec() {
      return CODEC;
   }

   public ShelfBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
               .setValue(TYPE, HorizontalConnectionType.SINGLE))
            .setValue(WATERLOGGED, false)
      );
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (hitResult.getDirection() != Direction.UP) {
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      } else if (level.getBlockEntity(pos) instanceof ShelfBlockEntity shelfBE) {
         Direction facing = (Direction)state.getValue(FACING);
         int slot = BlockPart.get2D(pos, hitResult.getLocation(), facing.getClockWise(), facing, 2, 2);
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

   public boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      Direction direction = (Direction)state.getValue(FACING);
      HorizontalConnectionType type = (HorizontalConnectionType)state.getValue(TYPE);
      if (direction == Direction.NORTH) {
         return switch (type) {
            case SINGLE -> T_NLR;
            case LEFT -> T_NL;
            case RIGHT -> T_NR;
            default -> TOP;
         };
      } else if (direction == Direction.EAST) {
         return switch (type) {
            case SINGLE -> T_ELR;
            case LEFT -> T_EL;
            case RIGHT -> T_ER;
            default -> TOP;
         };
      } else if (direction == Direction.SOUTH) {
         return switch (type) {
            case SINGLE -> T_SLR;
            case LEFT -> T_SL;
            case RIGHT -> T_SR;
            default -> TOP;
         };
      } else {
         return switch (type) {
            case SINGLE -> T_WLR;
            case LEFT -> T_WL;
            case RIGHT -> T_WR;
            default -> TOP;
         };
      }
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
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

      BlockState above = level.getBlockState(currentPos.above());
      if (direction == Direction.UP
         && above.isFaceSturdy(level, currentPos, Direction.DOWN)
         && !above.getVisualShape(level, currentPos.above(), CollisionContext.empty()).isEmpty()
         && level.getBlockEntity(currentPos) instanceof ShelfBlockEntity shelfBE) {
         shelfBE.removeAllItems();
      }

      Direction facing = (Direction)state.getValue(FACING);
      Direction dirL = facing.getClockWise();
      Direction dirR = facing.getCounterClockWise();
      if (direction != dirL && direction != dirR) {
         return state;
      } else {
         BlockState l_state = level.getBlockState(currentPos.relative(dirL));
         BlockState r_state = level.getBlockState(currentPos.relative(dirR));
         boolean l_side = l_state.getBlock() instanceof ShelfBlock && l_state.getValue(FACING) == facing;
         boolean r_side = r_state.getBlock() instanceof ShelfBlock && r_state.getValue(FACING) == facing;
         HorizontalConnectionType type = l_side && r_side
            ? HorizontalConnectionType.MIDDLE
            : (r_side ? HorizontalConnectionType.LEFT : (l_side ? HorizontalConnectionType.RIGHT : HorizontalConnectionType.SINGLE));
         return (BlockState)state.setValue(TYPE, type);
      }
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new ShelfBlockEntity(pos, state);
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{FACING, TYPE, WATERLOGGED});
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
