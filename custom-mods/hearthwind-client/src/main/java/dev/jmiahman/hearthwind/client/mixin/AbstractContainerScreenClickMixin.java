package dev.jmiahman.hearthwind.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import dev.jmiahman.hearthwind.client.TabStrip;

/**
 * Handles clicks on the 6-tab strip while on the Inventory screen.
 * Dispatches navigation to the matching Hearthwind survival panel.
 */
@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenClickMixin {

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void hearthwind$onTabClick(MouseButtonEvent event, boolean doubled,
            CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof InventoryScreen && event.button() == 0) {
            TabStrip.Tab tab = TabStrip.clicked(event.x(), event.y(), this.leftPos, this.topPos);
            if (tab != null && tab != TabStrip.Tab.INVENTORY) {
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                TabStrip.open(tab);
                cir.setReturnValue(true);
            }
        }
    }
}
