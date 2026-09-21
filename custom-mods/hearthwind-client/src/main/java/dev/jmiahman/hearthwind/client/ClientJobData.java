package dev.jmiahman.hearthwind.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side copy of job state for the HUD and the Jobs screen.
 *
 * <p>Full state arrives via {@code hearthwind_jobs:jobs_sync} (all jobs with
 * level/xp, the employed list, cooldown and the employed-slot cap); the
 * legacy single-job payload is still accepted and merged as the primary job.
 */
public final class ClientJobData {

    public record JobInfo(String id, int level, double xp, double nextCost, boolean employed) {}

    private static final Map<String, JobInfo> JOBS = new LinkedHashMap<>();
    private static List<String> employed = new ArrayList<>();
    private static double xpPerLevel = 100.0;
    private static int maxEmployed = 3;
    private static long cooldownUntilMillis = 0L;

    private ClientJobData() {}

    /** Full sync from the jobs module. */
    public static void updateAll(List<String> employedIds, List<String> ids, List<Integer> levels,
            List<Double> xp, List<Double> nextCost, int cooldownTicks, int max) {
        Map<String, JobInfo> next = new LinkedHashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            next.put(ids.get(i), new JobInfo(ids.get(i), levels.get(i), xp.get(i), nextCost.get(i),
                    employedIds.contains(ids.get(i))));
        }
        JOBS.clear();
        JOBS.putAll(next);
        employed = new ArrayList<>(employedIds);
        maxEmployed = Math.max(1, max);
        cooldownUntilMillis = cooldownTicks > 0
                ? System.currentTimeMillis() + cooldownTicks * 50L
                : 0L;
    }

    /** Legacy single-job sync (kept for compatibility with the survival payload). */
    public static void setJob(String id, int level, double exp, double perLevel) {
        xpPerLevel = perLevel;
        if (id == null || id.isEmpty()) {
            return;
        }
        JOBS.put(id, new JobInfo(id, level, exp, perLevel, true));
        if (!employed.contains(id)) {
            employed.add(id);
        }
    }

    /** Merges one job card's state (used by the periodic/simple payloads). */
    public static void put(String id, int level, double xp, double nextCost, boolean isEmployed) {
        if (id == null || id.isEmpty()) {
            return;
        }
        JOBS.put(id, new JobInfo(id, level, xp, nextCost, isEmployed));
        employed.remove(id);
        if (isEmployed) {
            employed.add(id);
        }
    }

    public static void clear() {
        JOBS.clear();
        employed = new ArrayList<>();
        cooldownUntilMillis = 0L;
    }

    public static JobInfo job(String id) {
        return JOBS.get(id);
    }

    public static List<JobInfo> allJobs() {
        return Collections.unmodifiableList(new ArrayList<>(JOBS.values()));
    }

    public static List<String> employed() {
        return Collections.unmodifiableList(employed);
    }

    public static List<String> employedInOrder(List<String> order) {
        List<String> out = new ArrayList<>();
        for (String id : order) {
            if (employed.contains(id)) {
                out.add(id);
            }
        }
        return out;
    }

    public static int maxEmployed() {
        return maxEmployed;
    }

    public static long cooldownRemainingMillis() {
        return Math.max(0L, cooldownUntilMillis - System.currentTimeMillis());
    }

    public static boolean onCooldown() {
        return cooldownRemainingMillis() > 0;
    }

    // --- Primary job accessors (first employed job) for the HUD ---

    public static String primaryJobId() {
        return employed.isEmpty() ? "" : employed.get(0);
    }

    public static JobInfo primaryJob() {
        String id = primaryJobId();
        return id.isEmpty() ? null : JOBS.get(id);
    }

    public static String jobId() { return primaryJobId(); }

    public static boolean hasJob() { return !employed.isEmpty(); }

    public static int level() {
        JobInfo info = primaryJob();
        return info == null ? 0 : info.level();
    }

    public static double xp() {
        JobInfo info = primaryJob();
        return info == null ? 0.0 : info.xp();
    }

    public static double xpPerLevel() {
        JobInfo info = primaryJob();
        return info == null || info.nextCost() <= 0 ? xpPerLevel : info.nextCost();
    }

    public static float xpProgress() {
        JobInfo info = primaryJob();
        if (info == null || info.nextCost() <= 0) {
            return 1f;
        }
        return (float) Math.max(0.0, Math.min(1.0, info.xp() / info.nextCost()));
    }
}
