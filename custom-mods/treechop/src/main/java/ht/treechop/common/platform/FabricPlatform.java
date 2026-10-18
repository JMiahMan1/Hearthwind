package ht.treechop.common.platform;

import ht.treechop.api.ChopData;
import ht.treechop.api.ChopDataImmutable;
import ht.treechop.api.FellData;
import ht.treechop.api.TreeChopEvents;
import ht.treechop.api.TreeData;
import ht.treechop.common.chop.ChopResult;
import ht.treechop.common.chop.FellTreeResult;
import ht.treechop.common.registry.FabricModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FabricPlatform implements Platform {
   @Override
   public boolean isDedicatedServer() {
      return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
   }

   @Override
   public boolean uses(ModLoader loader) {
      return loader == ModLoader.FABRIC;
   }

   @Override
   public TreeData detectTreeEvent(Level level, ServerPlayer player, BlockPos blockPos, BlockState blockState, TreeData treeData) {
      return ((TreeChopEvents.DetectTree)TreeChopEvents.DETECT_TREE.invoker()).onDetectTree(level, player, blockPos, blockState, treeData);
   }

   @Override
   public boolean startChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopData chopData, Object trigger) {
      return ((TreeChopEvents.BeforeChop)TreeChopEvents.BEFORE_CHOP.invoker()).beforeChop(level, agent, pos, blockState, chopData);
   }

   @Override
   public void finishChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopDataImmutable chopData, ChopResult chopResult) {
      ((TreeChopEvents.AfterChop)TreeChopEvents.AFTER_CHOP.invoker()).afterChop(level, agent, pos, blockState, chopData, chopResult instanceof FellTreeResult);
   }

   @Override
   public boolean startFellTreeEvent(ServerPlayer player, Level level, BlockPos choppedPos, FellData fellData) {
      return ((TreeChopEvents.BeforeFell)TreeChopEvents.BEFORE_FELL.invoker()).beforeFell(level, player, choppedPos, fellData);
   }

   @Override
   public void finishFellTreeEvent(ServerPlayer player, Level level, BlockPos choppedPos, FellData fellData) {
      ((TreeChopEvents.AfterFell)TreeChopEvents.AFTER_FELL.invoker()).afterFell(level, player, choppedPos, fellData);
   }

   @Override
   public Block getChoppedLogBlock() {
      return FabricModBlocks.CHOPPED_LOG;
   }

   @Override
   public net.minecraft.world.level.block.entity.BlockEntityType<?> getChoppedLogBlockEntity() {
      return FabricModBlocks.CHOPPED_LOG_ENTITY;
   }

   @Override
   public Identifier getResourceLocationForBlock(Block block) {
      return BuiltInRegistries.BLOCK.getKey(block);
   }

   @Override
   public Identifier getResourceLocationForItem(Item item) {
      return BuiltInRegistries.ITEM.getKey(item);
   }

   @Override
   public BlockState getStrippedState(BlockGetter level, BlockPos pos, BlockState state) {
      return null;
   }
}
