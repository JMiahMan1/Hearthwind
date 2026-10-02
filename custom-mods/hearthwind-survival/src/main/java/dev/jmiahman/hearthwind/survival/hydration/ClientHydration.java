package dev.jmiahman.hearthwind.survival.hydration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.jmiahman.hearthwind.survival.HydrationCorpusPayload;

/**
 * The client's copy of the hydration corpus, used only by the thirst droplet
 * tooltip. 26.2 builds item tooltips on the client, so the tooltip cannot ask
 * the server-side {@code HydrationCorpus} for the value an item quenches.
 *
 * <p>Kept on the survival side of the fence because the resolution logic
 * ({@link ThirstPreview}) lives there too and both mixins that consume it are
 * thin; only the payload receiver is client-side.
 */
public final class ClientHydration {
    private static final Map<Item, Integer> BY_ITEM = new HashMap<>();

    private ClientHydration() {}

    /** Called from the payload receiver on the client thread. */
    public static void setCorpus(List<HydrationCorpusPayload.Entry> entries) {
        BY_ITEM.clear();
        int resolved = 0;
        for (HydrationCorpusPayload.Entry entry : entries) {
            Item item = BuiltInRegistries.ITEM.getOptional(entry.item()).orElse(null);
            if (item != null) {
                BY_ITEM.put(item, entry.hydration());
                resolved++;
            }
        }
        System.out.println("[hearthwind_survival] thirst preview corpus: "
                + resolved + " of " + entries.size() + " items resolved");
    }

    /** Hydration points this stack quenches, or 0 when it is not catalogued. */
    public static int quench(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return BY_ITEM.getOrDefault(stack.getItem(), 0);
    }

    public static int size() {
        return BY_ITEM.size();
    }

    public static void reset() {
        BY_ITEM.clear();
    }
}