package net.voidz.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class VoidChunkGenerator extends ChunkGenerator {
   public static final MapCodec<VoidChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(RegistryOps.retrieveElement(Biomes.PLAINS)).apply(instance, instance.stable(VoidChunkGenerator::new))
   );

   public VoidChunkGenerator(net.minecraft.core.Holder.Reference<net.minecraft.world.level.biome.Biome> biomeEntry) {
      super(new FixedBiomeSource(biomeEntry));
   }

   protected MapCodec<? extends net.minecraft.world.level.chunk.ChunkGenerator> codec() {
      return CODEC;
   }

   public void spawnOriginalMobs(WorldGenRegion region) {
   }

   public void applyBiomeDecoration(WorldGenLevel world, ChunkAccess chunk, StructureManager structureAccessor) {
   }

   public void createStructures(
      RegistryAccess registryManager,
      ChunkGeneratorStructureState placementCalculator,
      StructureManager structureAccessor,
      ChunkAccess chunk,
      StructureTemplateManager structureTemplateManager,
      net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimensionKey
   ) {
   }

   public void createReferences(WorldGenLevel world, StructureManager accessor, ChunkAccess chunk) {
   }

   public WeightedList<net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData> getMobsAt(
      net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biome,
      net.minecraft.world.level.StructureManager accessor,
      net.minecraft.world.entity.MobCategory group,
      net.minecraft.core.BlockPos pos
   ) {
      return WeightedList.of();
   }

   public int getGenDepth() {
      return 256;
   }

   public int getSeaLevel() {
      return 0;
   }

   public int getMinY() {
      return 0;
   }

   public void applyCarvers(
      WorldGenRegion chunkRegion,
      long l,
      RandomState noiseConfig,
      BiomeManager biomeAccess,
      StructureManager structureAccessor,
      ChunkAccess chunk
   ) {
   }

   public void buildSurface(WorldGenRegion chunkRegion, StructureManager structureAccessor, RandomState noiseConfig, ChunkAccess chunk) {
   }

   public CompletableFuture<net.minecraft.world.level.chunk.ChunkAccess> fillFromNoise(
      net.minecraft.world.level.levelgen.blending.Blender blender,
      net.minecraft.world.level.levelgen.RandomState noiseConfig,
      net.minecraft.world.level.StructureManager structureAccessor,
      net.minecraft.world.level.chunk.ChunkAccess chunk
   ) {
      return CompletableFuture.completedFuture(chunk);
   }

   public int getBaseHeight(int x, int z, Types type, LevelHeightAccessor heightLimitView, RandomState noiseConfig) {
      return 0;
   }

   public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor var3, RandomState var4) {
      return new NoiseColumn(z, new BlockState[0]);
   }

   public void addDebugScreenInfo(List<String> list, net.minecraft.world.level.levelgen.RandomState noiseConfig, net.minecraft.core.BlockPos blockPos) {
   }
}
