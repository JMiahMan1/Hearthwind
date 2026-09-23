package firenh.profundis.util;

import com.mojang.datafixers.util.Pair;

import firenh.profundis.Profundis;
import firenh.profundis.gen.ProfundisBiomeKeys;
// import firenh.profundis.biomes.ProfundisBiomeKeys;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import net.minecraft.world.level.biome.Climate.Parameter;

public class VanillaBiomeParametersHelper {
    public static void writeCaveBiomeParameters(
            java.util.function.Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameters, Climate.Parameter temperature,
            Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter erosion,
            Climate.Parameter depth, Climate.Parameter weirdness, float offset, ResourceKey<Biome> biome) {
        boolean bl = false;

        if (biome.identifier().equals(ProfundisBiomeKeys.FROZEN_CAVES.identifier()) && Profundis.config().generateFrozenCaves) bl = true;
        else if (biome.identifier().equals(ProfundisBiomeKeys.MUSHROOM_CAVES.identifier()) && Profundis.config().generateMushroomCaves) bl = true;
        else if (biome.identifier().equals(ProfundisBiomeKeys.MOLTEN_CAVES.identifier()) && Profundis.config().generateMoltenCaves) bl = true;
        else if (biome.identifier().equals(ProfundisBiomeKeys.AMETHYST_CAVES.identifier()) && Profundis.config().generateAmethystCaves) bl = true;
        else if (biome.identifier().equals(ProfundisBiomeKeys.BLACK_CAVES.identifier()) && Profundis.config().generateBlackCaves) bl = true;
        else if (biome.identifier().equals(ProfundisBiomeKeys.ARID_CAVES.identifier()) && Profundis.config().generateAridCaves) bl = true;
        else if (biome.identifier().equals(ProfundisBiomeKeys.FLORAL_LUSH_CAVES.identifier()) && Profundis.config().generateFloralLushCaves) bl = true;
        else if (biome.identifier().equals(ProfundisBiomeKeys.SPARSE_LUSH_CAVES.identifier()) && Profundis.config().generateSparseLushCaves) bl = true;
        else if (biome.identifier().equals(ProfundisBiomeKeys.WHITE_CAVES.identifier()) && Profundis.config().generateWhiteCaves) bl = true;
        else if (biome.identifier().equals(ProfundisBiomeKeys.PAINTED_CAVES.identifier()) && Profundis.config().generatePaintedCaves) bl = true;




        if (bl) {
            parameters.accept(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, depth, weirdness, offset), biome));
        }
    }
}
