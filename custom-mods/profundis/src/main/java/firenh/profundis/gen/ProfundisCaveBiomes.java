package firenh.profundis.gen;

import com.google.common.collect.ImmutableList;

// import firenh.profundis.biomes.ProfundisBiomeKeys;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate; 
import net.minecraft.world.level.biome.Climate.Parameter;

public class ProfundisCaveBiomes {
    /**
     * {@linkplain net.minecraft.world.biome.source.util.VanillaBiomeParameters}
     */


    public static class CaveBiome {
        public final Climate.Parameter temperature, humidity, continentalness, erosion, depth, weirdness;
        public final float offset;
        public final ResourceKey<Biome> biome;

        public CaveBiome(Climate.Parameter temperature, Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter erosion, Climate.Parameter depth, Climate.Parameter weirdness, float offset, ResourceKey<Biome> biome) {
            this.temperature = temperature;
            this.humidity = humidity;
            this.continentalness = continentalness;
            this.erosion = erosion;
            this.depth = depth;
            this.weirdness = weirdness;
            this.offset = offset; 
            this.biome = biome;
        }

        public static CaveBiome of(Climate.Parameter temperature, Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter erosion, Climate.Parameter depth, Climate.Parameter weirdness, float offset, ResourceKey<Biome> biome) {
            return new CaveBiome(temperature, humidity, continentalness, erosion, depth, weirdness, offset, biome);
        }
    }

    private static final Climate.Parameter DEFAULT_PARAMETER = Climate.Parameter.span(-1.0f, 1.0f);
    static final Climate.Parameter ALL_HEIGHT_RANGE = Climate.Parameter.span(0.2f, 0.9f);
    static final Climate.Parameter ALL_HEIGHT_RANGE_DEEPER = Climate.Parameter.span(-0.2f, 0.9f);
    static final Climate.Parameter HIGH_RANGE = Climate.Parameter.span(0.55f, 0.9f);

    /* Ordering of terms:
     *      temperature,
     *      humidity,
     *      continentalness,
     *      erosion,
     *      depth,
     *      weirdness,
     *      offset
     *      biome
     */ 

    public final static ImmutableList<CaveBiome> DEFAULT_CAVE_BIOMES = ImmutableList.of(
        CaveBiome.of(
            Climate.Parameter.span(-1.0f, -0.6f),
            DEFAULT_PARAMETER,
            DEFAULT_PARAMETER,
            DEFAULT_PARAMETER,
            ALL_HEIGHT_RANGE,
            Climate.Parameter.span(0.8f, 1.0f),
            0f,
            ProfundisBiomeKeys.FROZEN_CAVES
        ),

        CaveBiome.of(
            Climate.Parameter.span(-1.0f, -0.6f),
            DEFAULT_PARAMETER,
            DEFAULT_PARAMETER,
            DEFAULT_PARAMETER,
            ALL_HEIGHT_RANGE,
            Climate.Parameter.span(-1.0f, -0.8f),
            0f,
            ProfundisBiomeKeys.FROZEN_CAVES
        ),

        CaveBiome.of(
            Climate.Parameter.span(-0.15f, 0.2f),
            Climate.Parameter.span(0.3375f, 1.0f),
            Climate.Parameter.span(0f, 0.35f),
            DEFAULT_PARAMETER,
            ALL_HEIGHT_RANGE,
            Climate.Parameter.span(0.7f, 1.0f),
            0f,
            ProfundisBiomeKeys.MUSHROOM_CAVES
        ),

        CaveBiome.of(
            Climate.Parameter.span(-0.15f, 0.2f),
            Climate.Parameter.span(0.3375f, 1.0f),
            Climate.Parameter.span(0f, 0.35f),
            DEFAULT_PARAMETER,
            ALL_HEIGHT_RANGE,
            Climate.Parameter.span(-1.0f, -0.7f),
            0f,
            ProfundisBiomeKeys.MUSHROOM_CAVES
        ),

        CaveBiome.of(
            DEFAULT_PARAMETER,
            DEFAULT_PARAMETER,
            Climate.Parameter.span(0.4f, 0.6f),
            DEFAULT_PARAMETER,
            ALL_HEIGHT_RANGE,
            Climate.Parameter.span(0.75f, 1.0f),
            0f,
            ProfundisBiomeKeys.MOLTEN_CAVES
        ),

        CaveBiome.of(
            DEFAULT_PARAMETER,
            DEFAULT_PARAMETER,
            Climate.Parameter.span(-0.2f, 0f),
            Climate.Parameter.span(-1f, -0.5f),
            ALL_HEIGHT_RANGE,
            Climate.Parameter.span(-1.0f, -0.9f),
            0.1f,
            ProfundisBiomeKeys.AMETHYST_CAVES
        ), 

        CaveBiome.of(
            DEFAULT_PARAMETER,
            DEFAULT_PARAMETER,
            Climate.Parameter.span(-0.3f, 0f),
            Climate.Parameter.span(0f, 1f),
            ALL_HEIGHT_RANGE,
            Climate.Parameter.span(0.8f, 1.0f),
            0.1f,
            ProfundisBiomeKeys.BLACK_CAVES
        ),
        CaveBiome.of(
            DEFAULT_PARAMETER,
            DEFAULT_PARAMETER,
            Climate.Parameter.span(-0.3f, 0f),
            Climate.Parameter.span(0f, 1f),
            ALL_HEIGHT_RANGE,
            Climate.Parameter.span(-1.0f, -0.8f),
            0.1f,
            ProfundisBiomeKeys.WHITE_CAVES
        ),

        CaveBiome.of(
            Climate.Parameter.span(0.8f, 1.0f),
            Climate.Parameter.span(-1.0f, 0),
            DEFAULT_PARAMETER,
            DEFAULT_PARAMETER,
            HIGH_RANGE,
            Climate.Parameter.span(-1.0f, -0.5f),
            0f,
            ProfundisBiomeKeys.ARID_CAVES
        ),

        CaveBiome.of(
            Climate.Parameter.span(0.8f, 1.0f),
            Climate.Parameter.span(-1.0f, 0),
            DEFAULT_PARAMETER,
            DEFAULT_PARAMETER,
            HIGH_RANGE,
            Climate.Parameter.span(0.5f, 1.0f),
            0f,
            ProfundisBiomeKeys.PAINTED_CAVES
        ),

        CaveBiome.of(
            DEFAULT_PARAMETER,
            Climate.Parameter.span(0.65f, 1.0f),
            DEFAULT_PARAMETER,
            DEFAULT_PARAMETER,
            ALL_HEIGHT_RANGE,
            Climate.Parameter.span(0.3f, 1.0f),
            0.1f,
            ProfundisBiomeKeys.FLORAL_LUSH_CAVES
        )

        

        // CaveBiome.of(
        //     DEFAULT_PARAMETER,
        //     Climate.Parameter.span(0.65f, 1.0f),
        //     DEFAULT_PARAMETER,
        //     DEFAULT_PARAMETER,
        //     ALL_HEIGHT_RANGE,
        //     Climate.Parameter.span(-1.0f, 0.5f),
        //     0.1f,
        //     ProfundisBiomeKeys.SPARSE_LUSH_CAVES
        // )

        // CaveBiome.of(
        //     DEFAULT_PARAMETER,
        //     Climate.Parameter.span(1.0f, 2.0f),
        //     DEFAULT_PARAMETER,
        //     DEFAULT_PARAMETER,
        //     ALL_HEIGHT_RANGE,
        //     DEFAULT_PARAMETER,
        //     0f,
        //     ProfundisBiomeKeys.FLORAL_LUSH_CAVES
        // )
    );
}
