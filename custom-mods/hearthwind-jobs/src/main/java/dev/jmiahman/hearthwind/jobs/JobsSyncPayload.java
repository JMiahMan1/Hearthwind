package dev.jmiahman.hearthwind.jobs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Full multi-job sync for the Jobs screen: every defined job with its level,
 * within-level XP and next-level cost, plus the employed list, the remaining
 * job-change cooldown and the employed-slot cap.
 */
public record JobsSyncPayload(
        List<String> employed,
        List<String> ids,
        List<Integer> levels,
        List<Double> xp,
        List<Double> nextCost,
        int cooldownTicks,
        int maxEmployed) implements CustomPacketPayload {

    public static final Type<JobsSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_jobs", "jobs_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JobsSyncPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public JobsSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    int employedCount = buf.readVarInt();
                    List<String> employed = new ArrayList<>(employedCount);
                    for (int i = 0; i < employedCount; i++) {
                        employed.add(buf.readUtf());
                    }
                    int count = buf.readVarInt();
                    List<String> ids = new ArrayList<>(count);
                    List<Integer> levels = new ArrayList<>(count);
                    List<Double> xp = new ArrayList<>(count);
                    List<Double> nextCost = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        ids.add(buf.readUtf());
                        levels.add(buf.readVarInt());
                        xp.add(buf.readDouble());
                        nextCost.add(buf.readDouble());
                    }
                    int cooldown = buf.readVarInt();
                    int maxEmployed = buf.readVarInt();
                    return new JobsSyncPayload(employed, ids, levels, xp, nextCost, cooldown, maxEmployed);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, JobsSyncPayload payload) {
                    buf.writeVarInt(payload.employed().size());
                    for (String id : payload.employed()) {
                        buf.writeUtf(id);
                    }
                    buf.writeVarInt(payload.ids().size());
                    for (int i = 0; i < payload.ids().size(); i++) {
                        buf.writeUtf(payload.ids().get(i));
                        buf.writeVarInt(payload.levels().get(i));
                        buf.writeDouble(payload.xp().get(i));
                        buf.writeDouble(payload.nextCost().get(i));
                    }
                    buf.writeVarInt(payload.cooldownTicks());
                    buf.writeVarInt(payload.maxEmployed());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
