package com.teamremastered.tlc.registries;

import com.mojang.serialization.MapCodec;
import com.teamremastered.tlc.processors.FoundationProcessor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

/**
 * 26.2 turned {@code STRUCTURE_PROCESSOR} into a
 * {@code Registry<MapCodec<? extends StructureProcessor>>}: a processor is
 * identified by its own codec, and {@code StructureProcessor} became an
 * interface that must implement {@code codec()}.  Upstream 2.1.1 still
 * registered a {@code StructureProcessorType} supplier, which the loader
 * accepted at registration time and then failed on with
 * "TLCProcessors$$Lambda cannot be cast to MapCodec" while freezing the
 * processor_list registry.
 */
public final class TLCProcessors {
    public static final MapCodec<FoundationProcessor> FOUNDATION_PROCESSOR = FoundationProcessor.CODEC;

    private TLCProcessors() {
    }

    public static void init() {
        Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR,
                Identifier.fromNamespaceAndPath("tlc", "foundation_processor"),
                (MapCodec<? extends StructureProcessor>) FOUNDATION_PROCESSOR);
    }
}
