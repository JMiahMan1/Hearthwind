package net.jmb19905.niftycarts.network.serverbound;

import java.util.Optional;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.NiftyCartsConfig;
import net.jmb19905.niftycarts.entity.AbstractDrawnEntity;
import net.jmb19905.niftycarts.util.NiftyWorld;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ToggleSlowPayload() implements CustomPacketPayload {
   public static final Identifier PULL_SLOWLY_MODIFIER_ID = NiftyCarts.resLoc("pull_slowly");
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ToggleSlowPayload> TYPE = CustomPacketPayload.createType(
      "niftycarts_toggle_slow"
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, ToggleSlowPayload> CODEC = new net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, ToggleSlowPayload>() {
      @NotNull
      public ToggleSlowPayload decode(FriendlyByteBuf object) {
         return new ToggleSlowPayload();
      }

      public void encode(FriendlyByteBuf object, ToggleSlowPayload object2) {
      }
   };

   @NotNull
   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(Player player) {
      Entity vehicle = player.getControlledVehicle();
      if (vehicle != null && !toggleSlow(vehicle)) {
         getCart(player).map(AbstractDrawnEntity::getPulling).ifPresent(ToggleSlowPayload::toggleSlow);
      }
   }

   public static boolean toggleSlow(Entity vehicle) {
      if (vehicle instanceof LivingEntity living) {
         AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
         if (speed == null) {
            return false;
         } else {
            AttributeModifier modifier = speed.getModifier(PULL_SLOWLY_MODIFIER_ID);
            if (modifier == null) {
               speed.addTransientModifier(new AttributeModifier(PULL_SLOWLY_MODIFIER_ID, (Double)NiftyCartsConfig.get().slowSpeed.get(), Operation.ADD_MULTIPLIED_TOTAL));
            } else {
               speed.removeModifier(modifier.id());
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean isSlowable(Entity vehicle) {
      return !(vehicle instanceof LivingEntity) ? false : ((LivingEntity)vehicle).getAttribute(Attributes.MOVEMENT_SPEED) != null;
   }

   public static boolean isSlow(Entity vehicle) {
      if (!(vehicle instanceof LivingEntity)) {
         return false;
      } else {
         AttributeInstance speed = ((LivingEntity)vehicle).getAttribute(Attributes.MOVEMENT_SPEED);
         if (speed == null) {
            return false;
         } else {
            AttributeModifier modifier = speed.getModifier(PULL_SLOWLY_MODIFIER_ID);
            return modifier != null;
         }
      }
   }

   public static Optional<AbstractDrawnEntity> getCart(net.minecraft.world.entity.player.Player player) {
      Entity ridden = player.getVehicle();
      if (ridden == null) {
         return Optional.empty();
      } else {
         return ridden instanceof AbstractDrawnEntity ? Optional.of((AbstractDrawnEntity)ridden) : NiftyWorld.get(player.level()).getDrawn(ridden);
      }
   }
}
