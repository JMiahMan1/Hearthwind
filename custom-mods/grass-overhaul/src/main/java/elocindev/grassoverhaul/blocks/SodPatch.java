package elocindev.grassoverhaul.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SodPatch extends Block {
   public static final int MAX_LAYERS = 8;
   public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
   protected static final VoxelShape[] LAYERS_TO_SHAPE = new VoxelShape[]{
      Shapes.empty(),
      Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
   };
   public static final int HEIGHT_IMPASSABLE = 5;

   public SodPatch(Properties settings) {
      super(settings);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(LAYERS, 1));
   }

   protected boolean isPathfindable(BlockState state, PathComputationType type) {
      return (Integer)state.getValue(LAYERS) < 5;
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return LAYERS_TO_SHAPE[state.getValue(LAYERS)];
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return LAYERS_TO_SHAPE[state.getValue(LAYERS) - 1];
   }

   public VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos pos) {
      return LAYERS_TO_SHAPE[state.getValue(LAYERS)];
   }

   public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return LAYERS_TO_SHAPE[state.getValue(LAYERS)];
   }

   public boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
      return state.getValue(LAYERS) == 8 ? 0.2F : 1.0F;
   }

   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
      BlockState downState = world.getBlockState(pos.below());
      if (downState.is(BlockTags.CANNOT_SUPPORT_SNOW_LAYER)) {
         return false;
      } else {
         return downState.is(BlockTags.SUPPORT_OVERRIDE_SNOW_LAYER)
            ? true
            : Block.isFaceFull(downState.getCollisionShape(world, pos.below()), Direction.UP)
               || downState.is(this) && (Integer)downState.getValue(LAYERS) == 8;
      }
   }

   @Deprecated
   public BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
      return !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, world, tickAccess, pos, direction, neighborPos, neighborState, random);
   }

   public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
      int i = (Integer)state.getValue(LAYERS);
      if (!context.getItemInHand().is(this.asItem()) || i >= 8) {
         return i == 1;
      } else {
         return context.replacingClickedOnBlock() ? context.getClickedFace() == Direction.UP : true;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos());
      if (blockState.is(this)) {
         int i = (Integer)blockState.getValue(LAYERS);
         return (BlockState)blockState.setValue(LAYERS, Math.min(8, i + 1));
      } else {
         return super.getStateForPlacement(ctx);
      }
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{LAYERS});
   }
}
