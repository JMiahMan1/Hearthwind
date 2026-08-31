package dev.jmiahman.hearthwind.survival;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TempSyncPayload(float temperature) implements CustomPacketPayload {
    public static final Type<TempSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "temp"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TempSyncPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public TempSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    return new TempSyncPayload(buf.readFloat());
                }
                @Override
                public void encode(RegistryFriendlyByteBuf buf, TempSyncPayload payload) {
                    buf.writeFloat(payload.temperature());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
