package ht.treechop.common.block;

import ht.treechop.TreeChop;
import ht.treechop.api.IChoppableBlock;
import ht.treechop.client.Client;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.loot.TreeChopLootContextParams;
import ht.treechop.common.network.ServerUpdateChopsPacket;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.util.ClassUtil;
import ht.treechop.common.util.FaceShape;
import ht.treechop.server.Server;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class ChoppedLogBlock extends BlockImitator implements IChoppableBlock, EntityBlock, SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final int DEFAULT_SUPPORT_FACTOR = 7;
   public static final int DEFAULT_MAX_NUM_CHOPS = 7;
   public static final int DEFAULT_UNCHOPPED_RADIUS = 8;

   public ChoppedLogBlock(Properties properties) {
      super(properties.dynamicShape().isViewBlocking((blockState, level, pos) -> false));
      this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue(WATERLOGGED, Boolean.FALSE));
   }

   public static ChoppedLogShape getPlacementShape(Level level, BlockPos blockPos, BlockState state) {
      byte DOWN = 1;
      byte UP = 2;
      byte NORTH = 4;
      byte SOUTH = 8;
      byte WEST = 16;
      byte EAST = 32;
      byte openSides = (byte)(
         (isBlockOpen(level, blockPos.below()) ? 1 : 0)
            | (!ChopUtil.isBlockALog(level, blockPos.above()) ? 2 : 0)
            | (!ChopUtil.isBlockALog(level, blockPos.north()) ? 4 : 0)
            | (!ChopUtil.isBlockALog(level, blockPos.south()) ? 8 : 0)
            | (!ChopUtil.isBlockALog(level, blockPos.west()) ? 16 : 0)
            | (!ChopUtil.isBlockALog(level, blockPos.east()) ? 32 : 0)
      );
      return ChoppedLogShape.forOpenSides(openSides);
   }

   private static boolean isBlockOpen(Level level, BlockPos pos) {
      return level.isEmptyBlock(pos) || ChopUtil.isBlockLeaves(level, pos);
   }

   @Override
   public BlockState getImitatedBlockState(BlockGetter level, BlockPos pos) {
      return level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity && !entity.getOriginalState().is(this)
         ? entity.getOriginalState()
         : Blocks.OAK_LOG.defaultBlockState();
   }

   public float getDestroyProgress(BlockState blockState, Player player, BlockGetter level, BlockPos pos) {
      return (float)Math.min(0.35, (double)this.getImitatedBlockState(level, pos).getDestroyProgress(player, level, pos));
   }

   @NotNull
   public VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      double scale = 0.0625;
      if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
         AABB box = entity.getShape().getBoundingBox(entity.getRadius());
         return Shapes.box(
            box.minX * 0.0625,
            box.minY * 0.0625,
            box.minZ * 0.0625,
            box.maxX * 0.0625,
            box.maxY * 0.0625,
            box.maxZ * 0.0625
         );
      } else {
         return Shapes.block();
      }
   }

   public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
      return ConfigHandler.removeBarkOnInteriorLogs.get()
            && level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity
            && entity.getOriginalState().isSolidRender()
         ? entity.getOcclusionShape(level, pos)
         : Shapes.empty();
   }

   public boolean useShapeForLightOcclusion(BlockState blockState) {
      return false;
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{WATERLOGGED});
   }

   @Override
   public int getNumChops(BlockGetter level, BlockPos pos, BlockState blockState) {
      return level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity ? entity.getChops() : 0;
   }

   @Override
   public int getMaxNumChops(BlockGetter level, BlockPos pos, BlockState blockState) {
      return level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity ? entity.getMaxNumChops() : 7;
   }

   @Override
   public double getSupportFactor(BlockGetter level, BlockPos pos, BlockState blockState) {
      return level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity ? entity.getSupportFactor() : 7.0;
   }

   @Override
   public void chop(Player player, ItemStack tool, Level level, BlockPos pos, BlockState blockState, int numChops, boolean felling) {
      int currentNumChops = ChopUtil.getNumChops(level, pos, blockState);
      int maxNumChops = ChopUtil.getMaxNumChops(level, pos, blockState);
      int newNumChops = Math.min(currentNumChops + numChops, maxNumChops);
      int numAddedChops = newNumChops - currentNumChops;
      if (!felling) {
         if (numAddedChops > 0) {
            if (!blockState.is(this)) {
               int chopZeroRadius = Optional.ofNullable(ClassUtil.getCylinderBlock(blockState.getBlock()))
                  .map(slimBlock -> slimBlock.getRadius(level, pos, blockState))
                  .orElse(8);
               double supportFactor = Optional.ofNullable(ClassUtil.getFellableBlock(blockState.getBlock()))
                  .map(fellableBlock -> fellableBlock.getSupportFactor(level, pos, blockState))
                  .orElse(1.0);
               BlockState newBlockState = blockState.is(this) ? blockState : this.getPlacementState(level, pos);
               if (level.setBlockAndUpdate(pos, newBlockState)
                  && level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity
                  && level instanceof ServerLevel serverLevel) {
                  entity.setShape(getPlacementShape(level, pos, blockState));
                  entity.setOriginalState(blockState);
                  entity.setParameters(chopZeroRadius, maxNumChops, supportFactor);
                  if ((Boolean)ConfigHandler.COMMON.dropLootOnFirstChop.get()) {
                     Block.getDrops(blockState, serverLevel, pos, entity, player, tool).forEach(stack -> popResource(level, pos, stack));
                  }
               }
            }

            if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entityx) {
               entityx.setChops(newNumChops);
               entityx.setChanged();
            }
         } else {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
         }
      }

      if (level instanceof ServerLevel serverLevelx) {
         Identifier chopLootTableId = BuiltInRegistries.BLOCK.getKey(this.asBlock()).withPrefix("chopping/");
         net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> chopLootTableKey = ResourceKey.create(
            Registries.LOOT_TABLE, chopLootTableId
         );
         LootTable lootTable = serverLevelx.getServer().reloadableRegistries().getLootTable(chopLootTableKey);
         int finalBlockChopCount = currentNumChops + numAddedChops;

         for (int i = 0; i < numAddedChops; i++) {
            int blockChopCount = 1 + currentNumChops + i;
            Builder builder = new Builder(serverLevelx)
               .withParameter(LootContextParams.BLOCK_STATE, this.defaultBlockState())
               .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
               .withParameter(LootContextParams.TOOL, tool)
               .withParameter(TreeChopLootContextParams.BLOCK_CHOP_COUNT, blockChopCount)
               .withParameter(TreeChopLootContextParams.DESTROY_BLOCK, felling && blockChopCount == finalBlockChopCount)
               .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
               .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos));
            lootTable.getRandomItems(builder.create(TreeChopLootContextParams.SET)).forEach(stack -> popResource(serverLevelx, pos, stack));
         }
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.getPlacementState(context.getLevel(), context.getClickedPos());
   }

   private BlockState getPlacementState(Level level, BlockPos pos) {
      return (BlockState)this.defaultBlockState().setValue(WATERLOGGED, this.shouldPlaceAsWaterlogged(level, pos));
   }

   private boolean shouldPlaceAsWaterlogged(Level level, BlockPos pos) {
      Direction[] waterSourceDirections = new Direction[]{
         Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP
      };
      return Arrays.stream(waterSourceDirections)
            .filter(direction -> level.getFluidState(pos.offset(direction.getUnitVec3i())).isSource())
            .limit(2L)
            .count()
         == 2L;
   }

   public FluidState getFluidState(BlockState blockState) {
      return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
   }

   public BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction side, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
      if ((Boolean)blockState.getValue(WATERLOGGED)) {
         tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
         entity.rerender();
      }

      return super.updateShape(blockState, level, tickAccess, pos, side, neighborPos, neighborState, random);
   }

   @NotNull
   public List<net.minecraft.world.item.ItemStack> getDrops(
      net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.world.level.storage.loot.LootParams.Builder context
   ) {
      List<net.minecraft.world.item.ItemStack> stacks = new ArrayList<>(super.getDrops(blockState, context));
      if ((Boolean)ConfigHandler.COMMON.dropLootForChoppedBlocks.get()
         && context.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ChoppedLogBlock.MyEntity entity) {
         ItemStack tool = (ItemStack)context.getOptionalParameter(LootContextParams.TOOL);
         Entity player = (Entity)context.getOptionalParameter(LootContextParams.THIS_ENTITY);
         stacks.addAll(
            Block.getDrops(entity.originalState, context.getLevel(), entity.getBlockPos(), entity, player, tool == null ? ItemStack.EMPTY : tool)
         );
      }

      return stacks;
   }

   public abstract static class MyEntity extends BlockEntity {
      public static final String KEY_CHOPS = "Chops";
      public static final String KEY_SHAPE = "Shape";
      public static final String KEY_ORIGINAL_STATE = "OriginalState";
      public static final String KEY_UNCHOPPED_RADIUS = "UnchoppedRadius";
      public static final String KEY_MAX_NUM_CHOPS = "MaxNumChops";
      public static final String KEY_SUPPORT_FACTOR = "SupportFactor";
      protected BlockState originalState = Blocks.OAK_LOG.defaultBlockState();
      private ChoppedLogShape shape = ChoppedLogShape.PILLAR_Y;
      private int chops = 1;
      private int unchoppedRadius = 8;
      private int maxNumChops = 7;
      private double supportFactor = 7.0;

      public MyEntity(BlockPos pos, BlockState blockState) {
         super(TreeChop.platform.getChoppedLogBlockEntity(), pos, blockState);
      }

      public int hashCode() {
         return Objects.hash(
            this.level, this.worldPosition, this.originalState, this.shape, this.chops, this.unchoppedRadius, this.maxNumChops, this.supportFactor
         );
      }

      public void setParameters(int unchoppedRadius, int maxNumChops, double supportFactor) {
         this.unchoppedRadius = unchoppedRadius;
         this.maxNumChops = maxNumChops;
         this.supportFactor = supportFactor;
      }

      public int getChops() {
         return this.chops;
      }

      public void setChops(int chops) {
         this.chops = chops;
      }

      public ChoppedLogShape getShape() {
         return this.shape;
      }

      public void setShape(ChoppedLogShape shape) {
         this.shape = shape;
      }

      public BlockState getOriginalState() {
         return this.originalState;
      }

      public void setOriginalState(BlockState originalState) {
         this.originalState = originalState;
      }

      public int getUnchoppedRadius() {
         return this.unchoppedRadius;
      }

      public int getRadius() {
         return Math.max(this.getUnchoppedRadius() - this.getChops(), 1);
      }

      public int getMaxNumChops() {
         return this.maxNumChops;
      }

      public double getSupportFactor() {
         return this.supportFactor;
      }

      public void saveAdditional(@NotNull ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("OriginalState", Block.getId(this.getOriginalState()));
      output.putInt("Chops", this.getChops());
      output.putInt("Shape", this.getShape().ordinal());
      if (this.unchoppedRadius != 8) {
         output.putInt("UnchoppedRadius", this.unchoppedRadius);
      }
      if (this.maxNumChops != 7) {
         output.putInt("MaxNumChops", this.maxNumChops);
      }
      if (this.supportFactor != 7.0) {
         output.putDouble("SupportFactor", this.supportFactor);
      }
   }

      public void loadAdditional(@NotNull ValueInput input) {
      super.loadAdditional(input);
      int hash = this.hashCode();
      int stateId = input.getIntOr("OriginalState", 0);
      this.setOriginalState(stateId > 0 ? Block.stateById(stateId) : Blocks.OAK_LOG.defaultBlockState());
      this.setChops(input.getIntOr("Chops", 0));
      this.setShape(ChoppedLogShape.values()[input.getIntOr("Shape", 0)]);
      int unchoppedRadius = input.getIntOr("UnchoppedRadius", 8);
      int maxNumChops = input.getIntOr("MaxNumChops", 7);
      double supportFactor = input.getDoubleOr("SupportFactor", 7.0);
      this.setParameters(unchoppedRadius, maxNumChops, supportFactor);
         if (hash != this.hashCode()) {
            this.rerenderNeighborhood();
         }
      }

      @NotNull
      public CompoundTag getUpdateTag(Provider lookup) {
         return this.saveWithoutMetadata(lookup);
      }

      @Nullable
      public ClientboundBlockEntityDataPacket getUpdatePacket() {
         return ClientboundBlockEntityDataPacket.create(this);
      }

      public void setChanged() {
         super.setChanged();
         this.syncWithClients();
      }

      private void syncWithClients() {
         if (this.level instanceof ServerLevel serverLevel) {
            Server.instance()
               .broadcast(serverLevel, this.worldPosition, new ServerUpdateChopsPacket(this.worldPosition, this.getUpdateTag(this.level.registryAccess())));
         }
      }

      protected void rerender() {
         if (this.level != null && this.level.isClientSide()) {
            this.level.setBlocksDirty(this.worldPosition, Blocks.AIR.defaultBlockState(), this.getBlockState());
            Client.treeCache.invalidate();
         }
      }

      private void rerenderNeighborhood() {
         if (this.level != null && this.level.isClientSide()) {
            this.rerender();
            MutableBlockPos pos = new MutableBlockPos();

            for (Direction dir : Direction.values()) {
               pos.setWithOffset(this.worldPosition, dir);
               if (this.level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity neighbor) {
                  neighbor.rerender();
               }
            }
         }
      }

      public Stream<net.minecraft.core.Direction> streamSolidSides(net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
         return (Stream<net.minecraft.core.Direction>)(ConfigHandler.removeBarkOnInteriorLogs.get()
            ? Arrays.stream(Direction.values()).filter(direction -> !this.shape.isSideOpen(direction)).filter(direction -> {
               BlockPos neighborPos = pos.relative(direction);
               BlockState blockState = level.getBlockState(neighborPos);
               return ChopUtil.isBlockChoppable(level, neighborPos, blockState) && blockState.isCollisionShapeFullBlock(level, neighborPos);
            })
            : Stream.empty());
      }

      public VoxelShape getOcclusionShape(BlockGetter level, BlockPos pos) {
         return Shapes.or(
            Shapes.empty(),
            this.streamSolidSides(level, pos).map(direction -> Shapes.create(FaceShape.get(direction).toAABB())).toArray(VoxelShape[]::new)
         );
      }
   }
}
