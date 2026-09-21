package net.satisfy.vinery.core.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import static net.satisfy.vinery.core.registry.ObjectRegistry.*;

public class FlammableBlockRegistry {

    public static void init() {
        addFlammable(5, 20, DARK_CHERRY_PLANKS.get(), DARK_CHERRY_SLAB.get(), DARK_CHERRY_STAIRS.get(), DARK_CHERRY_FENCE.get(),
                DARK_CHERRY_FENCE_GATE.get());

        addFlammable(5, 5, STRIPPED_DARK_CHERRY_LOG.get(), DARK_CHERRY_LOG.get(), APPLE_LOG.get(),
                STRIPPED_DARK_CHERRY_WOOD.get(), DARK_CHERRY_WOOD.get(), APPLE_WOOD.get());

        addFlammable(30, 60, DARK_CHERRY_LEAVES.get(), GRAPEVINE_LEAVES.get(), APPLE_LEAVES.get());
    }

    public static void addFlammable(int burnOdd, int igniteOdd, Block... blocks) {
        // 26.2: FireBlock has no mod-facing flammability API anymore, so this
        // is a no-op (custom wood simply does not catch fire). Same values
        // preserved here for whenever an API returns.
    }
}
