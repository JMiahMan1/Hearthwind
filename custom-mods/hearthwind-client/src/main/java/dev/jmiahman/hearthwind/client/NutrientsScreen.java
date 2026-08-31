package dev.jmiahman.hearthwind.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

/**
 * Exact Aged 3.1.2 NutritionZ parity panel:
 * - Vanilla grey #C6C6C6 panel (176x166) with 4-tab strip at top.
 * - 5 rows, pitch 24 GUI px (Fruits, Vegetables, Grains, Proteins, Sugars).
 * - Segmented 140x5 bars with authentic Aged palettes:
 *   Row 1 (Fruits): Red #E54016 / #591E08
 *   Row 2 (Vegetables): Amber #F1910C / #322202
 *   Row 3 (Grains): Yellow #F0DE1A / #312905
 *   Row 4 (Proteins): Green #64CA0C / #0C2800
 *   Row 5 (Sugars): Tan #99916E / #312905
 */
@Environment(EnvType.CLIENT)
public class NutrientsScreen extends Screen {
    private static final Identifier PANEL = Identifier.fromNamespaceAndPath("hearthwind", "nutrition/panel");
    private static final Identifier ARROW = Identifier.fromNamespaceAndPath("hearthwind", "nutrition/arrow");
    private static final Identifier ARROW_HOVER = Identifier.fromNamespaceAndPath("hearthwind", "nutrition/arrow_hover");
    private static final Identifier[] BAR_BG = {
            Identifier.fromNamespaceAndPath("hearthwind", "nutrition/bar_bg_fruits"),
            Identifier.fromNamespaceAndPath("hearthwind", "nutrition/bar_bg_vegetables"),
            Identifier.fromNamespaceAndPath("hearthwind", "nutrition/bar_bg_grains"),
            Identifier.fromNamespaceAndPath("hearthwind", "nutrition/bar_bg_proteins"),
            Identifier.fromNamespaceAndPath("hearthwind", "nutrition/bar_bg_sugars") };
    private static final Identifier[] BAR_FILL = {
            Identifier.fromNamespaceAndPath("hearthwind", "nutrition/bar_fill_fruits"),
            Identifier.fromNamespaceAndPath("hearthwind", "nutrition/bar_fill_vegetables"),
            Identifier.fromNamespaceAndPath("hearthwind", "nutrition/bar_fill_grains"),
            Identifier.fromNamespaceAndPath("hearthwind", "nutrition/bar_fill_proteins"),
            Identifier.fromNamespaceAndPath("hearthwind", "nutrition/bar_fill_sugars") };
    private static final Item[] ICONS = { Items.APPLE, Items.CARROT, Items.BREAD, Items.COOKED_BEEF, Items.SUGAR };
    private static final String[] LABEL_KEYS = { "fruits", "vegetables", "grains", "proteins", "sugars" };

    // Authentic text colors against #C6C6C6 panel
    private static final int TITLE = 0xFF2A2A2A;
    private static final int INK = 0xFF222222;
    private static final int VALUE = 0xFF444444;
    private static final int BALANCED_COLOR = 0xFF2E7D32;
    private static final int UNBALANCED_COLOR = 0xFF666666;

    private int x;
    private int y;

    public NutrientsScreen() {
        super(Component.translatable("screen.hearthwind.nutrients"));
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - 176) / 2;
        this.y = (this.height - 166) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        Font font = Minecraft.getInstance().font;

        // Draw vanilla-grey panel background + 4 tab strip
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PANEL, this.x, this.y, 176, 166);
        TabStrip.draw(graphics, font, this.x, this.y, TabStrip.Tab.NUTRITION, mouseX, mouseY);

        String title = this.title.getString();
        graphics.text(font, title, this.x + 88 - font.width(title) / 2, this.y + 6, TITLE, false);

        boolean allBalanced = true;
        for (int i = 0; i < 5; i++) {
            // Row pitch: 24 GUI px
            int rowRelY = 18 + i * 24;
            int rowY = this.y + rowRelY;
            int level = Math.round(ClientDietData.get(i));
            if (level < 80) {
                allBalanced = false;
            }

            // Food item icon (16x16)
            graphics.item(new ItemStack(ICONS[i]), this.x + 8, rowY);

            // Group label
            graphics.text(font,
                    Component.translatable("screen.hearthwind.nutrients." + LABEL_KEYS[i]).getString(),
                    this.x + 28, rowY + 1, INK, false);

            // Segmented 140x5 Bar Background & Fill
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_BG[i], this.x + 28, rowY + 11, 140, 5);
            int fill = Math.min(140, Math.round(140f * level / 100f));
            if (fill > 0) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_FILL[i], this.x + 28, rowY + 11, fill, 5);
            }

            // Numeric indicator "level/100"
            String valStr = level + "/100";
            graphics.text(font, valStr, this.x + 168 - font.width(valStr), rowY + 1, VALUE, false);
        }

        // Status banner
        Component status = allBalanced
                ? Component.translatable("screen.hearthwind.nutrients.balanced")
                : Component.translatable("screen.hearthwind.nutrients.unbalanced");
        int statusColor = allBalanced ? BALANCED_COLOR : UNBALANCED_COLOR;
        String statusText = status.getString();
        graphics.text(font, statusText, this.x + 88 - font.width(statusText) / 2, this.y + 148, statusColor, false);

        // Back-arrow in top-left of panel
        boolean arrowHovered = mouseX >= this.x + 5 && mouseX < this.x + 19 && mouseY >= this.y + 5 && mouseY < this.y + 19;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrowHovered ? ARROW_HOVER : ARROW, this.x + 5, this.y + 5, 14, 14);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mx = event.x();
            double my = event.y();

            // Tab bar click
            TabStrip.Tab clickedTab = TabStrip.clicked(mx, my, this.x, this.y);
            if (clickedTab != null) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                TabStrip.open(clickedTab);
                return true;
            }

            // Back-arrow click
            if (mx >= this.x + 5 && mx < this.x + 19 && my >= this.y + 5 && my < this.y + 19) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.setScreenAndShow(new InventoryScreen(this.minecraft.player));
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.keyInventory.matches(event)
                || NutrientsKey.openNutrients.matches(event)
                || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
