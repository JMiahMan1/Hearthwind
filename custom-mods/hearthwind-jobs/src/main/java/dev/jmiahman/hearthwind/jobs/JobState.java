package dev.jmiahman.hearthwind.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Per-player job state: employed job list (up to
 * {@link HearthwindJobsConfig#employedJobs}), per-job total XP and the
 * job-change cooldown end timestamp.
 *
 * <p>Leveling follows the Aged jobs-addon curve: the cost to go from level L
 * to L+1 is {@code (int)(jobXPBaseCost + jobXPCostMultiplicator * L^jobXPExponent)}
 * (100, 101, 103 ... with the Aged config), capped by the job's corpus ladder
 * and {@code jobMaxLevel}. XP is kept per job even while unemployed, exactly
 * like the reference mod; withdrawing from a job only starts the cooldown.
 */
public final class JobState {

    /**
     * @param job        primary (first employed) job id, "" when unemployed
     * @param xp         primary job's total XP (legacy single-job field)
     * @param employed   employed job ids in join order
     * @param xpByJob    total XP per job id (kept after leaving)
     * @param cooldownEnd absolute game time when the change cooldown expires
     */
    public record Data(String job, double xp, List<String> employed, Map<String, Double> xpByJob,
            long cooldownEnd) {

        /** Legacy two-arg shape kept for old saves, tests and HUD callers. */
        public Data(String job, double xp) {
            this(job, xp, job == null || job.isEmpty() ? List.of() : List.of(job),
                    job == null || job.isEmpty() ? Map.of() : Map.of(job, xp), 0L);
        }

        public Data {
            if (job == null) {
                job = "";
            }
            List<String> employedCopy = employed == null ? new ArrayList<>() : new ArrayList<>(employed);
            Map<String, Double> xpCopy = xpByJob == null ? new HashMap<>() : new HashMap<>(xpByJob);
            if (!job.isEmpty() && !xpCopy.containsKey(job)) {
                xpCopy.put(job, xp);
            }
            if (!job.isEmpty() && !employedCopy.contains(job)) {
                employedCopy.add(job);
            }
            if (job.isEmpty() && !employedCopy.isEmpty()) {
                job = employedCopy.get(0);
            }
            if (!job.isEmpty()) {
                xp = xpCopy.getOrDefault(job, xp);
            }
            employed = employedCopy;
            xpByJob = xpCopy;
        }
    }

    private static final Data NONE = new Data("", 0.0);

    public static final AttachmentType<Data> STATE =
            AttachmentRegistry.<Data>builder()
                    .persistent(codec())
                    .copyOnDeath()
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("hearthwind_jobs", "state"));

    private JobState() {}

    static Codec<Data> codec() {
        return com.mojang.serialization.codecs.RecordCodecBuilder.create(i ->
                i.group(
                        Codec.STRING.optionalFieldOf("job", "").forGetter(Data::job),
                        Codec.DOUBLE.optionalFieldOf("xp", 0.0).forGetter(Data::xp),
                        Codec.STRING.listOf().optionalFieldOf("employed", List.<String>of())
                                .forGetter(Data::employed),
                        Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                                .optionalFieldOf("xp_by_job", Map.<String, Double>of())
                                .forGetter(Data::xpByJob),
                        Codec.LONG.optionalFieldOf("cooldown_end", 0L)
                                .forGetter(Data::cooldownEnd))
                        .apply(i, Data::new));
    }

    private static Data data(Entity entity) {
        return entity.getAttachedOrElse(STATE, NONE);
    }

    public static List<String> employedJobs(Entity entity) {
        return data(entity).employed();
    }

    public static boolean employed(Entity entity, String jobId) {
        return jobId != null && data(entity).employed().contains(jobId);
    }

    /** Primary job id (first employed), "" when unemployed. */
    public static String jobId(Entity entity) {
        return data(entity).job();
    }

    /** Primary job's total XP (legacy accessor). */
    public static double xp(Entity entity) {
        return data(entity).xp();
    }

    /** Total XP for any job id (kept after leaving a job). */
    public static double xp(Entity entity, String jobId) {
        if (jobId == null || jobId.isEmpty()) {
            return 0.0;
        }
        return data(entity).xpByJob().getOrDefault(jobId, 0.0);
    }

    /** XP accumulated inside the current level (JobsAddon "jobXP"). */
    public static double xpInLevel(Entity entity, String jobId) {
        int level = level(entity, jobId);
        return Math.max(0.0, xp(entity, jobId) - xpForLevel(jobId, level));
    }

    /** Cost of the next level from the current one, 0 when capped. */
    public static double nextCost(Entity entity, String jobId) {
        JobDefs.JobDef def = JobDefs.byId(jobId);
        if (def == null) {
            return 0.0;
        }
        int level = level(entity, jobId);
        return level >= capLevel(def) ? 0.0 : nextCost(level);
    }

    public static int level(Entity entity) {
        return level(entity, data(entity).job());
    }

    public static int level(Entity entity, String jobId) {
        JobDefs.JobDef def = JobDefs.byId(jobId);
        if (def == null) {
            return 0;
        }
        return Math.min(capLevel(def), levelFor(jobId, xp(entity, jobId)));
    }

    /** Aged curve: 100, 101, 103, ... cost to leave {@code level}. */
    public static double nextCost(int level) {
        HearthwindJobsConfig cfg = HearthwindJobsConfig.get();
        return (int) (cfg.jobXPBaseCost
                + cfg.jobXPCostMultiplicator * Math.pow(level, cfg.jobXPExponent));
    }

    /** Cumulative XP needed to reach {@code level}. */
    public static double xpForLevel(String jobId, int level) {
        double total = 0.0;
        for (int l = 0; l < level; l++) {
            total += nextCost(l);
        }
        return total;
    }

    private static int levelFor(String jobId, double totalXp) {
        JobDefs.JobDef def = JobDefs.byId(jobId);
        if (def == null) {
            return 0;
        }
        int cap = capLevel(def);
        double accumulated = 0.0;
        int level = 0;
        while (level < cap) {
            double cost = nextCost(level);
            if (totalXp < accumulated + cost) {
                break;
            }
            accumulated += cost;
            level++;
        }
        return level;
    }

    /** Job's ladder cap, additionally limited by the Aged jobMaxLevel. */
    public static int capLevel(JobDefs.JobDef def) {
        int agedCap = HearthwindJobsConfig.get().jobMaxLevel;
        int cap = def.maxLevel();
        if (agedCap > 0 && agedCap < cap) {
            cap = agedCap;
        }
        return cap;
    }

    /** Remaining change cooldown in ticks for the sync/footer line. */
    public static int cooldownTicks(Entity entity) {
        long now = entity.level().getGameTime();
        long end = data(entity).cooldownEnd();
        return (int) Math.max(0L, end - now);
    }

    public static boolean join(ServerPlayer player, String id) {
        JobDefs.JobDef def = JobDefs.byId(id);
        if (def == null) {
            return false;
        }
        if ((id.equals("smither") || id.equals("brewer")) && AgeState.get(player) < 2) {
            player.sendSystemMessage(Component.literal(
                    "The " + id + " job unlocks at Copper Age (Age 2)."));
            return false;
        }
        Data d = data(player);
        HearthwindJobsConfig cfg = HearthwindJobsConfig.get();
        if (d.employed().contains(id)) {
            player.sendSystemMessage(Component.literal("You already work as a " + id + "."));
            return false;
        }
        if (cooldownTicks(player) > 0) {
            player.sendSystemMessage(Component.literal(
                    "You recently changed jobs. Wait " + (cooldownTicks(player) / 20)
                            + "s before changing again."));
            return false;
        }
        if (d.employed().size() >= Math.max(1, cfg.employedJobs)) {
            player.sendSystemMessage(Component.literal(
                    "You can only hold " + cfg.employedJobs + " jobs at once."));
            return false;
        }
        List<String> employed = new ArrayList<>(d.employed());
        employed.add(id);
        Map<String, Double> xpByJob = new HashMap<>(d.xpByJob());
        xpByJob.putIfAbsent(id, 0.0);
        long cooldownEnd = player.level().getGameTime() + Math.max(0, cfg.jobChangeTime);
        player.setAttached(STATE,
                new Data(id, xpByJob.getOrDefault(id, 0.0), employed, xpByJob, cooldownEnd));
        // Aged parity: selecting a job is silent (screen updates + JobsSync
        // packet); no "Joined the X job..." chat/title dialogue.
        return true;
    }

    /** Leaves one job (XP for that job is kept). */
    public static boolean leave(ServerPlayer player, String id) {
        Data d = data(player);
        if (!d.employed().contains(id)) {
            player.sendSystemMessage(Component.literal("You do not work as a " + id + "."));
            return false;
        }
        List<String> employed = new ArrayList<>(d.employed());
        employed.remove(id);
        long cooldownEnd = player.level().getGameTime()
                + Math.max(0, HearthwindJobsConfig.get().jobChangeTime);
        String primary = employed.isEmpty() ? "" : employed.get(0);
        player.setAttached(STATE, new Data(primary, d.xpByJob().getOrDefault(primary, 0.0),
                employed, d.xpByJob(), cooldownEnd));
        // Aged parity: leaving a job is silent (screen packet covers it)
        return true;
    }

    /** Legacy no-arg leave: withdraws from every job. */
    public static void leave(ServerPlayer player) {
        Data d = data(player);
        long cooldownEnd = d.employed().isEmpty() ? d.cooldownEnd()
                : player.level().getGameTime() + Math.max(0, HearthwindJobsConfig.get().jobChangeTime);
        player.setAttached(STATE, new Data("", 0.0, List.of(), d.xpByJob(), cooldownEnd));
        // Aged parity: bulk leave is silent
    }

    /** Awards XP to every employed job whose ladder lists {@code id}. */
    public static void awardIfMatch(Entity entity, String id) {
        Data d = data(entity);
        if (d.employed().isEmpty()) {
            return;
        }
        Map<String, Double> xpByJob = new HashMap<>(d.xpByJob());
        boolean changed = false;
        for (String jobId : d.employed()) {
            JobDefs.JobDef def = JobDefs.byId(jobId);
            if (def == null) {
                continue;
            }
            double current = xpByJob.getOrDefault(jobId, 0.0);
            int before = levelFor(jobId, current);
            if (before >= capLevel(def)) {
                continue; // maxed jobs stop accruing
            }
            if (!matchesAny(def, id)) {
                continue;
            }
            // Reward tiers: content listed in the corpus pays its unlock level
            // (iron ore as a miner pays 7, diamond 20); anything outside the
            // ladder pays the flat fallback.
            int tier = JobCorpus.levelFor(jobId, id);
            double amount = tier > 0 ? tier : HearthwindJobsConfig.get().xpPerAction;
            double newXp = current + amount;
            xpByJob.put(jobId, newXp);
            changed = true;
            int after = levelFor(jobId, newXp);
            if (entity instanceof ServerPlayer p && after > before) {
                p.sendSystemMessage(Component.literal(
                        "Job level up! You are now " + jobId + " level " + after + "."));
                JobRewards.apply(p, jobId, after);
            }
        }
        if (changed) {
            entity.setAttached(STATE, new Data(d.job(), d.xp(), d.employed(), xpByJob, d.cooldownEnd()));
        }
    }

    private static boolean matchesAny(JobDefs.JobDef def, String id) {
        for (JobDefs.Level l : def.levels) {
            if (l.entities().contains(id) || l.blocks().contains(id) || l.items().contains(id)) {
                return true;
            }
        }
        return false;
    }
}
