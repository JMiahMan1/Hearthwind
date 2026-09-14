package net.dungeonz.init;

import net.dungeonz.structure.DimensionStructure;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_7151;
import net.minecraft.class_7923;

public class WorldInit {

    public static class_7151<DimensionStructure> DIMENSION_STRUCTURES;

    public static void init() {
        DIMENSION_STRUCTURES = class_2378.method_10230(class_7923.field_41147, class_2960.method_60655("dungeonz", "dimension_structures"), () -> DimensionStructure.CODEC);
    }

}
