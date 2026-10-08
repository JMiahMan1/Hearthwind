package folk.sisby.surveyor.packet;

import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.terrain.ChunkSummary;
import folk.sisby.surveyor.terrain.RegionSummary;
import folk.sisby.surveyor.util.BitSetUtil;
import folk.sisby.surveyor.util.ListUtil;
import folk.sisby.surveyor.util.RegionPos;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import folk.sisby.surveyor.util.Tuple;
import net.minecraft.world.level.Level;

public record S2CUpdateRegionPacket(
   ResourceKey<Level> dimension,
   boolean shared,
   RegionPos regionPos,
   List<Integer> biomePalette,
   List<Integer> blockPalette,
   BitSet set,
   List<ChunkSummary> chunks
) implements S2CPacket, ShareFlagged<S2CUpdateRegionPacket> {
   private static final StreamCodec<RegistryFriendlyByteBuf, Tuple<ResourceKey<Level>, Boolean>> DIMENSION_SHARED = StreamCodec.composite(
      ResourceKey.streamCodec(Registries.DIMENSION), Tuple::getA, ByteBufCodecs.BOOL, Tuple::getB, Tuple::new);
   public static final Type<S2CUpdateRegionPacket> ID = new Type(Surveyor.id("s2c_update_region"));
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CUpdateRegionPacket> CODEC = StreamCodec.composite(
      DIMENSION_SHARED,
      p -> new Tuple(p.dimension(), p.shared()),
      RegionPos.PACKET_CODEC,
      S2CUpdateRegionPacket::regionPos,
      ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
      S2CUpdateRegionPacket::biomePalette,
      ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
      S2CUpdateRegionPacket::blockPalette,
      ByteBufCodecs.fromCodec(ExtraCodecs.BIT_SET),
      S2CUpdateRegionPacket::set,
      StreamCodec.ofMember(ChunkSummary::writeBuf, ChunkSummary::new).apply(ByteBufCodecs.list()),
      S2CUpdateRegionPacket::chunks,
      (pair, rp, bi, bl, s, c) -> new S2CUpdateRegionPacket((ResourceKey<Level>)pair.getA(), (Boolean)pair.getB(), rp, bi, bl, s, c)
   );

   public static S2CUpdateRegionPacket of(ResourceKey<Level> dimension, boolean shared, RegionPos regionPos, RegionSummary summary, BitSet keys) {
      return summary.createUpdatePacket(dimension, shared, regionPos, keys);
   }

   public S2CUpdateRegionPacket withShared(boolean shared) {
      return new S2CUpdateRegionPacket(this.dimension, shared, this.regionPos, this.biomePalette, this.blockPalette, this.set, this.chunks);
   }

   @Override
   public List<SurveyorPacket> toPayloads(RegistryAccess registryManager) {
      List<SurveyorPacket> payloads = new ArrayList<>();
      RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.buffer()), registryManager);
      CODEC.encode(buf, this);
      if (buf.readableBytes() < 1048576) {
         payloads.add(this);
      } else {
         if (this.set.cardinality() == 1) {
            int bit = this.set.stream().findFirst().orElseThrow();
            Surveyor.LOGGER
               .error(
                  "Couldn't create a terrain update packet at {} - an individual chunk would be too large to send!",
                  "[%d,%d]".formatted(this.regionPos.toChunk(bit).x(), this.regionPos.toChunk(bit).z())
               );
            return List.of();
         }

         for (BitSet splitChunks : BitSetUtil.half(this.set)) {
            payloads.addAll(
               new S2CUpdateRegionPacket(
                     this.dimension,
                     this.shared,
                     this.regionPos,
                     this.biomePalette,
                     this.blockPalette,
                     splitChunks,
                     ListUtil.splitSet(this.chunks, splitChunks, this.set)
                  )
                  .toPayloads(registryManager)
            );
         }
      }

      return payloads;
   }

   public Type<S2CUpdateRegionPacket> type() {
      return ID;
   }
}
