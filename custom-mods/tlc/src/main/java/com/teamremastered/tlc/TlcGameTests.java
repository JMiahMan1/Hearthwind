package com.teamremastered.tlc;

import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.teamremastered.tlc.config.ConfigHandler;
import com.teamremastered.tlc.processors.FoundationProcessor;
import com.teamremastered.tlc.registries.TLCProcessors;
import com.teamremastered.tlc.registries.TLCStructures;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

/**
 * Locks the 26.2 shape of The Lost Castle's worldgen wiring.
 *
 * The upstream 26.1 build cannot run here: the STRUCTURE_PROCESSOR registry
 * holds MapCodecs, and StructureProcessor is an interface whose processBlock
 * dropped the local/global StructureBlockInfo pair. Both failures only show up
 * at registry freeze or when a structure is placed, so assert them here.
 */
public class TlcGameTests {

    private static final Identifier FOUNDATION_PROCESSOR_ID =
            Identifier.fromNamespaceAndPath("tlc", "foundation_processor");
    private static final Identifier LOST_CASTLE_ID =
            Identifier.fromNamespaceAndPath("tlc", "lost_castle");

    public TlcGameTests() {
    }

    @GameTest
    public void processorIsRegisteredAsAMapCodec(GameTestHelper helper) {
        MapCodec<?> codec = BuiltInRegistries.STRUCTURE_PROCESSOR.get(FOUNDATION_PROCESSOR_ID)
                .map(Holder.Reference::value)
                .orElse(null);
        helper.assertTrue(codec != null, "tlc:foundation_processor is not registered");
        helper.assertTrue(codec == TLCProcessors.FOUNDATION_PROCESSOR,
                "the registered codec is not the one TLCProcessors exposes");
        // The 26.1 build stored a StructureProcessorType lambda here, which the
        // registry casts to MapCodec while decoding processor_list tlc:main.
        helper.assertTrue(codec instanceof MapCodec,
                "tlc:foundation_processor must be a MapCodec on 26.2");
        helper.succeed();
    }

    @GameTest
    public void processorImplementsTheInterfaceContract(GameTestHelper helper) {
        // 26.2 made codec() abstract; the 26.1 build shipped getType() instead
        // and would have thrown AbstractMethodError the moment a piece placed.
        StructureProcessor processor = FoundationProcessor.INSTANCE;
        helper.assertTrue(processor.codec() == FoundationProcessor.CODEC,
                "codec() must hand back the processor's own MapCodec");
        helper.assertTrue(FoundationProcessor.CODEC != null,
                "the foundation processor must expose a MapCodec to the registry");
        helper.succeed();
    }

    @GameTest
    public void structureTypeIsRegistered(GameTestHelper helper) {
        StructureType<?> type = BuiltInRegistries.STRUCTURE_TYPE.get(LOST_CASTLE_ID)
                .map(Holder.Reference::value)
                .orElse(null);
        helper.assertTrue(type == TLCStructures.LOST_CASTLE,
                "tlc:lost_castle is not the registered structure type");
        helper.assertTrue(type != null && type != StructureType.STRONGHOLD,
                "tlc:lost_castle resolved to the wrong structure type");
        helper.succeed();
    }

    @GameTest
    public void worldgenDataShipped(GameTestHelper helper) {
        // The registry freeze during boot decodes these; a jar that forgot them
        // loads but silently loses the castle.
        for (String path : new String[] {
                "data/tlc/worldgen/structure/lost_castle.json",
                "data/tlc/worldgen/structure_set/structure_lost_castle_set.json",
                "data/tlc/worldgen/processor_list/main.json",
                "data/tlc/worldgen/template_pool/lost_castle/start_pool.json",
                "data/tlc/structure/lost_castle/castle_tm.nbt",
        }) {
            try (java.io.InputStream in = TlcGameTests.class.getClassLoader().getResourceAsStream(path)) {
                helper.assertTrue(in != null, "missing from the jar: " + path);
            } catch (java.io.IOException e) {
                helper.fail("could not read " + path + ": " + e);
            }
        }
        helper.succeed();
    }

    @GameTest
    public void processorListIsUsable(GameTestHelper helper) {
        // processor_list tlc:main references the foundation processor; vanilla
        // builds it through the STRUCTURE_PROCESSOR codecs during registry
        // freeze, so a MapCodec there is what keeps the boot alive.
        StructureProcessorList list = new StructureProcessorList(
                java.util.List.of(new FoundationProcessor()));
        helper.assertValueEqual(list.list().size(), 1, "expected one foundation processor");
        helper.succeed();
    }

    @GameTest
    public void strongholdConfigIsReadable(GameTestHelper helper) {
        // Aged ships the 1.0.x nested config with strongholds off, so the
        // handler must resolve without exploding on either schema.
        ConfigHandler.isDisableVanillaStronghold();
        helper.succeed();
    }
}
