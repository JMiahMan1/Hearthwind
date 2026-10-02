package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.client.HearthwindPanelScreen;
import dev.jmiahman.hearthwind.client.JobsScreen;
import dev.jmiahman.hearthwind.client.NutrientsScreen;
import dev.jmiahman.hearthwind.client.PartyScreen;
import dev.jmiahman.hearthwind.client.SkillInfoScreen;
import dev.jmiahman.hearthwind.client.SkillRestrictionScreen;
import dev.jmiahman.hearthwind.client.SkillsScreen;
import dev.jmiahman.hearthwind.client.TabStrip;
import dev.jmiahman.hearthwind.client.WelcomeScreen;
import dev.jmiahman.hearthwind.survival.StarterKit;
import draylar.inmis.client.InmisBackpackLayer;
import draylar.inmis.mixin.client.LivingEntityRendererInvoker;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import dev.jmiahman.hearthwind.client.ui.FirstRunMenuGuard;
import dev.jmiahman.hearthwind.survival.hydration.ThirstPreview;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Tours every Hearthwind UI surface in one pass against a real client:
 * inventory (E), Nutrients (N), Skills (K), Jobs (J), Party (P) - one
 * screenshot each. Catches screen/keybind/registration regressions across
 * the whole client mod set in a single headless run.
 *
 * <p>Every screen is driven through {@link ClientGameTestContext#setScreen}
 * rather than the keybinds: the fabric-gametest keypress plumbing
 * (holdKey -&gt; WindowMixin -&gt; KeyboardHandler -&gt; KeyMapping.click) does
 * not reliably deliver custom keybinds under headless software GL, so
 * keybind-driven screen transitions time out even though the screens
 * themselves are fine.
 */
public class ScreensTourGameTests implements FabricClientGameTest {
    private static final int SLOW_TIMEOUT_TICKS = 20 * 300;

    /**
     * The welcome screen's Start button must be a real, visible widget: inside
     * the window, big enough to hit, and not hidden. This screen once called
     * {@code super.extractRenderState} FIRST and then painted an opaque
     * full-screen fill over it, so the button existed, was clickable and was
     * invisible - the "I cannot find the way in" reports. The same trap is why
     * every hearthwind screen now extracts its widgets last; the pixels of
     * {@code 0023_tour_welcome.png} in the same run are the evidence.
     */
    /**
     * The main menu must paint Hearthwind's art, and that art must actually
     * resolve. A path that does not resolve renders as the missing-texture
     * checkerboard - magenta and black - which is exactly what players saw:
     * the pack shipped a FancyMenu layout pointing at
     * {@code aged:textures/main_menu_background.png}, and that file only ever
     * reached the instance as {@code .minecraft/resources/aged/textures/}, a
     * folder no game ever loads, so both the image and its fallback were
     * missing. A screenshot alone did not catch it because the test client
     * never applied the layout, so this asserts the resource directly.
     */
    private static void assertTitleScreenArtResolves(ClientGameTestContext context) {
        context.runOnClient(client -> {
            List<Identifier> art = new ArrayList<>();
            // The full-screen background, and every button/icon the mixin
            // paints: once as the sprite the atlas bakes, once as the plain
            // texture where one exists.
            art.add(Identifier.fromNamespaceAndPath("hearthwind",
                    "textures/gui/title/main_menu_background.png"));
            art.add(Identifier.fromNamespaceAndPath("hearthwind", "textures/gui/title/blank_button.png"));
            for (String name : List.of("language", "language_hovered", "discord", "discord_hovered",
                    "github", "github_hovered", "mannequin", "mannequin_hovered",
                    "modrinth", "modrinth_hovered")) {
                art.add(Identifier.fromNamespaceAndPath("hearthwind", "textures/gui/sprites/title/"
                        + name + ".png"));
                art.add(Identifier.fromNamespaceAndPath("hearthwind", "textures/gui/title/" + name + ".png"));
            }
            List<String> missing = new ArrayList<>();
            for (Identifier id : art) {
                if (client.getResourceManager().getResource(id).isEmpty()) {
                    missing.add(id.toString());
                }
            }
            if (!missing.isEmpty()) {
                throw new AssertionError("the main menu art does not resolve, so it would draw as the "
                        + "magenta-and-black missing-texture checkerboard: " + missing);
            }
        });
    }

    /**
     * Every welcome block must resolve to real text, not just exist as a key: an
     * unresolvable key renders as its own dotted name, which is the exact "the
     * screen is broken" symptom the button assertion exists for. This asks the
     * screen for all of its text rather than the subset that fits, because on a
     * 427x240 test window the layout gives blocks up from the end and the
     * screenshot alone would not show whether the dropped ones are valid.
     */
    private static void assertWelcomeScreenShowsTheKeysAndLoadout(ClientGameTestContext context) {
        context.runOnClient(client -> {
            if (!(client.gui.screen() instanceof WelcomeScreen welcome)) {
                throw new AssertionError("the welcome screen is not open, so its new blocks cannot be checked");
            }
            String joined = String.join(" ", welcome.debugAllText());
            for (String needle : List.of("skills", "nutrition", "jobs", "party", "backpack")) {
                if (!joined.contains(needle)) {
                    throw new AssertionError("the keybind block is missing \"" + needle + "\"; the screen says: " + joined);
                }
            }
            for (String needle : List.of("Survival Guidebook", "skill points", "campfire")) {
                if (!joined.contains(needle)) {
                    throw new AssertionError("the loadout block is missing \"" + needle + "\"; the screen says: " + joined);
                }
            }
        });
    }

    /**
     * The first-run menu guard closes FancyMenu's greeting and nothing else. Its
     * decision is pure, so it is pinned here rather than inferred from a
     * screenshot: FancyMenu's own screen is closed while no world is loaded and
     * within the start-up window, a world-loaded screen is never closed, a later
     * screen is left for the player who opened it, and our own screens are never
     * touched.
     */
    private static void assertFirstRunMenuGuardDecidesCorrectly() {
        String fancy = "de.keksuccino.fancymenu.customization.overlay.CustomizationOverlayUI";
        if (!FirstRunMenuGuard.shouldClose(fancy, false, 10)) {
            throw new AssertionError("the guard must close FancyMenu's first-run greeting on the main menu");
        }
        if (FirstRunMenuGuard.shouldClose(fancy, true, 10)) {
            throw new AssertionError("the guard must never close a screen once a world is loaded");
        }
        if (FirstRunMenuGuard.shouldClose(fancy, false, FirstRunMenuGuard.WINDOW_TICKS)) {
            throw new AssertionError("the guard must leave a FancyMenu screen the player opened later alone");
        }
        for (String ours : List.of("net.minecraft.client.gui.screens.TitleScreen",
                "dev.jmiahman.hearthwind.client.WelcomeScreen",
                "net.minecraft.client.gui.screens.PauseScreen")) {
            if (FirstRunMenuGuard.shouldClose(ours, false, 10)) {
                throw new AssertionError("the guard must never close a non-FancyMenu screen: " + ours);
            }
        }
    }

    /**
     * FancyMenu's editor bar and first-run panel sit over the main menu, and it
     * rewrites its own config on first launch, so the pack's options.txt asking
     * for them off is not enough. The client turns them off at runtime; this
     * proves it, because a screenshot can show a panel that is about to vanish
     * and cannot show a boolean.
     */
    private static void assertFancyMenuEditorIsNotShown(ClientGameTestContext context) {
        context.runOnClient(client -> {
            // modpack_mode is the one that actually decides. FancyMenu's MixinGui
            // opens the welcome panel when
            //     showWelcomeScreen && !modpackMode && screen instanceof TitleScreen
            // so modpack_mode = true suppresses it by design. It is the value the
            // pack ships; a fresh game directory overwrites it to false, which is
            // why the panel showed in every earlier screenshot of this test.
            Boolean modpackMode = FirstRunMenuGuard.menuEditorOption("modpackMode");
            if (modpackMode == null) {
                throw new AssertionError("cannot read FancyMenu's modpackMode option, so the guard is not running: "
                        + FirstRunMenuGuard.lastLookupError());
            }
            if (!modpackMode) {
                throw new AssertionError("FancyMenu's modpack_mode is off, so it opens its first-run panel "
                        + "over the main menu; the pack ships it true");
            }
            for (String option : new String[] {"showCustomizationOverlay", "showWelcomeScreen",
                    "advancedCustomizationMode"}) {
                Boolean value = FirstRunMenuGuard.menuEditorOption(option);
                // null is NOT a pass: it means the reflection failed and the
                // guard is not running. An earlier version of this test accepted
                // null, so it went green while the panel kept opening.
                if (value == null) {
                    throw new AssertionError("cannot read FancyMenu's " + option
                            + ", so the guard is not actually running");
                }
                if (value) {
                    throw new AssertionError("FancyMenu's " + option + " is on, so its editor draws over the main menu");
                }
            }
        });
    }

    private static void assertStartButtonReachable(ClientGameTestContext context) {
        context.runOnClient(client -> {
            if (!(client.gui.screen() instanceof WelcomeScreen screen)) {
                throw new AssertionError("the welcome screen is not open");
            }
            AbstractWidget start = null;
            for (Object child : screen.children()) {
                if (child instanceof AbstractWidget widget && widget.getMessage().getString()
                        .equals(Component.translatable("screen.hearthwind.welcome.start").getString())) {
                    start = widget;
                }
            }
            if (start == null) {
                throw new AssertionError("the welcome screen has no Start button widget");
            }
            int[] box = {start.getX(), start.getY(), start.getWidth(), start.getHeight()};
            if (box[0] < 0 || box[1] < 0 || box[0] + box[2] > screen.width || box[1] + box[3] > screen.height) {
                throw new AssertionError("the Start button is off-screen: x=" + box[0] + " y=" + box[1]
                        + " w=" + box[2] + " h=" + box[3] + " screen=" + screen.width + "x" + screen.height);
            }
            if (!start.visible || !start.active) {
                throw new AssertionError("the Start button is not renderable: visible=" + start.visible
                        + " active=" + start.active);
            }
        });
    }

    /**
     * The thirst droplet preview, asserted rather than photographed.
     *
     * <p>26.2 builds item tooltips entirely on the client, so this is the only
     * place the resolution can actually be checked end to end: it runs the real
     * {@code ItemStack#getTooltipImage} on the client and inspects what comes
     * back. The player's starter apples are the probe - an apple is catalogued
     * at hydration 4, so the preview must be a 4-droplet row.
     *
     * <p>Geometry, not a screenshot, because a screenshot of a tooltip is easy
     * to get right by accident (the "invisible Start button" release) and easy
     * to get wrong without noticing (a downed overlay that never drew at all).
     */
    private static void assertThirstPreviewReachesTheTooltip(ClientGameTestContext context) {
        context.runOnClient(client -> {
            if (client.player == null) {
                throw new AssertionError("no player, so no inventory to read a preview from");
            }
            if (dev.jmiahman.hearthwind.survival.hydration.ClientHydration.size() == 0) {
                throw new AssertionError("the hydration corpus never reached the client - the thirst "
                        + "preview would silently be empty for every food");
            }
            var apple = net.minecraft.world.item.Items.APPLE;
            int corpus =
                    dev.jmiahman.hearthwind.survival.hydration.ClientHydration
                            .quench(new net.minecraft.world.item.ItemStack(apple));
            if (corpus <= 0) {
                throw new AssertionError("an apple is not catalogued on the client, corpus="
                        + corpus);
            }
            var preview = new net.minecraft.world.item.ItemStack(apple).getTooltipImage();
            if (preview.isEmpty()) {
                throw new AssertionError("hovering an apple produces no thirst preview; "
                        + "thirstPreview=" + dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig
                                .get().thirst.thirstPreview);
            }
            if (!(preview.get() instanceof dev.jmiahman.hearthwind.survival.hydration.ThirstPreview p)) {
                throw new AssertionError("an apple's tooltip image is a "
                        + preview.get().getClass().getName() + ", not a ThirstPreview");
            }
            if (p.quench() != corpus) {
                throw new AssertionError("the tooltip promises " + p.quench()
                        + " thirst but the corpus says " + corpus);
            }
            int width = ThirstPreview.widthFor(p.quench());
            var component = ClientTooltipComponent.create(preview.get());
            int fontWidth = component.getWidth(client.font);
            int height = component.getHeight(client.font);
            if (fontWidth != width) {
                throw new AssertionError("the rendered row is " + fontWidth
                        + " wide but the reference's arithmetic says " + width);
            }
            if (height != ThirstPreview.HEIGHT) {
                throw new AssertionError("the row is " + height
                        + " tall but the reference hardcodes " + ThirstPreview.HEIGHT);
            }
            // A splash potion is refused outright by the reference, and this is
            // the assertion that would catch a hook that matched everything.
            var splash = new net.minecraft.world.item.ItemStack(
                    net.minecraft.world.item.Items.SPLASH_POTION);
            if (!splash.getTooltipImage().isEmpty()) {
                throw new AssertionError("a splash potion must show no thirst preview, got "
                        + splash.getTooltipImage().get());
            }
        });
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        // Capture the Aged/FancyMenu title-screen layout before entering a
        // world - first-impression parity evidence for every run.
        context.waitForScreen(net.minecraft.client.gui.screens.TitleScreen.class);
        context.waitTicks(40);
        assertTitleScreenArtResolves(context);
        assertFancyMenuEditorIsNotShown(context);
        context.takeScreenshot("tour_title");

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender(SLOW_TIMEOUT_TICKS);
            context.waitTicks(40);

            // Aged's welcome screen: it opens on a first join and its Start
            // button is what hands over the starter loadout, so this pass
            // covers the whole path - screen, button, five hotbar slots.
            // The tour asks the server for a deterministic first join (a
            // session that joined a world earlier already has the starter tag,
            // so the screen would legitimately stay closed). waitFor fails the
            // test on timeout.
            world.getServer().computeOnServer(server -> {
                net.minecraft.server.level.ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                StarterKit.resetAndOfferWelcomeScreen(p);
                return Boolean.TRUE;
            });
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof WelcomeScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            assertStartButtonReachable(context);
            assertFirstRunMenuGuardDecidesCorrectly();
            assertWelcomeScreenShowsTheKeysAndLoadout(context);
            assertThirstPreviewReachesTheTooltip(context);
            context.takeScreenshot("tour_welcome");
            context.clickScreenButton("screen.hearthwind.welcome.start");
            context.waitFor(minecraft -> minecraft.gui.screen() == null, SLOW_TIMEOUT_TICKS);
            // Twenty ticks is far short of the server's 60-tick fallback, and
            // in singleplayer the two tick together: the loadout can only be in
            // the hotbar this early if the Start button's payload did it.
            context.waitTicks(20);
            for (int slot : new int[] {StarterKit.SLOT_BREAD, StarterKit.SLOT_APPLE, StarterKit.SLOT_GUIDE,
                    StarterKit.SLOT_PURIFIED_WATER, StarterKit.SLOT_CAMPFIRE}) {
                int count = world.getServer().computeOnServer(server -> {
                    net.minecraft.server.level.ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                    return p.getInventory().getItem(slot).getCount();
                });
                if (count == 0) {
                    throw new AssertionError("Start left hotbar slot " + slot + " empty");
                }
            }
            closeAnyScreen(context);

            // Drive every screen through context.setScreen: the fabric-gametest
            // keypress plumbing (holdKey -> WindowMixin -> KeyboardHandler ->
            // KeyMapping.click) does not reliably deliver custom keybinds under
            // headless software GL, so keybind-driven screen transitions time
            // out even though the screens themselves are fine.
            Player player = context.computeOnClient(minecraft -> minecraft.player);
            context.setScreen(() -> new InventoryScreen(player));
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof InventoryScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_inventory");

            // Inmis parity: a chest-slot backpack renders on the player's
            // back. Equip one client-side, assert the layer is registered on
            // the avatar renderer, then spin the inventory preview away from
            // the camera so the screenshot shows the back.
            context.runOnClient(minecraft -> minecraft.player.setItemSlot(EquipmentSlot.CHEST,
                    new ItemStack(BuiltInRegistries.ITEM.get(Identifier.parse("inmis:frayed_backpack"))
                            .orElseThrow().value())));
            context.waitTicks(5);
            context.runOnClient(minecraft -> {
                var renderer = minecraft.getEntityRenderDispatcher().getRenderer(minecraft.player);
                if (!(renderer instanceof LivingEntityRendererInvoker invoker)
                        || invoker.inmis$layers().stream().noneMatch(layer -> layer instanceof InmisBackpackLayer)) {
                    throw new AssertionError("the Inmis backpack layer must be registered on the avatar renderer");
                }
                if (minecraft.gui.screen() instanceof InventoryScreen screen) {
                    screen.mouseMoved(20, minecraft.getWindow().getGuiScaledHeight() / 2.0);
                }
            });
            context.waitTicks(10);
            context.takeScreenshot("tour_backpack_on_back");

            // BackSlot parity: the back and belt slots render their items on
            // the avatar and in the HUD. Fill both client-side, assert the
            // layers are registered, and screenshot the inventory (the two
            // extra slots sit right of the shield slot at Aged's positions).
            context.runOnClient(minecraft -> {
                net.backslot.BackSlotSlots.set(minecraft.player, net.backslot.BackSlot.BACK_SLOT,
                        new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
                net.backslot.BackSlotSlots.set(minecraft.player, net.backslot.BackSlot.BELT_SLOT,
                        new ItemStack(net.minecraft.world.item.Items.LANTERN));
            });
            context.waitTicks(5);
            context.runOnClient(minecraft -> {
                var renderer = minecraft.getEntityRenderDispatcher().getRenderer(minecraft.player);
                if (!(renderer instanceof net.backslot.mixin.client.AvatarRendererInvoker invoker)
                        || invoker.backslot$layers().stream()
                                .noneMatch(layer -> layer instanceof net.backslot.client.BackItemLayer)
                        || invoker.backslot$layers().stream()
                                .noneMatch(layer -> layer instanceof net.backslot.client.BeltItemLayer)) {
                    throw new AssertionError("the BackSlot layers must be registered on the avatar renderer");
                }
                if (minecraft.gui.screen() instanceof InventoryScreen screen) {
                    screen.mouseMoved(20, minecraft.getWindow().getGuiScaledHeight() / 2.0);
                }
            });
            context.waitTicks(10);
            context.takeScreenshot("tour_backslot");

            // BackSlot Addon parity: a shield on the back and a sword on the
            // belt get their own hip-mounted transforms.
            context.runOnClient(minecraft -> {
                net.backslot.BackSlotSlots.set(minecraft.player, net.backslot.BackSlot.BACK_SLOT,
                        new ItemStack(net.minecraft.world.item.Items.SHIELD));
                net.backslot.BackSlotSlots.set(minecraft.player, net.backslot.BackSlot.BELT_SLOT,
                        new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
            });
            context.waitTicks(10);
            context.takeScreenshot("tour_backslot_addon");

            context.setScreen(NutrientsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof NutrientsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_nutrients");

            context.setScreen(SkillsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_skills");

            // Aged hub parity: the right rail's attributes slide-out and the
            // mining/crafting gate buttons.
            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof SkillsScreen screen) {
                    screen.toggleAttributes();
                }
            });
            context.waitTicks(5);
            context.takeScreenshot("tour_skills_attributes");

            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof SkillsScreen screen) {
                    screen.openRestrictions(SkillRestrictionScreen.ListKind.MINING);
                }
            });
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillRestrictionScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_skill_restrictions_mining");

            context.setScreen(SkillsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillsScreen, SLOW_TIMEOUT_TICKS);
            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof SkillsScreen screen) {
                    screen.openRestrictions(SkillRestrictionScreen.ListKind.CRAFTING);
                }
            });
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillRestrictionScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_skill_restrictions_crafting");

            context.setScreen(SkillsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);

            context.setScreen(() -> new SkillInfoScreen("mining"));
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillInfoScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_skill_info");

            // The unlock list scrolls through every level (Aged's scrollable
            // info page); jump a few rows down and confirm the screen keeps
            // rendering with the slider thumb moved.
            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof SkillInfoScreen screen) {
                    screen.scrollTo(4);
                }
            });
            context.waitTicks(5);
            context.takeScreenshot("tour_skill_info_scrolled");

            context.setScreen(SkillsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);

            context.setScreen(JobsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof JobsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            // Aged's panel chrome, measured off Job_Screen.png: the panel is
            // 200x215 and the four LibZ tabs FLOAT on the 21 rows above it, so
            // a click there picks a tab and a click inside the panel must not.
            context.runOnClient(minecraft -> {
                if (!(minecraft.gui.screen() instanceof JobsScreen screen)) {
                    throw new AssertionError("the Jobs screen should be open for the tab strip check");
                }
                int left = screen.panelLeft();
                int top = screen.panelTop();
                if (TabStrip.clicked(left + 4, top - 15, left, top, TabStrip.Tab.JOBS)
                        != TabStrip.Tab.INVENTORY) {
                    throw new AssertionError("the first tab must be clickable above the panel");
                }
                if (TabStrip.clicked(left + 4, top + 5, left, top, TabStrip.Tab.JOBS) != null) {
                    throw new AssertionError("a click inside the panel must not hit a tab");
                }
                // LibZ hit-tests every UNSELECTED tab as 21 rows, even the
                // first, which is drawn 25 tall. Ours used to take 25 and so
                // swallowed the top 4 rows of the panel underneath.
                if (TabStrip.clicked(left + 4, top + 3, left, top, TabStrip.Tab.JOBS) != null) {
                    throw new AssertionError("an unselected first tab must not reach into the panel");
                }
                // The reference's hover titles are translatable screen names,
                // not literals with key hints glued on.
                for (TabStrip.Tab tab : TabStrip.Tab.values()) {
                    String title = tab.title().getString();
                    if (title.isEmpty() || title.contains("[") || title.contains("]")) {
                        throw new AssertionError("tab title should be a plain screen name, got: " + title);
                    }
                }
                if (!TabStrip.Tab.INVENTORY.title().getString().equals("Crafting")) {
                    throw new AssertionError("the first tab is Crafting in the reference, got: "
                            + TabStrip.Tab.INVENTORY.title().getString());
                }
                // No backpack equipped in the tour, so the conditional fifth
                // tab must not be drawn and must not be clickable.
                if (TabStrip.backpackTab(minecraft) != null) {
                    throw new AssertionError("the backpack tab needs an equipped backpack");
                }
                if (TabStrip.visibleTabs(minecraft).length != TabStrip.Tab.values().length - 1) {
                    throw new AssertionError("without a backpack the strip is four tabs wide");
                }
                if (HearthwindPanelScreen.PANEL_W != 200 || HearthwindPanelScreen.PANEL_H != 215) {
                    throw new AssertionError("the Aged panel is 200x215, not "
                            + HearthwindPanelScreen.PANEL_W + "x" + HearthwindPanelScreen.PANEL_H);
                }
            });
            context.takeScreenshot("tour_jobs");

            context.setScreen(PartyScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof PartyScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_party");

            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitTicks(5);
        }
    }

    private static void closeAnyScreen(ClientGameTestContext context) {
        for (int i = 0; i < 3 && Boolean.TRUE.equals(
                context.computeOnClient(minecraft -> minecraft.gui.screen() != null)); i++) {
            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitTicks(5);
        }
        context.waitFor(minecraft -> minecraft.gui.screen() == null, 100);
    }
}