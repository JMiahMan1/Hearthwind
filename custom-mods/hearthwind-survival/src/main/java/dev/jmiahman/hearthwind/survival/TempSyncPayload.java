package dev.jmiahman.hearthwind.survival;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * EnvironmentZ temperature sync: body temperature (integer, +-2400 bands),
 * wetness (0..200) and the thermometer reading (roughly +-6, scaled by the
 * HUD against the thermometer bands).
 */
public record TempSyncPayload(int bodyTemperature, int wetness, int thermometer)
        implements CustomPacketPayload {
    public static final Type<TempSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "temp"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TempSyncPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public TempSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    return new TempSyncPayload(buf.readInt(), buf.readInt(), buf.readInt());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, TempSyncPayload payload) {
                    buf.writeInt(payload.bodyTemperature());
                    buf.writeInt(payload.wetness());
                    buf.writeInt(payload.thermometer());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
