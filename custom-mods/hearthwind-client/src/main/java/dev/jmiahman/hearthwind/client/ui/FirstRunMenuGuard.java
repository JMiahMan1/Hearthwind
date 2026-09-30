package dev.jmiahman.hearthwind.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Closes FancyMenu's first-run greeting, which appears over the main menu.
 *
 * <p>The pack's FancyMenu configuration is byte-for-byte the reference pack's
 * apart from the window title, so nothing here is a Hearthwind preference.
 * {@code modpack_mode = 'true'} is the value that matters: FancyMenu's
 * {@code MixinGui} opens the "Thank you for using FancyMenu!" panel only when
 * {@code showWelcomeScreen && !modpackMode && screen instanceof TitleScreen},
 * so modpack mode suppresses it by design. The panel is nevertheless visible in
 * our gametest screenshots, because the headless client rebuilds the game
 * directory on every run and FancyMenu rewrites its own config there, putting
 * {@code modpack_mode} back to {@code false}. Re-asserting the shipped values
 * every tick is what keeps the tests showing the menu a player actually gets.
 *
 * <p>Only a FancyMenu screen that opens by itself while no world is loaded, and
 * only shortly after start-up, is closed. A screen the player opened from the
 * overlay bar afterwards is left alone, so the editor stays reachable.
 */
public final class FirstRunMenuGuard {
    /** The package every FancyMenu screen lives in. */
    public static final String FANCYMENU_PACKAGE = "de.keksuccino.fancymenu.";

    /**
     * Ticks after start-up during which a self-opened FancyMenu screen counts
     * as first-run noise. Ten seconds is long enough to catch the greeting and
     * short enough that it cannot swallow a screen someone opened on purpose.
     */
    public static final int WINDOW_TICKS = 200;

    private int ticks;

    private boolean windowsClosed;

    private FirstRunMenuGuard() {
    }

    private static FirstRunMenuGuard instance;

    public static FirstRunMenuGuard get() {
        if (instance == null) {
            instance = new FirstRunMenuGuard();
        }
        return instance;
    }

    /**
     * Puts FancyMenu's menu options back the way the pack ships them.
     *
     * <p>Not a workaround for a misconfigured pack: the pack's
     * {@code config/fancymenu/options.txt} is the reference pack's file
     * unchanged, and it already sets {@code modpack_mode = 'true'} and the three
     * editor flags {@code false}. The options are not honoured because FancyMenu
     * regenerates that file on first launch - a freshly created game directory
     * comes back with {@code modpack_mode = false} and
     * {@code show_customization_overlay = true} no matter what the pack shipped,
     * and the customization directory the pack ships is rebuilt too. So the
     * values are restored here, at runtime, instead.
     *
     * <p>{@code modpack_mode} is the one that actually matters, and getting that
     * wrong is what made the first-run panel survive three rounds of
     * suppression. FancyMenu's {@code MixinGui} opens the welcome panel under
     * exactly this condition:
     *
     * <pre>{@code
     * if (showWelcomeScreen.getValue()
     *         && !modpackMode.getValue()
     *         && screen instanceof TitleScreen) {
     *     showWelcomeScreen.setValue(false);
     *     WelcomeWindowBody.openInWindow();
     * }
     * }</pre>
     *
     * <p>so the second term, {@code !modpackMode}, suppresses the panel by
     * design: FancyMenu assumes a pack that ships {@code modpack_mode = true}
     * has already greeted its players. Ours does not, so a fresh game directory
     * - which is exactly what our client gametest creates on every run - shows
     * the panel until modpack mode is back on. Clearing
     * {@code show_welcome_screen} alone cannot help, because the panel had
     * already opened by the time anything read it.
     *
     * <p>Both the option objects and the whole class are resolved by name, so
     * this compiles and runs whether or not FancyMenu is present.
     *
     * @return true if at least one option was found and changed
     */
    public static boolean disableMenuEditorOptions() {
        int changed = 0;
        changed += force("modpackMode", Boolean.TRUE);
        changed += forceFalse("showCustomizationOverlay");
        changed += forceFalse("showWelcomeScreen");
        changed += forceFalse("advancedCustomizationMode");
        return changed > 0;
    }

    /**
     * The live value of one of FancyMenu's editor options, or null when
     * FancyMenu is absent or the lookup failed. A test seam: the flags being off
     * is the whole point of the runtime override, and a screenshot cannot prove
     * a boolean.
     *
     * <p>{@link #lastLookupError()} carries the reason a lookup failed. Swallowing
     * it is what made two earlier rounds of this look like a working guard: the
     * value read back null, null was accepted as "not shown", and the panel kept
     * opening. A reflection that cannot run must say so.
     */
    public static Boolean menuEditorOption(String optionName) {
        try {
            Object option = option(optionName);
            return (Boolean) option.getClass().getMethod("getValue").invoke(option);
        } catch (ReflectiveOperationException | RuntimeException failure) {
            lastError = optionName + ": " + failure;
            return null;
        }
    }

    /** Why the last {@link #menuEditorOption} lookup failed, or null. */
    public static String lastLookupError() {
        return lastError;
    }

    private static String lastError;

    private static int forceFalse(String optionName) {
        return force(optionName, Boolean.FALSE);
    }

    /**
     * Sets one of FancyMenu's boolean options to {@code wanted}, reporting
     * whether it actually had to be changed.
     */
    private static int force(String optionName, Boolean wanted) {
        try {
            Object option = option(optionName);
            // getValue()/setValue(), not a field: AbstractOptions$Option keeps
            // no `value` field at all - the value lives in the Konkrete config
            // behind `config`/`key` and is only reachable through these two
            // methods. Writing a field that does not exist throws, and the throw
            // was being swallowed, which is why a flag appeared to be set and the
            // panel appeared anyway.
            Object current = option.getClass().getMethod("getValue").invoke(option);
            if (current instanceof Boolean flag && !flag.equals(wanted)) {
                option.getClass().getMethod("setValue", Object.class).invoke(option, wanted);
                return 1;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // FancyMenu absent, renamed, or the API moved: nothing to do.
        }
        return 0;
    }

    /**
     * Looks one of FancyMenu's option objects up by its Java field name - the
     * camelCase {@code Options} field, NOT the snake_case key it writes to
     * {@code options.txt}. {@code Options} declares
     * {@code modpackMode}, {@code showWelcomeScreen}, {@code showCustomizationOverlay}
     * and {@code advancedCustomizationMode}; asking for the config spelling
     * finds nothing, and a lookup that finds nothing looks exactly like a guard
     * that is not running.
     */
    private static Object option(String fieldName) throws ReflectiveOperationException {
        Class<?> fancyMenu = Class.forName("de.keksuccino.fancymenu.FancyMenu");
        // The Options fields are INSTANCE fields, so they have to be read off
        // FancyMenu.getOptions() and not off the class. Reading them off the
        // class handed back null, which the guard counted as "option not found"
        // and the test counted as "nothing to see here" - so the flags were never
        // actually set and the panel kept opening, while both looked green.
        Object options = fancyMenu.getMethod("getOptions").invoke(null);
        if (options == null) {
            throw new IllegalStateException("FancyMenu.getOptions() returned null");
        }
        return options.getClass().getField(fieldName).get(options);
    }

    /**
     * Closes FancyMenu's floating windows - its first-run "Thank you for using
     * FancyMenu!" panel among them - using the mod's own handler.
     *
     * <p>Kept as a backstop behind {@link #disableMenuEditorOptions()}, which is
     * the half that actually prevents it. The panel is a picture-in-picture
     * window body ({@code WelcomeWindowBody}) floating above the still-open
     * title screen, so it is neither a {@link Screen} nor something closing
     * screens touches.
     *
     * @return true when FancyMenu's window handler was found and asked to close
     */
    public static boolean closeFancyMenuWindows() {
        try {
            Class<?> handler = Class.forName(
                    "de.keksuccino.fancymenu.util.rendering.ui.pipwindow.PiPWindowHandler");
            Object instance = handler.getField("INSTANCE").get(null);
            handler.getMethod("closeAllWindows").invoke(instance);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    public void tick(Minecraft client) {
        if (this.ticks < WINDOW_TICKS) {
            this.ticks++;
        }
        Screen screen = client.gui.screen();
        // The flag has to be off BEFORE FancyMenu's own injection reads it.
        // MixinGui opens the panel like this:
        //     if (FancyMenu.getOptions().showWelcomeScreen.getValue()) {
        //         WelcomeWindowBody.openInWindow();
        //         FancyMenu.getOptions().showWelcomeScreen.setValue(false);
        //     }
        // so the option is a one-shot that the mod clears itself AFTER opening,
        // which is why observing it as false later proves nothing and why
        // setting it once at mod init is too early: FancyMenu rebuilds its
        // Options from a freshly generated file and can put it back. Re-asserting
        // it every tick until the title screen has been up means the read always
        // sees false. Aged ships this panel on first launch too; we suppress it
        // on purpose, which is a recorded deviation.
        if (this.ticks < WINDOW_TICKS) {
            disableMenuEditorOptions();
        }
        if (screen != null && shouldClose(screen.getClass().getName(), client.level != null, this.ticks)) {
            client.setScreenAndShow(null);
        }
        if (this.ticks < WINDOW_TICKS && !windowsClosed) {
            this.windowsClosed = closeFancyMenuWindows();
        }
    }

    /**
     * The decision on its own, so a test can pin it without a FancyMenu screen.
     *
     * @param className the class name of the open screen
     * @param inWorld whether a world is loaded
     * @param ticksSinceStart ticks since this guard started counting
     * @return true when the screen should be closed
     */
    public static boolean shouldClose(String className, boolean inWorld, int ticksSinceStart) {
        return !inWorld
                && ticksSinceStart < WINDOW_TICKS
                && className.startsWith(FANCYMENU_PACKAGE);
    }
}
