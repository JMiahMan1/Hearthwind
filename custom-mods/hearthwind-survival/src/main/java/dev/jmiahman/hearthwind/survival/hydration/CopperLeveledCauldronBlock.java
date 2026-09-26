package dev.jmiahman.hearthwind.survival.hydration;

import java.util.Map;
import java.util.function.Predicate;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Port of Dehydration 1.3.6 {@code CopperLeveledCauldronBlock}: water,
 * powder snow and purified water copper cauldrons, level 1..3 each.
 */
public class CopperLeveledCauldronBlock extends AbstractCopperCauldronBlock {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
    public static final Predicate<Biome.Precipitation> RAIN_PREDICATE =
            precipitation -> precipitation == Biome.Precipitation.RAIN;
    public static final Predicate<Biome.Precipitation> SNOW_PREDICATE =
            precipitation -> precipitation == Biome.Precipitation.SNOW;

    private final Predicate<Biome.Precipitation> precipitationPredicate;

    public CopperLeveledCauldronBlock(BlockBehaviour.Properties properties) {
        this(properties, RAIN_PREDICATE, CopperCauldronBehavior.WATER_COPPER_CAULDRON_BEHAVIOR);
    }

    public CopperLeveledCauldronBlock(BlockBehaviour.Properties properties,
            Predicate<Biome.Precipitation> precipitationPredicate,
            Map<Item, CopperCauldronBehavior> behaviorMap) {
        super(properties, behaviorMap);
        this.precipitationPredicate = precipitationPredicate;
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 1));
    }

    @Override
    protected MapCodec<CopperLeveledCauldronBlock> codec() {
        return simpleCodec(CopperLeveledCauldronBlock::new);
    }

    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == 3;
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return fluid == Fluids.WATER && this.precipitationPredicate == RAIN_PREDICATE;
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return (6.0D + (double) state.getValue(LEVEL) * 3.0D) / 16.0D;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
            InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos blockPos = pos.immutable();
            effectApplier.runBefore(InsideBlockEffectType.EXTINGUISH, e -> {
                if (e.isOnFire() && this.isEntityTouchingFluid(state, blockPos, e)
                        && e.mayInteract(serverLevel, blockPos)) {
                    decrementFluidLevel(state, level, blockPos);
                }
            });
        }
        effectApplier.apply(InsideBlockEffectType.EXTINGUISH);
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos,
            Biome.Precipitation precipitation) {
        if (CopperCauldronBlock.canFillWithPrecipitation(level, precipitation)
                && state.getValue(LEVEL) != 3
                && this.precipitationPredicate.test(precipitation)) {
            level.setBlockAndUpdate(pos, state.cycle(LEVEL));
        }
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos,
            net.minecraft.core.Direction direction) {
        return state.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid) {
        if (!this.isFull(state)) {
            level.setBlockAndUpdate(pos, state.setValue(LEVEL, state.getValue(LEVEL) + 1));
            level.levelEvent(1047, pos, 0);
        }
    }

    public static void decrementFluidLevel(BlockState state, Level level, BlockPos pos) {
        setFluidLevel(state, level, pos, state.getValue(LEVEL) - 1);
    }

    public static void setFluidLevel(BlockState state, Level level, BlockPos pos, int fluidLevel) {
        level.setBlockAndUpdate(pos, fluidLevel <= 0
                ? HydrationBlocks.COPPER_CAULDRON.defaultBlockState()
                : state.setValue(LEVEL, fluidLevel));
    }
}
