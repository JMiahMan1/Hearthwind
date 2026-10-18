package ht.treechop.client.gui.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public enum Sprite {
   CHOP_INDICATOR(0, 0, 20, 20),
   NO_FELL_INDICATOR(20, 0, 20, 20),
   INDICATOR_ERROR(60, 0, 12, 12),
   INDICATOR_WARNING(72, 0, 12, 12),
   TOGGLE_BUTTON_OFF(0, 20, 32, 20),
   TOGGLE_BUTTON_ON(32, 20, 32, 20),
   HIGHLIGHTED_TOGGLE_BUTTON_OFF(0, 40, 32, 20),
   HIGHLIGHTED_TOGGLE_BUTTON_ON(32, 40, 32, 20),
   LOCKED_TOGGLE_BUTTON_OFF(0, 60, 32, 20),
   LOCKED_TOGGLE_BUTTON_ON(32, 60, 32, 20),
   PAGE_ONE(0, 80, 32, 20),
   HIGHLIGHTED_PAGE_ONE(32, 80, 32, 20),
   PAGE_TWO(0, 100, 32, 20),
   HIGHLIGHTED_PAGE_TWO(32, 100, 32, 20);

   public static final Identifier TEXTURE_PATH = Identifier.fromNamespaceAndPath("treechop", "textures/gui/widgets.png");
   public static final int TEXTURE_WIDTH = 64;
   public static final int TEXTURE_HEIGHT = 120;
   private final int u;
   private final int v;
   public final int width;
   public final int height;

   private Sprite(int u, int v, int width, int height) {
      this.u = u;
      this.v = v;
      this.width = width;
      this.height = height;
   }

   public void blit(GuiGraphicsExtractor gui, int x, int y) {
      this.blit(gui, x, y, this.width, this.height, false);
   }

   public void blit(GuiGraphicsExtractor gui, int x, int y, int width, int height) {
      this.blit(gui, x, y, width, height, false);
   }

   public void blit(GuiGraphicsExtractor gui, int x, int y, int width, int height, boolean mirror) {
      this.blit(gui, net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, x, y, width, height, mirror, 1.0F);
   }

   public void blit(GuiGraphicsExtractor gui, int x, int y, int width, int height, float alpha) {
      this.blit(gui, net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, x, y, width, height, false, alpha);
   }

   public void blit(
      GuiGraphicsExtractor gui,
      com.mojang.blaze3d.pipeline.RenderPipeline pipeline,
      int x,
      int y,
      int width,
      int height,
      boolean mirror,
      float alpha
   ) {
      int color = (int)(alpha * 255.0F) << 24 | 16777215;
      if (mirror) {
         gui.pose().pushMatrix();
         gui.pose().translate((float)(x + width), (float)y);
         gui.pose().scale(-1.0F, 1.0F);
         gui.blit(pipeline, TEXTURE_PATH, 0, 0, (float)this.u, (float)this.v, width, height, 64, 120, color);
         gui.pose().popMatrix();
      } else {
         gui.blit(pipeline, TEXTURE_PATH, x, y, (float)this.u, (float)this.v, width, height, 64, 120, color);
      }
   }
}
