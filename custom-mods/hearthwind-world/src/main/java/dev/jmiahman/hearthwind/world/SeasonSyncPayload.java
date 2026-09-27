package dev.jmiahman.hearthwind.world;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Season state pushed to clients for the top-left season HUD widget.
 * Pure data; the client never derives season itself.
 */
public record SeasonSyncPayload(int seasonOrdinal, int dayOfSeason, int daysPerSeason)
        implements CustomPacketPayload {
    public static final Type<SeasonSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_world", "season"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SeasonSyncPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public SeasonSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    return new SeasonSyncPayload(buf.readVarInt(), buf.readVarInt(),
                            buf.readVarInt());
                }
                @Override
                public void encode(RegistryFriendlyByteBuf buf, SeasonSyncPayload payload) {
                    buf.writeVarInt(payload.seasonOrdinal());
                    buf.writeVarInt(payload.dayOfSeason());
                    buf.writeVarInt(payload.daysPerSeason());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Pure day math so gametests can cover it without a level.
     * {@code cycleTicks} is the world-clock length of one Minecraft day
     * (24000 vanilla, 36000 with Aged's Time &amp; Wind data).
     */
    public static SeasonSyncPayload ofGameTime(long gameTime, int daysPerSeason, long cycleTicks) {
        long day = gameTime / (cycleTicks <= 0 ? 24000L : cycleTicks);
        Season season = Season.fromDay(day, daysPerSeason);
        int dayOfSeason = (int) (Math.floorMod(day, daysPerSeason) + 1L);
        return new SeasonSyncPayload(season.ordinal(), dayOfSeason, daysPerSeason);
    }
}
