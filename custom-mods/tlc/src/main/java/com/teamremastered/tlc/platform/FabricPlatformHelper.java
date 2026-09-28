package com.teamremastered.tlc.platform;

import com.mojang.serialization.MapCodec;
import com.teamremastered.tlc.platform.services.IPlatformHelper;
import com.teamremastered.tlc.registries.TLCProcessors;
import com.teamremastered.tlc.registries.TLCStructures;
import com.teamremastered.tlc.structures.LostCastle;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public StructureType<LostCastle> getStructureType() {
        return TLCStructures.LOST_CASTLE;
    }

    @Override
    public MapCodec<? extends StructureProcessor> getProcessorCodec() {
        return TLCProcessors.FOUNDATION_PROCESSOR;
    }
}
