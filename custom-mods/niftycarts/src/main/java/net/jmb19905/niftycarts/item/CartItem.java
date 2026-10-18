package net.jmb19905.niftycarts.item;

import java.util.List;
import java.util.Optional;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.advancement.NCCriteriaTriggers;
import net.jmb19905.niftycarts.entity.AbstractDrawnEntity;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;

public class CartItem extends Item {
   private final WoodType woodType;
   private final String cartType;

   public CartItem(WoodType woodType, String cartType, Properties settings) {
      super(settings);
      this.woodType = woodType;
      this.cartType = cartType;
   }

   @NotNull
   public InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand interactionHand) {
      ItemStack stack = player.getItemInHand(interactionHand);
      BlockHitResult result = getPlayerPOVHitResult(level, player, Fluid.ANY);
      if (result.getType() == Type.MISS) {
         return InteractionResult.PASS;
      } else {
         Vec3 lookVec = player.getLookAngle();
         List<net.minecraft.world.entity.Entity> list = level.getEntities(
            player, player.getBoundingBox().expandTowards(lookVec.scale(5.0)).inflate(5.0), EntitySelector.CAN_BE_COLLIDED_WITH
         );
         if (!list.isEmpty()) {
            Vec3 eyePos = player.getEyePosition(1.0F);

            for (Entity entity : list) {
               AABB axisalignedbb = entity.getBoundingBox().inflate(entity.getPickRadius());
               if (axisalignedbb.contains(eyePos)) {
                  return InteractionResult.PASS;
               }
            }
         }

         if (result.getType() == Type.BLOCK) {
            net.minecraft.resources.ResourceKey<net.minecraft.world.entity.EntityType<?>> key = ResourceKey.create(
               Registries.ENTITY_TYPE, NiftyCarts.resLoc(this.cartType)
            );
            Optional<net.minecraft.core.Holder.Reference<net.minecraft.world.entity.EntityType<?>>> type = BuiltInRegistries.ENTITY_TYPE
               .get(key);
            if (type.isEmpty()) {
               return InteractionResult.PASS;
            } else {
               Entity cart = ((EntityType)((Reference)type.get()).value()).create(level, EntitySpawnReason.SPAWN_ITEM_USE);
               if (cart == null) {
                  return InteractionResult.PASS;
               } else {
                  if (cart instanceof AbstractDrawnEntity drawn) {
                     drawn.setWoodType(this.woodType);
                  }

                  cart.snapTo(result.getLocation().x, result.getLocation().y, result.getLocation().z);
                  cart.setYRot((player.getYRot() + 180.0F) % 360.0F);
                  if (!level.noCollision(cart, cart.getBoundingBox().inflate(0.1F, -0.1F, 0.1F))) {
                     return InteractionResult.FAIL;
                  } else {
                     if (!level.isClientSide()) {
                        level.addFreshEntity(cart);
                        level.playSound(
                           null, cart.getX(), cart.getY(), cart.getZ(), NiftyCarts.PLACE_SOUND, SoundSource.BLOCKS, 0.75F, 0.8F
                        );
                        NCCriteriaTriggers.PLACE_CART_ITEM.trigger((ServerPlayer)player, stack);
                     }

                     if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                     }

                     player.awardStat(Stats.ITEM_USED.get(this));
                     return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
                  }
               }
            }
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public String getCartType() {
      return this.cartType;
   }
}
