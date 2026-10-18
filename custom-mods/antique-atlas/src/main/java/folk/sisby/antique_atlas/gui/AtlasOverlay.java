package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.util.AtlasCanvas;
import folk.sisby.antique_atlas.util.GuiAtlasCanvas;
import folk.sisby.surveyor.PlayerSummary;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.Nullable;

public interface AtlasOverlay {
   default void onScreenInit(AtlasScreen screen) {
   }

   default void onScreenRender(AtlasOverlay.AtlasScreenRenderContext context) {
      this.onRender(
         new AtlasOverlay.AtlasRenderContext(
            context.screen(), new GuiAtlasCanvas(context.context()), context.mouseX(), context.mouseY(), 15728880, context.markerScale(), context.friends()
         )
      );
   }

   default void onRender(AtlasOverlay.AtlasRenderContext context) {
   }

   public record AtlasRenderContext(
      AtlasRenderer renderer,
      AtlasCanvas canvas,
      @Nullable Integer mouseX,
      @Nullable Integer mouseY,
      int light,
      float markerScale,
      Map<UUID, PlayerSummary> friends
   ) {
   }

   public record AtlasScreenRenderContext(AtlasScreen screen, GuiGraphicsExtractor context, int mouseX, int mouseY, float markerScale, Map<UUID, PlayerSummary> friends) {
   }
}
