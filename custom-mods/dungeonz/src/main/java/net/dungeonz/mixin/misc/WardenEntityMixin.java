package net.dungeonz.mixin.misc;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.DimensionInit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;

@Mixin(Warden.class)
public class WardenEntityMixin {

    @Inject(method = "canTargetEntity", at = @At("HEAD"), cancellable = true)
    private void isValidTargetMixin(@Nullable Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (entity != null && entity.level().dimension() == DimensionInit.DUNGEON_WORLD && entity instanceof Monster) {
            info.setReturnValue(false);
        }
    }
}
