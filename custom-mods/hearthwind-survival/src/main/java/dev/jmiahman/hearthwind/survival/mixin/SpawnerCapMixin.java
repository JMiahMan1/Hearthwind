package dev.jmiahman.hearthwind.survival.mixin;

import dev.jmiahman.hearthwind.survival.additionz.AdditionZParity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A spawner gives up after twenty waves, then forgets it ten minutes later.
 *
 * <p>AdditionZ counts one per wave - the reference increments its total on the
 * last spawn of each activation, not once per mob - and at the limit it either
 * locks the spawner for {@code spawner_tick_deactivation} ticks or, if that
 * window is zero, breaks the block. Aged ships the window, so the block-break
 * branch is dead there and is not reproduced. The lockout throws electric
 * sparks every third tick while it runs, which is what the reference does and
 * is the only way a player can tell a locked spawner from a spent one.
 */
@Mixin(BaseSpawner.class)
public abstract class SpawnerCapMixin {

    @Shadow
    private int spawnDelay;

    @Unique
    private int hearthwind$totalSpawns;

    @Unique
    private int hearthwind$deactivationTicks;

    @Unique
    private boolean hearthwind$firing;

    @Inject(method = "serverTick", at = @At("HEAD"))
    private void hearthwind$beforeTick(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        if (hearthwind$deactivationTicks > 0) {
            // The reference cancels the whole tick while locked out.
            ci.cancel();
            if (level.getGameTime() % 3 == 0) {
                RandomSource random = level.getRandom();
                for (int i = 0; i < 5; i++) {
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.getX() + random.nextDouble(),
                            pos.getY() + random.nextDouble(),
                            pos.getZ() + random.nextDouble(),
                            1, 0, 0, 0, 0.0);
                }
            }
            return;
        }
        hearthwind$firing = spawnDelay <= 0;
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private void hearthwind$afterTick(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        if (!hearthwind$firing) {
            return;
        }
        hearthwind$firing = false;
        int cfg = AdditionZParity.config().maxSpawnerCount;
        if (cfg <= 0) {
            return;
        }
        int total = AdditionZParity.spawnerTick(hearthwind$totalSpawns + 1,
                AdditionZParity.config().spawnerTickDeactivation,
                AdditionZParity.config().spawnerTickDeactivation, cfg);
        if (total < 0) {
            int window = AdditionZParity.config().spawnerTickDeactivation;
            if (window > 0) {
                spawnDelay = window;
                hearthwind$deactivationTicks = window;
            }
            return;
        }
        hearthwind$totalSpawns = total;
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void hearthwind$readCount(Level level, BlockPos pos, ValueInput input, CallbackInfo ci) {
        hearthwind$totalSpawns = input.getIntOr("TotalSpawnCount", 0);
        hearthwind$deactivationTicks = input.getIntOr("DeactivationTicks", 0);
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void hearthwind$writeCount(ValueOutput output, CallbackInfo ci) {
        output.putInt("TotalSpawnCount", hearthwind$totalSpawns);
        output.putInt("DeactivationTicks", hearthwind$deactivationTicks);
    }
}
