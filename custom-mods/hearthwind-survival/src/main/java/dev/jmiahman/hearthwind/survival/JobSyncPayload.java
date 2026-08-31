package dev.jmiahman.hearthwind.survival;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record JobSyncPayload(String job, int level, double xp, double xpPerLevel) implements CustomPacketPayload {
    public static final Type<JobSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "job"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JobSyncPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public JobSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    String job = buf.readUtf();
                    int level = buf.readInt();
                    double xp = buf.readDouble();
                    double xpPerLevel = buf.readDouble();
                    return new JobSyncPayload(job, level, xp, xpPerLevel);
                }
                @Override
                public void encode(RegistryFriendlyByteBuf buf, JobSyncPayload payload) {
                    buf.writeUtf(payload.job());
                    buf.writeInt(payload.level());
                    buf.writeDouble(payload.xp());
                    buf.writeDouble(payload.xpPerLevel());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
