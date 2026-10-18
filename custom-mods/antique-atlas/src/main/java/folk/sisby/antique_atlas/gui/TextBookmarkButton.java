package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class TextBookmarkButton extends BookmarkButton {
   public static final Identifier RULER_TEXTURE_RIGHT = AntiqueAtlas.id("textures/gui/ruler_right.png");
   public static final int SHORT_WIDTH = 20;
   protected Component label;

   public TextBookmarkButton(Component title, Component label) {
      super(RULER_TEXTURE_RIGHT, title, null, null, null, 32, 32, false, false);
      this.clickSound = null;
      this.label = label;
   }

   public void setLabel(Component label) {
      this.label = label;
   }

   @Override
   public void renderTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick, boolean mouseOver) {
      boolean isExtended = mouseOver || this.isSelected();
      int centerOffsetX = (20 - this.font.width(this.label)) / 2;
      context.text(this.font, this.label, this.getGuiX() + centerOffsetX + (isExtended ? 3 : 0), this.getGuiY() + 5, -16777216, false);
      if (!this.label.getString().equals("1c")) {
         super.renderTooltip(context, mouseX, mouseY, partialTick, mouseOver);
      }
   }
}
