package ht.treechop.client.gui.widget;

import ht.treechop.client.gui.util.GUIUtil;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class StickyWidget extends AbstractWidget {
   public static final Identifier WIDGETS_LOCATION = Identifier.withDefaultNamespace("textures/gui/widgets.png");
   private final Supplier<StickyWidget.State> stateSupplier;
   private final Runnable onPress;

   public StickyWidget(
      int x, int y, int width, int height, net.minecraft.network.chat.Component name, Runnable onPress, Supplier<StickyWidget.State> stateSupplier
   ) {
      super(x, y, Math.max(width, GUIUtil.getMinimumButtonWidth(name)), Math.max(height, 20), name);
      this.onPress = onPress;
      this.stateSupplier = stateSupplier;
   }

   public void onClick(double mouseX, double mouseY) {
      this.onPress.run();
   }

   public int getFGColor() {
      return this.active ? 16777215 : 10526880;
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
      this.active = this.stateSupplier.get() == StickyWidget.State.Up;
      this.height = Math.min(this.height, 20);
      Minecraft minecraft = Minecraft.getInstance();
      int argb = (int)(this.alpha * 255.0F) << 24 | 16777215;
      if (this.stateSupplier.get() != StickyWidget.State.Locked) {
         int i = this.getYImage(this.isHoveredOrFocused());
         gui.blit(RenderPipelines.GUI_TEXTURED, WIDGETS_LOCATION, this.getX(), this.getY(), 0.0F, (float)(46 + i * 20), this.width / 2, this.height, 256, 256, argb);
         gui.blit(
            RenderPipelines.GUI_TEXTURED,
            WIDGETS_LOCATION,
            this.getX() + this.width / 2,
            this.getY(),
            (float)(200 - this.width / 2),
            (float)(46 + i * 20),
            this.width / 2,
            this.height,
            256,
            256,
            argb
         );
      }

      int j = this.getFGColor();
      gui.centeredText(
         minecraft.font,
         this.getMessage(),
         this.getX() + this.width / 2,
         this.getY() + (this.height - 8) / 2,
         j | (int)Math.ceil(this.alpha * 255.0F) << 24
      );
   }

   private int getYImage(boolean hoveredOrFocused) {
      return this.active ? (hoveredOrFocused ? 2 : 1) : 0;
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }

   public static enum State {
      Up,
      Down,
      Locked;

      public static StickyWidget.State of(boolean enabled, boolean canBeEnabled) {
         if (canBeEnabled) {
            return enabled ? Down : Up;
         } else {
            return Locked;
         }
      }
   }
}
