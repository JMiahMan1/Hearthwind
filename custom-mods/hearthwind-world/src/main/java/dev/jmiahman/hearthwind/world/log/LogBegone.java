package dev.jmiahman.hearthwind.world.log;

import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 26.2 Modern port of Log Begone:
 * Suppresses repetitive console spam, phantom warnings, and non-fatal tag noise.
 */
public final class LogBegone {
    private static final List<String> SUPPRESSED_PATTERNS = List.of(
            "Couldn't parse loot table",
            "Ignoring entity with duplicate UUID",
            "Mismatched structure template",
            "Unable to parse recipe",
            "Potentially dangerous alternative prefix",
            "Received passengers for unknown entity"
    );

    private LogBegone() {}

    public static void register() {
        try {
            Logger rootLogger = Logger.getLogger("");
            Filter existing = rootLogger.getFilter();
            rootLogger.setFilter(record -> {
                if (record != null && shouldSuppress(record.getMessage())) {
                    return false;
                }
                return existing == null || existing.isLoggable(record);
            });
        } catch (Exception ignored) {}
    }

    public static boolean shouldSuppress(String message) {
        if (message == null) return false;
        for (String pattern : SUPPRESSED_PATTERNS) {
            if (message.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
