package firenh.profundis.features.features;

import java.util.Iterator;
import java.util.Optional;

import com.mojang.serialization.Codec;

import firenh.profundis.features.features.config.NetherrackBaseFeatureConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class NetherrackBaseFeature extends Feature<NetherrackBaseFeatureConfig> {
    public NetherrackBaseFeature(Codec<NetherrackBaseFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NetherrackBaseFeatureConfig> context) {
        final NetherrackBaseFeatureConfig config = context.config();
        final BlockPos origin = context.origin();
        final RandomSource random = context.random();
        final WorldGenLevel world = context.level();
        final int count = config.count().sample(random);
        final int height = 3; // config.height().sample(random);
        final int y = origin.getY();
        final int radius = config.radius().sample(random);
        final int minX = origin.getX() - radius;
        final int maxX = origin.getX() + radius;
        final int minZ = origin.getZ() - radius;
        final int maxZ = origin.getZ() + radius;
        final BlockState topState = config.topState();
        final BlockState underState = config.underState();
        final RuleTest canPlaceThrough = config.canPlaceThrough();
        final boolean sometimesUseUnderAsTop = config.sometimesUseUnderAsTop();
        final boolean isContained = config.contained();

        boolean hasPlaced = false;
        

        Iterator<BlockPos> iter = BlockPos.randomBetweenClosed(random, count, (int)origin.getX() - (radius / 2), (int)y, (int)origin.getZ() - (radius / 2), (int)origin.getX() + (radius / 2), (int)y, (int)origin.getZ() + (radius / 2)).iterator();

        while (iter.hasNext()) {
            boolean bl = iterateOnce(iter.next(), origin, radius, height, topState, underState, canPlaceThrough, sometimesUseUnderAsTop, world, random, isContained);
            if (!hasPlaced) hasPlaced = bl;
        }

        Iterator<BlockPos> iter2 = BlockPos.randomBetweenClosed(random, count, (int)minX, (int)y, (int)minZ, (int)maxX, (int)y, (int)maxZ).iterator();

        while (iter2.hasNext()) {
            boolean bl = iterateOnce(iter2.next(), origin, radius, height, topState, underState, canPlaceThrough, sometimesUseUnderAsTop, world, random, isContained);
            if (!hasPlaced) hasPlaced = bl;
        }

        return hasPlaced;
    }

    private boolean iterateOnce(BlockPos pos, BlockPos origin, int radius, int height, BlockState topState, BlockState underState, RuleTest canPlaceThrough, boolean sometimesUseUnderAsTop, WorldGenLevel world, RandomSource random, boolean isContained) {
        // Thermorarium.LOGGER.info("Iter once");
        
        boolean hasPlaced = false;
        // // Thermorarium.LOGGER.info("Original: " + pos);

        if (pos.distSqr(origin) > (double) radius * (double) radius) return false;

        // Thermorarium.LOGGER.info("pos:: " + pos);
        Optional<BlockPos> posMatcher = adjustWorldPosition(pos, height, world);

        if (posMatcher.isPresent()) {
            // Thermorarium.LOGGER.info("Matching");

            // // Thermorarium.LOGGER.info("Adjusted: " + posMatcher.get());
            boolean bl = generatePillarGoingDown(radius, height, posMatcher.get(), origin, topState, underState, canPlaceThrough, world, isContained, random);
            if (!hasPlaced) hasPlaced = bl;
            // Thermorarium.LOGGER.info("This spot: " + posMatcher.get());


            for (Direction d : DIRECTIONS) {

                Optional<BlockPos> posMatcher2 = adjustWorldPosition(posMatcher.get().relative(d), 1 + (int) Math.ceil((float) height / 3.0f), world);
                
                if (posMatcher2.isPresent() && random.nextFloat() < (Math.sqrt(posMatcher2.get().distSqr(origin) / (double) radius))) {
                    generatePillarGoingDown(radius, 1 + ((height + 3) / 3), posMatcher2.get(), origin, (sometimesUseUnderAsTop && random.nextBoolean() ? underState : topState), underState, canPlaceThrough, world, isContained, random);
                }
            }
        }

        return hasPlaced;
    }

    private Optional<BlockPos> adjustWorldPosition(BlockPos blockPos, int height, WorldGenLevel world) {
        BlockPos.MutableBlockPos pos = blockPos.mutable();
        // Thermorarium.LOGGER.info("At: " + pos);
        // Thermorarium.LOGGER.info("Is AIr: " + world.isEmptyBlock(pos));
        // Thermorarium.LOGGER.info("Is AIr: " + world.getBlockState(pos));
        
        if (world.isEmptyBlock(pos)) {
            // Thermorarium.LOGGER.info(pos + "Going down");

            for (int i = 0; i < height; i += 1) {
                // Thermorarium.LOGGER.info(world.getBlockState(pos).canOcclude());

                if (world.getBlockState(pos).canOcclude() && world.isEmptyBlock(pos.above())) {
                    // Thermorarium.LOGGER.info("Return adjustment: " + pos.immutable());
                    return Optional.of(pos.immutable());
                }

                pos.move(Direction.DOWN);
            }
        } else {
            // Thermorarium.LOGGER.info("Going up");

            for (int i = 0; i < height; i += 1) {
                // Thermorarium.LOGGER.info(world.getBlockState(pos).canOcclude());

                if (world.getBlockState(pos).canOcclude() && world.isEmptyBlock(pos.above())) {
                    // Thermorarium.LOGGER.info("Return adjustment: " + pos.immutable());
                    return Optional.of(pos.immutable());
                }

                pos.move(Direction.UP);
            }
        }

        // Thermorarium.LOGGER.info("None");

        return Optional.empty();
    }

    private final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private boolean generatePillarGoingDown(int maxRadius, int maxHeight, BlockPos target, BlockPos origin, BlockState topState, BlockState underState, RuleTest canPlaceThrough, WorldGenLevel world, boolean isContained, RandomSource random) {
        // Thermorarium.LOGGER.info("Generating pillar");
        
        final double distanceToOrigin = Math.sqrt(target.distSqr(origin));
        final int height = maxHeight > 1 ? (int) Math.ceil(maxHeight * ((distanceToOrigin / (double) maxRadius))) : 1;
        
        // // Thermorarium.LOGGER.info("distanceToOrigin = " + distanceToOrigin + " ; maxRadius = " + maxRadius);
        // // Thermorarium.LOGGER.info("(distanceToOrigin / (double) maxRadius) = " + (distanceToOrigin / (double) maxRadius));
        // // Thermorarium.LOGGER.info("Height: " + height);

        BlockPos pos = target;

        for (int i = 0; i < height; i += 1) {
            pos = target.below(i);
            BlockState stateAt = world.getBlockState(pos);
            
            boolean bl = canPlaceThrough.test(stateAt, random);

            if (!bl) break;

            if (world.isEmptyBlock(pos.above())) {
                attemptPlaceBlock(world, pos, topState, isContained);
            } else {
                attemptPlaceBlock(world, pos, underState, isContained);
            }
        }

        return height > 0;
    }

    private boolean attemptPlaceBlock(WorldGenLevel world, BlockPos pos, BlockState state, boolean contained) {
        // Thermorarium.LOGGER.info("COntained!");
        
        if (contained) {
            for (Direction d : DIRECTIONS) {
                BlockState over = world.getBlockState(pos.relative(d));

                // Thermorarium.LOGGER.info("isOpaque: " + over.canOcclude()+ "; isOf:" + over.is(state.getBlock()));

                if (!over.canOcclude() && !over.is(state.getBlock())) return false;
            }

            BlockState over = world.getBlockState(pos.relative(Direction.DOWN));
            // Thermorarium.LOGGER.info("isOpaque: " + over.canOcclude()+ "; isOf:" + over.is(state.getBlock()));

            if (!over.canOcclude() && !over.is(state.getBlock())) return false;
        }

        // Thermorarium.LOGGER.info("Setting block!!!");

        world.setBlock(pos, state, 3);
        return true;
    }
    
}
