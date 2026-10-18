package folk.sisby.antique_atlas.gui.core;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class Component extends Screen {
   public Component parent = null;
   public final List<Component> children = new CopyOnWriteArrayList<>();
   protected int properWidth;
   protected int properHeight;
   protected int contentWidth;
   protected int contentHeight;
   public boolean isClipped = false;
   public int guiX = 0;
   public int guiY = 0;

   public Component() {
      super(net.minecraft.network.chat.Component.literal("component"));
   }

   public void setGuiCoords(int x, int y) {
      int dx = x - this.guiX;
      int dy = y - this.guiY;
      this.guiX = x;
      this.guiY = y;

      for (Component child : this.children) {
         child.offsetGuiCoords(dx, dy);
      }

      if (this.parent != null && (dx != 0 || dy != 0)) {
         this.parent.updateSize();
      }
   }

   public final void setRelativeCoords(int x, int y) {
      if (this.parent != null) {
         this.setGuiCoords(this.parent.getGuiX() + x, this.parent.getGuiY() + y);
      } else {
         this.setGuiCoords(x, y);
      }
   }

   public final void setRelativeX(int x) {
      if (this.parent != null) {
         this.setGuiCoords(this.parent.getGuiX() + x, this.guiY);
      } else {
         this.setGuiCoords(x, this.guiY);
      }
   }

   public final void setRelativeY(int y) {
      if (this.parent != null) {
         this.setGuiCoords(this.guiX, this.parent.getGuiY() + y);
      } else {
         this.setGuiCoords(this.guiX, y);
      }
   }

   public final void offsetGuiCoords(int dx, int dy) {
      this.setGuiCoords(this.guiX + dx, this.guiY + dy);
   }

   public int getGuiX() {
      return this.guiX;
   }

   public int getGuiY() {
      return this.guiY;
   }

   public int getRelativeX() {
      return this.parent == null ? this.guiX : this.guiX - this.parent.guiX;
   }

   public int getRelativeY() {
      return this.parent == null ? this.guiY : this.guiY - this.parent.guiY;
   }

   public void setSize(int width, int height) {
      this.properWidth = width;
      this.properHeight = height;
      this.contentWidth = width;
      this.contentHeight = height;
      this.updateSize();
   }

   public Component addChild(Component child) {
      this.doAddChild(null, child, null);
      return child;
   }

   public Component addChildBehind(Component behind, Component child) {
      this.doAddChild(null, child, behind);
      return child;
   }

   public void doAddChild(Component inFrontOf, Component child, Component behind) {
      if (child != null && !this.children.contains(child) && this.parent != child) {
         int i = this.children.indexOf(inFrontOf);
         if (i == -1) {
            int j = this.children.indexOf(behind);
            if (j == -1) {
               this.children.add(child);
            } else {
               this.children.add(j, child);
            }
         } else {
            this.children.add(i + 1, child);
         }

         child.parent = this;
         child.setGuiCoords(this.guiX, this.guiY);
         if (Minecraft.getInstance() != null) {
            child.init(this.width, this.height);
         }

         this.updateSize();
      }
   }

   public Component removeChild(Component child) {
      if (child != null && this.children.contains(child)) {
         child.parent = null;
         this.children.remove(child);
         this.updateSize();
         this.onChildClosed(child);
      }

      return child;
   }

   public void removeAllChildren() {
      this.children.clear();
      this.updateSize();
   }

   public Component getParent() {
      return this.parent;
   }

   public List<Component> getChildren() {
      return this.children;
   }

   public boolean iterateInput(Predicate<Component> callMethod) {
      ListIterator<Component> iter = this.children.listIterator(this.children.size());

      while (iter.hasPrevious()) {
         Component child = iter.previous();
         if (callMethod.test(child)) {
            return true;
         }
      }

      return false;
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
      return !this.iterateInput(c -> c.mouseClicked(event, bl)) ? super.mouseClicked(event, bl) : true;
   }

   public boolean mouseReleased(MouseButtonEvent event) {
      return !this.iterateInput(c -> c.mouseReleased(event)) ? super.mouseReleased(event) : true;
   }

   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      return !this.iterateInput(c -> c.mouseDragged(event, dx, dy)) ? super.mouseClicked(event, false) : true;
   }

   public boolean mouseScrolled(double mx, double my, double dx, double dy) {
      return !this.iterateInput(c -> c.mouseScrolled(mx, my, dx, dy)) ? super.mouseScrolled(mx, my, dx, dy) : true;
   }

   public void mouseMoved(double mx, double my) {
      if (!this.iterateInput(c -> {
         c.mouseMoved(mx, my);
         return false;
      })) {
         super.mouseMoved(mx, my);
      }
   }

   public boolean keyPressed(KeyEvent event) {
      return !this.iterateInput(cpt -> cpt.keyPressed(event)) ? super.keyPressed(event) : true;
   }

   public boolean charTyped(CharacterEvent event) {
      return !this.iterateInput(cpt -> cpt.charTyped(event)) ? super.charTyped(event) : true;
   }

   public boolean keyReleased(KeyEvent event) {
      return !this.iterateInput(cpt -> cpt.keyReleased(event)) ? super.keyReleased(event) : true;
   }

   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      for (Component child : this.children) {
         if (!child.isClipped) {
            child.extractRenderState(graphics, mouseX, mouseY, partialTick);
         }
      }
   }

   public void onClose() {
      for (Component child : this.children) {
         child.onClose();
      }

      super.onClose();
   }

   public void tick() {
      for (Component child : this.children) {
         child.tick();
      }

      super.tick();
   }

   public void init() {
      super.init();

      for (Component child : this.children) {
         child.init(this.width, this.height);
      }
   }

   public int getWidth() {
      return this.contentWidth;
   }

   public int getHeight() {
      return this.contentHeight;
   }

   public void setClipped(boolean value) {
      this.isClipped = value;
   }

   public void updateSize() {
      int leftmost = Integer.MAX_VALUE;
      int rightmost = Integer.MIN_VALUE;
      int topmost = Integer.MAX_VALUE;
      int bottommost = Integer.MIN_VALUE;

      for (Component child : this.children) {
         int x = child.getGuiX();
         if (x < leftmost) {
            leftmost = x;
         }

         int childWidth = child.getWidth();
         if (x + childWidth > rightmost) {
            rightmost = x + childWidth;
         }

         int y = child.getGuiY();
         if (y < topmost) {
            topmost = y;
         }

         int childHeight = child.getHeight();
         if (y + childHeight > bottommost) {
            bottommost = y + childHeight;
         }
      }

      this.contentWidth = Math.max(this.properWidth, rightmost - leftmost);
      this.contentHeight = Math.max(this.properHeight, bottommost - topmost);
      if (this.parent != null) {
         this.parent.updateSize();
      }
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return mouseX >= this.getGuiX() && mouseX < this.getGuiX() + this.getWidth() && mouseY >= this.getGuiY() && mouseY < this.getGuiY() + this.getHeight();
   }

   public void closeChild() {
      if (this.parent != null) {
         this.parent.removeChild(this);
      } else {
         Minecraft.getInstance().gui.setScreen(null);
      }
   }

   public void onChildClosed(Component child) {
   }

   public void drawCentered(GuiGraphicsExtractor context, net.minecraft.network.chat.Component text, int y, int color, boolean dropShadow) {
      int length = this.font.width(text);
      context.text(this.font, text, (this.width - length) / 2, y, color, dropShadow);
   }

   public double getMouseX() {
      return Minecraft.getInstance().mouseHandler.xpos() * this.width / Minecraft.getInstance().getWindow().getScreenWidth();
   }

   public double getMouseY() {
      return Minecraft.getInstance().mouseHandler.ypos() * this.height / Minecraft.getInstance().getWindow().getScreenHeight();
   }
}
