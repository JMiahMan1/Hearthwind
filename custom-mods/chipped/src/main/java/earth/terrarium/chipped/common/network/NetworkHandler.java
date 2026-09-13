package earth.terrarium.chipped.common.network;

import com.teamresourceful.resourcefullib.common.network.Network;
import earth.terrarium.chipped.Chipped;
import net.minecraft.resources.Identifier;

public class NetworkHandler {

    public static final Network CHANNEL = new Network(Identifier.fromNamespaceAndPath(Chipped.MOD_ID, "main"), 1);

    public static void init() {
        CHANNEL.register(ServerboundCraftPacket.TYPE);
    }
}
