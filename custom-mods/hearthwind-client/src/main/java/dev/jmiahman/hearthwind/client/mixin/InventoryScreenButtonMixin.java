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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import dev.jmiahman.hearthwind.client.TabStrip;

/**
 * Draws the 4-tab strip (Inventory, Skills, Jobs, Party) and the Nutrition
 * drumstick button on the vanilla player inventory screen matching Aged parity.
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

        // Nutrition drumstick button on top right of inventory panel
        int nutX = this.leftPos + 154;
        int nutY = this.topPos + 5;
        boolean hover = mouseX >= nutX && mouseX < nutX + 16 && mouseY >= nutY && mouseY < nutY + 16;
        if (hover) {
            graphics.fill(nutX - 1, nutY - 1, nutX + 17, nutY + 17, 0x40FFFFFF);
            graphics.setTooltipForNextFrame(Component.literal("Nutrients [N]"), mouseX, mouseY);
        }
        graphics.item(new ItemStack(Items.COOKED_BEEF), nutX, nutY);
    }
}
