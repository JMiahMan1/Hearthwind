package com.faboslav.villagesandpillages.init;

import com.faboslav.villagesandpillages.platform.StructureProcessorTypeRegistry;
import com.faboslav.villagesandpillages.world.processor.*;
import com.mojang.serialization.MapCodec;

public final class VillagesAndPillagesProcessorTypes {
    public static MapCodec<PillarProcessor> PILLAR_PROCESSOR = PillarProcessor.CODEC;
    public static MapCodec<CollapsedUnderwaterProcessor> COLLAPSED_UNDERWATER_PROCESSOR = CollapsedUnderwaterProcessor.CODEC;
    public static MapCodec<VillagerWitchOpenedDoorProcessor> VILLAGE_WITCH_OPENED_DOOR_PROCESSOR = VillagerWitchOpenedDoorProcessor.CODEC;
    public static MapCodec<VillageWitchBrewingStandProcessor> VILLAGE_WITCH_BREWING_STAND_PROCESSOR = VillageWitchBrewingStandProcessor.CODEC;
    public static MapCodec<VillageWitchFlowerPotProcessor> VILLAGE_WITCH_FLOWER_POT_PROCESSOR = VillageWitchFlowerPotProcessor.CODEC;
    public static MapCodec<WaterlogProcessor> WATERLOG_PROCESSOR = WaterlogProcessor.CODEC;

    public static void init() {
        StructureProcessorTypeRegistry.registerStructureProcessorType("pillar_processor", PILLAR_PROCESSOR);
        StructureProcessorTypeRegistry.registerStructureProcessorType("collapsed_underwater_processor", COLLAPSED_UNDERWATER_PROCESSOR);
        StructureProcessorTypeRegistry.registerStructureProcessorType("village_witch_opened_door_processor", VILLAGE_WITCH_OPENED_DOOR_PROCESSOR);
        StructureProcessorTypeRegistry.registerStructureProcessorType("village_witch_brewing_stand_processor", VILLAGE_WITCH_BREWING_STAND_PROCESSOR);
        StructureProcessorTypeRegistry.registerStructureProcessorType("village_witch_flower_pot_processor", VILLAGE_WITCH_FLOWER_POT_PROCESSOR);
        StructureProcessorTypeRegistry.registerStructureProcessorType("waterlog_processor", WATERLOG_PROCESSOR);
    }

    private VillagesAndPillagesProcessorTypes() {}
}
