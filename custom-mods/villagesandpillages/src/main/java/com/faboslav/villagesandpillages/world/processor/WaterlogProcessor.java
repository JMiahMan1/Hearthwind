package com.faboslav.villagesandpillages.world.processor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class WaterlogProcessor implements StructureProcessor {
    public static final MapCodec<WaterlogProcessor> CODEC = MapCodec.unit(WaterlogProcessor::new);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level,
            BlockPos offset,
            BlockPos pivot,
            BlockPos pos,
            StructureTemplate.StructureBlockInfo original,
            StructurePlaceSettings settings
    ) {
        return original;
    }

    @Override
    public MapCodec<WaterlogProcessor> codec() {
        return CODEC;
    }
}
