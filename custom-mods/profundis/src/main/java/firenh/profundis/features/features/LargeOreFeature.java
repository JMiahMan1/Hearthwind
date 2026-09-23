package firenh.profundis.features.features;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;

import firenh.profundis.features.features.config.LargeOreFeatureConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class LargeOreFeature extends Feature<LargeOreFeatureConfig> {

    public LargeOreFeature(Codec<LargeOreFeatureConfig> configCodec) {
        super(configCodec);
    }

    protected Optional<BlockState> getBlockState(WorldGenLevel world, BlockPos pos, BlockState currentState, RandomSource random, List<OreConfiguration.TargetBlockState> targets) {
        for (OreConfiguration.TargetBlockState t : targets) {
            RuleTest rule = t.target;
            if (rule.test(currentState, random)) {
                return Optional.of(t.state);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean place(FeaturePlaceContext<LargeOreFeatureConfig> context) {
        RandomSource random = context.random();
        LargeOreFeatureConfig config = context.config();
        WorldGenLevel world = context.level();
        BlockPos origin = context.origin();
        boolean returnVal = false;

        List<OreConfiguration.TargetBlockState> targets = config.targets();
        int radius = config.radius().sample(random);
        double scale = config.scale();
        double factor = config.factor();
        double smearing = config.smearing();
        double valueRange = config.valueRange();
        double valueOffset = config.valueOffset();
        boolean bordersAir = config.bordersAir();

        ChunkPos originChunkPos = new ChunkPos(origin.getX() / 16, origin.getZ() / 16);
        ImprovedNoise noise = new ImprovedNoise(random);

        Iterator<BlockPos> iter = BlockPos.betweenClosed(
            origin.offset(-radius, -radius, -radius),
            origin.offset(radius, radius, radius)).iterator();

        while (iter.hasNext()) {
            BlockPos pos = iter.next();
            if (origin.distSqr(pos) > (double) radius * (double) radius) continue;

            if (Math.abs(originChunkPos.x() - (pos.getX() / 16)) > 1 || Math.abs(originChunkPos.z() - (pos.getZ() / 16)) > 1) continue;

            double value = noise.noise(pos.getX() * scale * factor, pos.getY() * scale * factor, pos.getZ() * scale * factor, smearing, smearing);
            double distance = Math.sqrt(origin.distSqr(pos));
            double checkVal = value + (distance / radius);

            if (Math.abs(checkVal - valueOffset) < valueRange && world.ensureCanWrite(pos)) {
                boolean hasAir = false;

                if (bordersAir) {
                    for (Direction d : Direction.values()) {
                        if (!world.getBlockState(pos.relative(d)).canOcclude()) {
                            hasAir = true;
                            break;
                        }
                    }
                } else {
                    hasAir = true;
                }

                if (hasAir) {
                    BlockState currentState = world.getBlockState(pos);
                    Optional<BlockState> newState = getBlockState(world, pos, currentState, random, targets);

                    if (newState.isPresent()) {
                        world.setBlock(pos, newState.get(), 3);
                        returnVal = true;
                    }
                }
            }
        }

        return returnVal;
    }
}
