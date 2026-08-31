package dev.jmiahman.hearthwind.survival.revive;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Syncs downed bleedout state to the client for the downed HUD indicator.
 */
public record DownedSyncPayload(boolean isDowned, int remainingSeconds, int reviveProgressPercent)
        implements CustomPacketPayload {

    public static final Type<DownedSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind", "downed_sync"));

    public static final StreamCodec<ByteBuf, DownedSyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, DownedSyncPayload::isDowned,
            ByteBufCodecs.VAR_INT, DownedSyncPayload::remainingSeconds,
            ByteBufCodecs.VAR_INT, DownedSyncPayload::reviveProgressPercent,
            DownedSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
