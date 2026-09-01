package com.faboslav.villagesandpillages.world.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class PillarProcessor implements StructureProcessor {
    public static final MapCodec<PillarProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    BlockState.CODEC.fieldOf("target_block").forGetter(config -> config.targetBlock),
                    BlockState.CODEC.fieldOf("target_block_output").forGetter(config -> config.targetBlockOutput),
                    Direction.CODEC.optionalFieldOf("direction", Direction.DOWN).forGetter(processor -> processor.direction),
                    Codec.INT.optionalFieldOf("pillar_length", -1).forGetter(config -> config.length))
            .apply(instance, instance.stable(PillarProcessor::new)));

    public final BlockState targetBlock;
    public final BlockState targetBlockOutput;
    public final Direction direction;
    public final int length;

    public PillarProcessor(BlockState targetBlock, BlockState targetBlockOutput, Direction direction, int length) {
        this.targetBlock = targetBlock;
        this.targetBlockOutput = targetBlockOutput;
        this.direction = direction;
        this.length = length;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level,
            BlockPos offset,
            BlockPos pivot,
            BlockPos pos,
            StructureTemplate.StructureBlockInfo original,
            StructurePlaceSettings settings
    ) {
        if (original.state().is(this.targetBlock.getBlock())) {
            return new StructureTemplate.StructureBlockInfo(
                    original.pos(),
                    targetBlockOutput,
                    original.nbt()
            );
        }
        return original;
    }

    @Override
    public MapCodec<PillarProcessor> codec() {
        return CODEC;
    }
}
