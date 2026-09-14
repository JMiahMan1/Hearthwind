package net.dungeonz.mixin.misc;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.DimensionInit;
import net.minecraft.class_1927;
import net.minecraft.class_1927.class_4179;
import net.minecraft.class_1937;

@Mixin(class_1927.class)
public class ExplosionMixin {

    @Shadow
    @Mutable
    @Final
    private class_1937 world;

    @Shadow
    @Mutable
    @Final
    private class_4179 destructionType;

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void affectWorldMixin(boolean particles, CallbackInfo info) {
        if (world.method_27983() == DimensionInit.DUNGEON_WORLD) {
            destructionType = class_4179.field_40878;
        }
    }

}
