package net.dungeonz.mixin.misc;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.DimensionInit;
import net.minecraft.class_1297;
import net.minecraft.class_1588;
import net.minecraft.class_7260;

@Mixin(class_7260.class)
public class WardenEntityMixin {

    @Inject(method = "isValidTarget", at = @At("HEAD"), cancellable = true)
    private void isValidTargetMixin(@Nullable class_1297 entity, CallbackInfoReturnable<Boolean> info) {
        if (entity != null && entity.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD && entity instanceof class_1588) {
            info.setReturnValue(false);
        }
    }
}
