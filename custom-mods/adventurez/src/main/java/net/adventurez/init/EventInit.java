package net.adventurez.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.level.ServerPlayer;

public class EventInit {

    public static void init() {
        ServerPlayerEvents.AFTER_RESPAWN.register((ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) -> {
            if (alive && oldPlayer.hasEffect(EffectInit.FAME)) {
                newPlayer.addEffect(new MobEffectInstance(EffectInit.FAME, oldPlayer.getEffect(EffectInit.FAME).getDuration(), 0, false, false, true));
            }
        });
    }

}
