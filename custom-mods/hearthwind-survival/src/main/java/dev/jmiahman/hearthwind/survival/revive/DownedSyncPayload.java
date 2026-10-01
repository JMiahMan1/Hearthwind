package dev.jmiahman.hearthwind.survival.revive;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Syncs the downed state to the client for the downed overlay.
 *
 * <p>The shape changed in 0.1.47 to match revive 1.0.7: the bleedout
 * countdown is gone (Aged's {@code timer} is {@code -1}, so its death screen
 * never renders the {@code Time: %d} line either), and in its place we send
 * <ul>
 *   <li>{@code armed} - the reference's {@code canRevive} flag, which decides
 *       whether the Revive button is live; and</li>
 *   <li>the death coordinates, which the reference's {@code DeathScreenMixin}
 *       always draws ({@code showDeathCoordinates} defaults to {@code true}).</li>
 * </ul>
 */
public record DownedSyncPayload(boolean isDowned, boolean armed, int x, int y, int z)
        implements CustomPacketPayload {

    public static final Type<DownedSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind", "downed_sync"));

    public static final StreamCodec<ByteBuf, DownedSyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, DownedSyncPayload::isDowned,
            ByteBufCodecs.BOOL, DownedSyncPayload::armed,
            ByteBufCodecs.VAR_INT, DownedSyncPayload::x,
            ByteBufCodecs.VAR_INT, DownedSyncPayload::y,
            ByteBufCodecs.VAR_INT, DownedSyncPayload::z,
            DownedSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}