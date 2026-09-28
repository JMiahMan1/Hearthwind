package com.teamremastered.tlc.registries;

import com.teamremastered.tlc.structures.LostCastle;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** Unchanged by 26.2: STRUCTURE_TYPE is still a Registry&lt;StructureType&lt;?&gt;&gt;. */
public final class TLCStructures {
    public static StructureType<LostCastle> LOST_CASTLE;

    private TLCStructures() {
    }

    public static void init() {
        LOST_CASTLE = Registry.register(BuiltInRegistries.STRUCTURE_TYPE,
                Identifier.fromNamespaceAndPath("tlc", "lost_castle"),
                () -> LostCastle.CODEC);
    }
}
