package folk.sisby.antique_atlas.gui.core;

import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.input.MouseButtonEvent;

public class ScrollBoxComponent extends Component {
   public static final Identifier ARROW = AntiqueAtlas.id("textures/gui/arrow.png");
   public static final int ARROW_SIZE = 16;
   public static final int ARROW_TEXTURE_WIDTH = 32;
   public static final int ARROW_TEXTURE_HEIGHT = 64;
   protected final int scrollStep;
   protected final boolean vertical;
   protected final ViewportComponent viewport;
   protected int scrollPos = 0;

   public ScrollBoxComponent(boolean vertical, int scrollStep) {
      this.vertical = vertical;
      this.scrollStep = scrollStep;
      this.viewport = new ViewportComponent();
      this.addChild(this.viewport);
   }

   public void renderArrow(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean prev) {
      int x = !this.vertical ? (prev ? this.getGuiX() - 16 : this.getGuiX() + this.getWidth()) : this.getGuiX() + (this.getWidth() - 16) / 2;
      int y = this.vertical ? (prev ? this.getGuiY() - 16 : this.getGuiY() + this.getHeight()) : this.getGuiY() + (this.getHeight() - 16) / 2;
      boolean hovered = new Rect2i(x, y, 16, 16).contains(mouseX, mouseY);
      int u = prev ? 0 : 16;
      int v = (this.vertical ? 0 : 16) + (hovered ? 32 : 0);
      context.blit(RenderPipelines.GUI_TEXTURED, ARROW, x, y, u, v, 16, 16, 32, 64);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
      folk.sisby.antique_atlas.util.AtlasCanvas canvas = new folk.sisby.antique_atlas.util.GuiAtlasCanvas(context);
      super.extractRenderState(context, mouseX, mouseY, partialTick);
      if (this.scrollPos > 0) {
         this.renderArrow(context, mouseX, mouseY, true);
      }

      if (this.scrollPos < this.getContentSize() - this.getViewportSize()) {
         this.renderArrow(context, mouseX, mouseY, false);
      }
   }

   public boolean clickArrow(double mouseX, double mouseY, boolean prev) {
      int x = !this.vertical ? (prev ? this.getGuiX() - 16 : this.getGuiX() + this.getWidth()) : this.getGuiX() + (this.getWidth() - 16) / 2;
      int y = this.vertical ? (prev ? this.getGuiY() - 16 : this.getGuiY() + this.getHeight()) : this.getGuiY() + (this.getHeight() - 16) / 2;
      boolean hovered = new Rect2i(x, y, 16, 16).contains((int)mouseX, (int)mouseY);
      if (hovered) {
         int numSteps = (int)Math.round((double)this.getViewportSize() / this.scrollStep);
         this.setScrollPos(this.scrollPos + numSteps * this.scrollStep * (prev ? -1 : 1));
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
      if (this.scrollPos > 0 && this.clickArrow(event.x(), event.y(), true)) {
         return true;
      } else {
         return this.scrollPos < this.getContentSize() - this.getViewportSize() && this.clickArrow(event.x(), event.y(), false)
            ? true
            : super.mouseClicked(event, bl);
      }
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double dx, double dy) {
      if (this.isMouseOver(mx, my) && dy != 0.0) {
         dy = dy > 0.0 ? -1.0 : 1.0;
         this.setScrollPos((int)(this.scrollPos + dy * this.scrollStep));
         return true;
      } else {
         return super.mouseScrolled(mx, my, dx, dy);
      }
   }

   public void setScrollPos(int scrollPos) {
      this.viewport.content.updateSize();
      this.viewport.updateSize();
      this.doSetScrollPos(scrollPos);
   }

   protected void doSetScrollPos(int newPos) {
      this.scrollPos = Math.max(0, Math.min(newPos, this.getContentSize() - this.getViewportSize()));
      this.updateContentPos();
   }

   protected void updateContentPos() {
      this.viewport
         .content
         .setRelativeCoords(
            this.vertical ? this.viewport.content.getRelativeX() : -this.scrollPos, this.vertical ? -this.scrollPos : this.viewport.content.getRelativeY()
         );
   }

   public int getContentSize() {
      return this.vertical ? this.viewport.contentHeight : this.viewport.contentWidth;
   }

   public int getViewportSize() {
      return this.vertical ? this.viewport.getHeight() : this.viewport.getWidth();
   }

   public ViewportComponent getViewport() {
      return this.viewport;
   }
}
