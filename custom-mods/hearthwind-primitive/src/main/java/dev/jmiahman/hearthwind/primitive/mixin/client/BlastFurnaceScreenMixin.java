package dev.jmiahman.hearthwind.primitive.mixin.client;


import dev.jmiahman.hearthwind.primitive.client.HearthwindPrimitiveClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the extra-slot hole above the input slot of the blast furnace GUI
 * (parity with earlystage; the menu owns the matching 4th Slot at 76,17).
 */
@Mixin(BlastFurnaceScreen.class)
public abstract class BlastFurnaceScreenMixin extends AbstractContainerScreen<BlastFurnaceMenu> {

    public BlastFurnaceScreenMixin(BlastFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Shadow
    public int leftPos;

    @Shadow
    public int topPos;

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void hearthwind$extraSlotIcon(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, HearthwindPrimitiveClient.EXTRA_SLOT_ICON,
                this.leftPos + 75, this.topPos + 16, 0.0F, 0.0F, 18, 18, 256, 256);
    }
}
