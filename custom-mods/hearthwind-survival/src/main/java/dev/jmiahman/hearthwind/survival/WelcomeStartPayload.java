package dev.jmiahman.hearthwind.survival;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client -> server: the player pressed {@code Start} on the welcome screen.
 *
 * <p>The server is the only side that may hand out items, so the loadout waits
 * for this packet instead of being granted during {@code JOIN}.
 */
public record WelcomeStartPayload(boolean pressed) implements CustomPacketPayload {
    public static final Type<WelcomeStartPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "welcome_start"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WelcomeStartPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public WelcomeStartPayload decode(RegistryFriendlyByteBuf buf) {
                    return new WelcomeStartPayload(buf.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, WelcomeStartPayload payload) {
                    buf.writeBoolean(payload.pressed());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
