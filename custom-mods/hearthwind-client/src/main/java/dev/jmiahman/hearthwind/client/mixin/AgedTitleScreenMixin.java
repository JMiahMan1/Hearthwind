package dev.jmiahman.hearthwind.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Polished, modern Hearthwind Main Menu:
 * 1. Full-screen atmospheric background art.
 * 2. Frameless, left-aligned typography buttons with signature color hover tints & smooth slide:
 *    - Singleplayer: #F3DFCD (warm cream)
 *    - Multiplayer:  #A8C4C1 (soft sage blue)
 *    - Mods:         #78B5C7 (slate cyan)
 *    - Options:      #CBB5CB (heather mauve)
 *    - Quit Game:    #F09E8F (sunset coral red)
 * 3. Top-right 20x20 action icons (GitHub, Language, Accessibility).
 * 4. Suppressed realms notifications (diamond circle, paper envelope, exclamation badges).
 * 5. Clean footer branding.
 */
@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public abstract class AgedTitleScreenMixin extends Screen {

    @Shadow
    private SplashRenderer splash;

    private static final Identifier BACKGROUND_TEX =
            Identifier.fromNamespaceAndPath("hearthwind", "textures/gui/title/main_menu_background_with_aged.png");

    private static final Identifier GITHUB =
            Identifier.fromNamespaceAndPath("hearthwind", "title/github");
    private static final Identifier GITHUB_HOVER =
            Identifier.fromNamespaceAndPath("hearthwind", "title/github_hovered");

    private static final Identifier LANGUAGE =
            Identifier.fromNamespaceAndPath("hearthwind", "title/language");
    private static final Identifier LANGUAGE_HOVER =
            Identifier.fromNamespaceAndPath("hearthwind", "title/language_hovered");

    private static final Identifier MANNEQUIN =
            Identifier.fromNamespaceAndPath("hearthwind", "title/mannequin");
    private static final Identifier MANNEQUIN_HOVER =
            Identifier.fromNamespaceAndPath("hearthwind", "title/mannequin_hovered");

    private static final WidgetSprites GITHUB_SPRITES = new WidgetSprites(GITHUB, GITHUB_HOVER);
    private static final WidgetSprites LANGUAGE_SPRITES = new WidgetSprites(LANGUAGE, LANGUAGE_HOVER);
    private static final WidgetSprites MANNEQUIN_SPRITES = new WidgetSprites(MANNEQUIN, MANNEQUIN_HOVER);

    protected AgedTitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "realmsNotificationsEnabled", at = @At("HEAD"), cancellable = true)
    private void disableRealmsNotifications(CallbackInfoReturnable<Boolean> cir) {
        // Disables the realms envelope, paper news, and diamond exclamation badges completely
        cir.setReturnValue(false);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void customizeAgedLayout(CallbackInfo ci) {
        this.splash = null;
        this.clearWidgets();

        int menuX = Math.max(36, this.width / 8);
        int menuY = this.height / 2 - 35;
        int itemHeight = 22;
        int buttonWidth = 140;

        // 1. Singleplayer (#F3DFCD)
        this.addRenderableWidget(new HearthwindMenuButton(
                menuX, menuY, buttonWidth, 18,
                Component.literal("Singleplayer"),
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new SelectWorldScreen(this));
                    }
                },
                0xFFF3DFCD
        ));

        // 2. Multiplayer (#A8C4C1)
        this.addRenderableWidget(new HearthwindMenuButton(
                menuX, menuY + itemHeight, buttonWidth, 18,
                Component.literal("Multiplayer"),
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new JoinMultiplayerScreen(this));
                    }
                },
                0xFFA8C4C1
        ));

        // 3. Mods (#78B5C7)
        this.addRenderableWidget(new HearthwindMenuButton(
                menuX, menuY + itemHeight * 2, buttonWidth, 18,
                Component.literal("Mods"),
                btn -> {
                    if (this.minecraft != null) {
                        try {
                            if (FabricLoader.getInstance().isModLoaded("modmenu")) {
                                Class<?> cls = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
                                Screen screen = (Screen) cls.getConstructor(Screen.class).newInstance(this);
                                this.minecraft.setScreenAndShow(screen);
                                return;
                            }
                        } catch (Exception ignored) {}
                        this.minecraft.setScreenAndShow(new OptionsScreen(this, this.minecraft.options, false));
                    }
                },
                0xFF78B5C7
        ));

        // 4. Options (#CBB5CB)
        this.addRenderableWidget(new HearthwindMenuButton(
                menuX, menuY + itemHeight * 3, buttonWidth, 18,
                Component.literal("Options"),
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new OptionsScreen(this, this.minecraft.options, false));
                    }
                },
                0xFFCBB5CB
        ));

        // 5. Quit Game (#F09E8F)
        this.addRenderableWidget(new HearthwindMenuButton(
                menuX, menuY + itemHeight * 4, buttonWidth, 18,
                Component.literal("Quit Game"),
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.stop();
                    }
                },
                0xFFF09E8F
        ));

        // Top-Right Action Icons
        int iconY = 12;
        int iconSpacing = 26;
        int rightX = this.width - 32;

        // Accessibility (Mannequin)
        this.addRenderableWidget(new ImageButton(
                rightX, iconY, 20, 20, MANNEQUIN_SPRITES,
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new AccessibilityOptionsScreen(this, this.minecraft.options));
                    }
                }
        ));

        // Language
        this.addRenderableWidget(new ImageButton(
                rightX - iconSpacing, iconY, 20, 20, LANGUAGE_SPRITES,
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()));
                    }
                }
        ));

        // GitHub Repository
        this.addRenderableWidget(new ImageButton(
                rightX - iconSpacing * 2, iconY, 20, 20, GITHUB_SPRITES,
                ConfirmLinkScreen.confirmLink(this, "https://github.com/JMiahMan1/Hearthwind", true)
        ));
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void renderAgedBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Fullscreen atmospheric background
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEX, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

        // Footer Branding
        Font font = Minecraft.getInstance().font;
        graphics.text(font, "Hearthwind 26.2", 12, this.height - 18, 0xFFB0A288, true);
        String mcVer = "Minecraft 26.2 / Fabric";
        graphics.text(font, mcVer, this.width - font.width(mcVer) - 12, this.height - 18, 0xFF777777, true);
    }

    /**
     * Sleek, frameless text button for the authentic Hearthwind menu.
     */
    public static class HearthwindMenuButton extends Button {
        private final int hoverColor;

        public HearthwindMenuButton(int x, int y, int width, int height, Component message, OnPress onPress, int hoverColor) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.hoverColor = hoverColor;
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            boolean hovered = this.isHovered();
            Font font = Minecraft.getInstance().font;

            int textX = this.getX() + (hovered ? 6 : 0);
            int textY = this.getY() + (this.getHeight() - 8) / 2;
            int color = hovered ? this.hoverColor : 0xFFDDD6CC;

            if (hovered) {
                graphics.text(font, "▸", this.getX() - 8, textY, this.hoverColor, true);
            }

            graphics.text(font, this.getMessage().getString(), textX, textY, color, true);
        }
    }
}
