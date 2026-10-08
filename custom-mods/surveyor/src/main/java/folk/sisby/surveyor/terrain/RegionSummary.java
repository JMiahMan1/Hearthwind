package folk.sisby.surveyor.terrain;

import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.config.SystemMode;
import folk.sisby.surveyor.packet.S2CUpdateRegionPacket;
import folk.sisby.surveyor.util.RegionPos;
import folk.sisby.surveyor.util.RegistryPalette;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public class RegionSummary {
   public static final String KEY_BIOMES = "biomes";
   public static final String KEY_BLOCKS = "blocks";
   public static final String KEY_BIOME_WATER = "biomeWater";
   public static final String KEY_BIOME_FOLIAGE = "biomeFoliage";
   public static final String KEY_BIOME_GRASS = "biomeGrass";
   public static final String KEY_BLOCK_COLORS = "blockColors";
   public static final String KEY_CHUNKS = "chunks";
   protected final RegionPos regionPos;
   protected final File saveFile;
   protected final WorldSummary summary;
   @Nullable
   protected RegistryPalette<Biome> biomePalette;
   @Nullable
   protected RegistryPalette<Block> blockPalette;
   @Nullable
   protected ChunkSummary[][] chunks;
   @Nullable
   protected BitSet bitSet;
   protected boolean dirty = false;
   protected boolean saving = false;

   private RegionSummary(
      WorldSummary summary,
      File saveFile,
      RegionPos regionPos,
      ChunkSummary[][] chunks,
      @Nullable BitSet bitSet,
      @Nullable RegistryPalette<Biome> biomePalette,
      @Nullable RegistryPalette<Block> blockPalette
   ) {
      this.summary = summary;
      this.biomePalette = biomePalette;
      this.blockPalette = blockPalette;
      this.saveFile = saveFile;
      this.regionPos = regionPos;
      this.chunks = chunks;
      this.bitSet = bitSet;
      if (bitSet == null) {
         this.readNbt(regionPos, true);
      }
   }

   public static <T, O> List<O> mapIterable(Iterable<T> palette, Function<T, O> mapper) {
      List<O> list = new ArrayList<>();

      for (T value : palette) {
         list.add(mapper.apply(value));
      }

      return list;
   }

   public static RegionSummary fromEmpty(File folder, WorldSummary summary, RegionPos regionPos) {
      return new RegionSummary(
         summary,
         new File(folder, "c.%d.%d.dat".formatted(regionPos.x(), regionPos.z())),
         regionPos,
         new ChunkSummary[32][32],
         new BitSet(1024),
         new RegistryPalette<>(summary.manager().lookupOrThrow(Registries.BIOME)),
         new RegistryPalette<>(summary.manager().lookupOrThrow(Registries.BLOCK))
      );
   }

   public static RegionSummary fromFile(File file, WorldSummary summary, RegionPos regionPos) {
      return new RegionSummary(summary, file, regionPos, null, null, null, null);
   }

   protected void readNbt(RegionPos pos, boolean bitsOnly) {
      Registry<Biome> biomeRegistry = this.summary.manager().lookupOrThrow(Registries.BIOME);
      Registry<Block> blockRegistry = this.summary.manager().lookupOrThrow(Registries.BLOCK);
      CompoundTag nbt = new CompoundTag();

      try {
         nbt = NbtIo.readCompressed(this.saveFile.toPath(), NbtAccounter.unlimitedHeap());
      } catch (ReportedNbtException | IOException var17) {
         Surveyor.LOGGER.error("[Surveyor] Error reading region summary file {}.", this.saveFile.getName(), var17);
      }

      CompoundTag chunksCompound = nbt.getCompound("chunks").orElse(new CompoundTag());
      BitSet oldSet = this.bitSet;
      this.bitSet = new BitSet(1024);
      if (bitsOnly) {
         for (String posKey : chunksCompound.keySet()) {
            int x = RegionPos.regionRelative(Integer.parseInt(posKey.split(",")[0]));
            int z = RegionPos.regionRelative(Integer.parseInt(posKey.split(",")[1]));
            this.bitSet.set(RegionPos.chunkToBit(x, z));
         }

         if (oldSet != null && oldSet.cardinality() > this.bitSet.cardinality()) {
            Surveyor.LOGGER
               .warn("[Surveyor] Reloading region {} caused {} chunks to be dropped.", this.regionPos, oldSet.cardinality() - this.bitSet.cardinality());
         }
      } else {
         this.biomePalette = new RegistryPalette<>(this.summary.manager().lookupOrThrow(Registries.BIOME));
         this.blockPalette = new RegistryPalette<>(this.summary.manager().lookupOrThrow(Registries.BLOCK));
         this.chunks = new ChunkSummary[32][32];
         ListTag biomeList = nbt.getList("biomes").orElse(new ListTag());
         Map<Integer, Integer> biomeRemap = new Int2IntArrayMap(biomeList.size());

         for (int i = 0; i < biomeList.size(); i++) {
            Identifier biomeId = Identifier.tryParse((String)biomeList.get(i).asString().orElseThrow());
            Biome biome = (Biome)biomeRegistry.getValue(biomeId);
            Biome newBiome = biome == null ? (Biome)biomeRegistry.getValue(Biomes.THE_VOID) : biome;
            int newIndex = this.biomePalette.findOrAdd(newBiome);
            if (biome == null || newIndex != i) {
               if (biome == null) {
                  Surveyor.LOGGER
                     .warn(
                        "[Surveyor] Remapping biome palette in region {}: {} (#{}) is now {} (#{})",
                        new Object[]{pos, biomeId, i, biomeRegistry.getKey(newBiome), newIndex}
                     );
               }

               biomeRemap.put(i, newIndex);
               this.dirty();
            }
         }

         ListTag blockList = nbt.getList("blocks").orElse(new ListTag());
         Map<Integer, Integer> blockRemap = new Int2IntArrayMap(blockList.size());

         for (int ix = 0; ix < blockList.size(); ix++) {
            Identifier blockId = Identifier.tryParse((String)blockList.get(ix).asString().orElseThrow());
            Block block = (Block)blockRegistry.getValue(blockId);
            Block newBlock = block == null ? Blocks.AIR : block;
            int newIndex = this.blockPalette.findOrAdd(newBlock);
            if (block == null || newIndex != ix) {
               if (block == null) {
                  Surveyor.LOGGER
                     .warn(
                        "[Surveyor] Remapping block palette in region {}: {} (#{}) is now {} (#{})",
                        new Object[]{pos, blockList.get(ix).asString(), ix, blockRegistry.getKey(newBlock), newIndex}
                     );
               }

               blockRemap.put(ix, newIndex);
               this.dirty();
            }
         }

         if (this.biomePalette.view().size() != 0 && this.blockPalette.view().size() != 0) {
            for (String posKey : chunksCompound.keySet()) {
               int x = RegionPos.regionRelative(Integer.parseInt(posKey.split(",")[0]));
               int z = RegionPos.regionRelative(Integer.parseInt(posKey.split(",")[1]));
               ChunkSummary summary = new ChunkSummary((CompoundTag)chunksCompound.getCompound(posKey).orElseThrow());
               this.set(x, z, summary);
               if (!biomeRemap.isEmpty() || !blockRemap.isEmpty()) {
                  summary.remap(biomeRemap, blockRemap);
               }
            }

            if (oldSet != null && oldSet.cardinality() > this.bitSet.cardinality()) {
               Surveyor.LOGGER
                  .warn("[Surveyor] Reloading region {} caused {} chunks to be dropped.", this.regionPos, oldSet.cardinality() - this.bitSet.cardinality());
            }
         } else {
            Surveyor.LOGGER.warn("[Surveyor] Palette was empty in region {}, skipping data load!", this.regionPos);
         }
      }
   }

   public boolean contains(ChunkPos pos) {
      return this.bitSet().get(RegionPos.chunkToBit(pos));
   }

   public ChunkSummary get(ChunkPos pos) {
      return !this.contains(pos) ? null : this.get(RegionPos.regionRelative(pos.x()), RegionPos.regionRelative(pos.z()));
   }

   @Nullable
   protected ChunkSummary get(int x, int z) {
      if (this.chunks == null) {
         this.readNbt(this.regionPos, false);
      }

      return this.chunks[x][z];
   }

   protected void set(int x, int z, ChunkSummary summary) {
      if (this.chunks == null || this.bitSet == null) {
         this.readNbt(this.regionPos, false);
      }

      this.chunks[x][z] = summary;
      this.bitSet.set(RegionPos.chunkToBit(x, z), summary != null);
   }

   public BitSet bitSet() {
      if (this.bitSet == null) {
         this.readNbt(this.regionPos, true);
      }

      return (BitSet)this.bitSet.clone();
   }

   public void putChunk(Level world, LevelChunk chunk) {
      if (Surveyor.CONFIG.terrain != SystemMode.FROZEN) {
         if (world.getHeight() != 0) {
            if (this.chunks == null) {
               this.readNbt(this.regionPos, false);
            }

            this.set(
               RegionPos.regionRelative(chunk.getPos().x()),
               RegionPos.regionRelative(chunk.getPos().z()),
               new ChunkSummary(world, chunk, DimensionSupport.getSummaryLayers(world), this.biomePalette, this.blockPalette, !(world instanceof ServerLevel))
            );
            this.dirty();
         }
      }
   }

   public boolean isUnloaded(Level world) {
      return this.regionPos.toChunks().stream().noneMatch(c -> world.hasChunk(c.x(), c.z()));
   }

   public void save(boolean unload) {
      if (!this.saving) {
         if (!this.isDirty()) {
            if (unload) {
               this.chunks = null;
            }
         } else {
            CompoundTag nbt = new CompoundTag();
            Registry<Biome> biomeRegistry = this.summary.manager().lookupOrThrow(Registries.BIOME);
            Registry<Block> blockRegistry = this.summary.manager().lookupOrThrow(Registries.BLOCK);
            nbt.put("biomes", new ListTag(mapIterable(this.biomePalette.view(), b -> StringTag.valueOf(biomeRegistry.getKey(b).toString()))));
            nbt.put("blocks", new ListTag(mapIterable(this.blockPalette.view(), b -> StringTag.valueOf(blockRegistry.getKey(b).toString()))));
            nbt.putIntArray("biomeWater", mapIterable(this.biomePalette.view(), Biome::getWaterColor).stream().mapToInt(i -> i).toArray());
            nbt.putIntArray("biomeFoliage", mapIterable(this.biomePalette.view(), Biome::getFoliageColor).stream().mapToInt(i -> i).toArray());
            nbt.putIntArray("biomeGrass", mapIterable(this.biomePalette.view(), b -> b.getGrassColor(0.0, 0.0)).stream().mapToInt(i -> i).toArray());
            nbt.putIntArray("blockColors", mapIterable(this.blockPalette.view(), b -> b.defaultMapColor().col).stream().mapToInt(i -> i).toArray());
            CompoundTag chunksCompound = new CompoundTag();
            this.regionPos.forXZ((x, z) -> {
               ChunkSummary chunk = this.get(x, z);
               if (chunk != null) {
                  ChunkPos pos = this.regionPos.toChunk(x, z);
                  chunksCompound.put("%s,%s".formatted(pos.x(), pos.z()), chunk.writeNbt(new CompoundTag()));
               }
            });
            nbt.put("chunks", chunksCompound);
            this.dirty = false;
            this.saving = true;
            Util.ioPool().execute(() -> {
               try {
                  NbtIo.writeCompressed(nbt, this.saveFile.toPath());
               } catch (IOException var7) {
                  Surveyor.LOGGER.error("[Surveyor] Error writing region summary file {}.", this.saveFile.getName(), var7);
               } finally {
                  if (unload && !this.dirty) {
                     this.chunks = null;
                  }

                  this.saving = false;
               }
            });
         }
      }
   }

   public BitSet readUpdatePacket(S2CUpdateRegionPacket packet) {
      if (Surveyor.CONFIG.terrain == SystemMode.FROZEN) {
         return new BitSet();
      } else {
         if (this.chunks == null) {
            this.readNbt(this.regionPos, false);
         }

         if (!packet.biomePalette().isEmpty() && !packet.blockPalette().isEmpty()) {
            Registry<Biome> biomeRegistry = this.summary.manager().lookupOrThrow(Registries.BIOME);
            Registry<Block> blockRegistry = this.summary.manager().lookupOrThrow(Registries.BLOCK);
            Map<Integer, Integer> biomeRemap = new Int2IntArrayMap();

            for (int i = 0; i < packet.biomePalette().size(); i++) {
               biomeRemap.put(i, this.biomePalette.findOrAdd((Biome)biomeRegistry.byId(packet.biomePalette().get(i))));
            }

            Map<Integer, Integer> blockRemap = new Int2IntArrayMap();

            for (int i = 0; i < packet.blockPalette().size(); i++) {
               blockRemap.put(i, this.blockPalette.findOrAdd((Block)blockRegistry.byId(packet.blockPalette().get(i))));
            }

            int[] indices = packet.set().stream().toArray();

            for (int i = 0; i < packet.chunks().size(); i++) {
               ChunkSummary summary = packet.chunks().get(i);
               summary.remap(biomeRemap, blockRemap);
               this.set(RegionPos.bitToX(indices[i]), RegionPos.bitToZ(indices[i]), summary);
            }

            this.dirty();
            return packet.set();
         } else {
            return new BitSet();
         }
      }
   }

   public S2CUpdateRegionPacket createUpdatePacket(ResourceKey<Level> dimension, boolean shared, RegionPos regionPos, BitSet set) {
      if (this.chunks == null) {
         this.readNbt(regionPos, false);
      }

      BitSet realSet = (BitSet)set.clone();
      realSet.and(this.bitSet);
      return new S2CUpdateRegionPacket(
         dimension,
         shared,
         regionPos,
         mapIterable(this.biomePalette, i -> i),
         mapIterable(this.blockPalette, i -> i),
         realSet,
         realSet.stream().mapToObj(i -> this.get(RegionPos.bitToX(i), RegionPos.bitToZ(i))).toList()
      );
   }

   public RegistryPalette<Biome>.ValueView getBiomePalette() {
      if (this.chunks == null) {
         this.readNbt(this.regionPos, false);
      }

      return this.biomePalette.view();
   }

   public RegistryPalette<Block>.ValueView getBlockPalette() {
      if (this.chunks == null) {
         this.readNbt(this.regionPos, false);
      }

      return this.blockPalette.view();
   }

   public boolean isLoaded() {
      return this.chunks != null;
   }

   public boolean isDirty() {
      return this.dirty && Surveyor.CONFIG.terrain != SystemMode.FROZEN;
   }

   private void dirty() {
      this.dirty = true;
   }
}
