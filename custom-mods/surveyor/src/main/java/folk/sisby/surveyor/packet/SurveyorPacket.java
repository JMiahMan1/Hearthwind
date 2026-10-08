package folk.sisby.surveyor.packet;

import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface SurveyorPacket extends CustomPacketPayload {
   int MAX_PAYLOAD_SIZE = 1048576;

   default List<SurveyorPacket> toPayloads(RegistryAccess registryManager) {
      return List.of(this);
   }
}
