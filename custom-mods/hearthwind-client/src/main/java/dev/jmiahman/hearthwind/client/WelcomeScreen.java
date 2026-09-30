package dev.jmiahman.hearthwind.client;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
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
 * the pack art becomes a letterbox strip that shrinks to the room left, and the
 * last text blocks are given up before the Start button is ever pushed out of
 * reach.
 */
@Environment(EnvType.CLIENT)
public class WelcomeScreen extends Screen {

    private static final Identifier PACK_ART = Identifier.fromNamespaceAndPath(
            "hearthwind", "textures/gui/title/main_menu_background.png");
    /** Aged's {@code image_1} box; we sample a 16:9 crop of the title art into it. */
    private static final int PACK_ART_BOX = 256;
    /** Centre of the crop in the 1920x1080 title texture. */
    private static final int ART_CENTER_U = 960;
    private static final int ART_CENTER_V = 540;
    private static final int ART_FULL_W = 256;
    private static final int ART_FULL_H = 144;
    /**
     * How much of the source texture one screen pixel covers. Drawing the art
     * with the 11-argument blit samples a w x h region and draws it 1:1, so a
     * 256x144 sample of a 1920x1080 texture is magnified on any display and
     * reads as grainy. Sampling a 3x larger region and scaling it down is what
     * makes the art smooth; the 3x is simply "more source than we draw".
     */
    private static final int ART_OVERSAMPLE = 3;
    private static final int ART_TEX_W = 1920;
    private static final int ART_TEX_H = 1080;
    /** A letterbox strip of the same art, for the narrow single-column layout. */
    private static final int ART_STRIP_MAX_W = 512;
    private static final int ART_STRIP_H = 72;
    /** Below this the strip is not worth the vertical room it would cost. */
    private static final int ART_STRIP_MIN_H = 32;

    private static final int TITLE_COLOR = 0xFFF3DFCD;
    private static final int TEXT_COLOR = 0xFFE6DFD2;
    private static final int HINT_COLOR = 0xFFA79A87;
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
        Font font = this.font;
        Layout layout = layout();
        int centerX = this.width / 2;

        graphics.fill(0, 0, this.width, this.height, SHADE_COLOR);
        // The Aged welcome screen tiles the advancements stone background; we
        // use the Hearthwind title art so the first thing a player sees in a
        // world is the same art as the main menu.
        if (layout.artW() > 0) {
            // Destination rect + source rect, so the GPU scales a large crop
            // down instead of magnifying a small one.
            int srcW = Math.min(ART_TEX_W, layout.artW() * ART_OVERSAMPLE);
            int srcH = Math.min(ART_TEX_H, layout.artH() * ART_OVERSAMPLE);
            float u0 = clampToTexture(ART_CENTER_U - srcW / 2.0F, srcW, ART_TEX_W);
            float v0 = clampToTexture(ART_CENTER_V - srcH / 2.0F, srcH, ART_TEX_H);
            graphics.blit(PACK_ART,
                    layout.artX(), layout.artY(),
                    layout.artX() + layout.artW(), layout.artY() + layout.artH(),
                    u0, u0 + srcW, v0, v0 + srcH);
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

        // The escape hatch, spelled out. Aged's screen has one button and
        // players reported not finding it; the hint costs one line and makes
        // the way in unmissable. Escape works too, see shouldCloseOnEsc.
        Component hint = Component.translatable("screen.hearthwind.welcome.hint");
        graphics.text(font, hint, centerX - font.width(hint) / 2, layout.hintY(), HINT_COLOR, false);

        // Widgets last: the opaque background above is a full-screen fill, so
        // extracting the Start button before it would paint the button out.
        // That is exactly what hid it for 0.1.30-0.1.33.
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    /**
     * Keeps an oversampled source rect inside the texture: near an edge the
     * window shrinks so the region still has the size we asked for.
     */
    private static float clampToTexture(float start, int size, int textureSize) {
        return Math.max(0.0F, Math.min(start, textureSize - size));
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

        // Ordered by how actionable each block is, because a window too short
        // for all of them gives them up from the end: the guide and the first
        // steps matter most, the pack's philosophy line least.
        List<FormattedCharSequence> guide = block("guide", wrap);
        List<FormattedCharSequence> first = block("first", wrap);
        List<FormattedCharSequence> keys = block("keys", wrap);
        List<FormattedCharSequence> loadout = block("loadout", wrap);
        List<FormattedCharSequence> pack = block("pack", wrap);
        List<List<FormattedCharSequence>> blocks =
                new ArrayList<>(List.of(guide, first, keys, loadout, pack));

        // The button and its hint are reserved BEFORE anything else and are then
        // clamped into the window: a player must never be left without a way in,
        // whatever the window size. The escape key works too, but a button the
        // layout pushed off the bottom of the screen is invisible, and players
        // reported not finding it.
        int hintH = font.lineHeight + 4;
        int buttonY = clamp(this.height - BUTTON_H - MARGIN - hintH, MARGIN, this.height - BUTTON_H - MARGIN);
        int buttonX = wide ? centerX + 90 - BUTTON_W / 2 : centerX - BUTTON_W / 2;

        if (wide) {
            // Aged: pack art in the box left of centre, the three text blocks in
            // the right-hand column, Start below them.
            int blocksHeight = blocksHeight(blocks, lineHeight);
            return new Layout(ART_FULL_W, ART_FULL_H,
                    centerX - 150, centerY - 20 - PACK_ART_BOX / 2,
                    centerY - 14, blocks, centerY + 40 - blocksHeight / 2,
                    buttonX, buttonY, hintAbove(buttonY, hintH));
        }

        // Narrow: one centred column with a letterbox strip of the same art. The
        // text gets what is left above the button; blocks, then lines, then the
        // art are given up in that order.
        int stripW = Math.min(this.width, ART_STRIP_MAX_W);
        int textTop = font.lineHeight + GAP;
        int budget = buttonY - GAP - MARGIN - textTop;
        while (blocks.size() > 1 && blocksHeight(blocks, lineHeight) > budget) {
            blocks = new ArrayList<>(blocks.subList(0, blocks.size() - 1));
        }
        if (blocks.size() == 1 && blocks.get(0).size() * lineHeight > budget) {
            // A window too short even for one whole block keeps as many of its
            // leading lines as fit rather than overflowing into the button.
            int lines = Math.max(1, budget / lineHeight);
            blocks = new ArrayList<>(List.of(new ArrayList<>(blocks.get(0).subList(0,
                    Math.min(lines, blocks.get(0).size())))));
        }
        int textHeight = blocksHeight(blocks, lineHeight);
        // The strip shrinks to whatever vertical room is left rather than being
        // all or nothing: on a 427x240 window (a 1080p display at GUI scale 4)
        // the full 72 px strip does not fit under the reserved button, and a
        // bare text screen is a worse first impression than a thin band of art.
        int free = buttonY - GAP - MARGIN - textTop - textHeight;
        int stripH = free >= ART_STRIP_MIN_H + GAP ? Math.min(ART_STRIP_H, free - GAP) : 0;
        boolean art = stripH > 0;
        int content = textHeight + (art ? stripH + GAP : 0) + textTop;
        int top = clamp((this.height - (content + BUTTON_H + hintH + 2 * MARGIN)) / 2,
                MARGIN, Math.max(MARGIN, buttonY - GAP - content));
        int artY = top + textTop;
        return new Layout(art ? stripW : 0, stripH,
                (this.width - stripW) / 2, artY,
                top, blocks, art ? artY + stripH + GAP : artY,
                buttonX, buttonY, hintAbove(buttonY, hintH));
    }

    /**
     * Every block's text, whether or not the window is tall enough to show it.
     *
     * <p>A test seam, and deliberately not {@link #layout()}'s fitted block list:
     * on a short window the layout gives blocks up from the end, so asserting on
     * what fit would only test the window size. What must never break is the
     * translations - an unresolvable key renders as its own dotted name, which is
     * a broken-looking screen that a correct layout screenshot hides.
     */
    public List<String> debugAllText() {
        List<String> lines = new ArrayList<>();
        lines.add(this.title.getString());
        for (String key : List.of("guide", "first", "keys", "loadout", "pack")) {
            lines.add(Component.translatable("screen.hearthwind.welcome.text." + key).getString());
        }
        lines.add(Component.translatable("screen.hearthwind.welcome.hint").getString());
        return lines;
    }

    /**
     * Where the hint goes: under the button when there is room for it, otherwise
     * above it, so it is never drawn off the bottom edge.
     */
    private int hintAbove(int buttonY, int hintH) {
        return buttonY + BUTTON_H + 4 <= this.height - MARGIN
                ? buttonY + BUTTON_H + 4
                : Math.max(MARGIN, buttonY - GAP - hintH + 4);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, Math.max(min, max)));
    }

    /** One of the welcome screen's text blocks, pre-wrapped for this window. */
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
    private record Layout(int artW, int artH, int artX, int artY, int titleY,
            List<List<FormattedCharSequence>> blocks, int blocksTop, int buttonX, int buttonY,
            int hintY) {}
}
