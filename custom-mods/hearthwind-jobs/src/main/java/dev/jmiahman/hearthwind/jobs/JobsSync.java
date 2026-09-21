package dev.jmiahman.hearthwind.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/** Builds and sends the full multi-job state used by the Jobs screen. */
public final class JobsSync {

    private JobsSync() {}

    public static JobsSyncPayload payload(ServerPlayer player) {
        List<String> employed = new ArrayList<>(JobState.employedJobs(player));
        List<String> ids = new ArrayList<>(JobDefs.all().keySet());
        Collections.sort(ids);
        List<Integer> levels = new ArrayList<>(ids.size());
        List<Double> xp = new ArrayList<>(ids.size());
        List<Double> nextCost = new ArrayList<>(ids.size());
        for (String id : ids) {
            levels.add(JobState.level(player, id));
            xp.add(JobState.xpInLevel(player, id));
            nextCost.add(JobState.nextCost(player, id));
        }
        return new JobsSyncPayload(employed, ids, levels, xp, nextCost,
                JobState.cooldownTicks(player),
                Math.max(1, HearthwindJobsConfig.get().employedJobs));
    }

    public static void send(ServerPlayer player) {
        ServerPlayNetworking.send(player, payload(player));
    }
}
