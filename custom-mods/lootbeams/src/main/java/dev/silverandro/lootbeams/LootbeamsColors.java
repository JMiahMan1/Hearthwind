package dev.silverandro.lootbeams;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * The beam takes the colour of the item's name, the same rule as
 * upstream's ColorGetter: the first tooltip line's style colour (which
 * carries the rarity colour or a custom name colour), with the first
 * sibling as a fallback. White means "common" and is filtered later
 * unless {@code showWhiteItems} is on.
 */
public final class LootbeamsColors {
    public static final int WHITE = 0xFFFFFF;

    private LootbeamsColors() {
    }

    public static int colorFor(ItemEntity item, LootbeamsConfig config) {
        ItemStack stack = config.useBaseColor ? new ItemStack(item.getItem().getItem()) : item.getItem();
        return colorFromStack(stack);
    }

    public static int colorFromStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return WHITE;
        }
        // Read the name and rarity components directly. Building a tooltip
        // here would fire every ItemTooltipCallback per tick, and the
        // bundled kiwi build calls I18n.exists (removed in 26.2), which
        // crashed the client.
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
            TextColor style = customName.getStyle().getColor();
            if (style != null && (style.getValue() & WHITE) != WHITE) {
                return style.getValue() & WHITE;
            }
        }
        TextColor rarity = TextColor.fromLegacyFormat(stack.getRarity().color());
        return rarity == null ? WHITE : rarity.getValue() & WHITE;
    }

    public static boolean isWhite(int rgb) {
        return (rgb & WHITE) == WHITE;
    }

    /** Drops only emit a beam when they are old enough and notable enough. */
    public static boolean shouldShow(ItemEntity item, int rgb, LootbeamsConfig config) {
        if (item.isRemoved() || !item.isAlive()) {
            return false;
        }
        if (item.getAge() < config.minimumAge) {
            return false;
        }
        return config.showWhiteItems || !isWhite(rgb);
    }
}
