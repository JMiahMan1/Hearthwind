package dev.jmiahman.hearthwind.world.herd;

import java.util.List;

import dev.jmiahman.hearthwind.world.HearthwindWorldConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;

/**
 * 26.2 Modern port of HerdPanic (Aged 3.1.2 parity, Tschipp):
 * When any animal in a herd/flock takes damage, all nearby animals of the same
 * species panic in unison, scattering and fleeing away from the threat source.
 */
public final class HerdPanic {
    private HerdPanic() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (!blocked && damageTaken > 0 && entity instanceof Animal animal && entity.level() instanceof ServerLevel level) {
                alertHerd(animal, source, level);
            }
        });
    }

    public static int alertHerd(Animal victim, DamageSource source, ServerLevel level) {
        HearthwindWorldConfig cfg = HearthwindWorldConfig.get();
        double radius = cfg.herdPanicAlertRadius;
        double speed = cfg.herdPanicSpeedMultiplier;

        Entity attacker = source.getEntity();
        Vec3 threatPos = attacker != null ? attacker.position() : victim.position();

        // Find all nearby animals of the exact same species
        List<? extends Animal> herd = level.getEntitiesOfClass(
                victim.getClass(),
                victim.getBoundingBox().inflate(radius),
                e -> e.isAlive() && e != victim);

        // First, make the victim panic
        panicAnimal(victim, threatPos, speed, cfg.herdPanicShelterSeeking);

        // Alert all fellow herd members
        int alerted = 0;
        for (Animal member : herd) {
            panicAnimal(member, threatPos, speed, cfg.herdPanicShelterSeeking);
            alerted++;
        }

        if (alerted > 0) {
            victim.playAmbientSound();
        }

        return alerted;
    }

    public static void panicAnimal(Animal animal, Vec3 threatPos, double speedMultiplier, boolean seekShelter) {
        if (!animal.isAlive()) return;

        Vec3 animalPos = animal.position();
        Vec3 awayDir = animalPos.subtract(threatPos).normalize();

        Vec3 target = null;
        if (seekShelter) {
            // Attempt to find a path position away from the threat that is safe
            target = DefaultRandomPos.getPosAway((PathfinderMob) animal, 16, 7, threatPos);
        }

        if (target == null) {
            target = animalPos.add(awayDir.scale(16.0));
        }

        // Steer navigation away from danger at panic sprint speed
        animal.getNavigation().moveTo(target.x, target.y, target.z, speedMultiplier);

        // Make the animal look towards its escape path
        animal.getLookControl().setLookAt(target.x, target.y, target.z);
    }
}
