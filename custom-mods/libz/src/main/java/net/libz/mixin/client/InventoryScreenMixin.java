package net.libz.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.api.Tab;
import net.libz.util.DrawTabHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({InventoryScreen.class})
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> implements Tab {
   private InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
   }

   @Inject(
      method = {"mouseClicked"},
      at = {@At("HEAD")}
   )
   private void mouseClickedMixin(MouseButtonEvent event, boolean bl, CallbackInfoReturnable<Boolean> info) {
      DrawTabHelper.onTabButtonClick(this.minecraft, this, this.leftPos, this.topPos, event.x(), event.y(), this.hoveredSlot != null);
   }

   @Inject(
      method = {"extractBackground"},
      at = {@At("TAIL")}
   )
   protected void extractBackgroundMixin(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo info) {
      DrawTabHelper.drawTab(this.minecraft, context, this, this.leftPos, this.topPos, mouseX, mouseY);
   }
}
