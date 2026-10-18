package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.util.AtlasCanvas;
import folk.sisby.antique_atlas.util.DrawBatcher;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.ARGB;
import org.joml.Vector2d;

public record MarkerTexture(
   Identifier id,
   Identifier accentId,
   Identifier item,
   int offsetX,
   int offsetY,
   int textureWidth,
   int textureHeight,
   int mipLevels,
   int nearClip,
   int farClip
) {
   public static final MarkerTexture DEFAULT = centered(
      AntiqueAtlas.id("custom/point"), Identifier.fromNamespaceAndPath("minecraft", "emerald"), 32, 32, 0, 1, Integer.MAX_VALUE, true
   );

   public static Identifier idToTexture(Identifier id) {
      return id.withPrefix("textures/atlas/marker/").withSuffix(".png");
   }

   public static MarkerTexture ofId(
      Identifier id, Identifier item, int offsetX, int offsetY, int width, int height, int mipLevels, int nearClip, int farClip, boolean accent
   ) {
      return new MarkerTexture(
         idToTexture(id), accent ? idToTexture(id.withSuffix("_accent")) : null, item, offsetX, offsetY, width, height, mipLevels, nearClip, farClip
      );
   }

   public static MarkerTexture centered(
      Identifier id, Identifier item, int width, int height, int mipLevels, int nearClip, int farClip, boolean accent
   ) {
      return ofId(id, item, -width / 2, -height / 2, width, height, mipLevels, nearClip, farClip, accent);
   }

   public Identifier keyId() {
      return Identifier.fromNamespaceAndPath(
         this.id.getNamespace(), this.id.getPath().substring("textures/atlas/marker/".length(), this.id.getPath().length() - 4)
      );
   }

   public String displayId() {
      return this.id.getNamespace().equals("antique_atlas") ? this.keyId().getPath() : this.keyId().toString();
   }

   public int fullTextureWidth() {
      int width = this.textureWidth;

      for (int i = 0; i < this.mipLevels; i++) {
         width += this.textureWidth >> i + 1;
      }

      return width;
   }

   public int getU(int mipLevel) {
      int currentMipLevel = mipLevel - 1;

      int u;
      for (u = 0; currentMipLevel >= 0; currentMipLevel--) {
         u += this.textureWidth / (1 << currentMipLevel);
      }

      return u;
   }

   public Vector2d getCenter(int tileChunks) {
      int mipLevel = Mth.clamp(Mth.ceillog2(tileChunks), 0, this.mipLevels);
      return new Vector2d((this.offsetX + this.textureWidth / 2.0) / (1 << mipLevel), (this.offsetY + this.textureHeight / 2.0) / (1 << mipLevel));
   }

   public double getSquaredSize(int tileChunks) {
      int mipLevel = Mth.clamp(Mth.ceillog2(tileChunks), 0, this.mipLevels);
      return (double)(this.textureWidth * this.textureHeight) / (1 << mipLevel);
   }

   public void drawIcon(GuiGraphicsExtractor context, int x, int y, float[] accent) {
      context.blit(RenderPipelines.GUI_TEXTURED, this.id, x, y, 0.0F, 0.0F, this.textureWidth, this.textureHeight, this.fullTextureWidth(), this.textureHeight);
      if (this.accentId != null && accent != null) {
                  context.blit(RenderPipelines.GUI_TEXTURED, this.accentId, x, y, 0.0F, 0.0F, this.textureWidth, this.textureHeight, this.fullTextureWidth(), this.textureHeight, ARGB.color(255, (int) (accent[0] * 255.0F), (int) (accent[1] * 255.0F), (int) (accent[2] * 255.0F)));
               }
   }

   public void draw(AtlasCanvas canvas,
      double markerX,
      double markerY,
      float z,
      float markerScale,
      int tileChunks,
      float[] accent,
      float tint,
      float alpha,
      int light
   ) {
      if (alpha != 0.0F) {
         canvas.pushPose();
         canvas.translate(markerX, markerY, 0.0);
         canvas.scale(markerScale, markerScale, 1.0F);
         int mainArgb = ARGB.color((int)(alpha * 255.0F), (int)(tint * 255.0F), (int)(tint * 255.0F), (int)(tint * 255.0F));
         int accentArgb = accent != null
            ? ARGB.color((int)(alpha * 255.0F), (int)(tint * accent[0] * 255.0F), (int)(tint * accent[1] * 255.0F), (int)(tint * accent[2] * 255.0F))
            : 0;
         if (tileChunks > 1 && this.mipLevels > 0) {
            int mipLevel = Mth.clamp(Mth.ceillog2(tileChunks), 0, this.mipLevels);
            DrawBatcher.drawSingle(canvas,
               this.id,
               this.fullTextureWidth(),
               this.textureHeight,
               light,
               this.offsetX / (1 << mipLevel),
               this.offsetY / (1 << mipLevel),
               z,
               this.textureWidth / (1 << mipLevel),
               this.textureHeight / (1 << mipLevel),
               this.getU(mipLevel),
               0,
               this.textureWidth / (1 << mipLevel),
               this.textureHeight / (1 << mipLevel),
               mainArgb,
               false
            );
            if (this.accentId != null && accent != null) {
               DrawBatcher.drawSingle(canvas,
                  this.accentId,
                  this.fullTextureWidth(),
                  this.textureHeight,
                  light,
                  this.offsetX / (1 << mipLevel),
                  this.offsetY / (1 << mipLevel),
                  z,
                  this.textureWidth / (1 << mipLevel),
                  this.textureHeight / (1 << mipLevel),
                  this.getU(mipLevel),
                  0,
                  this.textureWidth / (1 << mipLevel),
                  this.textureHeight / (1 << mipLevel),
                  accentArgb,
                  false
               );
            }
         } else {
            DrawBatcher.drawSingle(canvas,
               this.id,
               this.fullTextureWidth(),
               this.textureHeight,
               light,
               this.offsetX,
               this.offsetY,
               z,
               this.textureWidth,
               this.textureHeight,
               0,
               0,
               this.textureWidth,
               this.textureHeight,
               mainArgb,
               false
            );
            if (this.accentId != null && accent != null) {
               DrawBatcher.drawSingle(canvas,
                  this.accentId,
                  this.fullTextureWidth(),
                  this.textureHeight,
                  light,
                  this.offsetX,
                  this.offsetY,
                  z,
                  this.textureWidth,
                  this.textureHeight,
                  0,
                  0,
                  this.textureWidth,
                  this.textureHeight,
                  accentArgb,
                  false
               );
            }
         }

         canvas.popPose();
      }
   }
}
