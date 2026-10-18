package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.MarkerTexture;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class MarkerPreviewButton extends TexturePreviewButton<MarkerTexture> {
   public MarkerPreviewButton(MarkerTexture markerTexture, float[] tint) {
      super(markerTexture, markerTexture.id(), markerTexture.textureWidth(), markerTexture.textureHeight(), 0, tint);
   }

   @Override
   protected void drawTexture(GuiGraphicsExtractor context, int x, int y) {
      this.getValue().drawIcon(context, x, y, this.tint);
   }
}
