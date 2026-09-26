package dev.jmiahman.hearthwind.survival.hydration;

import java.util.function.Consumer;

/**
 * Entry point for the Dehydration hydration content: water bowls, campfire
 * cauldron and the copper cauldron family, plus their fluid storages and
 * the bowl-filling interaction.
 *
 * <p>Must be registered after {@code PurifiedWater.registerAll} (the
 * storages and the campfire cauldron reference the purified fluid).
 */
public final class Hydration {
    private Hydration() {}

    public static void registerAll(Consumer<String> log) {
        HydrationItems.registerAll(log);
        HydrationBlocks.registerAll(log);
        HydrationStorages.registerAll(log);
        HydrationBowlHandler.register();
    }
}
