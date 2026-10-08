package folk.sisby.surveyor.packet;

import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.config.NetworkMode;
import java.util.UUID;

public interface SyncPacket extends C2SPacket, S2CPacket {
   default void send(UUID sender, WorldSummary summary, NetworkMode mode, boolean withSelf) {
      if (!mode.atMost(NetworkMode.NONE)) {
         if (summary.isClient()) {
            this.send(summary.manager());
         } else {
            this.send(sender, summary.server(), mode, withSelf);
         }
      }
   }
}
