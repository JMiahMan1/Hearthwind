package dev.jmiahman.hearthwind.survival.hydration;

import com.mojang.serialization.MapCodec;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Port of Dehydration 1.3.6 {@code CopperCauldronBlock} (Aged 3.1.2):
 * empty copper cauldron. Rain fills it with purified water and snow with
 * powder snow, both at the 1.3.6 chances (0.1 / 0.15); a water dripstone
 * fills it with purified water.
 */
public class CopperCauldronBlock extends AbstractCopperCauldronBlock {

    public CopperCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, CopperCauldronBehavior.EMPTY_COPPER_CAULDRON_BEHAVIOR);
    }

    @Override
    protected MapCodec<CopperCauldronBlock> codec() {
        return simpleCodec(CopperCauldronBlock::new);
    }

    @Override
    public boolean isFull(BlockState state) {
        return false;
    }

    public static boolean canFillWithPrecipitation(Level level, Biome.Precipitation precipitation) {
        HearthwindSurvivalConfig.Hydration cfg = HearthwindSurvivalConfig.get().hydration;
        if (precipitation == Biome.Precipitation.RAIN) {
            return level.getRandom().nextFloat() < cfg.copperRainFillChance;
        } else if (precipitation == Biome.Precipitation.SNOW) {
            return level.getRandom().nextFloat() < cfg.copperSnowFillChance;
        }
        return false;
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos,
            Biome.Precipitation precipitation) {
        if (canFillWithPrecipitation(level, precipitation)) {
            if (precipitation == Biome.Precipitation.RAIN) {
                level.setBlockAndUpdate(pos, HydrationBlocks.COPPER_PURIFIED_WATER_CAULDRON.defaultBlockState());
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            } else if (precipitation == Biome.Precipitation.SNOW) {
                level.setBlockAndUpdate(pos, HydrationBlocks.COPPER_POWDERED_CAULDRON.defaultBlockState());
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
        }
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return true;
    }

    @Override
    protected void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid) {
        if (fluid == Fluids.WATER) {
            level.setBlockAndUpdate(pos, HydrationBlocks.COPPER_PURIFIED_WATER_CAULDRON.defaultBlockState());
            level.levelEvent(1047, pos, 0);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
    }
}
