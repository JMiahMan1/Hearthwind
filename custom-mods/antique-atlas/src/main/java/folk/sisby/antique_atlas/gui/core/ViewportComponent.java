package folk.sisby.antique_atlas.gui.core;

public class ViewportComponent extends Component {
   protected final Component content = new Component();

   public ViewportComponent() {
      this.addChild(this.content);
   }

   public Component addContent(Component child) {
      return this.content.addChild(child);
   }

   public void removeAllContent() {
      this.content.removeAllChildren();
   }

   @Override
   public int getWidth() {
      return this.properWidth;
   }

   @Override
   public int getHeight() {
      return this.properHeight;
   }

   @Override
   public void updateSize() {
      super.updateSize();

      for (Component child : this.content.getChildren()) {
         child.setClipped(
            child.getGuiY() > this.getGuiY() + this.properHeight
               || child.getGuiY() + child.getHeight() < this.getGuiY()
               || child.getGuiX() > this.getGuiX() + this.properWidth
               || child.getGuiX() + child.getWidth() < this.getGuiX()
         );
      }
   }
}
