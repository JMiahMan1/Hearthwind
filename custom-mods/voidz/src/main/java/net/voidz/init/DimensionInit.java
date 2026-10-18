package net.voidz.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.voidz.dimension.VoidChunkGenerator;

public class DimensionInit {
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> VOID_WORLD = ResourceKey.create(
      Registries.DIMENSION, Identifier.fromNamespaceAndPath("voidz", "void")
   );
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.dimension.DimensionType> VOID_DIMENSION_TYPE_KEY = ResourceKey.create(
      Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("voidz", "void")
   );

   public static void init() {
      Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Identifier.fromNamespaceAndPath("voidz", "void"), VoidChunkGenerator.CODEC);
   }
}
