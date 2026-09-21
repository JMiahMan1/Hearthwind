package dev.jmiahman.hearthwind.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * Shared base for the Aged-parity info panels (Skills, Jobs, Party).
 *
 * <p>Reproduces the reference panel chrome: 200x215 body, a 1 px black
 * outline, a 2 px white inner highlight and a flat {@code #C6C6C6} face,
 * with the LibZ tab strip drawn on top. All content coordinates are
 * panel-relative and come from the LevelZ/JobsAddon/PartyAddon sources.
 */
@Environment(EnvType.CLIENT)
public abstract class AgedPanelScreen extends Screen {

    public static final int PANEL_W = 200;
    public static final int PANEL_H = 215;

    private static final net.minecraft.resources.Identifier PANEL_TEXTURE =
            net.minecraft.resources.Identifier.fromNamespaceAndPath("hearthwind", "textures/gui/aged_panel.png");

    protected static final int INK = 0xFF3F3F3F;

    protected int x;
    protected int y;

    protected AgedPanelScreen(Component title) {
        super(title);
    }

    /** Tab highlighted while this screen is open. */
    protected abstract TabStrip.Tab activeTab();

    /** Draws the panel body's widgets (panel chrome already drawn). */
    protected abstract void drawContent(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY);

    /** Handles a click inside the panel; return true when consumed. */
    protected boolean onContentClick(MouseButtonEvent event) {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - PANEL_W) / 2;
        this.y = (this.height - PANEL_H) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        drawPanel(graphics);
        drawContent(graphics, this.font, mouseX, mouseY);
        TabStrip.draw(graphics, this.x, this.y, activeTab(), mouseX, mouseY);
    }

    /** Solid textured panel chrome (black outline, white ring, beveled face). */
    protected void drawPanel(GuiGraphicsExtractor graphics) {
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                PANEL_TEXTURE, this.x, this.y, 0F, 0F, PANEL_W, PANEL_H, PANEL_W, PANEL_H,
                0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (event.button() == 0) {
            TabStrip.Tab tab = TabStrip.clicked(event.x(), event.y(), this.x, this.y, activeTab());
            if (tab != null && tab != activeTab()) {
                click();
                TabStrip.open(tab);
                return true;
            }
            if (onContentClick(event)) {
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (NutrientsKey.openSkills.matches(event)) {
            if (activeTab() != TabStrip.Tab.SKILLS) {
                TabStrip.open(TabStrip.Tab.SKILLS);
            } else {
                this.onClose();
            }
            return true;
        }
        if (NutrientsKey.openJobs.matches(event)) {
            if (activeTab() != TabStrip.Tab.JOBS) {
                TabStrip.open(TabStrip.Tab.JOBS);
            } else {
                this.onClose();
            }
            return true;
        }
        if (NutrientsKey.openParty.matches(event)) {
            if (activeTab() != TabStrip.Tab.PARTY) {
                TabStrip.open(TabStrip.Tab.PARTY);
            } else {
                this.onClose();
            }
            return true;
        }
        if (mc.options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void click() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    protected boolean isOver(int relX, int relY, int w, int h, double mouseX, double mouseY) {
        double mx = mouseX - this.x;
        double my = mouseY - this.y;
        return mx >= relX - 1 && mx < relX + w + 1 && my >= relY - 1 && my < relY + h + 1;
    }
}
