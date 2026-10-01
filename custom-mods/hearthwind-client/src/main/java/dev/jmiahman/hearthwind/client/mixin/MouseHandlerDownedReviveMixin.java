package dev.jmiahman.hearthwind.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jmiahman.hearthwind.client.DownedHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/**
 * Routes a click on the downed overlay's Revive button to the server.
 *
 * <p>revive 1.0.7 puts this button on the vanilla death screen
 * ({@code DeathScreenMixin.initMixin}); our player is never technically dead,
 * so the button lives on the downed overlay and there is no {@link
 * net.minecraft.client.gui.screens.Screen} open to catch the click. The HUD
 * path in {@code MouseHandler.onButton} is the only place a click reaches us
 * while no screen is up.
 *
 * <p>The hit test lives in {@link DownedHud#isReviveButtonAt}, next to the
 * drawing code, so the two can never drift apart.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerDownedReviveMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void hearthwind$reviveOnClick(long handle, MouseButtonInfo rawButtonInfo, int action,
                                          CallbackInfo ci) {
        if (action != 1 || rawButtonInfo.button() != 0 || !DownedHud.isArmed()) {
            return;
        }
        MouseHandler self = (MouseHandler) (Object) this;
        if (minecraft.gui.screen() != null) {
            return;
        }
        var window = minecraft.getWindow();
        if (handle != window.handle()) {
            return;
        }
        double mouseX = self.getScaledXPos(window);
        double mouseY = self.getScaledYPos(window);
        if (DownedHud.isReviveButtonAt(window.getGuiScaledWidth(), window.getGuiScaledHeight(), mouseX, mouseY)) {
            DownedHud.requestRevive(minecraft);
            ci.cancel();
        }
    }
}