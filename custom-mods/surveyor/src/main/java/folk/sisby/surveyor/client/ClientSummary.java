package folk.sisby.surveyor.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import folk.sisby.surveyor.PlayerSummary;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.WorldSummary;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ReportedException;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientSummary {
   public static final String KEY_SHARED = "shared";
   private final long biomeSeed;
   private final ClientPacketListener handler;
   private final Map<UUID, PlayerSummary> players;
   private final Map<ResourceKey<Level>, WorldSummary> worlds;
   public final SurveyorClient.ClientExploration personal;
   public final SurveyorClient.ClientExploration shared;
   public final File saveFile;

   public ClientSummary(long biomeSeed, ClientPacketListener handler) {
      this.biomeSeed = biomeSeed;
      this.handler = handler;
      this.players = new HashMap<>();
      this.worlds = new ConcurrentHashMap<>();
      this.personal = new SurveyorClient.ClientExploration(new HashSet<>(), HashBasedTable.create(), HashBasedTable.create());
      this.shared = new SurveyorClient.ClientExploration(new HashSet<>(), HashBasedTable.create(), HashBasedTable.create());
      this.saveFile = SurveyorClient.getSavePath(biomeSeed).toPath().resolve(SurveyorClient.getClientUuid().toString() + ".dat").toFile();
   }

   public static ClientSummary of(ClientPacketListener handler) {
      return ((SurveyorNetworkHandler)handler).surveyor$getSummary();
   }

   public void mergeSummaries(Map<UUID, PlayerSummary> summaries) {
      this.players.putAll(summaries);
   }

   public void matchSummaries(Map<UUID, PlayerSummary> summaries) {
      this.players.clear();
      this.players.putAll(summaries);
   }

   public Map<UUID, PlayerSummary> players(Set<UUID> group) {
      Map<UUID, PlayerSummary> outMap = new HashMap<>();
      UnmodifiableIterator var3 = Sets.union(group, this.players.keySet()).iterator();

      while (var3.hasNext()) {
         UUID uuid = (UUID)var3.next();
         Player player = this.handler.getLevel().getPlayerByUUID(uuid);
         if (player != null) {
            outMap.put(uuid, new PlayerSummary.PlayerEntitySummary(player));
         } else {
            outMap.put(uuid, this.players.get(uuid));
         }
      }

      return outMap;
   }

   public WorldSummary getWorld(ResourceKey<Level> dimension) {
      return this.worlds
         .computeIfAbsent(
            dimension, k -> new WorldSummary(null, dimension, this.handler.registryAccess(), SurveyorClient.getWorldSavePath(dimension, this.biomeSeed))
         );
   }

   public void connect() {
      CompoundTag explorationNbt = new CompoundTag();
      if (this.saveFile.exists()) {
         try {
            explorationNbt = NbtIo.readCompressed(this.saveFile.toPath(), NbtAccounter.unlimitedHeap());
         } catch (ReportedException | IOException var3) {
            Surveyor.LOGGER.error("[Surveyor] Error loading client exploration file.", var3);
         }
      }

      this.personal.read(explorationNbt);
      this.shared.read(explorationNbt.getCompound("shared").orElse(new CompoundTag()));
      SurveyorClient.getSummaries(this.handler);
      SurveyorClient.handleInitialLoad(this.handler);
      SurveyorClient.sendKnownData(this.handler);
   }

   public void disconnect() {
      SurveyorClient.clearLoadingChunks();

      for (WorldSummary summary : this.worlds.values()) {
         summary.save(null, SurveyorClient.getWorldSavePath(summary.dimension(), this.biomeSeed), false);
      }

      try {
         CompoundTag nbt = this.personal.write(new CompoundTag());
         CompoundTag sharedNbt = this.shared.write(new CompoundTag());
         nbt.put("shared", sharedNbt);
         NbtIo.writeCompressed(nbt, this.saveFile.toPath());
      } catch (IOException var3) {
         Surveyor.LOGGER.error("[Surveyor] Error saving client exploration file.", var3);
      }
   }

   public void leaveWorld(ResourceKey<Level> dimension) {
      if (this.worlds.containsKey(dimension)) {
         this.worlds.get(dimension).save(null, SurveyorClient.getWorldSavePath(dimension, this.biomeSeed), false);
      }
   }
}
