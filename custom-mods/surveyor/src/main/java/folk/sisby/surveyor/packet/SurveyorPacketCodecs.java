package folk.sisby.surveyor.packet;

import java.util.List;

import folk.sisby.surveyor.structure.StructurePieceSummary;

import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import folk.sisby.surveyor.PlayerSummary;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.structure.RegionStructureSummary;
import folk.sisby.surveyor.structure.StructureStartSummary;
import folk.sisby.surveyor.util.MapUtil;
import folk.sisby.surveyor.util.RegionPos;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * The decompiler erased the type witnesses in these nested codec chains, and the
 * MapUtil adapters (asTable / asMultiMap / asListMap) cannot be given a target
 * type from inside a field initialiser. Each chain therefore lives in a static
 * helper so its intermediate steps can be locals with explicit types.
 */
public interface SurveyorPacketCodecs {
   StreamCodec<RegistryFriendlyByteBuf, Table<ResourceKey<Level>, RegionPos, BitSet>> TERRAIN_KEYS = buildTerrainKeys();
   StreamCodec<RegistryFriendlyByteBuf, Map<ResourceKey<Level>, Multimap<ResourceKey<Structure>, ChunkPos>>> STRUCTURE_KEYS = buildStructureKeys();
   StreamCodec<RegistryFriendlyByteBuf, Table<ResourceKey<Level>, ResourceKey<Structure>, LongSet>> STRUCTURE_KEYS_LONG_SET = buildStructureKeysLongSet();
   StreamCodec<RegistryFriendlyByteBuf, Map<ResourceKey<Level>, Multimap<UUID, Identifier>>> LANDMARK_KEYS = buildLandmarkKeys();
   StreamCodec<RegistryFriendlyByteBuf, Map<UUID, PlayerSummary>> GROUP_SUMMARIES = ByteBufCodecs.map(
      HashMap::new, UUIDUtil.STREAM_CODEC, StreamCodec.ofMember(PlayerSummary.OfflinePlayerSummary::writeBuf, PlayerSummary.OfflinePlayerSummary::readBuf)
   );
   StreamCodec<RegistryFriendlyByteBuf, Table<ResourceKey<Structure>, ChunkPos, StructureStartSummary>> STRUCTURE_SUMMARIES = buildStructureSummaries();
   StreamCodec<RegistryFriendlyByteBuf, Map<ResourceKey<Structure>, ResourceKey<StructureType<?>>>> STRUCTURE_TYPES = ByteBufCodecs.map(
      HashMap::new, ResourceKey.streamCodec(Registries.STRUCTURE), ResourceKey.streamCodec(Registries.STRUCTURE_TYPE)
   );
   StreamCodec<RegistryFriendlyByteBuf, Multimap<ResourceKey<Structure>, TagKey<Structure>>> STRUCTURE_TAGS = buildStructureTags();
   StreamCodec<ByteBuf, Table<UUID, Identifier, Landmark>> LANDMARK_SUMMARIES = ByteBufCodecs.fromCodec(WorldLandmarks.CODEC);

   static StreamCodec<RegistryFriendlyByteBuf, Table<ResourceKey<Level>, RegionPos, BitSet>> buildTerrainKeys() {
      StreamCodec<RegistryFriendlyByteBuf, Map<RegionPos, BitSet>> regions =
         ByteBufCodecs.map(HashMap::new, RegionPos.PACKET_CODEC, ByteBufCodecs.fromCodec(ExtraCodecs.BIT_SET));
      StreamCodec<RegistryFriendlyByteBuf, Map<ResourceKey<Level>, Map<RegionPos, BitSet>>> levels =
         ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceKey<Level>, Map<RegionPos, BitSet>, Map<ResourceKey<Level>, Map<RegionPos, BitSet>>>map(
            HashMap::new, ResourceKey.streamCodec(Registries.DIMENSION), regions);
      return levels.map(MapUtil::asTable, Table::rowMap);
   }

   static StreamCodec<RegistryFriendlyByteBuf, Map<ResourceKey<Level>, Multimap<ResourceKey<Structure>, ChunkPos>>> buildStructureKeys() {
      StreamCodec<ByteBuf, Map<ResourceKey<Structure>, List<ChunkPos>>> inner =
         ByteBufCodecs.<ByteBuf, ResourceKey<Structure>, List<ChunkPos>, Map<ResourceKey<Structure>, List<ChunkPos>>>map(
            HashMap::new,
            ResourceKey.streamCodec(Registries.STRUCTURE),
            ByteBufCodecs.VAR_LONG.map(ChunkPos::unpack, ChunkPos::pack)
               .apply(ByteBufCodecs.<ByteBuf, ChunkPos>list()));
      StreamCodec<ByteBuf, Multimap<ResourceKey<Structure>, ChunkPos>> multimap =
         inner.map(MapUtil::asMultiMap, MapUtil::asListMap);
      return ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceKey<Level>, Multimap<ResourceKey<Structure>, ChunkPos>, Map<ResourceKey<Level>, Multimap<ResourceKey<Structure>, ChunkPos>>>map(
         HashMap::new, ResourceKey.streamCodec(Registries.DIMENSION), multimap);
   }

   static StreamCodec<RegistryFriendlyByteBuf, Table<ResourceKey<Level>, ResourceKey<Structure>, LongSet>> buildStructureKeysLongSet() {
      StreamCodec<ByteBuf, LongSet> longs =
         ByteBufCodecs.fromCodec(Codec.LONG_STREAM).map(LongOpenHashSet::toSet, LongCollection::longStream);
      StreamCodec<RegistryFriendlyByteBuf, Map<ResourceKey<Structure>, LongSet>> structures =
         ByteBufCodecs.map(HashMap::new, ResourceKey.streamCodec(Registries.STRUCTURE), longs);
      StreamCodec<RegistryFriendlyByteBuf, Map<ResourceKey<Level>, Map<ResourceKey<Structure>, LongSet>>> levels =
         ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceKey<Level>, Map<ResourceKey<Structure>, LongSet>, Map<ResourceKey<Level>, Map<ResourceKey<Structure>, LongSet>>>map(
            HashMap::new, ResourceKey.streamCodec(Registries.DIMENSION), structures);
      return levels.map(MapUtil::asTable, Table::rowMap);
   }

   static StreamCodec<RegistryFriendlyByteBuf, Map<ResourceKey<Level>, Multimap<UUID, Identifier>>> buildLandmarkKeys() {
      return ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceKey<Level>, Multimap<UUID, Identifier>, Map<ResourceKey<Level>, Multimap<UUID, Identifier>>>map(
         HashMap::new, ResourceKey.streamCodec(Registries.DIMENSION), buildUuidMultimap());
   }

   static StreamCodec<ByteBuf, Multimap<UUID, Identifier>> buildUuidMultimap() {
      StreamCodec<ByteBuf, List<Identifier>> ids = Identifier.STREAM_CODEC.apply(ByteBufCodecs.list());
      StreamCodec<ByteBuf, Map<UUID, List<Identifier>>> inner =
         ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ids);
      return inner.map(MapUtil::asMultiMap, MapUtil::asListMap);
   }

   static StreamCodec<RegistryFriendlyByteBuf, Table<ResourceKey<Structure>, ChunkPos, StructureStartSummary>> buildStructureSummaries() {
      StreamCodec<RegistryFriendlyByteBuf, StructureStartSummary> starts =
         StreamCodec.ofMember(
               (StructurePieceSummary s, RegistryFriendlyByteBuf b) -> b.writeNbt(s.toNbt()),
               (RegistryFriendlyByteBuf b) -> RegionStructureSummary.readStructurePieceNbt(b.readNbt())
            )
            .apply(ByteBufCodecs.list())
            .map(StructureStartSummary::new, StructureStartSummary::getChildren);
      StreamCodec<RegistryFriendlyByteBuf, Map<ChunkPos, StructureStartSummary>> chunks =
         ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_LONG.map(ChunkPos::unpack, ChunkPos::pack), starts);
      StreamCodec<RegistryFriendlyByteBuf, Map<ResourceKey<Structure>, Map<ChunkPos, StructureStartSummary>>> structures =
         ByteBufCodecs.map(HashMap::new, ResourceKey.streamCodec(Registries.STRUCTURE), chunks);
      return structures.map(MapUtil::asTable, Table::rowMap);
   }

   static StreamCodec<RegistryFriendlyByteBuf, Multimap<ResourceKey<Structure>, TagKey<Structure>>> buildStructureTags() {
      StreamCodec<ByteBuf, List<TagKey<Structure>>> tags =
         ByteBufCodecs.fromCodec(TagKey.hashedCodec(Registries.STRUCTURE)).apply(ByteBufCodecs.list());
      StreamCodec<RegistryFriendlyByteBuf, Map<ResourceKey<Structure>, List<TagKey<Structure>>>> byStructure =
         ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceKey<Structure>, List<TagKey<Structure>>, Map<ResourceKey<Structure>, List<TagKey<Structure>>>>map(
            HashMap::new, ResourceKey.streamCodec(Registries.STRUCTURE), tags);
      return byStructure.map(MapUtil::asMultiMap, MapUtil::asListMap);
   }
}
