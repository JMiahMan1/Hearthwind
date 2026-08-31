package dev.jmiahman.hearthwind.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import dev.jmiahman.hearthwind.client.TabStrip;

/**
 * Draws the Hearthwind 6-tab strip above the vanilla player inventory screen.
 * Seamlessly matches the look and feel of all survival panel screens.
 */
@Environment(EnvType.CLIENT)
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenButtonMixin extends AbstractContainerScreen<InventoryMenu> {

    private InventoryScreenButtonMixin(InventoryMenu menu, net.minecraft.world.entity.player.Inventory inventory,
            Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void hearthwind$drawTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        TabStrip.draw(graphics, this.font, this.leftPos, this.topPos, TabStrip.Tab.INVENTORY, mouseX, mouseY);
    }
}
