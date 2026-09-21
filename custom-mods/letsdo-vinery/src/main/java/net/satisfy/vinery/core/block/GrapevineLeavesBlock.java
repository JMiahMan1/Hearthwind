package net.satisfy.vinery.core.block;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

// 26.2: LeavesBlock is abstract; plain leaves without falling particles.
public class GrapevineLeavesBlock extends LeavesBlock {
    public GrapevineLeavesBlock(BlockBehaviour.Properties settings) {
        super(0.0F, settings);
    }

    @Override
    public com.mojang.serialization.MapCodec<? extends LeavesBlock> codec() {
        return net.minecraft.world.level.block.UntintedParticleLeavesBlock.CODEC;
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
    }
}
