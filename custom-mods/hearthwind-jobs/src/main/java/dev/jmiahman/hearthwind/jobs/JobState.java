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
                    .persistent(Codec.recordCodec(JobState::codec)
                            .build())
                    .copyOnDeath()
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("hearthwind_jobs", "state"));

    private JobState() {}

    private static com.mojang.serialization.codecs.RecordCodecBuilder
            .Instance<Map<String, String>> unusedForNow() {
        return null;
    }

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
        Data d = entity.getAttachedOrDefault(STATE, NONE);
        return d.job();
    }

    public static JobDefs.JobDef job(Entity entity) {
        return JobDefs.byId(jobId(entity));
    }

    public static double xp(Entity entity) {
        Data d = entity.getAttachedOrDefault(STATE, NONE);
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
        player.setAttached(STATE, new Data(id, 0.0));
        player.sendSystemMessage(Component.literal(
                "Joined the " + id + " job. Good luck out there."));
        return true;
    }

    public static void leave(ServerPlayer player) {
        player.setAttached(STATE, NONE);
        player.sendSystemMessage(Component.literal("You are now unemployed."));
    }

    /** Award a point if {@code id} matches the current job's current level. */
    public static void awardIfMatch(Entity entity, String id) {
        Data d = entity.getAttachedOrDefault(STATE, NONE);
        if (d.job().isEmpty()) {
            return;
        }
        JobDefs.JobDef def = JobDefs.byId(d.job());
        if (def == null) {
            return;
        }
        int level = level(entity);
        JobDefs.Level spec = specAtOrBelow(def, level);
        if (spec == null || !matches(spec, id)) {
            return;
        }
        if (level >= def.maxLevel()) {
            return; // maxed jobs stop accruing
        }
        double newXp = d.xp() + HearthwindJobsConfig.get().xpPerAction;
        int before = level;
        entity.setAttached(STATE, new Data(d.job(), newXp));
        int after = level(entity);
        if (entity instanceof ServerPlayer p && after > before) {
            p.sendSystemMessage(Component.literal(
                    "Job level up! You are now " + d.job()
                            + " level " + after + "."));
        }
    }

    private static JobDefs.Level specAtOrBelow(JobDefs.JobDef def, int level) {
        JobDefs.Level best = null;
        for (JobDefs.Level l : def.levels()) {
            if (l.level() <= level + 1 && (best == null || l.level() > best.level())) {
                best = l;
            }
        }
        return best;
    }

    private static boolean matches(JobDefs.Level spec, String id) {
        return spec.entities().contains(id) || spec.blocks().contains(id);
    }
}
