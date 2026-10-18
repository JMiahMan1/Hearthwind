package com.starfish_studios.another_furniture.block;

import com.mojang.serialization.MapCodec;
import com.starfish_studios.another_furniture.block.entity.ServiceBellBlockEntity;
import com.starfish_studios.another_furniture.registry.AFBlockEntityTypes;
import com.starfish_studios.another_furniture.registry.AFSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ServiceBellBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<ServiceBellBlock> CODEC = simpleCodec(ServiceBellBlock::new);
   protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 7.0, 12.0);
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   public MapCodec<ServiceBellBlock> codec() {
      return CODEC;
   }

   public ServiceBellBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(WATERLOGGED, false)).setValue(POWERED, false));
   }

   public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
      return (BlockState)this.defaultBlockState().setValue(WATERLOGGED, waterlogged);
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if ((Boolean)state.getValue(POWERED)) {
         return InteractionResult.CONSUME;
      } else {
         this.press(state, level, pos);
         level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
         if (level.getBlockEntity(pos) instanceof ServiceBellBlockEntity servicebellBE) {
            servicebellBE.onHit();
         }

         level.playSound(null, pos, (SoundEvent)AFSoundEvents.SERVICE_BELL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
         return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
      }
   }

   public void press(BlockState state, Level level, BlockPos pos) {
      level.setBlock(pos, (BlockState)state.setValue(POWERED, true), 3);
      this.updateNeighbours(state, level, pos);
      level.scheduleTick(pos, this, 2);
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if ((Boolean)state.getValue(POWERED)) {
         level.setBlock(pos, (BlockState)state.setValue(POWERED, false), 3);
         this.updateNeighbours(state, level, pos);
         level.gameEvent(null, GameEvent.BLOCK_DEACTIVATE, pos);
      }
   }

   public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
      if (!isMoving && !level.getBlockState(pos).is(state.getBlock())) {
         if ((Boolean)state.getValue(POWERED)) {
            this.updateNeighbours(state, level, pos);
         }

         super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
      }
   }

   public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      return state.getValue(POWERED) ? 15 : 0;
   }

   public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      return state.getValue(POWERED) ? 15 : 0;
   }

   public boolean isSignalSource(BlockState state) {
      return true;
   }

   private void updateNeighbours(BlockState state, Level level, BlockPos pos) {
      level.updateNeighborsAt(pos, this);
      level.updateNeighborsAt(pos.below(), this);
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{WATERLOGGED, POWERED});
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new ServiceBellBlockEntity(pos, state);
   }

   @Nullable
   public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
      net.minecraft.world.level.Level level,
      net.minecraft.world.level.block.state.BlockState state,
      net.minecraft.world.level.block.entity.BlockEntityType<T> blockEntityType
   ) {
      return createTickerHelper(blockEntityType, (BlockEntityType)AFBlockEntityTypes.SERVICE_BELL.get(), level.isClientSide() ? ServiceBellBlockEntity::clientTick : null);
   }
}
