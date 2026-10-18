package ht.treechop.client.gui.widget;

import ht.treechop.client.gui.util.Sprite;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ToggleWidget extends AbstractWidget {
   private static final java.util.Map<ToggleWidget.State, Sprite> SPRITE_FOR_STATE = java.util.Map.of(
      ToggleWidget.State.OFF, Sprite.TOGGLE_BUTTON_OFF,
      ToggleWidget.State.ON, Sprite.TOGGLE_BUTTON_ON,
      ToggleWidget.State.LOCKED_OFF, Sprite.LOCKED_TOGGLE_BUTTON_OFF,
      ToggleWidget.State.LOCKED_ON, Sprite.LOCKED_TOGGLE_BUTTON_ON
   );
   private static final java.util.Map<ToggleWidget.State, Sprite> SPRITE_FOR_HOVERED_STATE = java.util.Map.of(
      ToggleWidget.State.OFF, Sprite.HIGHLIGHTED_TOGGLE_BUTTON_OFF,
      ToggleWidget.State.ON, Sprite.HIGHLIGHTED_TOGGLE_BUTTON_ON,
      ToggleWidget.State.LOCKED_OFF, Sprite.LOCKED_TOGGLE_BUTTON_OFF,
      ToggleWidget.State.LOCKED_ON, Sprite.LOCKED_TOGGLE_BUTTON_ON
   );
   private final Supplier<ToggleWidget.State> stateSupplier;
   private final Runnable onPress;

   public ToggleWidget(int x, int y, Runnable onPress, Supplier<ToggleWidget.State> stateSupplier) {
      super(x, y, Sprite.TOGGLE_BUTTON_OFF.width, Sprite.TOGGLE_BUTTON_OFF.height, Component.empty());
      this.onPress = onPress;
      this.stateSupplier = stateSupplier;
   }

   public void onClick(double mouseX, double mouseY) {
      this.onPress.run();
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
      this.active = !this.stateSupplier.get().isLocked;
      ToggleWidget.State state = this.stateSupplier.get();
      Sprite sprite = this.isHoveredOrFocused() ? SPRITE_FOR_HOVERED_STATE.get(state) : SPRITE_FOR_STATE.get(state);
      sprite.blit(gui, this.getX(), this.getY(), sprite.width, sprite.height, this.alpha);
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }

   public static enum State {
      ON(true, false),
      OFF(false, false),
      LOCKED_ON(true, true),
      LOCKED_OFF(false, true);

      public final boolean isOn;
      private final boolean isLocked;

      private State(boolean isOn, boolean isLocked) {
         this.isOn = isOn;
         this.isLocked = isLocked;
      }

      public static ToggleWidget.State of(boolean enabled, boolean canBeEnabled) {
         if (canBeEnabled) {
            return enabled ? ON : OFF;
         } else {
            return enabled ? LOCKED_ON : LOCKED_OFF;
         }
      }
   }
}
