package com.teamremastered.tlc.platform.services;

import com.mojang.serialization.MapCodec;
import com.teamremastered.tlc.structures.LostCastle;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public interface IPlatformHelper {
    StructureType<LostCastle> getStructureType();

    /**
     * 26.2 registers structure processors as their own MapCodec
     * ({@code Registry<MapCodec<? extends StructureProcessor>>}), so the
     * platform hands back the codec rather than the old
     * {@code StructureProcessorType} token.
     */
    MapCodec<? extends StructureProcessor> getProcessorCodec();
}
