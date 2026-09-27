package draylar.inmis.network;

import draylar.inmis.Inmis;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Empty C2S ping: the client asks to open its first backpack (B key). */
public record BackpackOpenPacket() implements CustomPacketPayload {

    public static final Type<BackpackOpenPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Inmis.MOD_ID, "open_backpack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackOpenPacket> CODEC =
            StreamCodec.unit(new BackpackOpenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
