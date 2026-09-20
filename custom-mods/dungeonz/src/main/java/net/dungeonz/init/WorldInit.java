package net.dungeonz.init;

import net.dungeonz.structure.DimensionStructure;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.core.registries.BuiltInRegistries;

public class WorldInit {

    public static StructureType<DimensionStructure> DIMENSION_STRUCTURES;

    public static void init() {
        DIMENSION_STRUCTURES = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, Identifier.fromNamespaceAndPath("dungeonz", "dimension_structures"), () -> DimensionStructure.CODEC);
    }

}
