package net.satisfy.nethervinery.core.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.satisfy.nethervinery.core.util.NetherVineryIdentifier;

import java.util.List;
import java.util.Set;

public class NetherStorageTypeRegistry {
    public static final Identifier BIG_BOTTLE = NetherVineryIdentifier.of("big_bottle");
    public static final Identifier FOUR_BOTTLE = NetherVineryIdentifier.of("four_bottle");
    public static final Identifier NINE_BOTTLE = NetherVineryIdentifier.of("nine_bottle");
    public static final Identifier WINE_BOTTLE = NetherVineryIdentifier.of("wine_bottle");
    

    public static Set<Block> registerBlocks(Set<Block> blocks) {
        blocks.add(NetherObjectRegistry.CRIMSON_WINE_RACK_BIG.get());
        blocks.add(NetherObjectRegistry.CRIMSON_WINE_RACK_SMALL.get());
        blocks.add(NetherObjectRegistry.CRIMSON_WINE_RACK_MID.get());
        blocks.add(NetherObjectRegistry.WARPED_WINE_RACK_BIG.get());
        blocks.add(NetherObjectRegistry.WARPED_WINE_RACK_MID.get());
        blocks.add(NetherObjectRegistry.WARPED_WINE_RACK_SMALL.get());
        blocks.addAll(List.of(NetherObjectRegistry.GHASTLY_GRENACHE.get(), NetherObjectRegistry.NETHERITE_NECTAR.get(), NetherObjectRegistry.BLAZEWINE_PINOT.get(), NetherObjectRegistry.LAVA_FIZZ.get(), NetherObjectRegistry.NETHER_FIZZ.get(), NetherObjectRegistry.IMPROVED_NETHER_FIZZ.get(), NetherObjectRegistry.IMPROVED_LAVA_FIZZ.get()));
        return blocks;
    }
}
