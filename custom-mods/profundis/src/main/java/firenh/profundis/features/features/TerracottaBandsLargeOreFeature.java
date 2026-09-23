package firenh.profundis.features.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;

import firenh.profundis.Profundis;
import firenh.profundis.features.features.config.LargeOreFeatureConfig;
import firenh.profundis.util.ProfundisTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class TerracottaBandsLargeOreFeature extends LargeOreFeature {
    private static final BlockState[] TERRACOTTA_BLOCKS = new BlockState[]{
        Blocks.TERRACOTTA.defaultBlockState(),
        Blocks.TERRACOTTA.defaultBlockState(),
        Blocks.TERRACOTTA.defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.GRAY).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.RED).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.BROWN).defaultBlockState(),
    };

    private static List<BlockState> terracottaList = new ArrayList<>();

    public TerracottaBandsLargeOreFeature(Codec<LargeOreFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    protected Optional<BlockState> getBlockState(WorldGenLevel world, BlockPos pos, BlockState currentState, RandomSource random, List<OreConfiguration.TargetBlockState> targets) {
        
        if (currentState.is(BlockTags.BASE_STONE_OVERWORLD)) {
            // Profundis.log(getTerracotta(world, pos.getY()).getBlock().getName().toString());
            return Optional.of(getTerracotta(world, pos.getY()));
        }

        return Optional.empty();
    }

    private BlockState getTerracotta(WorldGenLevel world, int y) {
        int adjY = y - world.getMinY();

        if (terracottaList.size() > adjY) {
            return terracottaList.get(adjY);
        }

        RandomSource seedBasedRandom = RandomSource.create(world.getSeed());

        for (int i = 0; i <= (adjY + 1); i += 1) {
            terracottaList.add(
                TERRACOTTA_BLOCKS[seedBasedRandom.nextInt(TERRACOTTA_BLOCKS.length)]
            );
        }

        return terracottaList.get(adjY);
    }

    
}
