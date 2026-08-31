package dev.jmiahman.hearthwind.client;

/**
 * Client-side copy of season state for the top-left season widget.
 * Updated via hearthwind_world:season payload from the server.
 */
public final class ClientSeasonData {
    private static int seasonOrdinal = 0;
    private static int dayOfSeason = 1;
    private static int daysPerSeason = 21;

    private static final String[] NAMES = {"Spring", "Summer", "Autumn", "Winter"};

    private ClientSeasonData() {}

    public static void set(int ordinal, int day, int perSeason) {
        seasonOrdinal = Math.max(0, Math.min(3, ordinal));
        dayOfSeason = Math.max(1, day);
        daysPerSeason = Math.max(1, perSeason);
    }

    public static int seasonOrdinal() {
        return seasonOrdinal;
    }

    public static String displayText() {
        return NAMES[seasonOrdinal] + ", Day " + dayOfSeason + "/" + daysPerSeason;
    }
}
