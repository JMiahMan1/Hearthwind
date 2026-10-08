package folk.sisby.surveyor.packet;

import folk.sisby.surveyor.Surveyor;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public record S2CGroupAmendedPacket(UUID player) implements S2CPacket {
   public static final Type<S2CGroupAmendedPacket> ID = new Type(Surveyor.id("s2c_group_amended"));
   public static final StreamCodec<ByteBuf, S2CGroupAmendedPacket> CODEC = UUIDUtil.STREAM_CODEC.map(S2CGroupAmendedPacket::new, S2CGroupAmendedPacket::player);

   public Type<S2CGroupAmendedPacket> type() {
      return ID;
   }
}
