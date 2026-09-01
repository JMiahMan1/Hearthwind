package com.faboslav.villagesandpillages.platform;

import com.faboslav.villagesandpillages.VillagesAndPillages;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class StructureProcessorTypeRegistry {
    public static <P extends StructureProcessor> MapCodec<P> registerStructureProcessorType(String name, MapCodec<P> codec) {
        return Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, VillagesAndPillages.makeID(name), codec);
    }
}
