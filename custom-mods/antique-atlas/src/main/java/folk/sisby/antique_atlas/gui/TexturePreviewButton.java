package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.gui.core.ToggleButtonComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class TexturePreviewButton<T> extends ToggleButtonComponent {
   public static final Identifier FRAME_SELECTED = AntiqueAtlas.id("textures/gui/frame_selected.png");
   public static final Identifier FRAME_UNSELECTED = AntiqueAtlas.id("textures/gui/frame.png");
   public static final int FRAME_SIZE = 34;
   protected final T value;
   protected final Identifier texture;
   protected final int textureWidth;
   protected final int textureHeight;
   protected final int v;
   protected float[] tint;

   public TexturePreviewButton(T value, net.minecraft.resources.Identifier texture, int textureWidth, int textureHeight, int v, float[] tint) {
      super(false);
      this.value = value;
      this.texture = texture;
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
      this.v = v;
      this.tint = tint;
      this.setSize(34, 34);
   }

   public T getValue() {
      return this.value;
   }

   public void reTint(float[] tint) {
      if (this.tint != null) {
         this.tint = tint;
      }
   }

   protected void drawTexture(GuiGraphicsExtractor context, int x, int y) {
      if (this.tint != null) {
      }

      context.blit(RenderPipelines.GUI_TEXTURED, this.texture, x, y, 0.0F, this.v, this.textureWidth, this.textureHeight, this.textureWidth, this.textureHeight + this.v);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
      folk.sisby.antique_atlas.util.AtlasCanvas canvas = new folk.sisby.antique_atlas.util.GuiAtlasCanvas(context);
      Identifier frameTexture = this.isSelected() ? FRAME_SELECTED : FRAME_UNSELECTED;
      context.blit(RenderPipelines.GUI_TEXTURED, frameTexture, this.getGuiX() + 1, this.getGuiY() + 1, 0.0F, 0.0F, 34, 34, 34, 34);
      int centerX = this.getGuiX() + (34 - this.textureWidth) / 2;
      int centerY = this.getGuiY() + (34 - this.textureHeight) / 2;
      this.drawTexture(context, centerX, centerY);
      super.extractRenderState(context, mouseX, mouseY, partialTick);
   }
}
