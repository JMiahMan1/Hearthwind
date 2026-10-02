package dev.jmiahman.hearthwind.survival.hydration;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import dev.jmiahman.hearthwind.survival.FlaskItems;
import dev.jmiahman.hearthwind.survival.FlaskData;
import dev.jmiahman.hearthwind.survival.ThirstHelper;

/**
 * The droplet preview Dehydration draws under a tooltip, a port of
 * {@code net.dehydration.misc.ThirstTooltipData} plus the four places that
 * build one:
 *
 * <ol>
 *   <li>{@code LeatherFlask#getTooltipImage} - the flask's own override.</li>
 *   <li>{@code ItemMixin#getTooltipDataMixin} - a tag ladder, then the
 *       hydration corpus, for food, stews and drinks.</li>
 *   <li>{@code PotionItemMixin#getTooltipImage} - the plain potion only.</li>
 *   <li>Water bowls fall out of (2); the reference gives them no override.</li>
 * </ol>
 *
 * <p>Every number here is the reference's: the row is a hardcoded {@code 11}
 * pixels tall, {@link #widthFor} is {@code quench * 9 / 2} plus a 9px half
 * droplet for an odd quench, and the sheet has four droplet qualities laid out
 * at {@code u = quality * 18} with the full droplet on the left of each pair
 * and the half on the right.
 *
 * <p>26.2 computes the whole tooltip on the CLIENT ({@code Item#getTooltipImage}
 * takes no player), so {@link #forItem} and {@link #forPotion} read the
 * client-side copy of the hydration corpus, not the server's world datapack.
 * That is the one place where this port cannot simply ask
 * {@code HydrationCorpus}.
 */
public record ThirstPreview(int quench, int quality) implements TooltipComponent {
    /** The reference hardcodes 11; the font is not consulted. */
    public static final int HEIGHT = 11;

    /** One droplet cell, and the step between two of them. */
    public static final int CELL = 9;

    /** The reference's {@code RenderInit.THIRST_ICON}. */
    public static final Identifier ICON =
            Identifier.fromNamespaceAndPath("dehydration", "textures/gui/thirst.png");

    /** The sheet is 256x256 although only the top 27 rows are ever drawn. */
    public static final int ICON_SIZE = 256;

    /** Droplet art always sits on this row; {@code v = 18} is never used. */
    public static final int ICON_V = 9;

    /**
     * {@code quench * 9 / 2 + (quench % 2 != 0 ? 9 : 0)} - the reference's own
     * expression, transcribed exactly. It is NOT {@code ceil(quench / 2) * 9}:
     * integer division runs first, so a single droplet reports 13 px of width
     * while drawing 9 px of art. The reference's own layout reserves the extra
     * 4 px and the port keeps that rather than tidying it away.
     */
    public static int widthFor(int quench) {
        return quench * CELL / 2 + (quench % 2 != 0 ? CELL : 0);
    }

    /** Source texture x for a full droplet of {@code quality}. */
    public static int fullU(int quality) {
        return quality * 18;
    }

    /** Source texture x for the half droplet of {@code quality}. */
    public static int halfU(int quality) {
        return quality * 18 + CELL;
    }

    /**
     * Reference {@code LeatherFlask#getTooltipImage}. Three cases, and the
     * middle one is the surprising one: a flask that HAS been filled but is
     * empty right now shows <b>nothing</b>, not a zero row.
     *
     * @param capacity {@code addition + 2} - the item already knows this.
     * @param perSip the {@code flask_thirst_quench} value.
     */
    public static Optional<ThirstPreview> forFlask(ItemStack stack, int capacity, int perSip) {
        FlaskData data = stack.get(FlaskItems.FLASK_DATA);
        if (data != null) {
            if (data.fillLevel() <= 0) {
                return Optional.empty();
            }
            // The two fields are INDEPENDENT. Reading the bytecode of
            // LeatherFlask#getTooltipData offsets 44-79: the constructor is
            // (quality, thirstQuench) and it is handed
            //   quality = nbt.getInt("purified_water")
            //   quench  = nbt.getInt("leather_flask") * flask_thirst_quench
            // so the water's purity picks the droplet ART and the fill level
            // picks the droplet COUNT. Multiplying them together (as an
            // earlier draft of this did) makes purified water preview nothing
            // at all, because its purity field is 0.
            return Optional.of(new ThirstPreview(
                    data.fillLevel() * perSip, data.qualityLevel()));
        }
        // Never filled: the preview advertises the full capacity in purified
        // water, which the reference renders at quality 2.
        return Optional.of(new ThirstPreview(2 * capacity * perSip, FlaskData.DIRTY));
    }

    /**
     * Reference {@code ItemMixin#getTooltipDataMixin}: the tag ladder first,
     * then the hydration corpus <b>overriding</b> the tag value. The quality
     * is always 0 on this path - only a bad potion is drawn differently.
     */
    public static Optional<ThirstPreview> forItem(ItemStack stack, TagQuench tags, int corpusQuench) {
        int quench = tags.stew();
        if (quench <= 0) {
            quench = tags.food();
        }
        if (quench <= 0) {
            quench = tags.drinks();
        }
        if (quench <= 0) {
            quench = tags.strongerStew();
        }
        if (quench <= 0) {
            quench = tags.strongerFood();
        }
        if (quench <= 0) {
            quench = tags.strongerDrinks();
        }
        if (corpusQuench > 0) {
            quench = corpusQuench;
        }
        return quench <= 0 ? Optional.empty() : Optional.of(new ThirstPreview(quench, 0));
    }

    /**
     * Reference {@code PotionItemMixin#getTooltipImage}. Splash and lingering
     * potions are skipped outright - the reference tests
     * {@code instanceof ThrowablePotionItem} and returns nothing - and a bad
     * potion is drawn at quality 2 so the player can see the risk before
     * drinking.
     */
    public static Optional<ThirstPreview> forPotion(ItemStack stack, int corpusQuench,
            int fallbackQuench, boolean splashOrLingering) {
        if (splashOrLingering) {
            return Optional.empty();
        }
        Potion potion = potionIn(stack);
        int quench = corpusQuench > 0 ? corpusQuench : fallbackQuench;
        if (quench <= 0) {
            return Optional.empty();
        }
        return Optional.of(new ThirstPreview(quench, ThirstHelper.isBadPotion(potion) ? 2 : 0));
    }

    /** The potion a stack holds, defaulting to plain water like the reference. */
    public static Potion potionIn(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return Potions.WATER.value();
        }
        return contents.potion().map(Holder::value).orElse(Potions.WATER.value());
    }

    /** True for the two throwing potion items the reference refuses to preview. */
    public static boolean isThrowable(ItemStack stack) {
        return stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

    /**
     * The six {@code dehydration} tag values the reference reads, in the
     * order it reads them. All six ship empty in both the reference jar and
     * Aged's pack, so this is a live extension point rather than dead weight:
     * a pack that fills {@code hydrating_stew} gets stews previewed from the
     * tag exactly as the reference would.
     */
    public record TagQuench(int stew, int food, int drinks,
            int strongerStew, int strongerFood, int strongerDrinks) {
        public static final TagQuench NONE = new TagQuench(0, 0, 0, 0, 0, 0);
    }
}