package dev.jmiahman.hearthwind.jobs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Applies bonus-item rewards when a player levels up. Rewards are read
 * from the job's per-level {@code items} array in {@code JobDefs}.
 *
 * <p>Smither and brewer rewards are gated behind Age 2+ (Copper Age);
 * other jobs grant rewards immediately. Creative-mode players receive
 * no rewards (they already have everything).
 */
public final class JobRewards {
    private static final String[] SMITHER_BREWERS = {"smither", "brewer"};

    private JobRewards() {}

    static void apply(ServerPlayer player, String jobId, int newLevel) {
        if (player.getAbilities().instabuild) {
            return;
        }
        if (isAgeGated(jobId) && AgeState.get(player) < 2) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "This reward requires Copper Age (Age 2+)."));
            return;
        }
        JobDefs.JobDef def = JobDefs.byId(jobId);
        if (def == null) {
            return;
        }
        for (JobDefs.Level lv : def.levels) {
            if (lv.level() == newLevel) {
                grantItems(player, lv.items());
                return;
            }
        }
    }

    static boolean isAgeGated(String jobId) {
        for (String s : SMITHER_BREWERS) {
            if (s.equals(jobId)) {
                return true;
            }
        }
        return false;
    }

    private static void grantItems(ServerPlayer player, java.util.List<String> ids) {
        for (String idStr : ids) {
            Identifier id = Identifier.parse(idStr);
            net.minecraft.world.item.Item byName = BuiltInRegistries.ITEM.getValue(id);
            if (byName == null) {
                continue;
            }
            ItemStack stack = new ItemStack(byName);
            if (!stack.isEmpty()) {
                player.getInventory().add(stack);
            }
        }
    }
}
