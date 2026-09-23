package firenh.profundis.util;

import firenh.profundis.Profundis;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ProfundisTags {
    public static final TagKey<Biome> FROZE_WATER_CAVE_FEATURE_WORKS = biome("frozen_water_cave_feature_works");  
    
    public static final TagKey<Block> BASE_STONE_OVERWORLD_PLUS = block("base_stone_overworld_plus");  


    private static TagKey<Block> block(String id) {
        return TagKey.create(Registries.BLOCK, Profundis.id(id));
    }

    private static TagKey<Biome> biome(String id) {
        return TagKey.create(Registries.BIOME, Profundis.id(id));
    }
}
