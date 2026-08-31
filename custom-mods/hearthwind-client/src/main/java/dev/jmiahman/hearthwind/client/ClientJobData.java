package dev.jmiahman.hearthwind.client;

/**
 * Client-side copy of job state for HUD rendering.
 * Updated via hearthwind:job payload from server.
 */
public final class ClientJobData {
    private static String jobId = "";
    private static int level = 0;
    private static double xp = 0.0;
    private static double xpPerLevel = 100.0;

    private ClientJobData() {}

    public static void setJob(String id, int lvl, double exp, double perLevel) {
        jobId = id;
        level = lvl;
        xp = exp;
        xpPerLevel = perLevel;
    }

    public static void clear() {
        jobId = "";
        level = 0;
        xp = 0.0;
    }

    public static String jobId() { return jobId; }
    public static int level() { return level; }
    public static double xp() { return xp; }
    public static double xpPerLevel() { return xpPerLevel; }

    public static boolean hasJob() { return !jobId.isEmpty(); }

    public static float xpProgress() {
        if (xpPerLevel <= 0) return 1f;
        return (float) (xp % xpPerLevel) / (float) xpPerLevel;
    }
}
