package dev.jmiahman.hearthwind.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.InventoryMenu;
import dev.jmiahman.hearthwind.client.NutrientsScreen;
import dev.jmiahman.hearthwind.client.TabStrip;

/**
 * Handles clicks on the 4-tab strip and the NutritionZ nutrients tab while
 * on the Inventory screen. The 9x9 nutrients tab only responds when no
 * inventory slot is hovered (original focusedSlot == null check).
 */
@Environment(EnvType.CLIENT)
@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractContainerScreenClickMixin extends AbstractContainerScreen<InventoryMenu> {

    private AbstractContainerScreenClickMixin(InventoryMenu menu, Inventory inventory,
            net.minecraft.network.chat.Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void hearthwind$onTabClick(MouseButtonEvent event, boolean doubled,
            CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof InventoryScreen && event.button() == 0) {
            TabStrip.Tab tab = TabStrip.clicked(event.x(), event.y(), this.leftPos, this.topPos,
                    TabStrip.Tab.INVENTORY);
            if (tab != null) {
                if (tab != TabStrip.Tab.INVENTORY) {
                    Minecraft.getInstance().getSoundManager()
                            .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    TabStrip.open(tab);
                }
                cir.setReturnValue(true);
                return;
            }

            // NutritionZ nutrients tab on the top-right of the inventory panel.
            if (this.hoveredSlot == null
                    && event.x() >= this.leftPos + 162 && event.x() < this.leftPos + 171
                    && event.y() >= this.topPos + 5 && event.y() < this.topPos + 14) {
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                Minecraft.getInstance().setScreenAndShow(new NutrientsScreen());
                cir.setReturnValue(true);
            }
        }
    }
}
