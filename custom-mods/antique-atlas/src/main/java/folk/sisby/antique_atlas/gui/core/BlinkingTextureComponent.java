package folk.sisby.antique_atlas.gui.core;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class BlinkingTextureComponent extends Component {
   protected Identifier texture;
   protected final long blinkTime;
   protected final float visibleAlpha;
   protected final float invisibleAlpha;
   protected long lastTickTime;
   protected boolean isVisible;

   public BlinkingTextureComponent(long blinkTime, float visibleAlpha, float invisibleAlpha) {
      this.blinkTime = blinkTime;
      this.visibleAlpha = visibleAlpha;
      this.invisibleAlpha = invisibleAlpha;
   }

   public BlinkingTextureComponent() {
      this(500L, 1.0F, 0.25F);
   }

   public void setTexture(Identifier texture, int width, int height) {
      this.texture = texture;
      this.setSize(width, height);
      this.lastTickTime = 0L;
      this.isVisible = false;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
      folk.sisby.antique_atlas.util.AtlasCanvas canvas = new folk.sisby.antique_atlas.util.GuiAtlasCanvas(context);
      long currentTime = System.currentTimeMillis();
      if (this.lastTickTime + this.blinkTime < currentTime) {
         this.lastTickTime = currentTime;
         this.isVisible = !this.isVisible;
      }
      this.drawImage(context);
   }

   protected void drawImage(GuiGraphicsExtractor context) {
      context.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getGuiX(), this.getGuiY(), 0.0F, 0.0F, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
   }
}
