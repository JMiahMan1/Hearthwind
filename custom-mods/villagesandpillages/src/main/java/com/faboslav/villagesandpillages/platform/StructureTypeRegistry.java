package com.faboslav.villagesandpillages.platform;

import com.faboslav.villagesandpillages.VillagesAndPillages;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class StructureTypeRegistry {
    public static <T extends Structure> StructureType<T> registerStructureType(String name, StructureType<T> type) {
        return Registry.register(BuiltInRegistries.STRUCTURE_TYPE, VillagesAndPillages.makeID(name), type);
    }
}
