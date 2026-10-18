package net.jmb19905.niftycarts.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;

@Environment(EnvType.CLIENT)
public class CargoRenderUtil {
   protected static final Identifier BACK_SPRITE_LOCATION = Identifier.withDefaultNamespace("back");

   public static void renderPainting(
      Context ctx, PaintingVariant painting, PoseStack stack, RenderType renderType, SubmitNodeCollector collector, int packedLight
   ) {
      collector.submitCustomGeometry(stack, renderType, (pose, buf) -> {
         TextureAtlas atlas = ctx.getAtlas(AtlasIds.PAINTINGS);
         int width = painting.width() * 16;
         int height = painting.height() * 16;
         TextureAtlasSprite art = atlas.getSprite(painting.assetId());
         TextureAtlasSprite back = atlas.getSprite(BACK_SPRITE_LOCATION);
         int blockWidth = width / 16;
         int blockHeight = height / 16;
         float offsetX = -blockWidth / 2.0F;
         float offsetY = -blockHeight / 2.0F;
         float depth = 0.03125F;
         float bu0 = back.getU0();
         float bu1 = back.getU1();
         float bv0 = back.getV0();
         float bv1 = back.getV1();
         float bup = back.getU(0.0625F);
         float bvp = back.getV(0.0625F);
         float uvX = 1.0F / blockWidth;
         float uvY = 1.0F / blockHeight;

         for (int x = 0; x < blockWidth; x++) {
            for (int y = 0; y < blockHeight; y++) {
               float x1 = offsetX + (x + 1);
               float x0 = offsetX + x;
               float y1 = offsetY + (y + 1);
               float y0 = offsetY + y;
               float u0 = art.getU(uvX * (blockWidth - x));
               float u1 = art.getU(uvX * (blockWidth - x - 1));
               float v0 = art.getV(uvY * (blockHeight - y));
               float v1 = art.getV(uvY * (blockHeight - y - 1));
               vert(pose, buf, x1, y0, u1, v0, -0.03125F, 0, 0, -1, packedLight);
               vert(pose, buf, x0, y0, u0, v0, -0.03125F, 0, 0, -1, packedLight);
               vert(pose, buf, x0, y1, u0, v1, -0.03125F, 0, 0, -1, packedLight);
               vert(pose, buf, x1, y1, u1, v1, -0.03125F, 0, 0, -1, packedLight);
               vert(pose, buf, x1, y1, bu0, bv0, 0.03125F, 0, 0, 1, packedLight);
               vert(pose, buf, x0, y1, bu1, bv0, 0.03125F, 0, 0, 1, packedLight);
               vert(pose, buf, x0, y0, bu1, bv1, 0.03125F, 0, 0, 1, packedLight);
               vert(pose, buf, x1, y0, bu0, bv1, 0.03125F, 0, 0, 1, packedLight);
               vert(pose, buf, x1, y1, bu0, bv0, -0.03125F, 0, 1, 0, packedLight);
               vert(pose, buf, x0, y1, bu1, bv0, -0.03125F, 0, 1, 0, packedLight);
               vert(pose, buf, x0, y1, bu1, bvp, 0.03125F, 0, 1, 0, packedLight);
               vert(pose, buf, x1, y1, bu0, bvp, 0.03125F, 0, 1, 0, packedLight);
               vert(pose, buf, x1, y0, bu0, bv0, 0.03125F, 0, -1, 0, packedLight);
               vert(pose, buf, x0, y0, bu1, bv0, 0.03125F, 0, -1, 0, packedLight);
               vert(pose, buf, x0, y0, bu1, bvp, -0.03125F, 0, -1, 0, packedLight);
               vert(pose, buf, x1, y0, bu0, bvp, -0.03125F, 0, -1, 0, packedLight);
               vert(pose, buf, x1, y1, bup, bv0, 0.03125F, -1, 0, 0, packedLight);
               vert(pose, buf, x1, y0, bup, bv1, 0.03125F, -1, 0, 0, packedLight);
               vert(pose, buf, x1, y0, bu0, bv1, -0.03125F, -1, 0, 0, packedLight);
               vert(pose, buf, x1, y1, bu0, bv0, -0.03125F, -1, 0, 0, packedLight);
               vert(pose, buf, x0, y1, bup, bv0, -0.03125F, 1, 0, 0, packedLight);
               vert(pose, buf, x0, y0, bup, bv1, -0.03125F, 1, 0, 0, packedLight);
               vert(pose, buf, x0, y0, bu0, bv1, 0.03125F, 1, 0, 0, packedLight);
               vert(pose, buf, x0, y1, bu0, bv0, 0.03125F, 1, 0, 0, packedLight);
            }
         }
      });
   }

   private static void vert(Pose pose, VertexConsumer buf, float x, float y, float u, float v, float z, int nx, int ny, int nz, int packedLight) {
      buf.addVertex(pose, x, y, z)
         .setColor(-1)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(pose, nx, ny, nz);
   }
}
