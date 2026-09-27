package dev.jmiahman.hearthwind.world.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Reaches the pieces of {@link ServerLevel} the Time &amp; Wind parity port
 * needs: the live {@link SleepStatus} and vanilla's private
 * {@code wakeUpAllPlayers()}, used when the accelerated night reaches dawn.
 */
@Mixin(ServerLevel.class)
public interface ServerLevelTimeAccessor {
    @Accessor("sleepStatus")
    SleepStatus hearthwind$sleepStatus();

    @Invoker("wakeUpAllPlayers")
    void hearthwind$wakeUpAllPlayers();
}
