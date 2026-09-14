package net.dungeonz.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2358;
import net.minecraft.class_3218;

@Mixin(class_2358.class)
public class FireBlockMixin {

    @Inject(method = "areBlocksAroundFlammable", at = @At("HEAD"), cancellable = true)
    private void areBlocksAroundFlammableMixin(class_1922 world, class_2338 pos, CallbackInfoReturnable<Boolean> info) {
        if ((world instanceof class_3218 && ((class_3218) world).method_27983() == DimensionInit.DUNGEON_WORLD) || ConfigInit.CONFIG.devMode) {
            info.cancel();
        }
    }

}
