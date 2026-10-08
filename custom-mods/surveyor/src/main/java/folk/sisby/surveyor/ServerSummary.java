package folk.sisby.surveyor;

import folk.sisby.surveyor.config.NetworkMode;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.packet.S2CGroupAmendedPacket;
import folk.sisby.surveyor.packet.S2CGroupChangedPacket;
import folk.sisby.surveyor.packet.S2CGroupUpdatedPacket;
import folk.sisby.surveyor.structure.WorldStructures;
import folk.sisby.surveyor.terrain.WorldTerrain;
import folk.sisby.surveyor.util.MapUtil;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public final class ServerSummary {
   public static final String KEY_GROUPS = "groups";
   public static final UUID HOST = UUID.fromString("00000000-0000-0000-0000-000000000000");
   private final MinecraftServer server;
   private final Map<UUID, PlayerSummary> offlineSummaries;
   private final Map<UUID, Set<UUID>> shareGroups;
   private final Map<ResourceKey<Level>, WorldSummary> worlds;
   private boolean dirty = false;

   public ServerSummary(MinecraftServer server, Map<UUID, PlayerSummary> offlineSummaries, @Nullable Map<UUID, Set<UUID>> shareGroups) {
      this.server = server;
      this.offlineSummaries = offlineSummaries;
      this.shareGroups = shareGroups;
      this.worlds = new HashMap<>();
   }

   public static ServerSummary of(MinecraftServer server) {
      return ((SurveyorServer)server).surveyor$getSummary();
   }

   public static Map<UUID, Set<UUID>> loadShareGroups(MinecraftServer server) {
      File folder = Surveyor.getSavePath(Level.OVERWORLD, server);
      CompoundTag sharingNbt = new CompoundTag();
      File sharingFile = new File(folder, "sharing.dat");
      if (sharingFile.exists()) {
         try {
            sharingNbt = NbtIo.readCompressed(sharingFile.toPath(), NbtAccounter.unlimitedHeap());
         } catch (ReportedNbtException | IOException var5) {
            Surveyor.LOGGER.error("[Surveyor] Error loading sharing file.", var5);
         }
      }

      Map<UUID, Set<UUID>> shareGroups = new ConcurrentHashMap<>();
      sharingNbt.getList("groups")
         .orElse(new ListTag())
         .stream()
         .map(l -> ((ListTag)l).stream().map(s -> UUID.fromString((String)s.asString().orElseThrow())).collect(Collectors.toCollection(HashSet::new)))
         .forEach(set -> {
            for (UUID uuid : set) {
               shareGroups.put(uuid, set);
            }
         });
      return shareGroups;
   }

   public static ServerSummary load(MinecraftServer server) {
      Map<UUID, Set<UUID>> shareGroups = Surveyor.CONFIG.networking.globalSharing ? null : loadShareGroups(server);
      File playerFolder = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile();
      Map<UUID, PlayerSummary> offlineSummaries = new ConcurrentHashMap<>();
      UUID hostProfile = server.getWorldData().getSinglePlayerUUID();

      for (File file : Optional.ofNullable(playerFolder.listFiles((dir, name) -> name.endsWith(".dat"))).orElse(new File[0])) {
         UUID uuid;
         try {
            uuid = UUID.fromString(file.getName().substring(0, file.getName().length() - ".dat".length()));
            if (uuid.equals(hostProfile)) {
               uuid = HOST;
            }
         } catch (IllegalArgumentException var12) {
            continue;
         }

         if (shareGroups == null || shareGroups.containsKey(uuid)) {
            try {
               CompoundTag playerNbt = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
               offlineSummaries.put(uuid, new PlayerSummary.OfflinePlayerSummary(uuid, playerNbt, false));
            } catch (ReportedNbtException | IOException var11) {
               Surveyor.LOGGER.error("[Surveyor] Error loading offline player data for {}!", uuid, var11);
            }
         }
      }

      if (shareGroups != null) {
         for (UUID uuidx : shareGroups.keySet()) {
            if (!offlineSummaries.containsKey(uuidx)) {
               Surveyor.LOGGER.warn("[Surveyor] Player data was missing for shared player {}! Removing from groups...", uuidx);
               shareGroups.get(uuidx).remove(uuidx);
               shareGroups.remove(uuidx);
            }
         }
      }

      return new ServerSummary(server, offlineSummaries, shareGroups);
   }

   public static void onPlayerJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
      ServerPlayer player = handler.getPlayer();
      ServerSummary summary = of(server);
      if (summary != null) {
         UUID uuid = Surveyor.getUuid(player);
         boolean known = summary.offlineSummaries.containsKey(uuid);
         if (!known) {
            summary.createPlayer(player);
         }

         if (summary.getGroup(uuid).size() > 1) {
            Map<UUID, PlayerSummary> groupSummaries = summary.getGroupSummaries(uuid);
            new S2CGroupChangedPacket(
                  groupSummaries,
                  summary.getSharingExploration(uuid, Surveyor.CONFIG.networking.terrain, false).chunks(),
                  summary.getSharingExploration(uuid, Surveyor.CONFIG.networking.structures, false).starts()
               )
               .send(player);
            new S2CGroupUpdatedPacket(groupSummaries).send(player);
         }

         if (!known && Surveyor.CONFIG.networking.globalSharing) {
            new S2CGroupAmendedPacket(uuid).send(uuid, server, NetworkMode.GROUP, false);
         }
      }
   }

   public static void onTick(MinecraftServer server) {
      if (server.getTickCount() % Surveyor.CONFIG.networking.positionTicks == 0) {
         ServerSummary summary = of(server);
         if (summary != null) {
            for (Set<UUID> group : summary.getPositionGroups()) {
               Map<UUID, PlayerSummary> onlinePlayers = new HashMap<>();

               for (UUID uuid : group) {
                  ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                  if (player != null) {
                     onlinePlayers.put(uuid, PlayerSummary.of(player));
                  }
               }

               if (onlinePlayers.size() > 1) {
                  new S2CGroupUpdatedPacket(onlinePlayers)
                     .send(null, server, p -> onlinePlayers.containsKey(Surveyor.getUuid(p)), Surveyor.CONFIG.networking.positions, true);
               }
            }
         }
      }
   }

   public WorldSummary getWorld(ResourceKey<Level> dimension) {
      return this.worlds
         .computeIfAbsent(
            dimension,
            dim -> new WorldSummary(
               this.server, (ResourceKey<Level>)dim, this.server.registryAccess(), Surveyor.getSavePath((ResourceKey<Level>)dim, this.server)
            )
         );
   }

   public void loadWorlds() {
      for (ResourceKey<Level> dimension : this.server.levelKeys()) {
         WorldSummary summary = this.getWorld(dimension);
         WorldTerrain terrain = summary.terrain();
         if (terrain != null) {
            SurveyorEvents.Invoke.terrainUpdated(summary, terrain.bitSet(null));
         }

         WorldStructures structures = summary.structures();
         if (structures != null) {
            SurveyorEvents.Invoke.structuresAdded(summary, structures.keySet(null));
         }

         WorldLandmarks landmarks = summary.landmarks();
         if (landmarks != null) {
            SurveyorEvents.Invoke.landmarksAdded(summary, landmarks.keySet(null));
         }
      }
   }

   public void save(boolean force, boolean suppressLogs) {
      if (this.isDirty()
         || !StreamSupport.<ServerLevel>stream(this.server.getAllLevels().spliterator(), false)
            .map(WorldSummary::of)
            .filter(Objects::nonNull)
            .noneMatch(WorldSummary::isDirty)) {
         if (!suppressLogs) {
            Surveyor.LOGGER.info("[Surveyor] Saving server data...");
         }

         for (ServerLevel world : this.server.getAllLevels()) {
            if (!world.noSave || force) {
               WorldSummary summary = WorldSummary.of(world);
               if (summary != null) {
                  summary.save(world, Surveyor.getSavePath(world.dimension(), this.server), suppressLogs);
               }
            }
         }

         File folder = Surveyor.getSavePath(Level.OVERWORLD, this.server);
         if (this.isDirty()) {
            File sharingFile = new File(folder, "sharing.dat");

            try {
               NbtIo.writeCompressed(this.writeNbt(new CompoundTag()), sharingFile.toPath());
               this.dirty = false;
            } catch (IOException var6) {
               Surveyor.LOGGER.error("[Surveyor] Error writing sharing file.", var6);
            }
         }

         if (!suppressLogs) {
            Surveyor.LOGGER.info("[Surveyor] Finished saving server data.");
         }
      }
   }

   private CompoundTag writeNbt(CompoundTag nbt) {
      if (this.shareGroups != null) {
         nbt.put(
            "groups",
            new ListTag(
               this.shareGroups
                  .values()
                  .stream()
                  .filter(s -> s.size() > 1)
                  .<Tag>map(s -> new ListTag(s.stream().<Tag>map(u -> StringTag.valueOf(u.toString())).toList()))
                  .toList()
            )
         );
      }

      return nbt;
   }

   public PlayerSummary getPlayer(UUID uuid) {
      ServerPlayer player = Surveyor.getPlayer(this.server, uuid);
      return player != null ? PlayerSummary.of(player) : this.offlineSummaries.get(uuid);
   }

   public SurveyorExploration getExploration(UUID player) {
      PlayerSummary summary = this.getPlayer(player);
      return summary == null ? null : summary.exploration();
   }

   public void createPlayer(ServerPlayer player) {
      this.offlineSummaries.put(Surveyor.getUuid(player), new PlayerSummary.OfflinePlayerSummary(player));
   }

   public void updatePlayer(UUID uuid, ValueOutput view, boolean online) {
      PlayerSummary newSummary = this.offlineSummaries.get(uuid);
      if (newSummary != null) {
         this.offlineSummaries.put(uuid, newSummary);
         S2CGroupUpdatedPacket.of(uuid, newSummary)
            .send(
               null,
               this.server,
               this.getSharingPlayers(uuid, Surveyor.CONFIG.networking.positions, false)::contains,
               Surveyor.CONFIG.networking.positions,
               false
            );
      }
   }

   public Set<Set<UUID>> getPositionGroups() {
      return Surveyor.CONFIG.networking.positions.atLeast(NetworkMode.SERVER)
         ? Set.of(this.offlineSummaries.keySet())
         : (Surveyor.CONFIG.networking.positions.atMost(NetworkMode.SOLO) ? Set.of() : this.getGroups());
   }

   public Set<Set<UUID>> getGroups() {
      return this.shareGroups == null ? new HashSet<>(Set.of(new HashSet<>(this.offlineSummaries.keySet()))) : new HashSet<>(this.shareGroups.values());
   }

   public Set<UUID> getGroup(UUID player) {
      return (Set<UUID>)(this.shareGroups == null
         ? new HashSet<>(this.offlineSummaries.keySet())
         : this.shareGroups.computeIfAbsent(player, p -> new HashSet<>(Set.of((UUID)p))));
   }

   public Map<UUID, PlayerSummary> getAllSummaries() {
      Map<UUID, PlayerSummary> map = new HashMap<>();

      for (UUID u : this.offlineSummaries.keySet()) {
         if (this.getPlayer(u) != null) {
            map.put(u, this.getPlayer(u));
         }
      }

      return map;
   }

   public Map<UUID, PlayerSummary> getGroupSummaries(UUID player) {
      Map<UUID, PlayerSummary> map = new HashMap<>();

      for (UUID u : this.getGroup(player)) {
         if (this.getPlayer(u) != null) {
            map.put(u, this.getPlayer(u));
         }
      }

      return map;
   }

   public void joinGroup(UUID player1, UUID player2) {
      if (this.shareGroups != null) {
         if (this.getGroup(player1).size() > 1 && this.getGroup(player2).size() > 1) {
            throw new IllegalStateException("Can't merge two groups!");
         } else {
            if (this.getGroup(player1).size() > 1) {
               this.getGroup(player1).add(player2);
               this.shareGroups.put(player2, this.getGroup(player1));
            } else {
               this.getGroup(player2).add(player1);
               this.shareGroups.put(player1, this.getGroup(player2));
            }

            for (ServerPlayer friend : this.getSharingPlayers(player1, NetworkMode.GROUP, true)) {
               UUID uuid = Surveyor.getUuid(friend);
               new S2CGroupChangedPacket(
                     this.getGroupSummaries(uuid),
                     this.getSharingExploration(uuid, Surveyor.CONFIG.networking.terrain, false).chunks(),
                     this.getSharingExploration(uuid, Surveyor.CONFIG.networking.structures, false).starts()
                  )
                  .send(friend);
            }

            this.dirty();
         }
      }
   }

   public void leaveGroup(UUID player) {
      if (this.shareGroups != null) {
         Set<ServerPlayer> groupPlayers = this.getSharingPlayers(player, NetworkMode.GROUP, true);
         this.getGroup(player).remove(player);
         this.shareGroups.put(player, new HashSet<>());
         this.getGroup(player).add(player);

         for (ServerPlayer friend : groupPlayers) {
            UUID uuid = Surveyor.getUuid(friend);
            new S2CGroupChangedPacket(
                  this.getGroupSummaries(uuid),
                  this.getSharingExploration(uuid, Surveyor.CONFIG.networking.terrain, false).chunks(),
                  this.getSharingExploration(uuid, Surveyor.CONFIG.networking.structures, false).starts()
               )
               .send(friend);
         }

         this.dirty();
      }
   }

   public int groupSize(UUID player) {
      return this.getGroup(player).size();
   }

   public Set<PlayerSummary> groupPlayers(UUID player) {
      return this.getGroup(player).stream().map(this::getPlayer).collect(Collectors.toSet());
   }

   public Set<UUID> getSharing(UUID player, NetworkMode mode, boolean withSelf) {
      return mode.atMost(NetworkMode.SOLO) && !withSelf
         ? Set.of()
         : (mode.atMost(NetworkMode.SOLO) ? Set.of(player) : (mode.atMost(NetworkMode.GROUP) ? this.getGroup(player) : this.offlineSummaries.keySet()));
   }

   public SurveyorExploration getSharingExploration(UUID player, NetworkMode mode, boolean withSelf) {
      Set<UUID> sharing = this.getSharing(player, mode, withSelf);
      return mode.atLeast(NetworkMode.SERVER)
         ? new PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration(
            sharing,
            MapUtil.asTable(
               this.worlds
                  .entrySet()
                  .stream()
                  .collect(Collectors.toMap(Entry::getKey, e -> Optional.ofNullable(e.getValue().terrain()).map(t -> t.bitSet(null)).orElse(Map.of())))
            ),
            MapUtil.asTable(
               this.worlds
                  .entrySet()
                  .stream()
                  .collect(
                     Collectors.toMap(
                        Entry::getKey,
                        e -> Optional.ofNullable(e.getValue().structures())
                           .map(
                              t -> MapUtil.asListMap(t.keySet(null))
                                 .entrySet()
                                 .stream()
                                 .collect(Collectors.toMap(Entry::getKey, e2 -> LongSet.of(((List<ChunkPos>)e2.getValue()).stream().mapToLong(ChunkPos::pack).toArray())))
                           )
                           .orElse(Map.of())
                     )
                  )
            ),
            false
         )
         : PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration.ofMerged(
            sharing.stream().map(this::getExploration).filter(Objects::nonNull).collect(Collectors.toSet())
         );
   }

   public Set<ServerPlayer> getSharingPlayers(UUID player, NetworkMode mode, boolean withSelf) {
      Set<UUID> sharing = this.getSharing(player, mode, withSelf);
      return this.server.getPlayerList().getPlayers().stream().filter(p -> sharing.contains(Surveyor.getUuid(p))).collect(Collectors.toSet());
   }

   public boolean isDirty() {
      return this.dirty && this.shareGroups != null;
   }

   private void dirty() {
      this.dirty = true;
   }

   public MinecraftServer getServer() {
      return this.server;
   }
}
