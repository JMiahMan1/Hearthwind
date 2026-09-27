package net.dungeonz.compat;

import java.lang.reflect.Method;
import java.util.List;

import net.dungeonz.DungeonzMain;
import net.minecraft.server.level.ServerPlayer;

public final class HearthwindLevels {
    public static final List<String> SKILLS = List.of("farming", "mining", "smithing", "strength", "agility", "defense",
            "health", "stamina", "luck", "archery", "alchemy", "trade");

    private HearthwindLevels() {}

    public static boolean meetsRequiredLevel(ServerPlayer player, int requiredLevel) {
        return !DungeonzMain.isLevelZLoaded || overallLevel(player) >= requiredLevel;
    }

    public static int overallLevel(ServerPlayer player) {
        if (!DungeonzMain.isLevelZLoaded) {
            return 0;
        }
        try {
            if (!DungeonzMain.isHearthwindSkillsLoaded) {
                Object manager = player.getClass().getMethod("getLevelManager").invoke(player);
                return ((Number) manager.getClass().getMethod("getOverallLevel").invoke(manager)).intValue();
            }
            return ((Number) SkillsApi.OVERALL_LEVEL.invoke(null, player)).intValue();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot read dungeon admission level", exception);
        }
    }

    private static final class SkillsApi {
        private static final Method OVERALL_LEVEL;

        static {
            try {
                Class<?> api = Class.forName("dev.jmiahman.hearthwind.skills.SkillXp");
                OVERALL_LEVEL = api.getMethod("overallLevel", net.minecraft.world.entity.Entity.class);
            } catch (ReflectiveOperationException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }
    }
}
