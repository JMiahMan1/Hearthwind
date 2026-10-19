package io.github.jamalam360.shut_up_gl_error.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputConstants.class)
public class InputUtilMixin {
    /**
     * GLFW spams GL errors when asked about invalid keycodes (-1 is GLFW_KEY_UNKNOWN
     * and 0 is GLFW_KEY_SPACE's enum slot, both of which can end up here from unbound
     * mappings). Vanilla happily asks anyway; upstream 1.0.0 answered "not pressed"
     * for both and so do we.
     */
    @Inject(method = "isKeyDown(Lcom/mojang/blaze3d/platform/Window;I)Z", at = @At("HEAD"), cancellable = true)
    private static void shutupglerror$shutUpGLError(Window window, int code, CallbackInfoReturnable<Boolean> cir) {
        if (code == -1 || code == 0) {
            cir.setReturnValue(false);
        }
    }
}
