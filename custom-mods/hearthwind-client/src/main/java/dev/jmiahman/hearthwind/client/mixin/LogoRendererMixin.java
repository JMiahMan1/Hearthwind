package dev.jmiahman.hearthwind.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the default Minecraft logo so the custom painted HearthWind title art is unobstructed.
 */
@Environment(EnvType.CLIENT)
@Mixin(LogoRenderer.class)
public class LogoRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IF)V", at = @At("HEAD"), cancellable = true)
    private void cancelLogoRendering1(GuiGraphicsExtractor graphics, int screenWidth, float alpha, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IFI)V", at = @At("HEAD"), cancellable = true)
    private void cancelLogoRendering2(GuiGraphicsExtractor graphics, int screenWidth, float alpha, int heightOffset, CallbackInfo ci) {
        ci.cancel();
    }
}
