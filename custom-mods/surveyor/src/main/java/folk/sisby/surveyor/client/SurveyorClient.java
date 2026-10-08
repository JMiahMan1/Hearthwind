package folk.sisby.surveyor.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.mojang.authlib.GameProfile;
import folk.sisby.surveyor.PlayerSummary;
import folk.sisby.surveyor.ServerSummary;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.SurveyorEvents;
import folk.sisby.surveyor.SurveyorExploration;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.config.NetworkMode;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.packet.C2SKnownLandmarksPacket;
import folk.sisby.surveyor.packet.C2SKnownStructuresPacket;
import folk.sisby.surveyor.packet.C2SKnownTerrainPacket;
import folk.sisby.surveyor.structure.WorldStructures;
import folk.sisby.surveyor.terrain.WorldTerrain;
import folk.sisby.surveyor.util.RegionPos;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.Load;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.Unload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndLevelTick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

public class SurveyorClient implements ClientModInitializer {
   public static final String SERVERS_FILE_NAME = "servers.txt";
   private static final Multimap<ResourceKey<Level>, LevelChunk> LOADING_CHUNKS = HashMultimap.create();

   public static void clearLoadingChunks() {
      LOADING_CHUNKS.clear();
   }

   public static File getSavePath(long biomeSeed) {
      String saveFolder = String.valueOf(biomeSeed);
      Path savePath = FabricLoader.getInstance().getGameDir().resolve("data").resolve("surveyor").resolve(saveFolder);
      savePath.toFile().mkdirs();
      File serversFile = savePath.resolve("servers.txt").toFile();

      try {
         ServerData info = Minecraft.getInstance().getCurrentServer();
         if (info != null && (!serversFile.exists() || !FileUtils.readFileToString(serversFile, StandardCharsets.UTF_8).contains(info.name + "\n" + info.ip))) {
            FileUtils.writeStringToFile(serversFile, info.name + "\n" + info.ip + "\n", StandardCharsets.UTF_8, true);
         }
      } catch (IOException var6) {
         Surveyor.LOGGER.error("[Surveyor] Error writing servers file for save {}.", savePath, var6);
      }

      return savePath.toFile();
   }

   public static File getWorldSavePath(ResourceKey<Level> dimension, long biomeSeed) {
      String dimNamespace = dimension.identifier().getNamespace();
      String dimPath = dimension.identifier().getPath();
      return getSavePath(biomeSeed).toPath().resolve(dimNamespace).resolve(dimPath).toFile();
   }

   @Nullable
   public static File getXaerosSavePath(ResourceKey<Level> dimension) {
      File baseFolder = FabricLoader.getInstance().getGameDir().resolve("xaero").resolve("minimap").toFile();
      if (!baseFolder.exists()) {
         return null;
      } else {
         String id = null;

         try {
            id = Minecraft.getInstance().getCurrentServer() != null
               ? Minecraft.getInstance().getCurrentServer().ip
               : Minecraft.getInstance().getSingleplayerServer().getWorldPath(LevelResource.ROOT).getParent().toFile().getName();
            String sanitized = (Minecraft.getInstance().getCurrentServer() != null ? "Multiplayer_" : "")
               + (id.contains(":") ? id.substring(0, id.indexOf(":")) : id)
                  .replace("_", "%us%")
                  .replace("\\", "%bs%")
                  .replace("/", "%fs%")
                  .replace(":", "§")
                  .replace("[", "%lb%")
                  .replace("]", "%rb%");
            File saveFolder = baseFolder.toPath().resolve(sanitized).toFile();
            if (!saveFolder.exists()) {
               return null;
            } else {
               String sanitizedDim = dimension == Level.OVERWORLD
                  ? "dim%0"
                  : (
                     dimension == Level.NETHER
                        ? "dim%-1"
                        : (dimension == Level.END ? "dim%1" : "dim%" + dimension.toString().replace(":", "$").replace('/', '%'))
                  );
               File dimFolder = saveFolder.toPath().resolve(sanitizedDim).toFile();
               return dimFolder.exists() && !dimFolder.toPath().resolve(".surveyor_migrated").toFile().exists() ? dimFolder : null;
            }
         } catch (Exception var7) {
            Surveyor.LOGGER.error("[Surveyor] Error fetching xaeros data for {} {}", new Object[]{id, dimension, var7});
            return null;
         }
      }
   }

   public static boolean serverSupported() {
      return ClientPlayNetworking.canSend(C2SKnownTerrainPacket.ID);
   }

   public static Map<UUID, PlayerSummary> getFriends() {
      if (Minecraft.getInstance().hasSingleplayerServer()) {
         MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
         return ServerSummary.of(server).getGroupSummaries(getClientUuid());
      } else {
         ClientPacketListener handler = Minecraft.getInstance().getConnection();
         if (handler == null) {
            return new HashMap<>();
         } else {
            ClientSummary clientSummary = ClientSummary.of(handler);
            return clientSummary.players(clientSummary.shared.sharedPlayers());
         }
      }
   }

   public static SurveyorExploration getExploration() {
      if (Minecraft.getInstance().hasSingleplayerServer()) {
         return SurveyorExploration.ofShared(getClientUuid(), Minecraft.getInstance().getSingleplayerServer());
      } else {
         Set<SurveyorExploration> set = new HashSet<>();
         set.add(ClientSummary.of(Minecraft.getInstance().getConnection()).personal);
         set.add(ClientSummary.of(Minecraft.getInstance().getConnection()).shared);
         return PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration.ofMerged(set);
      }
   }

   public static SurveyorExploration getPersonalExploration() {
      return (SurveyorExploration)(Minecraft.getInstance().hasSingleplayerServer()
         ? SurveyorExploration.of(getClientUuid(), Minecraft.getInstance().getSingleplayerServer())
         : ClientSummary.of(Minecraft.getInstance().getConnection()).personal);
   }

   public static SurveyorClient.ClientExploration getSharedExploration() {
      if (Minecraft.getInstance().hasSingleplayerServer()) {
         throw new IllegalStateException("You can't edit shared exploration in singleplayer!");
      } else {
         return ClientSummary.of(Minecraft.getInstance().getConnection()).shared;
      }
   }

   public static UUID getClientUuid() {
      if (Minecraft.getInstance().hasSingleplayerServer()) {
         return ServerSummary.HOST;
      } else {
         GameProfile profile = ((SurveyorNetworkHandler)Minecraft.getInstance().getConnection()).getProfile();
         return profile.id();
      }
   }

   public static ServerLevel stealServerWorld(ResourceKey<Level> dimension) {
      MinecraftServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
      return integratedServer == null ? null : integratedServer.getLevel(dimension);
   }

   public static Map<ResourceKey<Level>, WorldSummary> getSummaries(ClientPacketListener handler) {
      return handler.levels().stream().collect(Collectors.toMap(k -> (ResourceKey<Level>)k, k -> getSummary((ResourceKey<Level>)k, handler)));
   }

   public static WorldSummary getSummary(ResourceKey<Level> dimension, ClientPacketListener handler) {
      if (Minecraft.getInstance().hasSingleplayerServer()) {
         return WorldSummary.of(stealServerWorld(dimension));
      } else {
         ClientSummary summary = ClientSummary.of(handler);
         return summary == null ? null : summary.getWorld(dimension);
      }
   }

   public static boolean canModify(UUID landmarkOwner) {
      return Minecraft.getInstance().hasSingleplayerServer()
         ? Surveyor.canModify(
            landmarkOwner, Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayer(Minecraft.getInstance().player.getGameProfile().id())
         )
         : landmarkOwner.equals(getClientUuid())
            || !landmarkOwner.equals(WorldLandmarks.GLOBAL)
               && (
                  Surveyor.CONFIG.networking.waypoints.atLeast(NetworkMode.SERVER)
                     || Surveyor.CONFIG.networking.waypoints.atLeast(NetworkMode.GROUP) && getSharedExploration().groupPlayers().contains(landmarkOwner)
               );
   }

   @Nullable
   public static WorldSummary tryGetSummary(ResourceKey<Level> dimension) {
      if (Minecraft.getInstance().hasSingleplayerServer()) {
         return WorldSummary.of(stealServerWorld(dimension));
      } else {
         ClientPacketListener handler = Minecraft.getInstance().getConnection();
         return handler == null ? null : getSummary(dimension, handler);
      }
   }

   @Nullable
   public static Level getWorld(ResourceKey<Level> dimension) {
      ClientLevel world = Minecraft.getInstance().level;
      return world != null && world.dimension().equals(dimension) ? world : null;
   }

   public static void handleInitialLoad(ClientPacketListener handler) {
      SurveyorExploration exploration = getExploration();

      for (WorldSummary summary : getSummaries(handler).values()) {
         WorldTerrain terrain = summary.terrain();
         if (terrain != null) {
            SurveyorClientEvents.Invoke.terrainUpdated(summary, terrain.bitSet(exploration));
         }

         WorldStructures structures = summary.structures();
         if (structures != null) {
            SurveyorClientEvents.Invoke.structuresAdded(summary, structures.keySet(exploration));
         }

         WorldLandmarks landmarks = summary.landmarks();
         if (landmarks != null) {
            landmarks.tryMigrateXaeros(false);
            SurveyorClientEvents.Invoke.landmarksAdded(summary, landmarks.keySet(exploration));
         }
      }
   }

   public static void sendKnownData(ClientPacketListener handler) {
      Table<ResourceKey<Level>, RegionPos, BitSet> chunks = HashBasedTable.create();
      Map<ResourceKey<Level>, Multimap<ResourceKey<Structure>, ChunkPos>> starts = new HashMap<>();
      Map<ResourceKey<Level>, Multimap<UUID, Identifier>> landmarkKeys = new HashMap<>();
      boolean hasTerrain = false;
      boolean hasStructures = false;
      boolean hasLandmarks = false;
      RegistryAccess manager = null;

      for (WorldSummary summary : getSummaries(handler).values()) {
         manager = summary.manager();
         WorldTerrain terrain = summary.terrain();
         if (terrain != null && Surveyor.CONFIG.networking.terrain.atLeast(NetworkMode.SOLO)) {
            chunks.row(summary.dimension()).putAll(terrain.bitSet(null));
            hasTerrain = true;
         }

         WorldStructures structures = summary.structures();
         if (structures != null && Surveyor.CONFIG.networking.structures.atLeast(NetworkMode.SOLO)) {
            starts.put(summary.dimension(), structures.keySet(null));
            hasStructures = true;
         }

         WorldLandmarks landmarks = summary.landmarks();
         if (landmarks != null && Surveyor.CONFIG.networking.landmarks.atLeast(NetworkMode.SOLO)) {
            landmarkKeys.put(summary.dimension(), landmarks.keySet(null));
            hasLandmarks = true;
         }
      }

      if (hasTerrain) {
         new C2SKnownTerrainPacket(chunks).send(manager);
      }

      if (hasStructures) {
         new C2SKnownStructuresPacket(starts).send(manager);
      }

      if (hasLandmarks) {
         new C2SKnownLandmarksPacket(landmarkKeys).send(manager);
      }
   }

   public void onInitializeClient() {
      SurveyorClientNetworking.init();
      ClientCommandRegistrationCallback.EVENT.register(SurveyorClientCommands::registerCommands);
      ClientChunkEvents.CHUNK_LOAD.register((Load)(world, chunk) -> {
         if (!Minecraft.getInstance().isLocalServer()) {
            LOADING_CHUNKS.put(world.dimension(), chunk);
         }
      });
      ClientChunkEvents.CHUNK_UNLOAD.register((Unload)(world, chunk) -> {
         if (!Minecraft.getInstance().isLocalServer()) {
            WorldTerrain.onChunkUnload(world, chunk);
         }
      });
      ClientTickEvents.END_LEVEL_TICK.register((EndLevelTick)world -> {
         if (Minecraft.getInstance().levelRenderer.visibleSections().size() > 10 && Minecraft.getInstance().levelRenderer.hasRenderedAllSections()) {
            for (LevelChunk chunk : new HashSet<LevelChunk>(LOADING_CHUNKS.get(world.dimension()))) {
               WorldTerrain.onChunkLoad(world, chunk, false);
               ClientSummary.of(Minecraft.getInstance().getConnection()).personal.addChunk(world.dimension(), chunk.getPos(), false);
               LOADING_CHUNKS.remove(world.dimension(), chunk);
            }
         }
      });
      SurveyorEvents.Register.landmarksAdded(Surveyor.id("client"), (summary, landmarks) -> {
         SurveyorExploration exploration = getExploration();
         if (exploration != null) {
            SurveyorClientEvents.Invoke.landmarksAdded(summary, exploration.limit(summary.dimension(), summary.landmarks(), HashMultimap.create(landmarks)));
         }
      });
      SurveyorEvents.Register.landmarksRemoved(Surveyor.id("client"), SurveyorClientEvents.Invoke::landmarksRemoved);
      Surveyor.LOGGER.info("[Surveyor Client] is not a map mod either");
   }

   public record ClientExploration(
      Set<UUID> groupPlayers, Table<ResourceKey<Level>, RegionPos, BitSet> chunks, Table<ResourceKey<Level>, ResourceKey<Structure>, LongSet> starts
   ) implements SurveyorExploration {
      @Override
      public Set<UUID> sharedPlayers() {
         Set<UUID> sharedPlayers = new HashSet<>();
         sharedPlayers.add(SurveyorClient.getClientUuid());
         sharedPlayers.addAll(this.groupPlayers);
         return sharedPlayers;
      }

      @Override
      public boolean personal() {
         return true;
      }

      @Override
      public void addStructure(ResourceKey<Level> dimension, ResourceKey<Structure> structureKey, ChunkPos pos) {
         SurveyorExploration.super.addStructure(dimension, structureKey, pos);
         WorldSummary summary = SurveyorClient.tryGetSummary(dimension);
         if (summary != null) {
            this.updateClientForAddStructure(summary, structureKey, pos);
         }
      }

      @Override
      public void mergeRegion(ResourceKey<Level> dimension, RegionPos regionPos, BitSet chunks, boolean updateClient) {
         SurveyorExploration.super.mergeRegion(dimension, regionPos, chunks, updateClient);
         WorldSummary summary = SurveyorClient.tryGetSummary(dimension);
         if (summary != null) {
            this.updateClientForMergeRegion(summary, regionPos, chunks);
         }
      }

      @Override
      public void addChunk(ResourceKey<Level> dimension, ChunkPos pos, boolean updateClient) {
         SurveyorExploration.super.addChunk(dimension, pos, updateClient);
         WorldSummary summary = SurveyorClient.tryGetSummary(dimension);
         if (summary != null) {
            this.updateClientForAddChunk(summary, pos);
         }
      }
   }
}
