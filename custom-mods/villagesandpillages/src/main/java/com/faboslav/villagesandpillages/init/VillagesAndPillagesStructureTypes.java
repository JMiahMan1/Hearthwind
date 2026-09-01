package com.faboslav.villagesandpillages.init;

import com.faboslav.villagesandpillages.VillagesAndPillages;
import com.faboslav.villagesandpillages.platform.StructureTypeRegistry;
import com.faboslav.villagesandpillages.world.structures.VillageWitchStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class VillagesAndPillagesStructureTypes {
    public static final TagKey<Structure> VILLAGE_WITCH = TagKey.create(Registries.STRUCTURE,
            VillagesAndPillages.makeID("village_witch"));
    public static StructureType<VillageWitchStructure> VILLAGE_WITCH_STRUCTURE = () -> VillageWitchStructure.CODEC;

    public static void init() {
        StructureTypeRegistry.registerStructureType("village_witch_structure", VILLAGE_WITCH_STRUCTURE);
    }

    private VillagesAndPillagesStructureTypes() {}
}
