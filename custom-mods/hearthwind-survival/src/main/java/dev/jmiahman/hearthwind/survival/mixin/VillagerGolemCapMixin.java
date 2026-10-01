package dev.jmiahman.hearthwind.survival.mixin;

import dev.jmiahman.hearthwind.survival.additionz.AdditionZParity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A village stops asking for iron golems once it has eight.
 *
 * <p>26.2 has no cap of its own: {@code Villager.golemSpawnConditionsMet} only
 * checks that somebody slept recently, and {@code spawnGolemIfNeeded} then
 * calls {@code SpawnUtil.trySpawnMob} with no golem budget at all. AdditionZ
 * caps it at Aged's {@code max_iron_golem_villager_spawn: 8}.
 *
 * <p>The reference counts the golems it has spawned in an NBT tag on the
 * villager. We count the golems actually standing at the caller's position,
 * which tracks what the village really has rather than what it once tried to
 * build - a golem killed by a player no longer counts against the village.
 */
@Mixin(Villager.class)
public abstract class VillagerGolemCapMixin {

    @Inject(method = "spawnGolemIfNeeded", at = @At("HEAD"), cancellable = true)
    private void hearthwind$villageGolemAllowance(ServerLevel level, long timestamp,
            int villagersNeededToAgree, CallbackInfo ci) {
        if (AdditionZParity.golemCapReached(level, ((Villager) (Object) this).blockPosition(),
                AdditionZParity.config().maxIronGolemSpawn)) {
            ci.cancel();
        }
    }
}
