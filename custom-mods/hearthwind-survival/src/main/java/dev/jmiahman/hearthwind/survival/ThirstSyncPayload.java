package dev.jmiahman.hearthwind.survival;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client thirst state for the HUD.
 *
 * @param hydration the 0..20 thirst level, drawn as the droplet row
 * @param buffered whether the internal dehydration buffer is at or above 4.0,
 *        which is the predicate the reference uses to pick the droplet bob's
 *        fast or slow cadence (see {@code ThirstHudRender.renderThirstHud}
 *        offsets 269-358). It is a one-bit fact so the packet stays tiny even
 *        though the buffer itself changes every tick.
 */
public record ThirstSyncPayload(float hydration, boolean buffered) implements CustomPacketPayload {
    public static final Type<ThirstSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind", "thirst"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThirstSyncPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public ThirstSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ThirstSyncPayload(buf.readFloat(), buf.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ThirstSyncPayload payload) {
                    buf.writeFloat(payload.hydration());
                    buf.writeBoolean(payload.buffered());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}