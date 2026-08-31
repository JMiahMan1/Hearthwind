package dev.jmiahman.hearthwind.world.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jmiahman.hearthwind.world.HearthwindWorld;
import dev.jmiahman.hearthwind.world.HearthwindWorldConfig;
import dev.jmiahman.hearthwind.world.Season;

/**
 * Winter is a dead season for husbandry: animals refuse to breed, so the
 * player has to plan breeding around the calendar.
 */
@Mixin(Animal.class)
public abstract class HearthwindWinterBreedingMixin {

    @Inject(method = "spawnChildFromBreeding", at = @At("HEAD"), cancellable = true)
    private void hearthwind_winterBreeding$blockWinter(ServerLevel level, Animal partner, CallbackInfo ci) {
        if (HearthwindWorldConfig.get().animalsBreedInWinter) {
            return;
        }
        if (HearthwindWorld.currentSeason(level) != Season.WINTER) {
            return;
        }
        Animal self = (Animal) (Object) this;
        self.resetLove();
        partner.resetLove();
        ci.cancel();
    }
}
