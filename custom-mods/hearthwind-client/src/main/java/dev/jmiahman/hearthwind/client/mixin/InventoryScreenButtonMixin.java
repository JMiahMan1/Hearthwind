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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import dev.jmiahman.hearthwind.client.NutrientsScreen;
import dev.jmiahman.hearthwind.client.TabStrip;

/**
 * Draws the 4-tab strip (Inventory, Skills, Jobs, Party) and the original
 * NutritionZ 9x9 nutrients tab at (leftPos+162, topPos+5) inside the vanilla
 * player inventory panel.
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
        TabStrip.draw(graphics, this.leftPos, this.topPos, TabStrip.Tab.INVENTORY, mouseX, mouseY);

        // NutritionZ inventory tab (U176 V10 normal / U185 V10 hover).
        int nutX = this.leftPos + 162;
        int nutY = this.topPos + 5;
        boolean hover = mouseX >= nutX && mouseX < nutX + 9 && mouseY >= nutY && mouseY < nutY + 9;
        graphics.blit(RenderPipelines.GUI_TEXTURED, NutrientsScreen.ICONS, nutX, nutY,
                hover ? 185f : 176f, 10f, 9, 9, 256, 256, 0xFFFFFFFF);
        if (hover) {
            graphics.setTooltipForNextFrame(Component.translatable("screen.nutritionz"), mouseX, mouseY);
        }
    }
}
