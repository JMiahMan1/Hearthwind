package firenh.profundis.features.features;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import firenh.profundis.features.features.config.AmethystVeinFeatureConfig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;

public class AmethystVeinFeature extends Feature<AmethystVeinFeatureConfig> {
    private static final Block[] AMETHYST_BUDS = {Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD};
    private static final BlockStateProvider OUTLINE_STATE_PROVIDER = new NoiseProvider(956016019L, new NoiseParameters(1, 1), 0.25f, List.of(Blocks.CALCITE.defaultBlockState(), Blocks.SMOOTH_BASALT.defaultBlockState()));
    private static final BlockStateProvider SUSPENDED_STATE_PROVIDER = new NoiseProvider(326971689L, new NoiseParameters(1, 1), 0.025f, List.of(
        Blocks.AMETHYST_BLOCK.defaultBlockState(), 
        Blocks.AMETHYST_BLOCK.defaultBlockState(), 
        Blocks.AMETHYST_BLOCK.defaultBlockState(), 
        Blocks.CALCITE.defaultBlockState(),
        Blocks.AMETHYST_BLOCK.defaultBlockState(), 
        Blocks.CALCITE.defaultBlockState(),
        Blocks.AMETHYST_BLOCK.defaultBlockState(), 
        Blocks.CALCITE.defaultBlockState(),
        Blocks.CALCITE.defaultBlockState(),
        Blocks.CALCITE.defaultBlockState(),
        Blocks.CALCITE.defaultBlockState(),
        Blocks.CALCITE.defaultBlockState(),
        Blocks.CALCITE.defaultBlockState(),
        Blocks.CALCITE.defaultBlockState()
    ));

    public AmethystVeinFeature(Codec<AmethystVeinFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<AmethystVeinFeatureConfig> context) {
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        WorldGenLevel world = context.level();
        AmethystVeinFeatureConfig config = context.config();
        
        return generate(world, random, origin, config.undergroundOnly(), config.range().sample(random), config.targetCount().sample(random), config.radius().sample(random));
    }

    private boolean generate(WorldGenLevel world, RandomSource random, BlockPos origin, boolean undergroundOnly, int range, int targetCount, double radius) {
        ArrayList<BlockPos> targets = new ArrayList<>();
        Iterator<BlockPos> iter = BlockPos.randomInCube(random, range, origin, range).iterator();

        for (int i = 0; i < targetCount * 16 && targets.size() < targetCount; i += 1) {
            BlockPos pos = iter.next();
            if (world.isEmptyBlock(pos) || !adjacentToAir(world, pos)) {
                continue;
            }

            targets.add(pos.mutable());
        }

        // Profundis.LOGGER.info("Targets: " + targets);

        if (targets.size() == 0) {
            return false;
        }

        ArrayList<Long> futureOutline = new ArrayList<>();

        for (BlockPos target : targets) {
            // Profundis.LOGGER.info("On target: " + target + " !");

            final int factor = 1;
            Vec3 offsetVector = getOffsetVector(origin, target, factor);
            final double maxDistance = distance(origin, target);
            
            int toDo = (int) Math.ceil(factor * Math.sqrt(origin.distSqr(target)));

            for (int i = 0; i < toDo; i += 1) {
                BlockPos pos = vec3dToBlockPos(blockPosToVec3(origin).add(offsetVector.scale(i)));

                if (!adjacentToAir(world, pos)) break;

                setBlocks(world, pos, random, undergroundOnly, radius, maxDistance, distance(origin, pos), futureOutline);
            }
        }

        setOutline(world, futureOutline, random);
        
        return true;
    }

    private double distance(BlockPos origin, BlockPos target) {
        return Math.sqrt(origin.distSqr(target));
    }

    private Vec3 blockPosToVec3(BlockPos blockPos) {
        return new Vec3(
            blockPos.getX() + 0.5,
            blockPos.getY() + 0.5,
            blockPos.getZ() + 0.5
        );
    }

    private BlockPos vec3dToBlockPos(Vec3 vec) {
        return new BlockPos(
            (int) vec.x(),
            (int) vec.y(),
            (int) vec.z()
        );
    }

    private void setBlocks(WorldGenLevel world, BlockPos blockPos, RandomSource random, boolean undergroundOnly, double maxRadius, double maxDistance, double distance, ArrayList<Long> futureOutline) {
        double amethystRadius = (undergroundOnly ? (2.0 / 3.0) : 1) * (maxRadius - ((distance / (maxDistance * (undergroundOnly ? 1 : 0.5))) * maxRadius)) + (undergroundOnly ? 0 : 1);
        // double calciteRadius = (5.0 / 6.0) * (maxRadius - ((distance / (maxDistance)) * maxRadius));
        double radius = maxRadius - (undergroundOnly ? 1 : 2) *((distance / maxDistance) * maxRadius) + (undergroundOnly ? 0 : 1);
        int ceilRadius = (int) Math.ceil(radius);

        Iterator<BlockPos> iter = BlockPos.betweenClosed(blockPos.offset(-(ceilRadius), -(ceilRadius), -(ceilRadius)), blockPos.offset((ceilRadius), (ceilRadius), (ceilRadius))).iterator();

        while (iter.hasNext()) {
            BlockPos pos = iter.next();

            if (undergroundOnly && (!world.getBlockState(pos).canOcclude())) {
                continue;
            }

            if (pos.distSqr(blockPos) <= (double)(radius) * (double)(radius)) {

                if (pos.distSqr(blockPos) <= (double)(amethystRadius) * (double)(amethystRadius)) {
                    this.setAmethyst(world, pos, random, undergroundOnly);
                    futureOutline.remove(pos.asLong());
                } else {
                    futureOutline.add(pos.asLong());
                }

            }
        }
    }

    private void setOutline(WorldGenLevel world, ArrayList<Long> outline, RandomSource random) {
        for (long l : outline) {
            BlockPos pos = BlockPos.of(l);
            BlockState stateAt = world.getBlockState(pos);

            if (random.nextFloat() < 0.333333333f || !(stateAt.is(BlockTags.BASE_STONE_OVERWORLD))) continue;

            world.setBlock(pos, OUTLINE_STATE_PROVIDER.getState(world, random, pos), 3);
        }
    }

    private Vec3 getOffsetVector(BlockPos origin, BlockPos target, double factor) {
        Vec3i offsetI = origin.subtract(target);
        double distance = Math.sqrt(origin.distSqr(target));
        return new Vec3(offsetI.getX(), offsetI.getY(), offsetI.getZ()).scale(factor / distance);
    }

    private boolean isAmethystBudOrCluster(BlockState state) {
        for (Block b : AMETHYST_BUDS) {
            if (state.is(b)) return true;
        }

        return state.is(Blocks.AMETHYST_CLUSTER);
    }

    private boolean adjacentToAir(WorldGenLevel world, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (world.isEmptyBlock(pos.relative(dir)) || world.isWaterAt(pos.relative(dir))) {
                return true;
            }
        }

        return false;
    } 

    private void setAmethyst(WorldGenLevel world, BlockPos pos, RandomSource random, boolean undergroundOnly) {
        if (undergroundOnly && isAmethystBudOrCluster(world.getBlockState(pos))) {
            return;
        }

        ArrayList<Pair<BlockPos, Direction>> adjacentAir = new ArrayList<>();
        BlockState state = undergroundOnly ? Blocks.AMETHYST_BLOCK.defaultBlockState() : SUSPENDED_STATE_PROVIDER.getState(world, random, pos);
        
        world.setBlock(pos, state, 3);

        if (!state.is(Blocks.AMETHYST_BLOCK)) return;

        for (Direction dir : Direction.values()) {
            if (world.isEmptyBlock(pos.relative(dir)) || world.isWaterAt(pos.relative(dir))) {
                adjacentAir.add(Pair.of(pos.relative(dir), dir));
            }
        }

        while (adjacentAir.size() > 0) {
            int index = random.nextInt(adjacentAir.size());

            if (random.nextFloat() < 0.16666666666666666666666) {
                Pair<BlockPos, Direction> pair = adjacentAir.get(index);
                world.setBlock(pair.getFirst(), getAmethystCrystal(random).defaultBlockState()
                    .setValue(BlockStateProperties.FACING, pair.getSecond())
                    .setValue(BlockStateProperties.WATERLOGGED, world.isWaterAt(pair.getFirst())), 3);
            }

            adjacentAir.remove(index);
        }
    }


    private Block getAmethystCrystal(RandomSource random) {
        if (random.nextFloat() < 0.333333333333333333333333) {
            return Blocks.AMETHYST_CLUSTER;
        }

        return AMETHYST_BUDS[random.nextInt(AMETHYST_BUDS.length)];
    }
}
