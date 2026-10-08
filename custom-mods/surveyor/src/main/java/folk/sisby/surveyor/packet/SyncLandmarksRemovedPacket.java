package folk.sisby.surveyor.packet;

import com.google.common.collect.Multimap;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.util.MapUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record SyncLandmarksRemovedPacket(ResourceKey<Level> dimension, Multimap<UUID, Identifier> landmarks) implements SyncPacket {
   public static final Type<SyncLandmarksRemovedPacket> ID = new Type(Surveyor.id("landmarks_removed"));
   public static final StreamCodec<RegistryFriendlyByteBuf, SyncLandmarksRemovedPacket> CODEC = StreamCodec.composite(
      ResourceKey.streamCodec(Registries.DIMENSION),
      SyncLandmarksRemovedPacket::dimension,
      SurveyorPacketCodecs.buildUuidMultimap(),
      SyncLandmarksRemovedPacket::landmarks,
      SyncLandmarksRemovedPacket::new
   );

   public static SyncLandmarksRemovedPacket of(ResourceKey<Level> dimension, UUID uuid, Identifier id) {
      return new SyncLandmarksRemovedPacket(dimension, MapUtil.asMultiMap(Map.of(uuid, List.of(id))));
   }

   public Type<SyncLandmarksRemovedPacket> type() {
      return ID;
   }
}
