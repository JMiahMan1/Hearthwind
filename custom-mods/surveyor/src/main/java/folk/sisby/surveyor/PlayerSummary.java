package folk.sisby.surveyor;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import folk.sisby.surveyor.config.NetworkMode;
import folk.sisby.surveyor.mixin.AccessServerPlayerEntity;
import folk.sisby.surveyor.packet.S2CStructuresAddedPacket;
import folk.sisby.surveyor.packet.S2CUpdateRegionPacket;
import folk.sisby.surveyor.structure.WorldStructures;
import folk.sisby.surveyor.terrain.WorldTerrain;
import folk.sisby.surveyor.util.ArrayUtil;
import folk.sisby.surveyor.util.RegionPos;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public interface PlayerSummary {
   String KEY_DATA = "surveyor";
   String KEY_USERNAME = "username";

   static PlayerSummary of(ServerPlayer player) {
      return ((SurveyorPlayer)player).surveyor$getSummary();
   }

   static PlayerSummary of(UUID uuid, MinecraftServer server) {
      return ServerSummary.of(server).getPlayer(uuid);
   }

   SurveyorExploration exploration();

   String username();

   ResourceKey<Level> dimension();

   Vec3 pos();

   float yaw();

   int viewDistance();

   boolean online();

   default void copyFrom(PlayerSummary oldSummary) {
      this.exploration().copyFrom(oldSummary.exploration());
   }

   public record OfflinePlayerSummary(SurveyorExploration exploration, String username, ResourceKey<Level> dimension, Vec3 pos, float yaw, boolean online)
      implements PlayerSummary {
      public OfflinePlayerSummary(UUID uuid, CompoundTag nbt, boolean online) {
         this(
            PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration.from(uuid, nbt.getCompound("surveyor").orElse(new CompoundTag())),
            nbt.getCompound("surveyor").isPresent() && ((CompoundTag)nbt.getCompound("surveyor").get()).getString("username").isPresent()
               ? (String)((CompoundTag)nbt.getCompound("surveyor").get()).getString("username").get()
               : "???",
            ResourceKey.create(Registries.DIMENSION, Identifier.parse(nbt.getString("Dimension").orElse(Level.OVERWORLD.identifier().toString()))),
            nbt.getList("Pos").isPresent()
               ? ArrayUtil.toVec3d(((ListTag)nbt.getList("Pos").get()).stream().mapToDouble(e -> ((DoubleTag)e).doubleValue()).toArray())
               : new Vec3(0.0, 0.0, 0.0),
            nbt.getList("Rotation").isPresent() ? ((ListTag)nbt.getList("Rotation").get()).getFloat(0).orElse(0.0F) : 0.0F,
            online
         );
      }

      public OfflinePlayerSummary(ServerPlayer player) {
         this(
            PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration.ofMerged(Set.of(SurveyorExploration.of(player))),
            player.getGameProfile().name(),
            player.level().dimension(),
            player.position(),
            player.getYRot(),
            true
         );
      }

      public static void writeBuf(PlayerSummary summary, RegistryFriendlyByteBuf buf) {
         buf.writeUtf(summary.username());
         buf.writeResourceKey(summary.dimension());
         buf.writeDouble(summary.pos().x());
         buf.writeDouble(summary.pos().y());
         buf.writeDouble(summary.pos().z());
         buf.writeFloat(summary.yaw());
         buf.writeBoolean(summary.online());
      }

      public static PlayerSummary readBuf(RegistryFriendlyByteBuf buf) {
         return new PlayerSummary.OfflinePlayerSummary(
            null,
            buf.readUtf(),
            buf.readResourceKey(Registries.DIMENSION),
            new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
            buf.readFloat(),
            buf.readBoolean()
         );
      }

      @Override
      public int viewDistance() {
         return 0;
      }

      public record OfflinePlayerExploration(
         Set<UUID> sharedPlayers,
         Table<ResourceKey<Level>, RegionPos, BitSet> chunks,
         Table<ResourceKey<Level>, ResourceKey<Structure>, LongSet> starts,
         boolean personal
      ) implements SurveyorExploration {
         public static PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration empty(UUID uuid) {
            return new PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration(Set.of(uuid), HashBasedTable.create(), HashBasedTable.create(), true);
         }

         public static PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration ofMerged(Set<SurveyorExploration> explorations) {
            Set<UUID> sharedPlayers = new HashSet<>();
            Table<ResourceKey<Level>, RegionPos, BitSet> chunks = HashBasedTable.create();
            Table<ResourceKey<Level>, ResourceKey<Structure>, LongSet> starts = HashBasedTable.create();
            PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration outExploration = new PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration(
               sharedPlayers, chunks, starts, false
            );

            for (SurveyorExploration exploration : explorations) {
               sharedPlayers.addAll(exploration.sharedPlayers());
               exploration.chunks()
                  .cellSet()
                  .forEach(c -> outExploration.mergeRegion((ResourceKey<Level>)c.getRowKey(), (RegionPos)c.getColumnKey(), (BitSet)c.getValue(), false));
               exploration.starts()
                  .cellSet()
                  .forEach(
                     c -> outExploration.mergeStructures((ResourceKey<Level>)c.getRowKey(), (ResourceKey<Structure>)c.getColumnKey(), (LongSet)c.getValue())
                  );
            }

            return outExploration;
         }

         public static SurveyorExploration from(UUID uuid, CompoundTag nbt) {
            PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration mutable = new PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration(
               new HashSet<>(Set.of(uuid)), HashBasedTable.create(), HashBasedTable.create(), true
            );
            mutable.read(nbt);
            return mutable;
         }
      }
   }

   public static class PlayerEntitySummary implements PlayerSummary {
      protected final Player player;

      public PlayerEntitySummary(Player player) {
         this.player = player;
      }

      @Override
      public SurveyorExploration exploration() {
         return null;
      }

      @Override
      public String username() {
         return this.player.getGameProfile().name();
      }

      @Override
      public ResourceKey<Level> dimension() {
         return this.player.level().dimension();
      }

      @Override
      public Vec3 pos() {
         return this.player.position();
      }

      @Override
      public float yaw() {
         return this.player.getYRot();
      }

      @Override
      public int viewDistance() {
         return 0;
      }

      @Override
      public boolean online() {
         return true;
      }
   }

   public static class ServerPlayerEntitySummary extends PlayerSummary.PlayerEntitySummary implements PlayerSummary {
      private final PlayerSummary.ServerPlayerEntitySummary.ServerPlayerExploration exploration;

      public ServerPlayerEntitySummary(ServerPlayer player) {
         super(player);
         this.exploration = new PlayerSummary.ServerPlayerEntitySummary.ServerPlayerExploration(
            player, Tables.synchronizedTable(HashBasedTable.create()), Tables.synchronizedTable(HashBasedTable.create())
         );
      }

      @Override
      public SurveyorExploration exploration() {
         return this.exploration;
      }

      @Override
      public int viewDistance() {
         return ((ServerPlayer)this.player).requestedViewDistance();
      }

      public void copyExploration(PlayerSummary.ServerPlayerEntitySummary oldSummary) {
         this.exploration.copyFrom(oldSummary.exploration);
      }

      public void read(ValueInput view) {
         this.exploration.read(view.read("surveyor", CompoundTag.CODEC).orElse(new CompoundTag()));
      }

      public void writeNbt(ValueOutput view) {
         CompoundTag surveyorNbt = new CompoundTag();
         this.exploration.write(surveyorNbt);
         surveyorNbt.putString("username", this.username());
         view.store("surveyor", CompoundTag.CODEC, surveyorNbt);
      }

      public record ServerPlayerExploration(
         ServerPlayer player, Table<ResourceKey<Level>, RegionPos, BitSet> chunks, Table<ResourceKey<Level>, ResourceKey<Structure>, LongSet> starts
      ) implements SurveyorExploration {
         @Override
         public Set<UUID> sharedPlayers() {
            return Set.of(Surveyor.getUuid(this.player));
         }

         @Override
         public boolean personal() {
            return true;
         }

         @Override
         public void mergeRegion(ResourceKey<Level> dimension, RegionPos regionPos, BitSet chunks, boolean updateClient) {
            WorldSummary summary = WorldSummary.of(((AccessServerPlayerEntity)this.player).getServer().getLevel(dimension));
            WorldTerrain terrain = summary == null ? null : summary.terrain();
            if (((AccessServerPlayerEntity)this.player).getServer().isSingleplayerOwner(this.player.nameAndId())) {
               this.updateClientForMergeRegion(summary, regionPos, chunks);
            }

            if (Surveyor.CONFIG.networking.terrain.atLeast(NetworkMode.SOLO)) {
               for (ServerPlayer friend : ServerSummary.of(((AccessServerPlayerEntity)this.player).getServer())
                  .getSharingPlayers(Surveyor.getUuid(this.player), Surveyor.CONFIG.networking.terrain, !updateClient)) {
                  SurveyorExploration friendExploration = SurveyorExploration.of(friend);
                  BitSet sendSet = (BitSet)chunks.clone();
                  if (friendExploration.chunks().contains(dimension, regionPos)) {
                     sendSet.andNot((BitSet)friendExploration.chunks().get(dimension, regionPos));
                  }

                  if (!sendSet.isEmpty() && terrain != null) {
                     S2CUpdateRegionPacket.of(dimension, friend != this.player, regionPos, terrain.getRegion(regionPos), sendSet).send(friend);
                  }
               }
            }

            SurveyorExploration.super.mergeRegion(dimension, regionPos, chunks, updateClient);
         }

         @Override
         public void addChunk(ResourceKey<Level> dimension, ChunkPos pos, boolean updateClient) {
            WorldSummary summary = WorldSummary.of(((AccessServerPlayerEntity)this.player).getServer().getLevel(dimension));
            if (Surveyor.CONFIG.networking.terrain.atLeast(NetworkMode.SOLO)) {
               RegionPos regionPos = RegionPos.of(pos);
               WorldTerrain terrain = summary == null ? null : summary.terrain();
               if (terrain == null) {
                  return;
               }

               S2CUpdateRegionPacket packet = S2CUpdateRegionPacket.of(dimension, true, regionPos, terrain.getRegion(regionPos), RegionPos.chunkToBitSet(pos));
               packet.send(
                  Surveyor.getUuid(this.player),
                  ((AccessServerPlayerEntity)this.player).getServer(),
                  p -> !SurveyorExploration.of(p).exploredChunk(dimension, pos),
                  Surveyor.CONFIG.networking.terrain,
                  updateClient
               );
            }

            SurveyorExploration.super.addChunk(dimension, pos, updateClient);
            if (((AccessServerPlayerEntity)this.player).getServer().isSingleplayerOwner(this.player.nameAndId())) {
               this.updateClientForAddChunk(summary, pos);
            }
         }

         @Override
         public void addStructure(ResourceKey<Level> dimension, ResourceKey<Structure> structureKey, ChunkPos pos) {
            WorldSummary summary = WorldSummary.of(((AccessServerPlayerEntity)this.player).getServer().getLevel(dimension));
            WorldStructures structures = summary == null ? null : summary.structures();
            if (structures != null && Surveyor.CONFIG.networking.structures.atLeast(NetworkMode.SOLO)) {
               S2CStructuresAddedPacket packet = S2CStructuresAddedPacket.of(false, structureKey, pos, structures);
               packet.send(
                  Surveyor.getUuid(this.player),
                  ((AccessServerPlayerEntity)this.player).getServer(),
                  p -> !SurveyorExploration.of(p).exploredStructure(dimension, structureKey, pos),
                  Surveyor.CONFIG.networking.structures,
                  false
               );
            }

            SurveyorExploration.super.addStructure(dimension, structureKey, pos);
            if (((AccessServerPlayerEntity)this.player).getServer().isSingleplayerOwner(this.player.nameAndId())) {
               this.updateClientForAddStructure(summary, structureKey, pos);
            }
         }
      }
   }
}
