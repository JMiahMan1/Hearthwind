package dev.jmiahman.hearthwind.jobs;

import java.util.Map;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Per-player job state: current job id (null = unemployed) and job XP.
 *
 * Leveling: each matching action grants {@code xpPerAction} points; the
 * requirement for level L is {@code pointsPerLevel * L} cumulative points
 * within the CURRENT level tier (points reset on level-up, matching the
 * corpus' per-level unlock lists).
 */
public final class JobState {
    public record Data(String job, double xp) {}

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
                        Codec.STRING.optionalFieldOf("job", "")
                                .forGetter(Data::job),
                        Codec.DOUBLE.optionalFieldOf("xp", 0.0)
                                .forGetter(Data::xp))
                        .apply(i, Data::new));
    }

    public static String jobId(Entity entity) {
        Data d = entity.getAttachedOrElse(STATE, NONE);
        return d.job();
    }

    public static JobDefs.JobDef job(Entity entity) {
        return JobDefs.byId(jobId(entity));
    }

    public static double xp(Entity entity) {
        Data d = entity.getAttachedOrElse(STATE, NONE);
        return d.xp();
    }

    public static int level(Entity entity) {
        JobDefs.JobDef def = job(entity);
        if (def == null) {
            return 0;
        }
        double need = HearthwindJobsConfig.get().pointsPerLevel;
        return Math.min(def.maxLevel(), (int) (xp(entity) / need));
    }

    public static boolean join(ServerPlayer player, String id) {
        if (JobDefs.byId(id) == null) {
            return false;
        }
        // Smither and brewer are Iron Age jobs — require Age 2+ (Iron/Steel Age)
        if ("smither".equals(id) || "brewer".equals(id)) {
            int age = AgeState.get(player);
            if (age < 2) {
                player.sendSystemMessage(Component.literal(
                        "You must reach the Iron Age (Age 2) before joining the "
                        + id + " job."));
                return false;
            }
        }
        player.setAttached(STATE, new Data(id, 0.0));
        player.sendSystemMessage(Component.literal(
                "Joined the " + id + " job. Good luck out there."));
        return true;
    }

    public static void leave(ServerPlayer player) {
        player.setAttached(STATE, NONE);
        player.sendSystemMessage(Component.literal("You are now unemployed."));
    }

    /** Award a point if {@code id} matches any defined level for the current job. */
    public static void awardIfMatch(Entity entity, String id) {
        Data d = entity.getAttachedOrElse(STATE, NONE);
        if (d.job().isEmpty()) {
            return;
        }
        JobDefs.JobDef def = JobDefs.byId(d.job());
        if (def == null) {
            return;
        }
        int lvl = level(entity);
        if (lvl >= def.maxLevel()) {
            return; // maxed jobs stop accruing
        }
        if (!matchesAny(def, id)) {
            return;
        }
        // Reward tiers: content listed in the corpus pays its unlock level
        // (iron ore as a miner pays 7, diamond 20); anything outside the
        // ladder pays the flat fallback.
        int tier = JobCorpus.levelFor(d.job(), id);
        double amount = tier > 0 ? tier : HearthwindJobsConfig.get().xpPerAction;
        double newXp = d.xp() + amount;
        int before = lvl;
        entity.setAttached(STATE, new Data(d.job(), newXp));
        int after = level(entity);
        if (entity instanceof ServerPlayer p && after > before) {
            p.sendSystemMessage(Component.literal(
                    "Job level up! You are now " + d.job()
                            + " level " + after + "."));
            JobRewards.apply(p, d.job, after);
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
