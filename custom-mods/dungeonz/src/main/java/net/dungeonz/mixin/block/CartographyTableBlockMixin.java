package net.dungeonz.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.ItemInit;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3711;
import net.minecraft.class_3965;

@Mixin(class_3711.class)
public abstract class CartographyTableBlockMixin extends class_2248 {

    public CartographyTableBlockMixin(class_2251 settings) {
        super(settings);
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void onUseMixin(class_2680 state, class_1937 world, class_2338 pos, class_1657 player, class_3965 hit, CallbackInfoReturnable<class_1269> info) {
        if (player.method_6047().method_31574(ItemInit.DUNGEON_COMPASS)) {
            info.setReturnValue(class_1269.field_5811);
        }
    }

}
