package com.starfish_studios.another_furniture.block;

import com.mojang.serialization.MapCodec;
import com.starfish_studios.another_furniture.block.entity.FlowerBoxBlockEntity;
import com.starfish_studios.another_furniture.block.properties.HorizontalConnectionType;
import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.registry.AFItemTags;
import com.starfish_studios.another_furniture.util.block.BlockPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;

public class FlowerBoxBlock extends BaseEntityBlock {
   public static final MapCodec<FlowerBoxBlock> CODEC = simpleCodec(FlowerBoxBlock::new);
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final net.minecraft.world.level.block.state.properties.EnumProperty<HorizontalConnectionType> TYPE = ModBlockStateProperties.HORIZONTAL_CONNECTION_TYPE;
   public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
   protected static final VoxelShape NORTH_AABB = Block.box(0.0, 10.0, 8.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 10.0, 0.0, 16.0, 16.0, 8.0);
   protected static final VoxelShape WEST_AABB = Block.box(8.0, 10.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape EAST_AABB = Block.box(0.0, 10.0, 0.0, 8.0, 16.0, 16.0);
   protected static final VoxelShape Z_AXIS_AABB = Block.box(0.0, 0.0, 4.0, 16.0, 6.0, 12.0);
   protected static final VoxelShape X_AXIS_AABB = Block.box(4.0, 0.0, 0.0, 12.0, 6.0, 16.0);

   public MapCodec<FlowerBoxBlock> codec() {
      return CODEC;
   }

   public FlowerBoxBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
               .setValue(TYPE, HorizontalConnectionType.SINGLE))
            .setValue(ATTACHED, false)
      );
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      boolean attached = (Boolean)state.getValue(ATTACHED);

      return switch ((Direction)state.getValue(FACING)) {
         case WEST -> attached ? WEST_AABB : X_AXIS_AABB;
         case EAST -> attached ? EAST_AABB : X_AXIS_AABB;
         case SOUTH -> attached ? SOUTH_AABB : Z_AXIS_AABB;
         default -> attached ? NORTH_AABB : Z_AXIS_AABB;
      };
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      if ((Boolean)state.getValue(ATTACHED)) {
         Direction direction = (Direction)state.getValue(FACING);
         BlockPos facingPos = pos.relative(direction.getOpposite());
         BlockState facingState = level.getBlockState(facingPos);
         return facingState.isFaceSturdy(level, facingPos, direction);
      } else {
         return super.canSurvive(state, level, pos);
      }
   }

   public BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
   ) {
      BlockState above = level.getBlockState(currentPos.above());
      boolean attached = (Boolean)state.getValue(ATTACHED);
      if (direction == Direction.UP
         && attached
         && above.isFaceSturdy(level, currentPos, Direction.DOWN)
         && level.getBlockEntity(currentPos) instanceof FlowerBoxBlockEntity flowerBoxBE) {
         flowerBoxBE.removeAllItems();
         return state;
      } else {
         Direction facing = (Direction)state.getValue(FACING);
         if (attached && direction.getOpposite() == facing && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
         } else if (direction != facing.getClockWise() && direction != facing.getCounterClockWise()) {
            return state;
         } else {
            BlockState l_state = level.getBlockState(currentPos.relative(facing.getClockWise()));
            BlockState r_state = level.getBlockState(currentPos.relative(facing.getCounterClockWise()));
            boolean l_side = l_state.getBlock() instanceof FlowerBoxBlock
               && (Boolean)l_state.getValue(ATTACHED) == attached
               && (l_state.getValue(FACING) == facing || !attached && l_state.getValue(FACING) == facing.getOpposite());
            boolean r_side = r_state.getBlock() instanceof FlowerBoxBlock
               && (Boolean)r_state.getValue(ATTACHED) == attached
               && (r_state.getValue(FACING) == facing || !attached && r_state.getValue(FACING) == facing.getOpposite());
            HorizontalConnectionType type = l_side && r_side
               ? HorizontalConnectionType.MIDDLE
               : (r_side ? HorizontalConnectionType.LEFT : (l_side ? HorizontalConnectionType.RIGHT : HorizontalConnectionType.SINGLE));
            return (BlockState)state.setValue(TYPE, type);
         }
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction clickedFace = context.getClickedFace();
      boolean attached = clickedFace != Direction.UP && clickedFace != Direction.DOWN;
      Direction facing = attached ? clickedFace : context.getHorizontalDirection().getOpposite();
      return (BlockState)((BlockState)this.defaultBlockState().setValue(ATTACHED, attached)).setValue(FACING, facing);
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{FACING, TYPE, ATTACHED});
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new FlowerBoxBlockEntity(pos, state);
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (level.getBlockEntity(pos) instanceof FlowerBoxBlockEntity flowerBoxBE) {
         if (stack.is(AFItemTags.FLOWER_BOX_PLACEABLES) && !stack.is(AFItemTags.FLOWER_BOX_BANNED)) {
            Direction facing = (Direction)state.getValue(FACING);
            int slot = BlockPart.get1D(pos, hitResult.getLocation(), facing.getClockWise(), 2);
            return !level.isClientSide() && flowerBoxBE.placeFlower(player.getAbilities().instabuild ? stack.copy() : stack, slot)
               ? InteractionResult.SUCCESS
               : InteractionResult.CONSUME;
         } else {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         }
      } else {
         return InteractionResult.FAIL;
      }
   }

   public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
      if (!level.getBlockState(pos).is(state.getBlock())) {
         if (level.getBlockEntity(pos) instanceof FlowerBoxBlockEntity flowerBoxBE) {
            Containers.dropContents(level, pos, flowerBoxBE.getItems());
         }

         super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
      }
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
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
