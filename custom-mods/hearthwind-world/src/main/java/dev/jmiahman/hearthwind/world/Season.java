package dev.jmiahman.hearthwind.world;

public enum Season {
    SPRING, SUMMER, AUTUMN, WINTER;

    public static Season fromDay(long day, int daysPerSeason) {
        long cycle = Math.floorMod(day / daysPerSeason, 4);
        return switch ((int) cycle) {
            case 0 -> SPRING;
            case 1 -> SUMMER;
            case 2 -> AUTUMN;
            default -> WINTER;
        };
    }

    public static Season fromWorldTime(long gameTime, int daysPerSeason) {
        long day = gameTime / 24000L;
        return fromDay(day, daysPerSeason);
    }

    public double tempOffset(HearthwindWorldConfig cfg) {
        return switch (this) {
            case SPRING -> cfg.springTempOffset;
            case SUMMER -> cfg.summerTempOffset;
            case AUTUMN -> cfg.autumnTempOffset;
            case WINTER -> cfg.winterTempOffset;
        };
    }

    public double cropMultiplier(HearthwindWorldConfig cfg) {
        return switch (this) {
            case SPRING -> cfg.springCropMultiplier;
            case SUMMER -> cfg.summerCropMultiplier;
            case AUTUMN -> cfg.autumnCropMultiplier;
            case WINTER -> cfg.winterCropMultiplier;
        };
    }
}
