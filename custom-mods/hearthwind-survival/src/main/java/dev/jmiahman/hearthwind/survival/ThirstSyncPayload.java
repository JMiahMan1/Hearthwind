package dev.jmiahman.hearthwind.survival;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ThirstSyncPayload(float hydration) implements CustomPacketPayload {
    public static final Type<ThirstSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind", "thirst"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThirstSyncPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public ThirstSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ThirstSyncPayload(buf.readFloat());
                }
                @Override
                public void encode(RegistryFriendlyByteBuf buf, ThirstSyncPayload payload) {
                    buf.writeFloat(payload.hydration());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
