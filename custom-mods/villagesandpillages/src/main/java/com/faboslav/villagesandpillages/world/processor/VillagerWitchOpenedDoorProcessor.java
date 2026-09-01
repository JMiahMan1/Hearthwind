package com.faboslav.villagesandpillages.world.processor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class VillagerWitchOpenedDoorProcessor implements StructureProcessor {
    public static final MapCodec<VillagerWitchOpenedDoorProcessor> CODEC = MapCodec.unit(VillagerWitchOpenedDoorProcessor::new);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level,
            BlockPos offset,
            BlockPos pivot,
            BlockPos pos,
            StructureTemplate.StructureBlockInfo original,
            StructurePlaceSettings settings
    ) {
        BlockState blockState = original.state();
        Block block = blockState.getBlock();

        if (block instanceof DoorBlock) {
            if (blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                blockState = blockState.setValue(DoorBlock.OPEN, true);
                return new StructureTemplate.StructureBlockInfo(
                        original.pos(),
                        blockState,
                        original.nbt()
                );
            }
        }

        return original;
    }

    @Override
    public MapCodec<VillagerWitchOpenedDoorProcessor> codec() {
        return CODEC;
    }
}
