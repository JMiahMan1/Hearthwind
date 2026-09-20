package net.dungeonz.compat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.dungeonz.DungeonzMain;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
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
            Map<String, Double> xp = player.getAttached(SkillsApi.XP);
            if (xp == null) {
                return 0;
            }
            int total = 0;
            for (String skill : SKILLS) {
                total += ((Number) SkillsApi.LEVEL_FOR.invoke(null, xp.getOrDefault(skill, 0.0))).intValue();
            }
            return Math.min(30, total / SKILLS.size());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot read dungeon admission level", exception);
        }
    }

    private static final class SkillsApi {
        private static final AttachmentType<Map<String, Double>> XP;
        private static final Method LEVEL_FOR;

        static {
            try {
                Class<?> api = Class.forName("dev.jmiahman.hearthwind.skills.SkillXp");
                XP = attachment(api);
                LEVEL_FOR = api.getMethod("levelFor", double.class);
            } catch (ReflectiveOperationException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }

        @SuppressWarnings("unchecked")
        private static AttachmentType<Map<String, Double>> attachment(Class<?> api) throws ReflectiveOperationException {
            return (AttachmentType<Map<String, Double>>) api.getField("XP").get(null);
        }
    }
}
