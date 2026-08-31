package dev.jmiahman.hearthwind.primitive;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Item/block tag keys used by the primitive module. */
public final class HearthwindPrimitiveTags {
    private HearthwindPrimitiveTags() {}

    public static final TagKey<Item> USABLE_CRAFTING_ROCK_ITEMS = bindItem("usable_crafting_rock_items");
    public static final TagKey<Item> BARK_ITEMS = bindItem("bark_items");

    private static TagKey<Item> bindItem(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("earlystage", path));
    }
}
