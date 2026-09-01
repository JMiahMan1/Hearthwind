package com.faboslav.villagesandpillages.world.processor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class VillageWitchFlowerPotProcessor implements StructureProcessor {
    public static final MapCodec<VillageWitchFlowerPotProcessor> CODEC = MapCodec.unit(VillageWitchFlowerPotProcessor::new);

    private static final BlockState[] POTS = {
            Blocks.POTTED_DEAD_BUSH.defaultBlockState(),
            Blocks.POTTED_BLUE_ORCHID.defaultBlockState(),
            Blocks.POTTED_FLOWERING_AZALEA.defaultBlockState(),
            Blocks.POTTED_BROWN_MUSHROOM.defaultBlockState(),
            Blocks.POTTED_RED_MUSHROOM.defaultBlockState()
    };

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level,
            BlockPos offset,
            BlockPos pivot,
            BlockPos pos,
            StructureTemplate.StructureBlockInfo original,
            StructurePlaceSettings settings
    ) {
        if (!original.state().is(Blocks.FLOWER_POT)) {
            return original;
        }

        RandomSource random = settings.getRandom(original.pos());
        BlockState selected = POTS[random.nextInt(POTS.length)];
        return new StructureTemplate.StructureBlockInfo(
                original.pos(),
                selected,
                null
        );
    }

    @Override
    public MapCodec<VillageWitchFlowerPotProcessor> codec() {
        return CODEC;
    }
}
