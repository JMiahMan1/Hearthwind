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
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

/**
 * Auxiliary survival readouts (thirst / temperature) on the original
 * NutritionZ-styled 176x142 panel. Skills, Jobs and Party now have their own
 * Aged-parity screens; this class keeps the legacy meter cards and the
 * LibZ tab strip for navigation.
 */
@Environment(EnvType.CLIENT)
public class SurvivalInfoScreen extends Screen {
    public enum Kind { THIRST, TEMPERATURE }

    private static final Identifier PANEL = Identifier.fromNamespaceAndPath("hearthwind", "nutrition/panel");
    private static final Identifier ARROW = Identifier.fromNamespaceAndPath("hearthwind", "nutrition/arrow");
    private static final Identifier ARROW_HOVER = Identifier.fromNamespaceAndPath("hearthwind", "nutrition/arrow_hover");

    private static final int LABEL = 0xFF222222;
    private static final int HINT = 0xFF5A5A5A;
    private static final int VALUE = 0xFF444444;
    private static final int GOOD = 0xFF2E7D32;
    private static final int WARNING = 0xFFD87D12;
    private static final int BAD = 0xFFC62828;
    private static final int COLD_BLUE = 0xFF1976D2;

    private static final int CARD_BORDER = 0xFFB4B4B4;
    private static final int CARD_FACE = 0xFFDEDEDE;
    private static final int TRACK_COLOR = 0xFFB8B8B8;
    private static final int FILL_COLOR = 0xFF43A047;

    private final Kind kind;
    private int x;
    private int y;

    public SurvivalInfoScreen(Kind kind) {
        super(Component.translatable("screen.hearthwind." + kind.name().toLowerCase()));
        this.kind = kind;
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
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PANEL, this.x, this.y, 176, 166);
        TabStrip.draw(graphics, this.x, this.y, null, mouseX, mouseY);
        String title = this.title.getString();
        graphics.text(font, title, this.x + 88 - font.width(title) / 2, this.y + 6, LABEL, false);

        switch (this.kind) {
            case THIRST -> drawThirst(graphics, font);
            case TEMPERATURE -> drawTemperature(graphics, font);
        }

        boolean hoverArrow = within(5, 5, 11, 10, mouseX, mouseY);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, hoverArrow ? ARROW_HOVER : ARROW, this.x + 5, this.y + 5,
                11, 10);
    }

    private void drawThirst(GuiGraphicsExtractor graphics, Font font) {
        float hydration = ClientThirstData.getHydration();
        String value = String.format("%.1f / 20.0", hydration);

        int cardX = this.x + 8;
        int cardY = this.y + 17;
        int cardW = 160;
        int cardH = 34;
        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, CARD_BORDER);
        graphics.fill(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + cardH - 1, CARD_FACE);

        graphics.item(new ItemStack(Items.POTION), cardX + 4, cardY + 8);
        graphics.text(font, "Hydration Level", cardX + 24, cardY + 4, LABEL, false);
        graphics.text(font, value, cardX + cardW - 5 - font.width(value), cardY + 4, VALUE, false);

        int barX = cardX + 24;
        int barY = cardY + 15;
        int barW = 130;
        graphics.fill(barX, barY, barX + barW, barY + 6, TRACK_COLOR);
        int fill = Math.round(barW * Mth.clamp(hydration / 20f, 0f, 1f));
        if (fill > 0) {
            graphics.fill(barX, barY, barX + fill, barY + 6, COLD_BLUE);
        }

        String status;
        int statusColor;
        if (hydration >= 16f) {
            status = "● Well Hydrated";
            statusColor = GOOD;
        } else if (hydration >= 8f) {
            status = "● Normal";
            statusColor = VALUE;
        } else if (hydration >= 1f) {
            status = "● Thirsty";
            statusColor = WARNING;
        } else {
            status = "● Dehydrated!";
            statusColor = BAD;
        }
        graphics.text(font, status, cardX + 24, cardY + 23, statusColor, false);

        int guideY = this.y + 54;
        drawGuideRow(graphics, font, Items.WATER_BUCKET, "Sip Fresh Water", "Sneak + use on water", guideY);
        drawGuideRow(graphics, font, Items.CAMPFIRE, "Campfire Purification", "Boil bowls at campfires", guideY + 34);
        drawGuideRow(graphics, font, Items.LEATHER, "Leather Flasks", "Flasks carry water far", guideY + 68);
    }

    private void drawTemperature(GuiGraphicsExtractor graphics, Font font) {
        int temp = ClientTempData.getBodyTemperature();
        String tempStr = String.format("%+d", temp);

        int cardX = this.x + 8;
        int cardY = this.y + 17;
        int cardW = 160;
        int cardH = 34;
        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, CARD_BORDER);
        graphics.fill(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + cardH - 1, CARD_FACE);

        graphics.item(new ItemStack(temp >= 0 ? Items.CAMPFIRE : Items.SNOWBALL), cardX + 4, cardY + 8);
        graphics.text(font, "Body Temperature", cardX + 24, cardY + 4, LABEL, false);
        graphics.text(font, tempStr, cardX + cardW - 5 - font.width(tempStr), cardY + 4, VALUE, false);

        int barX = cardX + 24;
        int barY = cardY + 15;
        int barW = 130;
        graphics.fill(barX, barY, barX + barW, barY + 6, TRACK_COLOR);
        float norm = Mth.clamp((temp + 2400f) / 4800f, 0f, 1f);
        int fill = Math.round(barW * norm);
        int barColor = temp >= 240 ? WARNING : temp <= -240 ? COLD_BLUE : GOOD;
        if (fill > 0) {
            graphics.fill(barX, barY, barX + fill, barY + 6, barColor);
        }
        graphics.fill(barX + 64, barY - 1, barX + 66, barY + 7, 0xFF444444);

        String status;
        int statusColor;
        if (temp >= ClientTempData.BODY_MAX_HOT) {
            status = "● Overheating! (-30% damage)";
            statusColor = BAD;
        } else if (temp >= ClientTempData.BODY_MIN_HOT) {
            status = "● Hot (-12% damage)";
            statusColor = WARNING;
        } else if (temp <= ClientTempData.BODY_MAX_COLD) {
            status = "● Freezing! (-25% speed)";
            statusColor = BAD;
        } else if (temp <= ClientTempData.BODY_MIN_COLD) {
            status = "● Cold (-8% speed)";
            statusColor = COLD_BLUE;
        } else {
            status = "● Comfortable";
            statusColor = GOOD;
        }
        graphics.text(font, status, cardX + 24, cardY + 23, statusColor, false);

        int guideY = this.y + 54;
        drawGuideRow(graphics, font, Items.CAMPFIRE,
                "Campfires, Lava & Furnaces", "Heat within 3 blocks, clear line of sight", guideY);
        drawGuideRow(graphics, font, Items.LEATHER_CHESTPLATE,
                "Warm Armor", "+3/piece; ice packs cool -5", guideY + 42);
        drawGuideRow(graphics, font, Items.WATER_BUCKET,
                "Rain & Water", "Soaked -6; wet -3; shadow -1", guideY + 84);
    }

    /** Wraps {@code text} to at most {@code maxWidth} px per line (word based). */
    private static java.util.List<String> wrapText(Font font, String text, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (font.width(candidate) > maxWidth && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    private void drawGuideRow(GuiGraphicsExtractor graphics, Font font, Item icon, String header, String detail,
            int yPos) {
        int cardX = this.x + 8;
        int cardW = 184;
        int textX = cardX + 24;
        int textW = cardW - 28;
        java.util.List<String> detailLines = wrapText(font, detail, textW);
        int cardH = 16 + 10 * detailLines.size() + 4;
        graphics.fill(cardX, yPos, cardX + cardW, yPos + cardH, CARD_BORDER);
        graphics.fill(cardX + 1, yPos + 1, cardX + cardW - 1, yPos + cardH - 1, CARD_FACE);

        graphics.item(new ItemStack(icon), cardX + 4, yPos + 4);
        graphics.text(font, header, textX, yPos + 3, LABEL, false);
        for (int i = 0; i < detailLines.size(); i++) {
            graphics.text(font, detailLines.get(i), textX, yPos + 13 + 10 * i, HINT, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        TabStrip.Tab tab = TabStrip.clicked(event.x(), event.y(), this.x, this.y, null);
        if (tab != null) {
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            TabStrip.open(tab);
            return true;
        }
        if (within(5, 5, 11, 10, event.x(), event.y())) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (minecraft.player != null) {
                minecraft.setScreenAndShow(new InventoryScreen(minecraft.player));
            }
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (NutrientsKey.openNutrients.matches(event)) {
            mc.setScreenAndShow(new NutrientsScreen());
            return true;
        }
        if (NutrientsKey.openSkills.matches(event)) {
            TabStrip.open(TabStrip.Tab.SKILLS);
            return true;
        }
        if (NutrientsKey.openJobs.matches(event)) {
            TabStrip.open(TabStrip.Tab.JOBS);
            return true;
        }
        if (NutrientsKey.openParty.matches(event)) {
            TabStrip.open(TabStrip.Tab.PARTY);
            return true;
        }
        if (mc.options.keyInventory.matches(event) || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean within(int bx, int by, int bw, int bh, double px, double py) {
        px -= this.x;
        py -= this.y;
        return px >= bx && px < bx + bw && py >= by && py < by + bh;
    }
}
