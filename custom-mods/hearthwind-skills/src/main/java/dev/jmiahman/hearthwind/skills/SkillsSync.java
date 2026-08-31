package dev.jmiahman.hearthwind.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.server.level.ServerPlayer;

/**
 * Pushes the complete skill state (all 12 levels) to the owning client.
 * Fired on login and after every level-up so the client panels never show
 * stale/partial data (the client never computes skill state itself).
 */
public final class SkillsSync {

    public static void send(ServerPlayer player) {
        Map<String, Double> xpMap = player.getAttached(SkillXp.XP);
        List<String> ids = new ArrayList<>();
        List<Integer> levels = new ArrayList<>();
        for (Skill skill : Skill.values()) {
            double total = xpMap != null && xpMap.containsKey(skill.id)
                    ? xpMap.get(skill.id) : 0.0;
            ids.add(skill.id);
            levels.add(SkillXp.levelFor(total));
        }
        dev.jmiahman.hearthwind.survival.SkillsSyncPayload payload =
                new dev.jmiahman.hearthwind.survival.SkillsSyncPayload(ids, levels);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload);
    }

    private SkillsSync() {
    }
}
