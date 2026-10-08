package folk.sisby.surveyor.landmark;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.SurveyorEvents;
import folk.sisby.surveyor.SurveyorExploration;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClient;
import folk.sisby.surveyor.config.NetworkMode;
import folk.sisby.surveyor.config.SystemMode;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import folk.sisby.surveyor.packet.SyncLandmarksAddedPacket;
import folk.sisby.surveyor.packet.SyncLandmarksRemovedPacket;
import folk.sisby.surveyor.util.DispatchMapCodec;
import folk.sisby.surveyor.util.MapUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class WorldLandmarks {
   public static final UUID GLOBAL = UUID.fromString("99999999-9999-9999-9999-999999999999");
   public static final String KEY_LANDMARKS = "landmarks";
   public static final String KEY_REMOVED = "removed";
   public static final Codec<Table<UUID, Identifier, Landmark>> CODEC = DispatchMapCodec.of(
         UUIDUtil.STRING_CODEC, uuid -> DispatchMapCodec.of(Identifier.CODEC, id -> Landmark.createCodec(uuid, id))
      )
      .xmap(MapUtil::asTable, Table::rowMap);
   public static final Codec<Multimap<UUID, Identifier>> REMOVED_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.list(Identifier.CODEC))
      .xmap(MapUtil::asMultiMap, MapUtil::asListMap);
   public static final int[] XAERO_COLORS = new int[]{
      -16777216, -16777046, -16733696, -16733526, -5636096, -5635926, -22016, -5592406, -11184811, -11184641, -11141291, -11141121, -65536, -43521, -171, -1
   };
   protected final WorldSummary summary;
   protected final Table<UUID, Identifier, Landmark> landmarks = Tables.synchronizedTable(HashBasedTable.create());
   @Nullable
   protected final Multimap<UUID, Identifier> removed;
   protected boolean dirty;

   public static WorldLandmarks of(Level world) {
      return Optional.ofNullable(world).map(WorldSummary::of).map(WorldSummary::landmarks).orElse(null);
   }

   public WorldLandmarks(WorldSummary summary, Table<UUID, Identifier, Landmark> landmarks, Multimap<UUID, Identifier> removed, boolean dirty) {
      this.summary = summary;
      this.landmarks.putAll(landmarks);
      this.removed = removed == null ? null : Multimaps.synchronizedSetMultimap(HashMultimap.create(removed));
      if (this.removed != null) {
         this.landmarks.cellSet().forEach(c -> removed.remove(c.getRowKey(), c.getColumnKey()));
      }

      this.dirty = dirty;
   }

   public static WorldLandmarks load(WorldSummary summary, File folder) {
      CompoundTag landmarkNbt = new CompoundTag();
      File landmarksFile = new File(folder, "landmarks.dat");
      if (landmarksFile.exists()) {
         try {
            landmarkNbt = NbtIo.readCompressed(landmarksFile.toPath(), NbtAccounter.unlimitedHeap());
         } catch (ReportedNbtException | IOException var5) {
            Surveyor.LOGGER.error("[Surveyor] Error loading landmarks file for {}.", summary.dimension().identifier(), var5);
         }
      }

      WorldLandmarks worldLandmarks = fromNbt(summary, landmarkNbt, landmarksFile);
      if (!summary.isClient()) {
         worldLandmarks.tryMigrateXaeros(false);
      }

      return worldLandmarks;
   }

   public CompoundTag writeNbt(CompoundTag nbt) {
      nbt.put("landmarks", (Tag)CODEC.encodeStart(NbtOps.INSTANCE, this.landmarks).resultOrPartial(Surveyor.LOGGER::error).orElseThrow());
      if (this.removed != null) {
         nbt.put("removed", (Tag)REMOVED_CODEC.encodeStart(NbtOps.INSTANCE, this.removed).resultOrPartial(Surveyor.LOGGER::error).orElseThrow());
      }

      return nbt;
   }

   public static WorldLandmarks fromNbt(WorldSummary summary, CompoundTag nbt, File landmarksFile) {
      CompoundTag landmarks = nbt.getCompound("landmarks").orElse(new CompoundTag());
      boolean dirty = false;
      Table<UUID, Identifier, Landmark> outMap = HashBasedTable.create();
      Multimap<UUID, Identifier> removedMap = summary.server() == null ? null : HashMultimap.create();
      if (landmarks.keySet().stream().anyMatch(k -> k.contains(":"))) {
         Surveyor.LOGGER.warn("[Surveyor] Partially recovering landmarks from 0.X");

         try {
            Files.copy(landmarksFile.toPath(), landmarksFile.toPath().resolveSibling("landmarks.dat_v0"), StandardCopyOption.REPLACE_EXISTING);
         } catch (IOException var18) {
            throw new RuntimeException("Surveyor failed to back up v0 landmarks, was the file locked?", var18);
         }

         try {
            for (String key : landmarks.keySet()) {
               CompoundTag type = landmarks.getCompound(key).orElse(new CompoundTag());
               Identifier typeId = Identifier.tryParse(key);

               for (String coords : type.keySet()) {
                  CompoundTag landmark = type.getCompound(coords).orElse(new CompoundTag());
                  UUID owner = landmark.contains("owner")
                     ? (UUID)((Pair)UUIDUtil.AUTHLIB_CODEC.decode(NbtOps.INSTANCE, landmark.get("owner")).resultOrPartial(Surveyor.LOGGER::error).orElseThrow())
                        .getFirst()
                     : GLOBAL;
                  BlockPos pos = new BlockPos(
                     Integer.parseInt(coords.split(",")[0]), Integer.parseInt(coords.split(",")[1]), Integer.parseInt(coords.split(",")[2])
                  );
                  DyeColor dye = !landmark.contains("color")
                     ? null
                     : DyeColor.CODEC
                        .decode(NbtOps.INSTANCE, landmark.get("color"))
                        .resultOrPartial(Surveyor.LOGGER::error)
                        .<DyeColor>map(Pair::getFirst)
                        .orElse(null);
                  Identifier id = landmark.getString("texture")
                     .<Identifier>map(Identifier::tryParse)
                     .orElse(typeId)
                     .withSuffix((dye == null ? "" : "/" + dye.getName()) + "/" + pos.getX() + (pos.getY() == 0 ? "" : "/" + pos.getY()) + "/" + pos.getZ());
                  outMap.put(
                     owner,
                     id,
                     Landmark.create(
                        owner,
                        id,
                        b -> b.add(LandmarkComponentTypes.POS, pos)
                           .add(
                              LandmarkComponentTypes.BOX,
                              !landmark.contains("box")
                                 ? null
                                 : BoundingBox.CODEC
                                    .decode(NbtOps.INSTANCE, landmark.get("box"))
                                    .resultOrPartial(Surveyor.LOGGER::error)
                                    .<BoundingBox>map(Pair::getFirst)
                                    .orElse(null)
                           )
                           .add(LandmarkComponentTypes.COLOR, dye == null ? null : dye.getFireworkColor())
                           .add(
                              LandmarkComponentTypes.NAME,
                              landmark.contains("name")
                                 ? null
                                 : ComponentSerialization.CODEC
                                    .decode(NbtOps.INSTANCE, landmark.get("name"))
                                    .resultOrPartial(Surveyor.LOGGER::error)
                                    .<Component>map(Pair::getFirst)
                                    .orElse(null)
                           )
                           .add(LandmarkComponentTypes.SEED, (Integer)landmark.getInt("seed").orElse(null))
                           .add(LandmarkComponentTypes.TIME, (Long)landmark.getLong("created").orElse(null))
                     )
                  );
                  dirty = true;
               }
            }

            Surveyor.LOGGER.info("[Surveyor] Recovered {} landmarks from legacy data.", outMap.size());
         } catch (Exception var19) {
            Surveyor.LOGGER.error("[Surveyor] Encountered an error during v0 landmark migration, skipping...", var19);
         }
      } else {
         if (!landmarks.isEmpty()) {
            outMap.putAll((Table)((Pair)CODEC.decode(NbtOps.INSTANCE, landmarks).resultOrPartial(Surveyor.LOGGER::error).orElseThrow()).getFirst());
         }

         if (!summary.isClient()) {
            CompoundTag removed = nbt.getCompound("removed").orElse(new CompoundTag());
            if (!removed.isEmpty()) {
               removedMap = (Multimap<UUID, Identifier>)((Pair)REMOVED_CODEC.decode(NbtOps.INSTANCE, removed)
                     .resultOrPartial(Surveyor.LOGGER::error)
                     .orElseThrow())
                  .getFirst();
            }
         }
      }

      return new WorldLandmarks(summary, outMap, removedMap, dirty);
   }

   public boolean contains(UUID uuid, Identifier id) {
      return this.landmarks.contains(uuid, id);
   }

   @Nullable
   public Landmark get(UUID uuid, Identifier id) {
      return this.contains(uuid, id) ? (Landmark)this.landmarks.get(uuid, id) : null;
   }

   public Map<Identifier, Landmark> asMap(UUID uuid, SurveyorExploration exploration) {
      Map<Identifier, Landmark> outMap = new HashMap<>(this.landmarks.row(uuid));
      if (exploration != null) {
         outMap.values().removeIf(l -> !exploration.exploredLandmark(this.summary.dimension(), l));
      }

      return outMap;
   }

   public Table<UUID, Identifier, Landmark> asMap(SurveyorExploration exploration) {
      Table<UUID, Identifier, Landmark> outMap = HashBasedTable.create(this.landmarks);
      if (exploration != null) {
         outMap.values().removeIf(l -> !exploration.exploredLandmark(this.summary.dimension(), l));
      }

      return outMap;
   }

   public Multimap<UUID, Identifier> keySet(SurveyorExploration exploration) {
      Multimap<UUID, Identifier> outMap = MapUtil.keyMultiMap(this.landmarks);
      if (exploration != null) {
         outMap.entries().removeIf(e -> !exploration.exploredLandmark(this.summary.dimension(), (Landmark)this.landmarks.get(e.getKey(), e.getValue())));
      }

      return outMap;
   }

   public Multimap<UUID, Identifier> removed() {
      return HashMultimap.create(Objects.requireNonNullElse(this.removed, HashMultimap.create()));
   }

   public void handleChanged(Table<UUID, Identifier, Landmark> changed, boolean local, @Nullable UUID sender) {
      if (!changed.isEmpty()) {
         Table<UUID, Identifier, Landmark> landmarksRemoved = HashBasedTable.create(changed);
         landmarksRemoved.cellSet().removeAll(this.landmarks.cellSet());
         Table<UUID, Identifier, Landmark> landmarksAdded = HashBasedTable.create(changed);
         landmarksAdded.cellSet().removeAll(landmarksRemoved.cellSet());
         if (!landmarksRemoved.isEmpty()) {
            SurveyorEvents.Invoke.landmarksRemoved(this.summary, MapUtil.keyMultiMap(landmarksRemoved));
         }

         if (!landmarksAdded.isEmpty()) {
            SurveyorEvents.Invoke.landmarksAdded(this.summary, MapUtil.keyMultiMap(landmarksAdded));
         }

         if (!local) {
            Table<UUID, Identifier, Landmark> globalRemoved = MapUtil.asTable(Map.of(GLOBAL, landmarksRemoved.row(GLOBAL)));
            Table<UUID, Identifier, Landmark> globalAdded = MapUtil.asTable(Map.of(GLOBAL, landmarksAdded.row(GLOBAL)));
            landmarksRemoved.rowMap().remove(GLOBAL);
            landmarksAdded.rowMap().remove(GLOBAL);
            if (!globalRemoved.isEmpty()) {
               new SyncLandmarksRemovedPacket(this.summary.dimension(), MapUtil.keyMultiMap(globalRemoved))
                  .send(sender, this.summary, Surveyor.CONFIG.networking.landmarks, false);
            }

            if (!globalAdded.isEmpty()) {
               new SyncLandmarksAddedPacket(this.summary.dimension(), globalAdded).send(sender, this.summary, Surveyor.CONFIG.networking.landmarks, false);
            }

            if (!landmarksRemoved.isEmpty()) {
               new SyncLandmarksRemovedPacket(this.summary.dimension(), MapUtil.keyMultiMap(landmarksRemoved))
                  .send(sender, this.summary, Surveyor.CONFIG.networking.waypoints, false);
            }

            if (!landmarksAdded.isEmpty()) {
               new SyncLandmarksAddedPacket(this.summary.dimension(), landmarksAdded).send(sender, this.summary, Surveyor.CONFIG.networking.waypoints, false);
            }
         }
      }
   }

   public Table<UUID, Identifier, Landmark> putForBatch(Table<UUID, Identifier, Landmark> changed, Landmark landmark) {
      if (Surveyor.CONFIG.landmarks == SystemMode.FROZEN) {
         return changed;
      } else {
         this.landmarks.put(landmark.owner(), landmark.id(), landmark);
         if (this.removed != null) {
            this.removed.remove(landmark.owner(), landmark.id());
         }

         this.dirty();
         changed.put(landmark.owner(), landmark.id(), landmark);
         return changed;
      }
   }

   public Table<UUID, Identifier, Landmark> putForBatch(Landmark landmark) {
      return this.putForBatch(HashBasedTable.create(), landmark);
   }

   public void putLocal(Landmark landmark) {
      this.handleChanged(this.putForBatch(landmark), true, null);
   }

   public void put(Landmark landmark) {
      this.handleChanged(this.putForBatch(landmark), false, null);
   }

   public void put(UUID sender, Landmark landmark) {
      this.handleChanged(this.putForBatch(landmark), false, sender);
   }

   public Table<UUID, Identifier, Landmark> removeForBatch(Table<UUID, Identifier, Landmark> changed, UUID uuid, Identifier id) {
      if (Surveyor.CONFIG.landmarks == SystemMode.FROZEN) {
         return changed;
      } else if (!this.landmarks.contains(uuid, id)) {
         return changed;
      } else {
         Landmark landmark = (Landmark)this.landmarks.remove(uuid, id);
         if (this.removed != null) {
            this.removed.put(uuid, id);
         }

         this.dirty();
         changed.put(uuid, id, landmark);
         return changed;
      }
   }

   public Table<UUID, Identifier, Landmark> removeForBatch(UUID uuid, Identifier id) {
      return this.removeForBatch(HashBasedTable.create(), uuid, id);
   }

   public void removeLocal(UUID uuid, Identifier id) {
      this.handleChanged(this.removeForBatch(uuid, id), true, null);
   }

   public void remove(UUID uuid, Identifier id) {
      this.handleChanged(this.removeForBatch(uuid, id), false, null);
   }

   public void remove(UUID sender, UUID uuid, Identifier id) {
      this.handleChanged(this.removeForBatch(uuid, id), false, sender);
   }

   public Table<UUID, Identifier, Landmark> removeAllForBatch(Table<UUID, Identifier, Landmark> changed, Predicate<Landmark> predicate) {
      if (Surveyor.CONFIG.landmarks == SystemMode.FROZEN) {
         return null;
      } else {
         Table<UUID, Identifier, Landmark> toRemove = HashBasedTable.create(this.landmarks);
         toRemove.values().removeIf(predicate.negate());
         toRemove.cellSet().forEach(c -> this.removeForBatch(changed, (UUID)c.getRowKey(), (Identifier)c.getColumnKey()));
         return changed;
      }
   }

   public Table<UUID, Identifier, Landmark> removeAllForBatch(Predicate<Landmark> predicate) {
      return this.removeAllForBatch(HashBasedTable.create(), predicate);
   }

   public void removeAll(Predicate<Landmark> predicate) {
      this.handleChanged(this.removeAllForBatch(predicate), false, null);
   }

   public int save(File folder) {
      if (this.isDirty()) {
         File landmarksFile = new File(folder, "landmarks.dat");
         CompoundTag landmarksCompound = this.writeNbt(new CompoundTag());
         Util.ioPool().execute(() -> {
            try {
               NbtIo.writeCompressed(landmarksCompound, landmarksFile.toPath());
            } catch (IOException var4) {
               Surveyor.LOGGER.error("[Surveyor] Error writing landmarks file for {}.", this.summary.dimension().identifier(), var4);
            }
         });
         this.dirty = false;
         return this.landmarks.size();
      } else {
         return 0;
      }
   }

   public Table<UUID, Identifier, Landmark> readUpdatePacket(SyncLandmarksAddedPacket packet, @Nullable ServerPlayer sender) {
      Table<UUID, Identifier, Landmark> changed = HashBasedTable.create();
      packet.landmarks()
         .values()
         .forEach(
            landmark -> {
               boolean waypoint = !landmark.owner().equals(GLOBAL);
               if ((sender == null || Surveyor.canModify(landmark.owner(), sender))
                  && (
                     waypoint && Surveyor.CONFIG.networking.waypoints.atLeast(NetworkMode.SOLO)
                        || !waypoint && Surveyor.CONFIG.networking.landmarks.atLeast(NetworkMode.SOLO)
                  )) {
                  this.putForBatch(changed, landmark);
               }
            }
         );
      if (!changed.isEmpty()) {
         this.handleChanged(changed, sender == null, sender == null ? null : Surveyor.getUuid(sender));
      }

      return changed;
   }

   public Table<UUID, Identifier, Landmark> readUpdatePacket(SyncLandmarksRemovedPacket packet, @Nullable ServerPlayer sender) {
      Table<UUID, Identifier, Landmark> changed = HashBasedTable.create();
      packet.landmarks()
         .forEach(
            (uuid, id) -> {
               Landmark landmark = this.get(uuid, id);
               if (landmark != null) {
                  boolean waypoint = !landmark.owner().equals(GLOBAL);
                  if ((sender == null || Surveyor.canModify(landmark.owner(), sender))
                     && (
                        waypoint && Surveyor.CONFIG.networking.waypoints.atLeast(NetworkMode.SOLO)
                           || !waypoint && Surveyor.CONFIG.networking.landmarks.atLeast(NetworkMode.SOLO)
                     )) {
                     this.removeForBatch(changed, uuid, id);
                  }
               }
            }
         );
      if (!changed.isEmpty()) {
         this.handleChanged(changed, sender == null, sender == null ? null : Surveyor.getUuid(sender));
      }

      return changed;
   }

   public SyncLandmarksAddedPacket createUpdatePacket(Multimap<UUID, Identifier> keySet) {
      Table<UUID, Identifier, Landmark> updated = HashBasedTable.create();
      keySet.forEach((uuid, id) -> updated.put(uuid, id, this.get(uuid, id)));
      return new SyncLandmarksAddedPacket(this.summary.dimension(), updated);
   }

   public boolean isDirty() {
      return this.dirty && Surveyor.CONFIG.landmarks != SystemMode.FROZEN;
   }

   private void dirty() {
      this.dirty = true;
   }

   public void tryMigrateXaeros(boolean handleChanged) {
      if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
         File saveFolder = SurveyorClient.getXaerosSavePath(this.summary.dimension());
         if (saveFolder != null) {
            Table<UUID, Identifier, Landmark> changed = HashBasedTable.create();
            Surveyor.LOGGER.info("[Surveyor] Attempting to parse xaero's waypoints from {}/{}", saveFolder.getParentFile().getName(), saveFolder.getName());

            try {
               Files.createFile(saveFolder.toPath().resolve(".surveyor_migrated"));

               for (File file : Objects.requireNonNullElse(saveFolder.listFiles(f -> f.getName().endsWith(".txt")), new File[0])) {
                  for (String line : Files.readAllLines(file.toPath())) {
                     try {
                        if (line.startsWith("waypoint:")) {
                           String[] split = line.split(":");
                           BlockPos pos = new BlockPos(
                              Integer.parseInt(split[3]), split[4].equals("~") ? 0 : Integer.parseInt(split[4]), Integer.parseInt(split[5])
                           );
                           Identifier id = Identifier.fromNamespaceAndPath("xaeros", "waypoint/%d/%d/%d".formatted(pos.getX(), pos.getY(), pos.getZ()));
                           this.putForBatch(
                              changed,
                              Landmark.create(
                                 SurveyorClient.getClientUuid(),
                                 id,
                                 b -> b.add(LandmarkComponentTypes.POS, pos)
                                    .add(LandmarkComponentTypes.COLOR, XAERO_COLORS[Integer.parseInt(split[6])])
                                    .add(LandmarkComponentTypes.NAME, Component.literal(split[1]))
                              )
                           );
                           this.dirty = true;
                        }
                     } catch (Exception var13) {
                        Surveyor.LOGGER.error("[Surveyor] Error parsing xaeros waypoint: {}", line, var13);
                     }
                  }
               }
            } catch (Exception var14) {
               Surveyor.LOGGER.error("[Surveyor] Error parsing xaeros data from {}", saveFolder, var14);
            }

            Surveyor.LOGGER.info("[Surveyor] Migrated {} waypoints from xaeros data.", changed.size());
            if (handleChanged) {
               this.handleChanged(changed, Surveyor.CONFIG.networking.landmarks.atMost(NetworkMode.SOLO), null);
            }
         }
      }
   }
}
