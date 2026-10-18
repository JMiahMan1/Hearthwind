package net.jmb19905.niftycarts.network.serverbound;

import it.unimi.dsi.fastutil.Pair;
import java.util.Comparator;
import java.util.Optional;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.entity.AbstractDrawnEntity;
import net.jmb19905.niftycarts.util.NiftyWorld;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record ActionKeyPayload() implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ActionKeyPayload> TYPE = CustomPacketPayload.createType(
      "niftycarts_action_key"
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, ActionKeyPayload> CODEC = new net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, ActionKeyPayload>() {
      @NotNull
      public ActionKeyPayload decode(FriendlyByteBuf object) {
         return new ActionKeyPayload();
      }

      public void encode(FriendlyByteBuf object, ActionKeyPayload object2) {
      }
   };

   @NotNull
   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ServerPlayer player) {
      Level level = player.level();
      Entity pulling;
      if (player.getVehicle() == null) {
         pulling = player;
      } else {
         pulling = player.getVehicle();
      }

      Optional<AbstractDrawnEntity> drawn = NiftyWorld.getServer(NiftyCarts.server, level.dimension()).getDrawn(pulling);
      drawn.<Pair>map(c -> Pair.of(c, (Entity)null))
         .or(
            () -> level.getEntitiesOfClass(AbstractDrawnEntity.class, pulling.getBoundingBox().inflate(2.0), entity -> entity != pulling)
               .stream()
               .min(Comparator.comparing(pulling::distanceTo))
               .map(c -> Pair.of(c, pulling))
         )
         .filter(
            p -> (Boolean)((AbstractDrawnEntity)p.key()).getConfig().adventureModeInteract.get() || player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE
         )
         .ifPresent(p -> ((AbstractDrawnEntity)p.key()).setPulling((Entity)p.value()));
   }
}
