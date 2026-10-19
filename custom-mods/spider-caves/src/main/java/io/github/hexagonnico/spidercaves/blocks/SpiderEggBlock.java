package io.github.hexagonnico.spidercaves.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpiderEggBlock extends Block {
   private static final VoxelShape ONE_EGG_AABB = Block.box(3.0, 0.0, 3.0, 12.0, 7.0, 12.0);
   private static final VoxelShape MULTIPLE_EGGS_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 7.0, 15.0);

   public SpiderEggBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue(BlockStateProperties.EGGS, 1));
   }

   public void stepOn(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Entity entity) {
      if (!entity.isSteppingCarefully()) {
         destroyEgg(world, state, pos, entity, 30);
      }

      super.stepOn(world, pos, state, entity);
   }

   public void fallOn(@NotNull Level world, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull Entity entity, float v) {
      destroyEgg(world, state, pos, entity, 2);
      super.fallOn(world, state, pos, entity, v);
   }

   public void playerDestroy(
      @NotNull Level world,
      @NotNull Player player,
      @NotNull BlockPos pos,
      @NotNull BlockState state,
      @Nullable BlockEntity blockEntity,
      @NotNull ItemStack itemStack
   ) {
      super.playerDestroy(world, player, pos, state, blockEntity, itemStack);
      decreaseEggs(world, pos, state);
   }

   public boolean canBeReplaced(@NotNull BlockState state, @NotNull BlockPlaceContext context) {
      return !context.isSecondaryUseActive() && context.getItemInHand().is(this.asItem()) && state.getValue(BlockStateProperties.EGGS) < 4
         ? true
         : super.canBeReplaced(state, context);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = context.getLevel().getBlockState(context.getClickedPos());
      return state.is(this)
         ? (BlockState)state.setValue(BlockStateProperties.EGGS, Math.min(4, (Integer)state.getValue(BlockStateProperties.EGGS) + 1))
         : super.getStateForPlacement(context);
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return state.getValue(BlockStateProperties.EGGS) > 1 ? MULTIPLE_EGGS_AABB : ONE_EGG_AABB;
   }

   protected void createBlockStateDefinition(
      @NotNull net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder
   ) {
      builder.add(new Property[]{BlockStateProperties.EGGS});
   }

   private static void destroyEgg(Level world, BlockState state, BlockPos pos, Entity entity, int chance) {
      if (canDestroyEgg(world, entity) && !world.isClientSide() && world.getRandom().nextInt(chance) == 0) {
         decreaseEggs(world, pos, state);
      }
   }

   private static void decreaseEggs(Level world, BlockPos pos, BlockState state) {
      world.playSound(null, pos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + world.getRandom().nextFloat() * 0.2F);
      int count = (Integer)state.getValue(BlockStateProperties.EGGS);
      if (count <= 1) {
         world.destroyBlock(pos, false);
      } else {
         world.setBlock(pos, (BlockState)state.setValue(BlockStateProperties.EGGS, count - 1), 2);
         world.gameEvent(GameEvent.BLOCK_DESTROY, pos, Context.of(state));
      }

      Spider spider = (Spider)EntityTypes.SPIDER.create(world, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
      if (spider != null) {
         spider.snapTo(net.minecraft.world.phys.Vec3.atCenterOf(pos).add(0.0, 0.2, 0.0));
         world.addFreshEntity(spider);
      }
   }

   private static boolean canDestroyEgg(Level world, Entity entity) {
      if (!(entity instanceof LivingEntity)) {
         return false;
      } else {
         return !(entity instanceof Spider) && !(entity instanceof Bat)
            ? entity instanceof Player || ((net.minecraft.server.level.ServerLevel)world).getGameRules().get(GameRules.MOB_GRIEFING)
            : false;
      }
   }
}
