package folk.sisby.surveyor.terrain;

import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.SurveyorEvents;
import folk.sisby.surveyor.SurveyorExploration;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.config.SystemMode;
import folk.sisby.surveyor.packet.S2CUpdateRegionPacket;
import folk.sisby.surveyor.util.ChunkUtil;
import folk.sisby.surveyor.util.RegionPos;
import folk.sisby.surveyor.util.RegistryPalette;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public class WorldTerrain {
   protected final WorldSummary summary;
   protected final Map<RegionPos, RegionSummary> regions = new ConcurrentHashMap<>();
   protected final File folder;
   protected final Map<RegionPos, Map<UUID, BitSet>> queuedUpdates = new LinkedHashMap<>();

   public static WorldTerrain of(Level world) {
      return Optional.ofNullable(world).map(WorldSummary::of).map(WorldSummary::terrain).orElse(null);
   }

   public WorldTerrain(WorldSummary summary, Map<RegionPos, RegionSummary> regions, File folder) {
      this.summary = summary;
      this.regions.putAll(regions);
      this.folder = folder;
   }

   public static Set<ChunkPos> toKeys(Map<RegionPos, BitSet> bitSets) {
      return toKeys(bitSets, Comparator.comparingInt(pos -> pos.x() + pos.z()));
   }

   public static Set<ChunkPos> toKeys(Map<RegionPos, BitSet> bitSets, ChunkPos originChunk) {
      ChunkPos oPos = new ChunkPos(RegionPos.chunkToRegion(originChunk.x()), RegionPos.chunkToRegion(originChunk.z()));
      return toKeys(bitSets, Comparator.comparingDouble(pos -> (oPos.x() - pos.x()) * (oPos.x() - pos.x()) + (oPos.z() - pos.z()) * (oPos.z() - pos.z())));
   }

   public static Set<ChunkPos> toKeys(Map<RegionPos, BitSet> bitSets, Comparator<RegionPos> regionComparator) {
      Set<ChunkPos> set = new LinkedHashSet<>();
      bitSets.entrySet()
         .stream()
         .sorted(Entry.comparingByKey(regionComparator))
         .forEach(e -> e.getValue().stream().forEach(i -> set.add(e.getKey().toChunk(i))));
      return set;
   }

   public static WorldTerrain load(WorldSummary summary, File folder) {
      Map<RegionPos, RegionSummary> regions = new HashMap<>();
      ChunkUtil.getRegionFiles(folder, "c").forEach((pos, file) -> regions.put(pos, RegionSummary.fromFile(file, summary, pos)));
      return new WorldTerrain(summary, regions, folder);
   }

   public static void onChunkLoad(Level world, LevelChunk chunk, boolean generated) {
      WorldTerrain terrain = of(world);
      if (terrain != null) {
         ChunkSummary chunkSummary = terrain.get(chunk.getPos());
         if (chunkSummary == null || !Surveyor.CONFIG.lazyClientUpdating || !ChunkUtil.airCount(chunk).equals(chunkSummary.getAirCount())) {
            terrain.put(world, chunk);
         }
      }
   }

   public static void onChunkUnload(Level world, LevelChunk chunk) {
      WorldTerrain terrain = of(world);
      if (terrain != null) {
         if (chunk.isUnsaved()) {
            terrain.put(world, chunk);
         }
      }
   }

   public boolean contains(ChunkPos pos) {
      RegionPos regionPos = RegionPos.of(pos);
      return this.regions.containsKey(regionPos) && this.regions.get(regionPos).contains(pos);
   }

   public ChunkSummary get(ChunkPos pos) {
      RegionPos regionPos = RegionPos.of(pos);
      return this.regions.containsKey(regionPos) ? this.regions.get(regionPos).get(pos) : null;
   }

   public RegionSummary getRegion(RegionPos regionPos) {
      return this.regions.computeIfAbsent(regionPos, k -> RegionSummary.fromEmpty(this.folder, this.summary, regionPos));
   }

   public RegistryPalette<Biome>.ValueView getBiomePalette(ChunkPos pos) {
      RegionPos regionPos = RegionPos.of(pos);
      return this.regions.get(regionPos).getBiomePalette();
   }

   public RegistryPalette<Block>.ValueView getBlockPalette(ChunkPos pos) {
      RegionPos regionPos = RegionPos.of(pos);
      return this.regions.get(regionPos).getBlockPalette();
   }

   public Map<RegionPos, BitSet> bitSet(SurveyorExploration exploration) {
      Map<RegionPos, BitSet> map = new HashMap<>();
      this.regions.forEach((p, r) -> map.put(p, r.bitSet()));
      return exploration == null ? map : exploration.limit(this.summary.dimension(), map);
   }

   public void put(Level world, LevelChunk chunk) {
      if (Surveyor.CONFIG.terrain != SystemMode.FROZEN) {
         this.regions
            .computeIfAbsent(RegionPos.of(chunk.getPos()), k -> RegionSummary.fromEmpty(this.folder, this.summary, RegionPos.of(chunk.getPos())))
            .putChunk(world, chunk);
         SurveyorEvents.Invoke.terrainUpdated(WorldSummary.of(world), chunk.getPos());
      }
   }

   public static void onTick(ServerLevel world) {
      WorldTerrain terrain = of(world);
      if (terrain != null) {
         terrain.serverTick(world);
      }
   }

   public void sendUpdateForRegion(RegionPos regionPos, ServerPlayer player, BitSet set) {
      RegionSummary region = this.getRegion(regionPos);
      SurveyorExploration personalExploration = SurveyorExploration.of(player);
      BitSet personalSet = personalExploration.limit(this.summary.dimension(), regionPos, (BitSet)set.clone());
      if (!personalSet.isEmpty()) {
         S2CUpdateRegionPacket.of(this.summary.dimension(), false, regionPos, region, personalSet).send(player);
      }

      set.andNot(personalSet);
      if (!set.isEmpty()) {
         S2CUpdateRegionPacket.of(this.summary.dimension(), true, regionPos, region, set).send(player);
      }
   }

   public void serverTick(ServerLevel world) {
      if (world.getServer().getTickCount() % Surveyor.CONFIG.networking.terrainTicks == 0) {
         this.queuedUpdates.keySet().stream().findFirst().ifPresent(regionPos -> {
            RegionSummary region = this.getRegion(regionPos);
            this.queuedUpdates.get(regionPos).forEach((uuid, set) -> {
               ServerPlayer player = world.getServer().getPlayerList().getPlayer(uuid);
               if (player != null) {
                  this.sendUpdateForRegion(regionPos, player, set);
               }
            });
            this.queuedUpdates.remove(regionPos);
            if (region.isLoaded() && region.isUnloaded(world)) {
               region.save(true);
            }
         });
      }
   }

   public void queueUpdate(RegionPos regionPos, BitSet set, ServerPlayer player) {
      if (this.getRegion(regionPos).isLoaded()) {
         this.sendUpdateForRegion(regionPos, player, set);
      } else {
         this.queuedUpdates.computeIfAbsent(regionPos, k -> new LinkedHashMap<>()).put(player.getUUID(), set);
      }
   }

   public int save(@Nullable Level world) {
      List<RegionPos> savedRegions = new ArrayList<>();
      this.regions.forEach((pos, summary) -> {
         if (summary.isLoaded()) {
            if (summary.isDirty()) {
               savedRegions.add(pos);
            }

            summary.save(world == null || summary.isUnloaded(world));
         }
      });
      return savedRegions.size();
   }

   public boolean isDirty() {
      return this.regions.values().stream().anyMatch(RegionSummary::isDirty);
   }
}
