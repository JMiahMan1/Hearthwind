package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.survival.GuideBook;
import io.wispforest.lavender.book.Book;
import io.wispforest.lavender.book.BookLoader;
import io.wispforest.lavender.client.LavenderBookScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.resources.Identifier;

/**
 * The Hearthwind guide book loads from client assets, has every chapter, and
 * renders: landing page, a drop-cap entry, a recipe-embed entry. Screenshots
 * are the visual check for the manuscript art (parchment, initials, dividers).
 */
public class GuideBookGameTests implements FabricClientGameTest {
    private static final int SLOW_TIMEOUT_TICKS = 20 * 300;
    private static final int MIN_ENTRIES = 37;
    private static final int MIN_CATEGORIES = 9;
    private static final String[] TOUR = {
            "hearthwind:first_days/crafting_rock",
            "hearthwind:water/flasks",
            "hearthwind:heat_and_cold/clothing",
            "hearthwind:crafts/metals",
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender(SLOW_TIMEOUT_TICKS);
            context.waitTicks(40);

            String shape = context.computeOnClient(minecraft -> {
                Book book = BookLoader.get(GuideBook.ID);
                return book == null ? "missing" : book.entries().size() + "/" + book.categories().size();
            });
            if ("missing".equals(shape)) {
                throw new AssertionError("hearthwind guide book did not load from assets/hearthwind/lavender");
            }
            String[] parts = shape.split("/");
            if (Integer.parseInt(parts[0]) < MIN_ENTRIES || Integer.parseInt(parts[1]) < MIN_CATEGORIES) {
                throw new AssertionError("guide book is incomplete: entries/categories = " + shape);
            }

            LavenderBookScreen[] opened = new LavenderBookScreen[1];
            context.setScreen(() -> opened[0] = new LavenderBookScreen(BookLoader.get(GuideBook.ID)));
            context.waitTicks(30);
            context.takeScreenshot("guidebook_landing");

            for (String entryId : TOUR) {
                context.runOnClient(minecraft -> {
                    LavenderBookScreen screen = opened[0];
                    Book book = BookLoader.get(GuideBook.ID);
                    var entry = book.entryById(Identifier.parse(entryId));
                    if (entry == null) {
                        throw new AssertionError("guide entry missing: " + entryId);
                    }
                    screen.navPush(new LavenderBookScreen.EntryPageSupplier(screen, entry));
                });
                context.waitTicks(30);
                context.takeScreenshot("guidebook_" + entryId.substring(entryId.indexOf(':') + 1).replace('/', '_'));
            }
        }
    }
}
