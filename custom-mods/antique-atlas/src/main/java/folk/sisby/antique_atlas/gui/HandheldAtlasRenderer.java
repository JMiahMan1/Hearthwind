package folk.sisby.antique_atlas.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.WorldAtlasData;
import folk.sisby.antique_atlas.util.AtlasCanvas;
import folk.sisby.antique_atlas.util.DrawBatcher;
import folk.sisby.antique_atlas.util.WorldAtlasCanvas;
import folk.sisby.antique_atlas.util.MathUtil;
import folk.sisby.surveyor.client.SurveyorClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2d;

public record HandheldAtlasRenderer(
   int bookX,
   int bookY,
   int bookWidth,
   int bookHeight,
   int mapWidth,
   int mapHeight,
   int tilePixels,
   int tileChunks,
   double guiScale,
   double mapOffsetX,
   double mapOffsetY,
   int mapScale,
   Player player,
   WorldAtlasData worldAtlasData,
   net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim
) implements AtlasRenderer {
   public static HandheldAtlasRenderer fromContext(Player player) {
      return new HandheldAtlasRenderer(
         0,
         0,
         310,
         218,
         276,
         196,
         16,
         1,
         1.0,
         -player.getBlockX(),
         -player.getBlockZ(),
         1,
         player,
         WorldAtlasData.getOrCreate(player.level().dimension()),
         player.level().dimension()
      );
   }

   public void renderHandheldAtlas(PoseStack matrices, SubmitNodeCollector collector, int light) {
      AtlasCanvas canvas = new WorldAtlasCanvas(matrices, collector);
      matrices.pushPose();
      matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
      matrices.mulPose(Axis.ZP.rotationDegrees(180.0F));
      matrices.scale(0.24752294F, 0.24752294F, 0.38F);
      matrices.translate(-1.2, -0.88, 0.0);
      matrices.scale(0.0078125F, 0.0078125F, 0.0078125F);
      DrawBatcher.drawSingle(canvas,
         AtlasScreen.BOOK,
         this.bookWidth,
         this.bookHeight,
         light,
         this.bookX,
         this.bookY,
         0.01F,
         this.bookWidth,
         this.bookHeight,
         0,
         0,
         this.bookWidth,
         this.bookHeight,
         -1,
         false
      );
      if (!(Minecraft.getInstance().gui.screen() instanceof AtlasScreen)) {
         this.renderTiles(canvas, light);
         overlays.keySet()
            .forEach(
               id -> ((AtlasOverlay)overlays.get(id))
                  .onRender(new AtlasOverlay.AtlasRenderContext(this, canvas, null, null, light, 1.0F, AntiqueAtlas.getOrderedFriends()))
            );
         Rect2i mapArea = new Rect2i(this.bookX + 17, this.bookY + 11, this.mapWidth, this.mapHeight);
         this.worldAtlasData
            .getAllMarkers(this.tileChunks)
            .forEach(
               (landmark, texture) -> this.renderMarker(canvas,
                  landmark,
                  texture,
                  -0.02F,
                  light,
                  (x, y) -> (float)Mth.clamp(MathUtil.innerDistanceToEdge(mapArea, new Vector2d(x, y)) / 32.0, 0.0, 1.0),
                  false,
                  false,
                  1.0F
               )
            );
         AntiqueAtlas.getOrderedFriends()
            .forEach(
               (uuid, friend) -> this.renderPlayer(canvas, -0.04F, light, friend, 1.0F, 1.0F, false, uuid.equals(SurveyorClient.getClientUuid())
               )
            );
         DrawBatcher.drawSingle(canvas,
            BOOK_FRAME,
            this.bookWidth,
            this.bookHeight,
            light,
            this.bookX,
            this.bookY,
            -0.03F,
            this.bookWidth,
            this.bookHeight,
            0,
            0,
            this.bookWidth,
            this.bookHeight,
            -1,
            true
         );
      }

      matrices.popPose();
   }

   @Override
   public double getPixelsPerBlock() {
      return 1.0;
   }
}
