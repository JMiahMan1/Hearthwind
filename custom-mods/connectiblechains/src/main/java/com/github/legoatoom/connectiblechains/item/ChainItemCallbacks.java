package com.github.legoatoom.connectiblechains.item;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.entity.ChainKnotEntity;
import com.github.legoatoom.connectiblechains.entity.Chainable;
import com.github.legoatoom.connectiblechains.tag.ModTagRegistry;
import com.github.legoatoom.connectiblechains.util.Helper;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

public class ChainItemCallbacks {
   public static InteractionResult chainUseEvent(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
      if (player != null && !player.isShiftKeyDown()) {
         ItemStack stack = player.getItemInHand(hand);
         BlockPos blockPos = hitResult.getBlockPos();
         BlockState blockState = world.getBlockState(blockPos);
         if (blockState.is(ModTagRegistry.CHAIN_CONNECTIBLE)) {
            if (!Leashable.leashableInArea(world, net.minecraft.world.phys.Vec3.atCenterOf(blockPos), entity -> entity.getLeashHolder() == player).isEmpty()) {
               return InteractionResult.PASS;
            } else if (stack.is(ModTagRegistry.CATENARY_ITEMS)) {
               if (world instanceof ServerLevel serverWorld) {
                  ChainKnotEntity knot = ChainKnotEntity.getOrCreate(serverWorld, blockPos, stack.getItem());
                  return knot.interact(player, hand);
               } else {
                  return InteractionResult.SUCCESS;
               }
            } else if (world instanceof ServerLevel serverWorld) {
               return attachHeldChainsToBlock(player, serverWorld, blockPos);
            } else {
               return (InteractionResult)(!collectChainablesAround(world, blockPos, entity -> entity.getChainData(player) != null).isEmpty()
                  ? InteractionResult.SUCCESS
                  : InteractionResult.PASS);
            }
         } else {
            return InteractionResult.PASS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   public static InteractionResult attachHeldChainsToBlock(Player player, ServerLevel world, BlockPos pos) {
      List<Chainable> list = collectChainablesAround(world, pos, entity -> entity.getChainData(player) != null);
      ChainKnotEntity chainKnotEntity = null;

      for (Chainable chainable : list) {
         if (chainKnotEntity == null) {
            chainKnotEntity = ChainKnotEntity.getOrCreate(world, pos, chainable.getSourceItem());
            chainKnotEntity.onPlace();
         }

         if (chainable.canAttachTo(chainKnotEntity)) {
            Chainable.ChainData chainData = chainable.getChainData(player);

            assert chainData != null;

            chainable.attachChain(new Chainable.ChainData(chainKnotEntity, chainData.sourceItem), player, true);
         }
      }

      if (!list.isEmpty()) {
         world.gameEvent(GameEvent.BLOCK_ATTACH, pos, Context.of(player));
         return InteractionResult.SUCCESS_SERVER;
      } else {
         return InteractionResult.PASS;
      }
   }

   public static List<Chainable> collectChainablesAround(
      net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, Predicate<Chainable> predicate
   ) {
      double distance = ConnectibleChains.runtimeConfig.getMaxChainRange();
      AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ())
         .inflate(distance);
      return world.getEntitiesOfClass(Entity.class, box, entity -> entity instanceof Chainable chainable && predicate.test(chainable))
         .stream()
         .map(Chainable.class::cast)
         .toList();
   }

   @Environment(EnvType.CLIENT)
   public static void infoToolTip(
      net.minecraft.world.item.ItemStack itemStack,
      net.minecraft.world.item.Item.TooltipContext ignoredTooltipContext,
      net.minecraft.world.item.TooltipFlag ignoredTooltipType,
      List<net.minecraft.network.chat.Component> texts
   ) {
      if (ConnectibleChains.runtimeConfig.doShowToolTip() && itemStack.is(ModTagRegistry.CATENARY_ITEMS)) {
         if (Helper.hasShiftDown()) {
            texts.add(1, Component.translatable("message.connectiblechains.connectible_chain_detailed").withStyle(ChatFormatting.AQUA));
         } else {
            texts.add(1, Component.translatable("message.connectiblechains.connectible_chain").withStyle(ChatFormatting.YELLOW));
         }
      }
   }
}
