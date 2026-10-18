package net.nameplate.mixin;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.nameplate.util.NameplateTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ServerEntity.class},
   priority = 1001
)
public class EntityTrackerEntryMixin {
   @Shadow
   @Final
   private Entity entity;

   @Inject(
      method = {"addPairing"},
      at = {@At("TAIL")}
   )
   public void addPairingMixin(ServerPlayer serverPlayer, CallbackInfo info) {
      if (this.entity instanceof Mob) {
         NameplateTracker.startTracking((Mob)this.entity, serverPlayer);
      }
   }
}
