package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.gui.core.ToggleButtonComponent;
import folk.sisby.antique_atlas.util.ColorUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class BookmarkButton extends ToggleButtonComponent {
   public static final Identifier TEXTURE_LEFT = AntiqueAtlas.id("textures/gui/bookmark_left.png");
   public static final Identifier TEXTURE_RIGHT = AntiqueAtlas.id("textures/gui/bookmark_right.png");
   public static final Identifier TEXTURE_TOP = AntiqueAtlas.id("textures/gui/bookmark_top.png");
   public static final Identifier TEXTURE_BOTTOM = AntiqueAtlas.id("textures/gui/bookmark_bottom.png");
   public static final int WIDTH = 24;
   public static final int HEIGHT = 18;
   protected Component title;
   protected Identifier iconTexture;
   protected final float[] backgroundTint;
   protected final float[] iconTint;
   protected final int iconWidth;
   protected final int iconHeight;
   protected final boolean backwards;
   protected final boolean vertical;
   protected final Identifier backgroundTexture;

   public BookmarkButton(
      Identifier backgroundTexture,
      Component title,
      Identifier iconTexture,
      @Nullable Integer backgroundTint,
      @Nullable Integer iconTint,
      int iconWidth,
      int iconHeight,
      boolean backwards,
      boolean vertical
   ) {
      super(false);
      this.backgroundTexture = backgroundTexture;
      this.title = title;
      this.iconTexture = iconTexture;
      this.backgroundTint = backgroundTint == null ? null : ColorUtil.componentsFromRgb(backgroundTint);
      this.iconWidth = iconWidth;
      this.iconHeight = iconHeight;
      this.iconTint = iconTint == null ? null : ColorUtil.componentsFromRgb(iconTint);
      this.backwards = backwards;
      this.vertical = vertical;
      this.setTitle(title);
      this.setSize(vertical ? 18 : 24, vertical ? 24 : 18);
   }

   public BookmarkButton(
      Component title,
      Identifier iconTexture,
      @Nullable Integer backgroundTint,
      @Nullable Integer iconTint,
      int iconWidth,
      int iconHeight,
      boolean backwards,
      boolean vertical
   ) {
      this(
         vertical ? (backwards ? TEXTURE_TOP : TEXTURE_BOTTOM) : (backwards ? TEXTURE_LEFT : TEXTURE_RIGHT),
         title,
         iconTexture,
         backgroundTint,
         iconTint,
         iconWidth,
         iconHeight,
         backwards,
         vertical
      );
   }

   public void setIconTexture(Identifier iconTexture) {
      this.iconTexture = iconTexture;
   }

   public Component getTitle() {
      return this.title;
   }

   public void setTitle(Component title) {
      this.title = title;
   }

   public void drawIcon(GuiGraphicsExtractor context, int x, int y) {
      if (this.iconTint != null) {
      }

      context.blit(RenderPipelines.GUI_TEXTURED, this.iconTexture, x, y, 0.0F, 0.0F, this.iconWidth, this.iconHeight, this.iconWidth, this.iconHeight);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
      folk.sisby.antique_atlas.util.AtlasCanvas canvas = new folk.sisby.antique_atlas.util.GuiAtlasCanvas(context);
      boolean mouseOver = this.isMouseOver(mouseX, mouseY);
      boolean isExtended = mouseOver || this.isSelected();
      if (this.backgroundTint != null) {
      }

      context.blit(RenderPipelines.GUI_TEXTURED, 
         this.backgroundTexture,
         this.getGuiX(),
         this.getGuiY(),
         this.vertical && !isExtended ? 18.0F : 0.0F,
         !this.vertical && !isExtended ? 18.0F : 0.0F,
         this.vertical ? 18 : 24,
         this.vertical ? 24 : 18,
         this.vertical ? 36 : 24,
         this.vertical ? 24 : 36
      );
      if (this.iconTexture != null) {
         int iconX = this.getGuiX()
            + (!this.vertical ? 10 - this.iconWidth / 2 + (isExtended ? (this.backwards ? 3 : 1) : (this.backwards ? 4 : 0)) : 9 - this.iconHeight / 2);
         int iconY = this.getGuiY()
            + (this.vertical ? 10 - this.iconWidth / 2 + (isExtended ? (this.backwards ? 3 : 1) : (this.backwards ? 4 : 0)) : 9 - this.iconHeight / 2);
         this.drawIcon(context, iconX, iconY);
      }

      this.renderTooltip(context, mouseX, mouseY, partialTick, mouseOver);
   }

   public void renderTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick, boolean mouseOver) {
      if (mouseOver && !this.title.getString().isEmpty()) {
         context.setTooltipForNextFrame(this.font, this.title, mouseX, mouseY);
      }
   }
}
