package net.dungeonz.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.minecraft.class_2338;
import net.minecraft.class_2473;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_5819;

@Mixin(class_2473.class)
public class SaplingBlockMixin {

    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void generateMixin(class_3218 world, class_2338 pos, class_2680 state, class_5819 random, CallbackInfo info) {
        if (world.method_27983() == DimensionInit.DUNGEON_WORLD || ConfigInit.CONFIG.devMode) {
            info.cancel();
        }
    }

}
