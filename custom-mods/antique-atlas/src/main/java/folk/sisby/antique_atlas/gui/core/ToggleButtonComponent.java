package folk.sisby.antique_atlas.gui.core;

public class ToggleButtonComponent extends ButtonComponent {
   protected final boolean canToggle;
   protected boolean selected;

   public ToggleButtonComponent(boolean canToggle) {
      this.canToggle = canToggle;
   }

   public void setSelected(boolean value) {
      this.selected = value;
   }

   public boolean isSelected() {
      return this.selected;
   }

   @Override
   public void onClick() {
      if (!this.isSelected() || this.canToggle) {
         this.setSelected(!this.isSelected());
      }

      super.onClick();
   }
}
