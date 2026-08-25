package dev.jmiahman.hearthwind.survival;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ThirstSyncPayload(float hydration) implements CustomPacketPayload {
    public static final Type<ThirstSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind", "thirst"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThirstSyncPayload> CODEC =
            StreamCodec.composite(
                    net.minecraft.network.codec.ByteBufCodecs.FLOAT, ThirstSyncPayload::hydration,
                    ThirstSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
