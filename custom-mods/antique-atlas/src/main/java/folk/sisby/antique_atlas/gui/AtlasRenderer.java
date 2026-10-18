package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.TileTexture;
import folk.sisby.antique_atlas.WorldAtlasData;
import folk.sisby.antique_atlas.gui.core.ScreenState;
import folk.sisby.antique_atlas.gui.tiles.SubTile;
import folk.sisby.antique_atlas.gui.tiles.SubTileQuartet;
import folk.sisby.antique_atlas.gui.tiles.TileRenderIterator;
import folk.sisby.antique_atlas.util.ColorUtil;
import folk.sisby.antique_atlas.util.AtlasCanvas;
import folk.sisby.antique_atlas.util.DrawBatcher;
import folk.sisby.antique_atlas.util.DrawUtil;
import folk.sisby.antique_atlas.util.MathUtil;
import folk.sisby.antique_atlas.util.Rect;
import folk.sisby.surveyor.PlayerSummary;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import folk.sisby.surveyor.util.RegionPos;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public interface AtlasRenderer {
   Map<net.minecraft.resources.Identifier, AtlasOverlay> overlays = new HashMap<>();
   Identifier BOOK = AntiqueAtlas.id("textures/gui/book.png");
   Identifier BOOK_FULLSCREEN = AntiqueAtlas.id("book_fullscreen");
   Identifier BOOK_FULLSCREEN_M = AntiqueAtlas.id("middle/book_fullscreen_m");
   Identifier BOOK_FULLSCREEN_R = AntiqueAtlas.id("book_fullscreen_r");
   Identifier BOOK_FRAME = AntiqueAtlas.id("textures/gui/book_frame.png");
   Identifier BOOK_FRAME_FULLSCREEN = AntiqueAtlas.id("book_frame_fullscreen");
   Identifier BOOK_FRAME_FULLSCREEN_M = AntiqueAtlas.id("middle/book_frame_fullscreen_m");
   Identifier BOOK_FRAME_FULLSCREEN_R = AntiqueAtlas.id("book_frame_fullscreen_r");
   Identifier BOOK_FRAME_NARROW = AntiqueAtlas.id("textures/gui/book_frame_narrow.png");
   Identifier BOOK_FRAME_NARROW_FULLSCREEN = AntiqueAtlas.id("book_frame_narrow_fullscreen");
   Identifier BOOK_FRAME_NARROW_FULLSCREEN_M = AntiqueAtlas.id("middle/book_frame_narrow_fullscreen_m");
   Identifier BOOK_FRAME_NARROW_FULLSCREEN_R = AntiqueAtlas.id("book_frame_narrow_fullscreen_r");
   Identifier PLAYER = AntiqueAtlas.id("textures/gui/player.png");
   Identifier ERASER = AntiqueAtlas.id("textures/gui/eraser.png");
   Identifier ICON_ADD_MARKER = AntiqueAtlas.id("textures/gui/icons/add_marker.png");
   Identifier ICON_DELETE_MARKER = AntiqueAtlas.id("textures/gui/icons/del_marker.png");
   Identifier ICON_SHOW_MARKERS = AntiqueAtlas.id("textures/gui/icons/show_markers.png");
   Identifier ICON_HIDE_MARKERS = AntiqueAtlas.id("textures/gui/icons/hide_markers.png");
   Identifier ICON_UNKNOWN = AntiqueAtlas.id("textures/gui/icons/unknown.png");
   Component TEXT_ADD_MARKER = Component.translatable("gui.antique_atlas.addMarker");
   Component TEXT_ADD_MARKER_HERE = Component.translatable("gui.antique_atlas.addMarkerHere");
   int DEFAULT_BOOK_WIDTH = 310;
   int DEFAULT_BOOK_HEIGHT = 218;
   int MAP_BORDER_WIDTH = 17;
   int MAP_BORDER_HEIGHT = 11;
   float PLAYER_ROTATION_STEPS = 16.0F;
   int PLAYER_ICON_WIDTH = 7;
   int PLAYER_ICON_HEIGHT = 8;
   int BOOKMARK_SPACING = 2;
   int MARKER_SIZE = 32;
   int NAVIGATE_STEP = 24;
   int MAX_LIGHT = 15728880;
   ScreenState.State<AtlasScreen> NORMAL = new ScreenState.ToggleState<>();
   ScreenState.State<AtlasScreen> PLACING_MARKER = new ScreenState.ToggleState<>(s -> s.addMarkerBookmark);
   ScreenState.State<AtlasScreen> DELETING_MARKER = new ScreenState.ToggleState<>(
      s -> s.deleteMarkerBookmark, s -> s.addChild(s.eraser), s -> s.removeChild(s.eraser)
   );
   ScreenState.State<AtlasScreen> HIDING_MARKERS = new ScreenState.ToggleState<>(s -> s.markerVisibilityBookmark, s -> {
      s.markerVisibilityBookmark.setTitle(Component.translatable("gui.antique_atlas.showMarkers"));
      s.markerVisibilityBookmark.setIconTexture(ICON_SHOW_MARKERS);
   }, s -> {
      s.clearTargetBookmarks(s.playerBookmark);
      s.markerVisibilityBookmark.setTitle(Component.translatable("gui.antique_atlas.hideMarkers"));
      s.markerVisibilityBookmark.setIconTexture(ICON_HIDE_MARKERS);
   });

   static void registerOverlay(Identifier id, AtlasOverlay overlay) {
      overlays.put(id, overlay);
   }

   int bookX();

   int bookY();

   int bookWidth();

   int bookHeight();

   int mapWidth();

   int mapHeight();

   double mapOffsetX();

   double mapOffsetY();

   int tilePixels();

   int tileChunks();

   int mapScale();

   Player player();

   WorldAtlasData worldAtlasData();

   double getPixelsPerBlock();

   double guiScale();

   net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim();

   default int screenXToWorldX(double screenX) {
      return screenXToWorldX(screenX, this.bookX(), this.mapOffsetX(), this.mapWidth(), this.getPixelsPerBlock());
   }

   default int screenYToWorldZ(double screenY) {
      return screenYToWorldZ(screenY, this.bookY(), this.mapOffsetY(), this.mapHeight(), this.getPixelsPerBlock());
   }

   default double worldXToScreenX(double x) {
      return worldXToScreenX(x, this.bookX(), this.mapOffsetX(), this.mapWidth(), this.getPixelsPerBlock());
   }

   default double worldZToScreenY(double z) {
      return worldZToScreenY(z, this.bookY(), this.mapOffsetY(), this.mapHeight(), this.getPixelsPerBlock());
   }

   static int screenXToWorldX(double screenX, int bookX, double mapOffsetX, int mapWidth, double pixelsPerBlock) {
      double mapX = (int)Math.round(screenX - bookX - 17.0);
      return (int)Math.round((mapX - mapWidth / 2.0F - mapOffsetX) / pixelsPerBlock);
   }

   static int screenYToWorldZ(double screenY, int bookY, double mapOffsetY, int mapHeight, double pixelsPerBlock) {
      double mapY = (int)Math.round(screenY - bookY - 11.0);
      return (int)Math.round((mapY - mapHeight / 2.0F - mapOffsetY) / pixelsPerBlock);
   }

   static double worldXToScreenX(double x, int bookX, double mapOffsetX, int mapWidth, double pixelsPerBlock) {
      double mapX = x * pixelsPerBlock + mapOffsetX + mapWidth / 2.0F;
      return mapX + bookX + 17.0;
   }

   static double worldZToScreenY(double z, int bookY, double mapOffsetY, int mapHeight, double pixelsPerBlock) {
      double mapY = z * pixelsPerBlock + mapOffsetY + mapHeight / 2.0F;
      return mapY + bookY + 11.0;
   }

   default void renderMarker(
      AtlasCanvas canvas,
      Landmark landmark,
      MarkerTexture texture,
      float z,
      int light,
      BiFunction<Double, Double, Float> alphaGetter,
      boolean pinned,
      boolean hovering,
      float markerScale
   ) {
      BlockPos pos = (BlockPos)landmark.get(LandmarkComponentTypes.POS);
      Integer color = (Integer)landmark.get(LandmarkComponentTypes.COLOR);
      float[] accent = color == null ? null : ColorUtil.componentsFromRgb(color);
      float tint = hovering ? 0.8F : 1.0F;
      if (pos == null) {
         Set<net.minecraft.world.level.ChunkPos> chunks = RegionPos.regionsToChunks(
            (Map)landmark.getOrDefault(LandmarkComponentTypes.CHUNKS, new HashMap())
         );

         for (ChunkPos chunk : chunks) {
            double markerX = this.worldXToScreenX(chunk.getMinBlockX()) - this.bookX();
            double markerY = this.worldZToScreenY(chunk.getMinBlockZ()) - this.bookY();
            float effectiveScale = (float)(this.mapScale() / this.guiScale());
            canvas.pushPose();
            canvas.translate(markerX, markerY, 0.0);
            canvas.scale(effectiveScale, effectiveScale, 1.0F);
            int size = this.tilePixels() / this.tileChunks();
            int lineSize = this.tilePixels() / 16;
            if (size > 0) {
               float[] fillColor = accent == null ? ColorUtil.componentsFromRgb(16777215) : new float[]{tint * accent[0], tint * accent[1], tint * accent[2]};
               float alpha = alphaGetter.apply(markerX, markerY);
               DrawUtil.fill(canvas, z, light, 0, 0, size, size, 0.25F * alpha, fillColor);
               if (lineSize > 0) {
                  if (!chunks.contains(new ChunkPos(chunk.x() - 1, chunk.z()))) {
                     DrawUtil.fill(canvas, z, light, 0, 0, lineSize, size, 0.5F * alpha, fillColor);
                  }

                  if (!chunks.contains(new ChunkPos(chunk.x(), chunk.z() - 1))) {
                     DrawUtil.fill(canvas, z, light, 0, 0, size, lineSize, 0.5F * alpha, fillColor);
                  }

                  if (!chunks.contains(new ChunkPos(chunk.x() + 1, chunk.z()))) {
                     DrawUtil.fill(canvas, z, light, size - lineSize, 0, size, size, 0.5F * alpha, fillColor);
                  }

                  if (!chunks.contains(new ChunkPos(chunk.x(), chunk.z() + 1))) {
                     DrawUtil.fill(canvas, z, light, 0, size - lineSize, size, size, 0.5F * alpha, fillColor);
                  }
               }
            }

            canvas.popPose();
         }
      } else {
         double markerX = this.worldXToScreenX(pos.getX()) - this.bookX();
         double markerY = this.worldZToScreenY(pos.getZ()) - this.bookY();
         if (pinned) {
            markerX = Mth.clamp(markerX, 17.0, this.mapWidth() + 17);
            markerY = Mth.clamp(markerY, 11.0, this.mapHeight() + 11);
         }

         texture.draw(canvas, markerX, markerY, z, markerScale, this.tileChunks(), accent, tint, alphaGetter.apply(markerX, markerY), light);
      }
   }

   default void renderPlayer(
      AtlasCanvas canvas,
      float z,
      int light,
      PlayerSummary player,
      float iconScale,
      float alpha,
      boolean hovering,
      boolean self
   ) {
      double dimX = player.pos().x();
      double dimZ = player.pos().z();
      boolean inDim = this.dim().equals(player.dimension());
      if (!inDim) {
         Map<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>, Integer> scales = AntiqueAtlas.CONFIG
            .dimensions
            .getScales(Minecraft.getInstance().getConnection());
         int newScale = scales.getOrDefault(this.dim(), 0);
         int oldScale = scales.getOrDefault(player.dimension(), 0);
         if (newScale * oldScale == 0) {
            return;
         }

         double mult = (double)newScale / oldScale;
         dimX = mult * dimX;
         dimZ = mult * dimZ;
      }

      double playerOffsetX = this.worldXToScreenX(dimX) - this.bookX();
      double playerOffsetY = this.worldZToScreenY(dimZ) - this.bookY();
      playerOffsetX = Mth.clamp(playerOffsetX, 17.0, this.mapWidth() + 17);
      playerOffsetY = Mth.clamp(playerOffsetY, 11.0, this.mapHeight() + 11);
      float tint = (player.online() ? 1.0F : 0.5F) * (hovering ? 0.9F : 1.0F);
      float greenTint = self ? 1.0F : 0.7F;
      float redTint = inDim ? 1.0F : 0.7F;
      int argb = ARGB.color((int)(alpha * 255.0), (int)(tint * redTint * 255.0F), (int)(tint * greenTint * 255.0F), (int)(tint * 255.0F));
      float playerRotation = Math.round(player.yaw() / 360.0F * 16.0F) / 16.0F * 360.0F;
      DrawUtil.drawCenteredWithRotation(canvas, PLAYER, playerOffsetX, playerOffsetY, z, iconScale, 7, 8, playerRotation, light, argb);
   }

   default void renderTiles(AtlasCanvas canvas, int light) {
      int mapStartChunkX = MathUtil.roundToBase(this.screenXToWorldX(this.bookX()) >> 4, this.tileChunks()) - 2 * this.tileChunks();
      int mapStartChunkZ = MathUtil.roundToBase(this.screenYToWorldZ(this.bookY()) >> 4, this.tileChunks()) - 2 * this.tileChunks();
      int mapEndChunkX = MathUtil.roundToBase(this.screenXToWorldX(this.bookX() + this.bookWidth()) >> 4, this.tileChunks()) + 2 * this.tileChunks();
      int mapEndChunkZ = MathUtil.roundToBase(this.screenYToWorldZ(this.bookY() + this.bookHeight()) >> 4, this.tileChunks()) + 2 * this.tileChunks();
      double mapStartScreenX = this.worldXToScreenX(mapStartChunkX << 4);
      double mapStartScreenY = this.worldZToScreenY(mapStartChunkZ << 4);
      TileRenderIterator tiles = new TileRenderIterator(this.worldAtlasData());
      tiles.setScope(new Rect(mapStartChunkX, mapStartChunkZ, mapEndChunkX, mapEndChunkZ));
      tiles.setStep(this.tileChunks());
      int mapX = this.bookX() + 17;
      int mapY = this.bookY() + 11;
      float effectiveScale = (float)(this.mapScale() / this.guiScale());
      canvas.pushPose();
      canvas.translate((float)Math.round(mapStartScreenX), (float)Math.round(mapStartScreenY), 0.0F);
      canvas.scale(effectiveScale, effectiveScale, 1.0F);
      Map<TileTexture, Collection<SubTile>> tileTextures = new Reference2ObjectArrayMap();

      for (SubTileQuartet subTiles : tiles) {
         for (SubTile subtile : subTiles) {
            if (subtile != null && subtile.texture != null) {
               tileTextures.computeIfAbsent(subtile.texture, k -> new ArrayList<>()).add(subtile.copy());
            }
         }
      }

      int subTilePixels = this.tilePixels() / 2;
      tileTextures.forEach(
         (texture, subtiles) -> {
            try (DrawBatcher batcher = new DrawBatcher(canvas, texture.id(), 32, 48, light, true)) {
               for (SubTile subtilex : subtiles) {
                  int drawX = subtilex.x * subTilePixels;
                  int drawY = subtilex.y * subTilePixels;
                  if (!(drawX * effectiveScale > mapX + this.mapWidth() - mapStartScreenX)
                     && !(drawY * effectiveScale > mapY + this.mapHeight() - mapStartScreenY)
                     && !((drawX + subTilePixels) * effectiveScale < mapX - mapStartScreenX)
                     && !((drawY + subTilePixels) * effectiveScale < mapY - mapStartScreenY)) {
                     batcher.add(drawX, drawY, 0.0F, subTilePixels, subTilePixels, subtilex.getTextureU() * 8, subtilex.getTextureV() * 8, 8, 8, -1);
                  }
               }
            }
         }
      );
      canvas.popPose();
   }
}
