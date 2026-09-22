package io.wispforest.lavender.book;

import com.google.common.collect.ImmutableSet;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Function;

public record Entry(
        Identifier id,
        List<Identifier> categories,
        String title,
        Function<Sizing, UIComponent> iconFactory,
        boolean secret,
        int ordinal,
        ImmutableSet<Identifier> requiredAdvancements,
        ImmutableSet<ItemStack> associatedItems,
        ImmutableSet<String> additionalSearchTerms,
        String content
) implements Book.BookmarkableElement {

    public boolean canPlayerView(LocalPlayer player) {
        if (this.requiredAdvancements.isEmpty()) return true;

        var advancements = player.connection.getAdvancements();
        for (var advancementId : this.requiredAdvancements) {
            var holder = advancements.get(advancementId);
            if (holder == null) return false;
            if (advancements.getTree().get(holder) == null) return false;
        }

        return true;
    }

}
