package dev.jmiahman.hearthwind.flora;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class FloraLeavesBlock extends LeavesBlock {
    public static final MapCodec<FloraLeavesBlock> CODEC = simpleCodec(FloraLeavesBlock::new);

    public FloraLeavesBlock(BlockBehaviour.Properties properties) {
        super(0.01f, properties);
    }

    @Override
    public MapCodec<FloraLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
    }
}
