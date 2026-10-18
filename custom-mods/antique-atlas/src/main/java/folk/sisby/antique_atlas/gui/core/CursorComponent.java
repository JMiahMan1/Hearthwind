package folk.sisby.antique_atlas.gui.core;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class CursorComponent extends Component {
   protected Identifier texture;
   protected int textureWidth;
   protected int textureHeight;
   protected int pointX;
   protected int pointY;

   public void setTexture(Identifier texture, int width, int height, int pointX, int pointY) {
      this.texture = texture;
      this.textureWidth = width;
      this.textureHeight = height;
      this.pointX = pointX;
      this.pointY = pointY;
   }

   @Override
   public int getWidth() {
      return 0;
   }

   @Override
   public int getHeight() {
      return 0;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
      folk.sisby.antique_atlas.util.AtlasCanvas canvas = new folk.sisby.antique_atlas.util.GuiAtlasCanvas(context);
      context.blit(RenderPipelines.GUI_TEXTURED, 
         this.texture, mouseX - this.pointX, mouseY - this.pointY, 0.0F, 0.0F, this.textureWidth, this.textureHeight, this.textureWidth, this.textureHeight
      );
   }
}
