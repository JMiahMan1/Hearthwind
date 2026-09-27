package dev.jmiahman.hearthwind.world.mixin;

import dev.jmiahman.hearthwind.world.time.TimeAndWind;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Time &amp; Wind parity: while night-skip acceleration is active, vanilla
 * must not teleport the clock to dawn when every player sleeps. The mod
 * races the clock forward instead (see {@link TimeAndWind#tickLevel}), then
 * wakes the players at sunrise.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelTimeMixin {
    @Redirect(method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/players/SleepStatus;areEnoughDeepSleeping(ILjava/util/List;)Z"))
    private boolean hearthwind$blockInstantNightSkip(SleepStatus status, int percentage,
            List<ServerPlayer> players) {
        if (TimeAndWind.blocksInstantSkip((ServerLevel) (Object) this)) {
            return false;
        }
        return status.areEnoughDeepSleeping(percentage, players);
    }
}
