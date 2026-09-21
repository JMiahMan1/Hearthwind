package net.satisfy.brewery.core.event.brew_event;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.brewery.core.block.BrewKettleBlock;
import net.satisfy.brewery.core.block.property.Liquid;
import net.satisfy.brewery.core.registry.BlockStateRegistry;

import java.util.Set;

public class KettleEvent extends BrewEvent {
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {

    }

    @Override
    public void start(Set<BlockPos> components, Level level) {
        if (components == null || level == null) return;
        BlockPos basinPos = BrewHelper.getBlock(BrewKettleBlock.class, components, level);
        if (basinPos != null) {
            BlockState basinState = level.getBlockState(basinPos);
            level.setBlock(basinPos, basinState.setValue(BlockStateRegistry.LIQUID, Liquid.OVERFLOWING), 3);
        }
    }

    @Override
    public boolean isFinish(Set<BlockPos> components, Level level) {
        if (components == null || level == null) return true;
        BlockPos basinPos = BrewHelper.getBlock(BrewKettleBlock.class, components, level);
        if (basinPos != null) {
            BlockState basinState = level.getBlockState(basinPos);
            return basinState.getValue(BlockStateRegistry.LIQUID) != Liquid.OVERFLOWING;
        }
        return true;
    }

    @Override
    public void finish(Set<BlockPos> components, Level level) {
        BrewHelper.resetWater(components, level);
    }
}
