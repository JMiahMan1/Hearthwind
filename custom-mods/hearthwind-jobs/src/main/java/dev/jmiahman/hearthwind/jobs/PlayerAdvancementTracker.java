package dev.jmiahman.hearthwind.jobs;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import dev.jmiahman.hearthwind.skills.Skill;
import dev.jmiahman.hearthwind.skills.SkillXp;

/**
 * Bridges the {@code hearthwind:age/ageN} advancement chain to
 * {@link AgeState}. Called from the {@code PlayerAdvancements} mixin when an
 * advancement first completes (the same transition vanilla uses for
 * rewards/toasts), so Ages advance automatically instead of via {@code /job age}.
 *
 * <p>Ages 0-4 are pure datapack milestones (see
 * {@code conversion/datapacks/hearthwind/data/hearthwind/advancement/age/}).
 * Age 5 additionally requires smithing skill 20 and builder job level 3 on
 * top of the age4 parent: vanilla criteria cannot express skill/job levels,
 * so the HEAD of {@code PlayerAdvancements#award} cancels the age5 criterion
 * until {@link #meetsAge5Gates} passes, and {@link #tryCompleteAge5} re-awards
 * it from the job level-up path once the gates open.
 */
public final class PlayerAdvancementTracker {
    /** Fires once when the player first sees the age5 gate hint. */
    public static final String AGE5_HINT_TAG = "hearthwind:age5_gate_hint";

    public static final Identifier AGE0 = ageId(0);
    public static final Identifier AGE1 = ageId(1);
    public static final Identifier AGE2 = ageId(2);
    public static final Identifier AGE3 = ageId(3);
    public static final Identifier AGE4 = ageId(4);
    public static final Identifier AGE5 = ageId(5);

    public static final int AGE5_MIN_SMITHING = 20;
    public static final int AGE5_MIN_BUILDER = 3;

    private PlayerAdvancementTracker() {}

    public static Identifier ageId(int age) {
        return Identifier.fromNamespaceAndPath("hearthwind", "age/age" + age);
    }

    /** Age encoded by the advancement id, or -1 when it is not an Age node. */
    public static int ageFor(Identifier id) {
        if (id == null || !id.getNamespace().equals("hearthwind")) {
            return -1;
        }
        String path = id.getPath();
        if (!path.startsWith("age/age") || path.length() != "age/age".length() + 1) {
            return -1;
        }
        char last = path.charAt(path.length() - 1);
        if (last < '0' || last > '5') {
            return -1;
        }
        return last - '0';
    }

    public static boolean isAgeAdvancement(Identifier id) {
        return ageFor(id) >= 0;
    }

    public static boolean isAge5(Identifier id) {
        return ageFor(id) == 5;
    }

    /**
     * Invoked from the PlayerAdvancements mixin at the exact point vanilla
     * applies completion rewards - i.e. the advancement just became done.
     * AgeState only ever moves forward.
     */
    public static void onAdvancementDone(ServerPlayer player, AdvancementHolder holder) {
        if (player == null || holder == null) {
            return;
        }
        int age = ageFor(holder.id());
        if (age < 0) {
            return;
        }
        if (age > AgeState.get(player)) {
            AgeState.set(player, age);
        }
        if (age == 4) {
            // Someone may already qualify for Mechanical; don't wait for a rail.
            tryCompleteAge5(player);
        }
    }

    /** True when the player satisfies the Age 5 entry gates. */
    public static boolean meetsAge5Gates(ServerPlayer player) {
        return age4Done(player)
                && SkillXp.level(player, Skill.SMITHING) >= AGE5_MIN_SMITHING
                && JobState.level(player, "builder") >= AGE5_MIN_BUILDER;
    }

    static boolean age4Done(ServerPlayer player) {
        var manager = player.level().getServer().getAdvancements();
        AdvancementHolder age4 = manager.get(AGE4);
        return age4 != null && player.getAdvancements().getOrStartProgress(age4).isDone();
    }

    /**
     * Programmatic age5 completion for gate transitions that produce no
     * vanilla trigger (builder leveling up after the rail criterion was
     * deferred). Awards every age5 criterion; the normal award path then
     * runs rewards + {@link #onAdvancementDone}.
     */
    public static void tryCompleteAge5(ServerPlayer player) {
        if (player == null || AgeState.get(player) < 4 || !meetsAge5Gates(player)) {
            return;
        }
        AdvancementHolder age5 = player.level().getServer().getAdvancements().get(AGE5);
        if (age5 == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(age5);
        if (progress.isDone()) {
            if (AgeState.get(player) < 5) {
                AgeState.set(player, 5);
            }
            return;
        }
        for (String criterion : age5.value().criteria().keySet()) {
            player.getAdvancements().award(age5, criterion);
        }
    }

    /** One-shot hint when the age5 rail criterion is blocked by the gates. */
    public static void notifyAge5Gated(ServerPlayer player) {
        if (player.entityTags().contains(AGE5_HINT_TAG)) {
            return;
        }
        player.addTag(AGE5_HINT_TAG);
        player.sendSystemMessage(Component.literal(
                "Age 5 requires smithing " + AGE5_MIN_SMITHING
                        + " and builder job level " + AGE5_MIN_BUILDER + "."));
    }
}
