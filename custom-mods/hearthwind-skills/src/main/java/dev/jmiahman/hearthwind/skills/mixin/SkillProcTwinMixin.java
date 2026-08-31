package dev.jmiahman.hearthwind.skills.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jmiahman.hearthwind.skills.SkillProcs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;

/**
 * The FARMING capstone: breeding sometimes yields a second baby.
 *
 * Runs after the vanilla child is spawned so the parents' love state, age
 * reset and breeding XP are unchanged - the twin is a pure bonus.
 */
@Mixin(Animal.class)
public abstract class SkillProcTwinMixin {

    @Inject(method = "spawnChildFromBreeding", at = @At("TAIL"))
    private void hearthwind$twinBaby(ServerLevel level, Animal partner, CallbackInfo ci) {
        Animal self = (Animal) (Object) this;
        ServerPlayer breeder = self.getLoveCause() != null
                ? self.getLoveCause()
                : partner.getLoveCause();
        if (breeder == null) {
            return;
        }
        if (!SkillProcs.twinBaby(breeder, level.getRandom())) {
            return;
        }
        AgeableMob twin = self.getBreedOffspring(level, partner);
        if (twin == null) {
            return;
        }
        twin.setBaby(true);
        twin.snapTo(self.getX(), self.getY(), self.getZ(), 0.0f, 0.0f);
        level.addFreshEntityWithPassengers(twin);
    }
}
