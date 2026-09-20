package net.dungeonz.init;

import net.dungeonz.dungeon.DungeonChunkGenerator;
import net.minecraft.world.level.Level;
import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

public class DimensionInit {

    public static final ResourceKey<Level> DUNGEON_WORLD = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("dungeonz", "dungeon"));
    public static final ResourceKey<DimensionType> DUNGEON_DIMENSION_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("dungeonz", "dungeon"));

    public static void init() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Identifier.fromNamespaceAndPath("dungeonz", "dungeon"), DungeonChunkGenerator.CODEC);
    }

}