package folk.sisby.antique_atlas.gui.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ToggleButtonRadioGroup<B extends ToggleButtonComponent> implements Iterable<B> {
   protected final List<B> buttons = new ArrayList<>();
   protected final List<ISelectListener<? extends B>> listeners = new ArrayList<>();
   protected B selectedButton = (B)null;
   protected final ToggleButtonRadioGroup<B>.ClickListener clickListener = new ToggleButtonRadioGroup.ClickListener();

   public void addButton(B button) {
      if (!this.buttons.contains(button)) {
         this.buttons.add(button);
         button.addListener(this.clickListener);
      }
   }

   public void setSelectedButton(B button) {
      if (this.buttons.contains(button)) {
         if (this.selectedButton != null) {
            this.selectedButton.setSelected(false);
         }

         button.setSelected(true);
         this.selectedButton = button;
      }
   }

   @NotNull
   @Override
   public Iterator<B> iterator() {
      return this.buttons.iterator();
   }

   public void addListener(ISelectListener<? extends B> listener) {
      this.listeners.add(listener);
   }

   public class ClickListener implements IButtonListener<B> {
      public void onClick(B button) {
         if (button != ToggleButtonRadioGroup.this.selectedButton) {
            if (ToggleButtonRadioGroup.this.selectedButton != null) {
               ToggleButtonRadioGroup.this.selectedButton.setSelected(false);
            }

            ToggleButtonRadioGroup.this.selectedButton = button;

            for (ISelectListener listener : ToggleButtonRadioGroup.this.listeners) {
               listener.onSelect(ToggleButtonRadioGroup.this.selectedButton);
            }
         }
      }
   }
}
