package dev.jmiahman.hearthwind.skills;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.level.ServerPlayer;

/**
 * Pushes the complete skill state (all 12 levels) to the owning client.
 * Fired on login and after every level-up so the client panels never show
 * stale/partial data (the client never computes skill state itself).
 */
public final class SkillsSync {

    public static void send(ServerPlayer player) {
        // Mock players (gametests) have no network connection; skipping the
        // send is safe because their state is read directly.
        if (player.connection == null) {
            return;
        }
        List<String> ids = new ArrayList<>();
        List<Integer> levels = new ArrayList<>();
        for (Skill skill : Skill.values()) {
            ids.add(skill.id);
            levels.add(SkillXp.level(player, skill));
        }
        dev.jmiahman.hearthwind.survival.SkillsSyncPayload payload =
                new dev.jmiahman.hearthwind.survival.SkillsSyncPayload(ids, levels,
                        SkillXp.overallLevel(player), SkillXp.points(player),
                        (int) SkillXp.totalXp(player));
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload);
    }

    private SkillsSync() {
    }
}
