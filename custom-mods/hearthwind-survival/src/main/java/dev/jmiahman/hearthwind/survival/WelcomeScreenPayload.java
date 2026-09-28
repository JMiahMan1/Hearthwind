package dev.jmiahman.hearthwind.survival;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client: the first-join welcome screen is waiting for this player.
 *
 * <p>Aged 3.1.2 ships the {@code welcomescreen} mod plus the
 * {@code aged_welcome_screen} datapack: joining a world opens a welcome screen
 * and the {@code Start} button runs five {@code /item replace} commands. We
 * rebuild that flow, so the server tells the client when the screen belongs and
 * the client answers with {@link WelcomeStartPayload}.
 */
public record WelcomeScreenPayload(boolean show) implements CustomPacketPayload {
    public static final Type<WelcomeScreenPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "welcome_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WelcomeScreenPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public WelcomeScreenPayload decode(RegistryFriendlyByteBuf buf) {
                    return new WelcomeScreenPayload(buf.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, WelcomeScreenPayload payload) {
                    buf.writeBoolean(payload.show());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
