package net.smitherz.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.smitherz.screen.SmitherScreen;
import net.smitherz.screen.widget.GemSlot;
import net.smitherz.util.UpgradeHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({AbstractContainerScreen.class})
public abstract class HandledScreenMixin<T extends net.minecraft.world.inventory.AbstractContainerMenu> extends net.minecraft.client.gui.screens.Screen {
   @Unique
   private final boolean isSmitherScreen = ((Object)this) instanceof SmitherScreen;
   @Shadow
   @Mutable
   @Final
   protected T menu;

   public HandledScreenMixin(Component title) {
      super(title);
   }

   @Inject(
      method = {"extractSlot"},
      at = {@At("TAIL")}
   )
   private void extractSlotMixin(GuiGraphicsExtractor context, Slot slot, int x, int y, CallbackInfo info) {
      if (this.isSmitherScreen && slot instanceof GemSlot) {
         int k = this.menu.slots.indexOf(slot);
         if (k >= 2) {
            ItemStack stack = ((Slot)this.menu.slots.get(0)).getItem();
            if (!stack.isEmpty()) {
               int gemSlotSize = UpgradeHelper.getGemSlotSize(stack);
               if (gemSlotSize > k - 2) {
                  context.blit(RenderPipelines.GUI_TEXTURED, SmitherScreen.TEXTURE, slot.x - 1, slot.y - 1, 0.0F, 166.0F, 18, 18, 256, 256);
               }
            }
         }
      }
   }
}
