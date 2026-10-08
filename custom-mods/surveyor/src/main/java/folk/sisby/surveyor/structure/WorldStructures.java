package folk.sisby.surveyor.structure;

import net.minecraft.nbt.Tag;

import folk.sisby.surveyor.util.ListTags;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Table;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.SurveyorEvents;
import folk.sisby.surveyor.SurveyorExploration;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.config.SystemMode;
import folk.sisby.surveyor.packet.S2CStructuresAddedPacket;
import folk.sisby.surveyor.util.ChunkUtil;
import folk.sisby.surveyor.util.MapUtil;
import folk.sisby.surveyor.util.RegionPos;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class WorldStructures {
   public static final String KEY_STRUCTURES = "structures";
   public static final String KEY_TYPE = "type";
   public static final String KEY_TAGS = "tags";
   protected final WorldSummary summary;
   protected final Map<RegionPos, RegionStructureSummary> regions = new ConcurrentHashMap<>();
   protected final Map<ResourceKey<Structure>, ResourceKey<StructureType<?>>> structureTypes = new ConcurrentHashMap<>();
   protected final Multimap<ResourceKey<Structure>, TagKey<Structure>> structureTags = Multimaps.synchronizedSetMultimap(HashMultimap.create());
   protected boolean dirty = false;

   public static WorldStructures of(Level world) {
      return Optional.ofNullable(world).map(WorldSummary::of).map(WorldSummary::structures).orElse(null);
   }

   public WorldStructures(
      WorldSummary summary,
      Map<RegionPos, RegionStructureSummary> regions,
      Map<ResourceKey<Structure>, ResourceKey<StructureType<?>>> structureTypes,
      Multimap<ResourceKey<Structure>, TagKey<Structure>> structureTags
   ) {
      this.summary = summary;
      this.regions.putAll(regions);
      this.structureTypes.putAll(structureTypes);
      this.structureTags.putAll(structureTags);
   }

   public static StructurePieceSummary readStructurePieceNbt(CompoundTag nbt) {
      return (StructurePieceSummary)(nbt.getString("id").equals(BuiltInRegistries.STRUCTURE_PIECE.getKey(StructurePieceType.JIGSAW).toString())
         ? new JigsawPieceSummary(nbt)
         : new StructurePieceSummary(nbt));
   }

   protected static WorldStructures readNbt(WorldSummary summary, CompoundTag nbt, Map<RegionPos, RegionStructureSummary> regions) {
      Map<ResourceKey<Structure>, ResourceKey<StructureType<?>>> structureTypes = new ConcurrentHashMap<>();
      Multimap<ResourceKey<Structure>, TagKey<Structure>> structureTags = HashMultimap.create();
      CompoundTag structuresCompound = nbt.getCompound("structures").orElse(new CompoundTag());

      for (String structureId : structuresCompound.keySet()) {
         ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, Identifier.parse(structureId));
         CompoundTag structureCompound = structuresCompound.getCompound(structureId).orElse(new CompoundTag());
         ResourceKey<StructureType<?>> type = ResourceKey.create(
            Registries.STRUCTURE_TYPE, Identifier.parse((String)structureCompound.getString("type").orElseThrow())
         );
         structureTypes.put(key, type);
         Collection<TagKey<Structure>> tags = structureCompound.getList("tags")
            .orElse(new ListTag())
            .stream()
            .map(e -> TagKey.create(Registries.STRUCTURE, Identifier.parse((String)e.asString().orElseThrow())))
            .toList();
         structureTags.putAll(key, tags);
      }

      for (RegionStructureSummary region : regions.values()) {
         region.starts.rowMap().keySet().removeIf(k -> !structureTypes.containsKey(k));
      }

      return new WorldStructures(summary, regions, structureTypes, structureTags);
   }

   public static WorldStructures load(WorldSummary summary, File folder) {
      File structuresFile = new File(folder, "structures.dat");
      CompoundTag worldNbt = new CompoundTag();
      if (structuresFile.exists()) {
         try {
            worldNbt = NbtIo.readCompressed(structuresFile.toPath(), NbtAccounter.unlimitedHeap());
         } catch (ReportedNbtException | IOException var5) {
            Surveyor.LOGGER.error("[Surveyor] Error loading structure summary file for {}.", summary.dimension().identifier(), var5);
         }
      }

      Map<RegionPos, RegionStructureSummary> regions = new HashMap<>();
      ChunkUtil.getRegionNbt(folder, "s").forEach((pos, nbt) -> regions.put(pos, RegionStructureSummary.readNbt(nbt)));
      return readNbt(summary, worldNbt, regions);
   }

   public static void onChunkLoad(ServerLevel world, LevelChunk chunk, boolean generated) {
      WorldStructures structures = of(world);
      if (structures != null) {
         chunk.getAllStarts().forEach((structure, start) -> {
            if (!structures.contains(world, start)) {
               structures.put(world, start);
            }
         });
      }
   }

   public static void onStructurePlace(ServerLevel world, StructureStart start) {
      WorldStructures structures = of(world);
      if (structures != null && !structures.contains(world, start)) {
         structures.put(world, start);
      }
   }

   public ResourceKey<StructureType<?>> getType(ResourceKey<Structure> key) {
      return this.structureTypes.get(key);
   }

   public Collection<TagKey<Structure>> getTags(ResourceKey<Structure> key) {
      return this.structureTags.get(key);
   }

   public boolean contains(Level world, StructureStart start) {
      RegionPos regionPos = RegionPos.of(start.getChunkPos());
      return this.regions.containsKey(regionPos) && this.regions.get(regionPos).contains(world, start);
   }

   public boolean contains(ResourceKey<Structure> key, ChunkPos pos) {
      RegionPos regionPos = RegionPos.of(pos);
      return this.regions.containsKey(regionPos) && this.regions.get(regionPos).contains(key, pos);
   }

   public StructureStartSummary get(ResourceKey<Structure> key, ChunkPos pos) {
      RegionPos regionPos = RegionPos.of(pos);
      return this.regions.containsKey(regionPos) ? this.regions.get(regionPos).get(key, pos) : null;
   }

   public Map<ResourceKey<Structure>, Map<ChunkPos, StructureStartSummary>> asMap(SurveyorExploration exploration) {
      Multimap<ResourceKey<Structure>, ChunkPos> keySet = this.keySet(exploration);
      Map<ResourceKey<Structure>, Map<ChunkPos, StructureStartSummary>> map = new HashMap<>();
      keySet.forEach((key, pos) -> map.computeIfAbsent(key, k -> new HashMap<>()).put(pos, this.get(key, pos)));
      return map;
   }

   public Multimap<ResourceKey<Structure>, ChunkPos> keySet(SurveyorExploration exploration) {
      Multimap<ResourceKey<Structure>, ChunkPos> map = HashMultimap.create();
      this.regions.values().forEach(r -> map.putAll(r.keySet()));
      if (exploration != null) {
         exploration.limit(this.summary.dimension(), map);
      }

      return map;
   }

   public void put(ServerLevel world, StructureStart start) {
      if (Surveyor.CONFIG.structures != SystemMode.FROZEN) {
         RegionPos regionPos = RegionPos.of(start.getChunkPos());
         ResourceKey<Structure> key = (ResourceKey<Structure>)world.registryAccess()
            .lookupOrThrow(Registries.STRUCTURE)
            .getResourceKey(start.getStructure())
            .orElseThrow();
         Optional<ResourceKey<StructureType<?>>> type = world.registryAccess()
            .lookupOrThrow(Registries.STRUCTURE_TYPE)
            .getResourceKey(start.getStructure().type());
         if (!start.isValid()) {
            Surveyor.LOGGER.error("Cowardly refusing to save structure {} as it has no pieces! Report this to the structure mod author!", key.identifier());
         } else if (type.isEmpty()) {
            Surveyor.LOGGER
               .error("Cowardly refusing to save structure {} as it has no structure type! Report this to the structure mod author!", key.identifier());
         } else {
            this.regions.computeIfAbsent(regionPos, k -> new RegionStructureSummary()).put(world, start);
            List<TagKey<Structure>> tags = world.registryAccess().lookupOrThrow(Registries.STRUCTURE).wrapAsHolder(start.getStructure()).tags().toList();
            this.structureTypes.put(key, type.orElseThrow());
            this.structureTags.putAll(key, tags);
            this.dirty();
            SurveyorEvents.Invoke.structuresAdded(this.summary, key, start.getChunkPos());
         }
      }
   }

   public void put(
      ResourceKey<Structure> key, ChunkPos pos, StructureStartSummary start, ResourceKey<StructureType<?>> type, Collection<TagKey<Structure>> tagKeys
   ) {
      if (Surveyor.CONFIG.structures != SystemMode.FROZEN) {
         RegionPos regionPos = RegionPos.of(pos);
         this.regions.computeIfAbsent(regionPos, k -> new RegionStructureSummary()).put(key, pos, start);
         this.structureTypes.put(key, type);
         this.structureTags.putAll(key, tagKeys);
         this.dirty();
         SurveyorEvents.Invoke.structuresAdded(this.summary, key, pos);
      }
   }

   protected CompoundTag writeNbt(CompoundTag nbt) {
      CompoundTag structuresCompound = new CompoundTag();
      this.structureTypes.forEach((key, starts) -> {
         CompoundTag structureCompound = new CompoundTag();
         structureCompound.putString("type", this.structureTypes.get(key).identifier().toString());
         structureCompound.put("tags", ListTags.of(this.structureTags.get(key).stream().map(t -> (Tag) StringTag.valueOf(t.location().toString())).toList()));
         structuresCompound.put(key.identifier().toString(), structureCompound);
      });
      nbt.put("structures", structuresCompound);
      return nbt;
   }

   public int save(File folder) {
      List<RegionPos> savedRegions = new ArrayList<>();
      if (this.isDirty()) {
         File structureFile = new File(folder, "structures.dat");
         CompoundTag structureCompound = this.writeNbt(new CompoundTag());
         Util.ioPool().execute(() -> {
            try {
               NbtIo.writeCompressed(structureCompound, structureFile.toPath());
            } catch (IOException var4x) {
               Surveyor.LOGGER.error("[Surveyor] Error writing world structure summary file for {}.", this.summary.dimension().identifier(), var4x);
            }
         });
         this.dirty = false;
         this.regions.forEach((pos, summary) -> {
            if (summary.isDirty()) {
               savedRegions.add(pos);
               CompoundTag regionCompound = summary.writeNbt(new CompoundTag());
               File regionFile = new File(folder, "s.%d.%d.dat".formatted(pos.x(), pos.z()));
               Util.ioPool().execute(() -> {
                  try {
                     NbtIo.writeCompressed(regionCompound, regionFile.toPath());
                  } catch (IOException var3x) {
                     Surveyor.LOGGER.error("[Surveyor] Error writing region structure summary file {}.", regionFile.getName(), var3x);
                  }
               });
               summary.dirty = false;
            }
         });
      }

      return savedRegions.size();
   }

   public Multimap<ResourceKey<Structure>, ChunkPos> readUpdatePacket(S2CStructuresAddedPacket packet) {
      if (Surveyor.CONFIG.structures == SystemMode.FROZEN) {
         return HashMultimap.create();
      } else {
         packet.starts()
            .cellSet()
            .forEach(
               c -> this.put(
                  (ResourceKey<Structure>)c.getRowKey(),
                  (ChunkPos)c.getColumnKey(),
                  (StructureStartSummary)c.getValue(),
                  packet.types().get(c.getRowKey()),
                  packet.tags().get((ResourceKey)c.getRowKey())
               )
            );
         return MapUtil.keyMultiMap(packet.starts());
      }
   }

   public S2CStructuresAddedPacket createUpdatePacket(boolean shared, Multimap<ResourceKey<Structure>, ChunkPos> keySet) {
      Table<ResourceKey<Structure>, ChunkPos, StructureStartSummary> packetStructures = HashBasedTable.create();
      Map<ResourceKey<Structure>, ResourceKey<StructureType<?>>> packetTypes = new HashMap<>();
      Multimap<ResourceKey<Structure>, TagKey<Structure>> packetTags = HashMultimap.create();
      keySet.forEach((keyx, pos) -> packetStructures.put(keyx, pos, this.get(keyx, pos)));

      for (ResourceKey<Structure> key : keySet.keySet()) {
         packetTypes.put(key, this.getType(key));
         packetTags.putAll(key, this.getTags(key));
      }

      return new S2CStructuresAddedPacket(this.summary.dimension(), shared, packetStructures, packetTypes, packetTags);
   }

   public boolean isDirty() {
      return (this.dirty || this.regions.values().stream().anyMatch(RegionStructureSummary::isDirty)) && Surveyor.CONFIG.structures != SystemMode.FROZEN;
   }

   private void dirty() {
      this.dirty = true;
   }
}
