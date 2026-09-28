package dev.jmiahman.hearthwind.client;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

/**
 * The first screen a Hearthwind player sees in a world, rebuilt from Aged
 * 3.1.2's {@code aged_welcome_screen} datapack (the {@code welcomescreen} mod).
 *
 * <p>Aged's layout, with every position taken from that JSON (x right of the
 * screen centre, y up from it):
 * <ul>
 *   <li>{@code image_1} - a 256x256 pack image left of centre;</li>
 *   <li>{@code title} - centred, 10 px above the middle;</li>
 *   <li>{@code text_1} / {@code text_2} / {@code text_3} - three centred
 *       blocks in the right-hand column at y +40, y 0 and y -60;</li>
 *   <li>{@code close} - the {@code Start} button at y -105, and pressing it
 *       runs the five {@code /item replace} commands that fill the hotbar.</li>
 * </ul>
 *
 * <p>Deviations from the Aged file, all deliberate:
 * <ul>
 *   <li>the pack image is the Hearthwind title art we own (Aged ships a
 *       {@code aged:textures/pack.png} we must not redistribute);</li>
 *   <li>there is no Discord button - Hearthwind has no Discord invite to
 *       point at;</li>
 *   <li>the second text block drops Aged's "sticks from leaves and flint on
 *       it and hit it a few times" knapping line, because the Hearthwind
 *       crafting rock is a 3x3 grid rather than a knapping bench, and this
 *       screen must not describe a mechanic the game does not have.</li>
 * </ul>
 *
 * <p>Aged's positions are absolute, so they only work on a wide screen. The GUI
 * sizes real players get are small - a 1080p display at Minecraft's automatic
 * GUI scale is 480x270 logical, and the headless test client runs 427x240 -
 * and there the right-hand text column would run off the edge. Below
 * {@link #WIDE_MIN_WIDTH} the same content is stacked in one centred column,
 * and if the window is too short even for that the pack art goes first and
 * then the last text block.
 */
@Environment(EnvType.CLIENT)
public class WelcomeScreen extends Screen {

    private static final Identifier PACK_ART = Identifier.fromNamespaceAndPath(
            "hearthwind", "textures/gui/title/main_menu_background.png");
    /** Aged's {@code image_1} box; we sample a 16:9 crop of the title art into it. */
    private static final int PACK_ART_BOX = 256;
    private static final int ART_U = 832;
    private static final int ART_V = 468;
    private static final int ART_FULL_W = 256;
    private static final int ART_FULL_H = 144;
    /** A letterbox strip of the same art, for the narrow single-column layout. */
    private static final int ART_STRIP_MAX_W = 512;
    private static final int ART_STRIP_H = 72;
    private static final int ART_STRIP_V = 508;

    private static final int TITLE_COLOR = 0xFFF3DFCD;
    private static final int TEXT_COLOR = 0xFFE6DFD2;
    private static final int SHADE_COLOR = 0xFF0B1016;

    /** Aged's text column width. */
    private static final int TEXT_WRAP = 268;
    /** Narrowest window where Aged's two-column arrangement still fits. */
    private static final int WIDE_MIN_WIDTH = 700;
    private static final int BUTTON_W = 100;
    private static final int BUTTON_H = 20;
    private static final int GAP = 6;
    private static final int MARGIN = 8;

    public WelcomeScreen() {
        super(Component.translatable("screen.hearthwind.welcome"));
    }

    @Override
    protected void init() {
        Layout layout = layout();
        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.hearthwind.welcome.start"),
                        button -> start())
                .bounds(layout.buttonX(), layout.buttonY(), BUTTON_W, BUTTON_H)
                .build());
    }

    private void start() {
        ClientWelcomeData.sendStart();
        this.onClose();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(null);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // Escape starts the game as well: nobody may be left staring at the
        // screen without the supplies the Start button hands over.
        start();
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        Font font = this.font;
        Layout layout = layout();
        int centerX = this.width / 2;

        graphics.fill(0, 0, this.width, this.height, SHADE_COLOR);
        // The Aged welcome screen tiles the advancements stone background; we
        // use the Hearthwind title art so the first thing a player sees in a
        // world is the same art as the main menu.
        if (layout.artW() > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, PACK_ART,
                    layout.artX(), layout.artY(), (float) layout.artU(), (float) layout.artV(),
                    layout.artW(), layout.artH(), 1920, 1080, 0xFFFFFFFF);
        }

        Component title = this.title;
        graphics.text(font, title, centerX - font.width(title) / 2, layout.titleY(), TITLE_COLOR, true);

        int y = layout.blocksTop();
        int lineHeight = font.lineHeight + 2;
        for (List<FormattedCharSequence> block : layout.blocks()) {
            for (FormattedCharSequence line : block) {
                graphics.text(font, line, centerX - font.width(line) / 2, y, TEXT_COLOR, false);
                y += lineHeight;
            }
            y += GAP;
        }
    }

    /**
     * Where everything sits for the current window: Aged's arrangement on a wide
     * screen, one centred column everywhere else. Both {@link #init()} and the
     * renderer go through it, so the Start button can never drift away from the
     * art or the text.
     */
    private Layout layout() {
        Font font = this.font;
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int lineHeight = font.lineHeight + 2;
        boolean wide = this.width >= WIDE_MIN_WIDTH;
        // Wide keeps Aged's 268 px text column; the stacked layout uses the full
        // window width, because vertical room - not measure - is what it is short of.
        int wrap = wide
                ? Math.max(120, Math.min(TEXT_WRAP, this.width - 2 * MARGIN))
                : Math.max(120, this.width - 2 * MARGIN);

        List<FormattedCharSequence> guide = block("guide", wrap);
        List<FormattedCharSequence> first = block("first", wrap);
        List<FormattedCharSequence> pack = block("pack", wrap);
        List<List<FormattedCharSequence>> blocks = new ArrayList<>(List.of(guide, first, pack));

        if (wide) {
            // Aged: pack art in the box left of centre, the three text blocks in
            // the right-hand column, Start below them.
            int blocksHeight = blocksHeight(blocks, lineHeight);
            return new Layout(ART_U, ART_V, ART_FULL_W, ART_FULL_H,
                    centerX - 150, centerY - 20 - PACK_ART_BOX / 2,
                    centerY - 14, blocks, centerY + 40 - blocksHeight / 2,
                    centerX + 90 - BUTTON_W / 2, centerY - 105);
        }

        // Narrow: one centred column with a letterbox strip of the same art, and
        // the art is the first thing to go if the window is too short for it.
        int stripW = Math.min(this.width, ART_STRIP_MAX_W);
        int buttonY = this.height - BUTTON_H - MARGIN;
        int budget = buttonY - MARGIN;
        int textHeight = font.lineHeight + GAP + blocksHeight(blocks, lineHeight);
        boolean art = textHeight + GAP + ART_STRIP_H <= budget;
        int content = textHeight + (art ? ART_STRIP_H + GAP : 0);
        while (!art && blocks.size() > 1 && content > budget) {
            content -= blocks.get(blocks.size() - 1).size() * lineHeight + GAP;
            blocks = new ArrayList<>(blocks.subList(0, blocks.size() - 1));
        }
        int top = Math.max(MARGIN, (this.height - (content + BUTTON_H + 2 * MARGIN)) / 2);
        int artY = top + font.lineHeight + GAP;
        return new Layout(art ? (1920 - stripW) / 2 : 0, art ? ART_STRIP_V : 0,
                art ? stripW : 0, art ? ART_STRIP_H : 0,
                (this.width - stripW) / 2, artY,
                top, blocks, art ? artY + ART_STRIP_H + GAP : artY,
                centerX - BUTTON_W / 2, buttonY);
    }

    /** One of the three Aged text blocks, pre-wrapped for this window. */
    private List<FormattedCharSequence> block(String key, int wrap) {
        return this.font.split(Component.translatable("screen.hearthwind.welcome.text." + key), wrap);
    }

    private static int blocksHeight(List<List<FormattedCharSequence>> blocks, int lineHeight) {
        int lines = 0;
        for (List<FormattedCharSequence> block : blocks) {
            lines += block.size();
        }
        return lines * lineHeight + (blocks.size() - 1) * GAP;
    }

    /** Everything {@link #layout()} needs; {@code artW == 0} means no art. */
    private record Layout(int artU, int artV, int artW, int artH, int artX, int artY, int titleY,
            List<List<FormattedCharSequence>> blocks, int blocksTop, int buttonX, int buttonY) {}
}
