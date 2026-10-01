package dev.jmiahman.hearthwind.survival.revive;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * The downed player's own "Revive" click, ported from revive 1.0.7's
 * {@code ReviveClientPacket.writeC2SRevivePacket}.
 *
 * <p>In the reference this is fired from a button the reference adds to the
 * player's DEATH screen ({@code DeathScreenMixin.initMixin}), so the flow is:
 * an ally arms the downed player with a sneaking empty-hand right-click, and
 * then the DOWNED player presses one button and is revived immediately. Our
 * player is never technically dead, so we fire the same packet from a button
 * on the downed overlay instead - the mechanic (one click, no hold) is the
 * reference's.
 *
 * <p>{@code boolean pressed} exists only because the payload must not be
 * empty; the reference carries a {@code boolean} too (its
 * {@code supportiveRevival} flag, which Aged can never set because the
 * supportive brewing recipe is dead code there).
 */
public record DownedRevivePayload(boolean pressed) implements CustomPacketPayload {

    public static final Type<DownedRevivePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind", "downed_revive"));

    public static final StreamCodec<ByteBuf, DownedRevivePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, DownedRevivePayload::pressed,
            DownedRevivePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}