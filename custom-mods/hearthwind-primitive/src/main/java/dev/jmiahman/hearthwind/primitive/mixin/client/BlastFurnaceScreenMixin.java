package dev.jmiahman.hearthwind.primitive.mixin.client;


import dev.jmiahman.hearthwind.primitive.client.HearthwindPrimitiveClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the extra-slot hole above the input slot of the blast furnace GUI
 * (parity with earlystage; the menu owns the matching 4th Slot at 76,17).
 *
 * Rules learned the hard way (each one crashed every client boot):
 * 1. {@code extractBackground(GuiGraphicsExtractor, int, int, float)} is
 *    declared in {@link AbstractFurnaceScreen}, NOT in
 *    {@code BlastFurnaceScreen} - mixin refuses to inject into inherited
 *    methods, so the mixin targets {@link AbstractFurnaceScreen} and guards
 *    with an instanceof check instead.
 * 2. {@code leftPos}/{@code topPos} are inherited from
 *    {@link AbstractContainerScreen} - they must NOT be re-declared with
 *    {@code @Shadow}: shadow validation only resolves members declared in
 *    the target class itself. The {@code extends} makes them plain
 *    inherited members.
 */
@Mixin(AbstractFurnaceScreen.class)
public abstract class BlastFurnaceScreenMixin extends AbstractContainerScreen<AbstractFurnaceMenu> {

    public BlastFurnaceScreenMixin(AbstractFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void hearthwind$extraSlotIcon(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        if (!((Object) this instanceof BlastFurnaceScreen)) {
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, HearthwindPrimitiveClient.EXTRA_SLOT_ICON,
                this.leftPos + 75, this.topPos + 16, 0.0F, 0.0F, 18, 18, 256, 256);
    }
}
