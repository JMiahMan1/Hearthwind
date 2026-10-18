package net.voidz.init;

import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents.Allow;
import net.minecraft.world.entity.player.Player;

public class EventInit {
   public static void init() {
      EntityElytraEvents.ALLOW
         .register(
            (Allow)entity -> !(entity instanceof Player player && !player.isCreative() && player.level().dimension() == DimensionInit.VOID_WORLD)
         );
   }
}
