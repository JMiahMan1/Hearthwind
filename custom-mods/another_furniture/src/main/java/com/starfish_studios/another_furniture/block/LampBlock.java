package com.starfish_studios.another_furniture.block;

import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.registry.AFBlocks;
import com.starfish_studios.another_furniture.registry.AFItemTags;
import com.starfish_studios.another_furniture.util.block.ShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;

public class LampBlock extends Block implements SimpleWaterloggedBlock {
   protected static final int LEVEL_MIN = 1;
   protected static final int LEVEL_MAX = 3;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty LIT = BlockStateProperties.LIT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final EnumProperty<Direction> FACING = ModBlockStateProperties.FACING_EXCEPT_DOWN;
   public static final IntegerProperty LEVEL = ModBlockStateProperties.LEVEL_1_3;
   public static final BooleanProperty BASE = ModBlockStateProperties.BASE;
   protected static final VoxelShape AABB_UP = Block.box(3.0, 8.0, 3.0, 13.0, 16.0, 13.0);
   protected static final VoxelShape AABB_UP_NORMAL = Shapes.or(AABB_UP, Block.box(5.0, 0.0, 5.0, 11.0, 8.0, 11.0));
   protected static final VoxelShape AABB_UP_TOP = Shapes.or(AABB_UP, Block.box(7.0, 0.0, 7.0, 9.0, 8.0, 9.0));
   protected static final VoxelShape AABB_NORTH = Shapes.or(
      Block.box(3.0, 8.0, 5.0, 13.0, 16.0, 15.0),
      new VoxelShape[]{
         Block.box(5.0, 0.0, 14.0, 11.0, 6.0, 16.0),
         Block.box(7.0, 2.0, 9.0, 9.0, 8.0, 11.0),
         Block.box(7.0, 2.0, 11.0, 9.0, 4.0, 14.0)
      }
   );
   protected static final VoxelShape AABB_EAST = ShapeUtil.rotateShape(AABB_NORTH, Direction.EAST);
   protected static final VoxelShape AABB_SOUTH = ShapeUtil.rotateShape(AABB_NORTH, Direction.SOUTH);
   protected static final VoxelShape AABB_WEST = ShapeUtil.rotateShape(AABB_NORTH, Direction.WEST);
   private final DyeColor color;

   public LampBlock(DyeColor color, Properties properties) {
      super(properties);
      this.color = color;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any())
                           .setValue(FACING, Direction.UP))
                        .setValue(LEVEL, 3))
                     .setValue(LIT, true))
                  .setValue(POWERED, false))
               .setValue(WATERLOGGED, false))
            .setValue(BASE, true)
      );
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return switch ((Direction)state.getValue(FACING)) {
         case NORTH -> AABB_NORTH;
         case EAST -> AABB_EAST;
         case SOUTH -> AABB_SOUTH;
         case WEST -> AABB_WEST;
         default -> state.getValue(BASE) ? AABB_UP_NORMAL : AABB_UP_TOP;
      };
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Direction clickedFace = context.getClickedFace();
      BlockState blockstate = this.defaultBlockState();
      if (level.hasNeighborSignal(pos)) {
         blockstate = (BlockState)blockstate.setValue(POWERED, true);
      }

      return clickedFace != Direction.DOWN
         ? (BlockState)((BlockState)blockstate.setValue(FACING, clickedFace)).setValue(WATERLOGGED, waterlogged)
         : null;
   }

   public BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
   ) {
      if (!state.canSurvive(level, currentPos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if ((Boolean)state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
         }

         if (state.getValue(FACING) == Direction.UP && (direction == Direction.UP || direction == Direction.DOWN)) {
            BlockState aState = level.getBlockState(currentPos.above());
            BlockState bState = level.getBlockState(currentPos.below());
            boolean aConnect = aState.getBlock() instanceof LampBlock lampBlock
                  && lampBlock.getColor() == this.getColor()
                  && aState.getValue(FACING) == Direction.UP
               || aState.getBlock() instanceof LampConnectorBlock connectorBlock && connectorBlock.getColor() == this.getColor();
            boolean bConnect = bState.getBlock() instanceof LampBlock lampBlockx
                  && lampBlockx.getColor() == this.getColor()
                  && bState.getValue(FACING) == Direction.UP
               || bState.getBlock() instanceof LampConnectorBlock connectorBlockx && connectorBlockx.getColor() == this.getColor();
            if (aConnect && !bConnect) {
               state = (BlockState)((BlockState)getLampConnectorByColor(this.color).defaultBlockState().setValue(BASE, true))
                  .setValue(WATERLOGGED, (Boolean)state.getValue(WATERLOGGED));
            } else if (!aConnect && bConnect) {
               state = (BlockState)state.setValue(BASE, false);
            } else if (aConnect) {
               state = (BlockState)((BlockState)getLampConnectorByColor(this.color).defaultBlockState().setValue(BASE, false))
                  .setValue(WATERLOGGED, (Boolean)state.getValue(WATERLOGGED));
            } else {
               state = (BlockState)state.setValue(BASE, true);
            }
         }

         return state;
      }
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      Direction direction = (Direction)state.getValue(FACING);
      BlockPos facingPos = pos.relative(direction.getOpposite());
      BlockState facingState = level.getBlockState(facingPos);
      return direction == Direction.UP || facingState.isFaceSturdy(level, facingPos, direction);
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if (!level.isClientSide()) {
         BlockState below = level.getBlockState(pos.below());
         boolean powered = level.hasNeighborSignal(pos) || below.getBlock() instanceof LampConnectorBlock && (Boolean)below.getValue(POWERED);
         if (powered != (Boolean)state.getValue(POWERED)) {
            if ((Boolean)state.getValue(LIT) != powered) {
               state = (BlockState)state.setValue(LIT, powered);
               level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            state = (BlockState)state.setValue(POWERED, powered);
         }

         level.setBlock(pos, state, 3);
      }
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (player.getItemInHand(hand).is(AFItemTags.LAMPS)
         && state.getValue(FACING) == Direction.UP
         && hitResult.getDirection() == Direction.UP) {
         return InteractionResult.FAIL;
      } else {
         if (player.isCrouching()) {
            int light = (Integer)state.getValue(LEVEL);
            light = light >= 3 ? 1 : light + 1;
            state = (BlockState)state.setValue(LEVEL, light);
         } else {
            state = (BlockState)state.cycle(LIT);
         }

         level.setBlock(pos, state, 3);
         level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.0F);
         return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
      }
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{WATERLOGGED, LIT, POWERED, FACING, LEVEL, BASE});
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

   public DyeColor getColor() {
      return this.color;
   }

   public static Block getLampConnectorByColor(DyeColor color) {
      return switch (color) {
         case WHITE -> (Block)AFBlocks.WHITE_LAMP_CONNECTOR.get();
         case ORANGE -> (Block)AFBlocks.ORANGE_LAMP_CONNECTOR.get();
         case MAGENTA -> (Block)AFBlocks.MAGENTA_LAMP_CONNECTOR.get();
         case LIGHT_BLUE -> (Block)AFBlocks.LIGHT_BLUE_LAMP_CONNECTOR.get();
         case YELLOW -> (Block)AFBlocks.YELLOW_LAMP_CONNECTOR.get();
         case LIME -> (Block)AFBlocks.LIME_LAMP_CONNECTOR.get();
         case PINK -> (Block)AFBlocks.PINK_LAMP_CONNECTOR.get();
         case GRAY -> (Block)AFBlocks.GRAY_LAMP_CONNECTOR.get();
         case LIGHT_GRAY -> (Block)AFBlocks.LIGHT_GRAY_LAMP_CONNECTOR.get();
         case CYAN -> (Block)AFBlocks.CYAN_LAMP_CONNECTOR.get();
         case PURPLE -> (Block)AFBlocks.PURPLE_LAMP_CONNECTOR.get();
         case BLUE -> (Block)AFBlocks.BLUE_LAMP_CONNECTOR.get();
         case BROWN -> (Block)AFBlocks.BROWN_LAMP_CONNECTOR.get();
         case GREEN -> (Block)AFBlocks.GREEN_LAMP_CONNECTOR.get();
         case RED -> (Block)AFBlocks.RED_LAMP_CONNECTOR.get();
         case BLACK -> (Block)AFBlocks.BLACK_LAMP_CONNECTOR.get();
         default -> throw new MatchException(null, null);
      };
   }
}
