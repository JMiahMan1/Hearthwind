package dev.jmiahman.hearthwind.client;

import java.util.List;

import dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlockEntity;
import dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dehydration client tints for the hydration cauldrons: water cauldrons use
 * the biome water colour, purified water cauldrons the upstream
 * {@code 0x3895C6} tint, and the campfire cauldron switches from water to
 * purified once its block entity reports the content boiled.
 */
public final class HydrationBlockTints {
    /** Dehydration 1.3.6/1.4.1 purified water tint (3708358). */
    private static final int PURIFIED_TINT = 0xFF3895C6;

    private HydrationBlockTints() {}

    public static void register() {
        BlockColorRegistry.register(List.of(BlockTintSources.water()),
                HydrationBlocks.COPPER_WATER_CAULDRON);
        BlockColorRegistry.register(List.of(BlockTintSources.constant(PURIFIED_TINT)),
                HydrationBlocks.COPPER_PURIFIED_WATER_CAULDRON);
        BlockColorRegistry.register(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return PURIFIED_TINT;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                if (level.getBlockEntity(pos) instanceof CampfireCauldronBlockEntity entity
                        && entity.isBoiled) {
                    return PURIFIED_TINT;
                }
                return BlockTintSources.water().colorInWorld(state, level, pos);
            }
        }), HydrationBlocks.CAMPFIRE_CAULDRON);
    }
}
