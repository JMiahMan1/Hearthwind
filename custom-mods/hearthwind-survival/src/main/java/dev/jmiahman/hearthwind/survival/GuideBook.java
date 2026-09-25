package dev.jmiahman.hearthwind.survival;

import io.wispforest.lavender.book.LavenderBookItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * The Hearthwind guidebook: a Lavender book whose pages live in
 * {@code assets/hearthwind/lavender/} (Lavender loads books from CLIENT
 * resources). Only touched when Lavender is loaded, so this class is the
 * single place the survival module references Lavender types.
 */
public final class GuideBook {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("hearthwind", "hearthwind_guide_book");

    private GuideBook() {
    }

    static void register() {
        LavenderBookItem.registerForBook(ID, ID, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }
}
