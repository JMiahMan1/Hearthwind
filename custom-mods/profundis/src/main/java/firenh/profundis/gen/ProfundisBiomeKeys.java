package firenh.profundis.gen;

import firenh.profundis.Profundis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;

public class ProfundisBiomeKeys {
    private static ResourceKey<Biome> register(String name) {
        return ResourceKey.create(Registries.BIOME, Profundis.id(name));
    }

    public static final ResourceKey<Biome> FROZEN_CAVES = register("frozen_caves");
    public static final ResourceKey<Biome> MUSHROOM_CAVES = register("mushroom_caves");
    public static final ResourceKey<Biome> MOLTEN_CAVES = register("molten_caves");
    public static final ResourceKey<Biome> AMETHYST_CAVES = register("amethyst_caves");
    public static final ResourceKey<Biome> BLACK_CAVES = register("black_caves");
    public static final ResourceKey<Biome> ARID_CAVES = register("arid_caves");
    public static final ResourceKey<Biome> FLORAL_LUSH_CAVES = register("floral_lush_caves");
    public static final ResourceKey<Biome> DIRT_CAVES = register("dirt_caves");
    public static final ResourceKey<Biome> SPARSE_LUSH_CAVES = register("sparse_lush_caves");
    public static final ResourceKey<Biome> WHITE_CAVES = register("white_caves");
    public static final ResourceKey<Biome> PAINTED_CAVES = register("painted_caves");

}
