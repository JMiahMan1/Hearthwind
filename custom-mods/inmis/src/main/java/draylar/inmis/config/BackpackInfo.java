package draylar.inmis.config;

/**
 * One backpack tier: registry name ({@code <name>_backpack}), row width,
 * number of rows, fire immunity, opening sound id and whether the item can
 * be dyed. Gson reads and writes these fields directly.
 */
public final class BackpackInfo {

    private final String name;
    private final int rowWidth;
    private final int numberOfRows;
    private final boolean fireImmune;
    private String openSound;
    private boolean dyeable;

    public BackpackInfo(String name, int rowWidth, int numberOfRows, boolean fireImmune, String openSound, boolean dyeable) {
        this.name = name;
        this.rowWidth = rowWidth;
        this.numberOfRows = numberOfRows;
        this.fireImmune = fireImmune;
        this.openSound = openSound;
        this.dyeable = dyeable;
    }

    public String getName() {
        return name;
    }

    public int getRowWidth() {
        return rowWidth;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public boolean isFireImmune() {
        return fireImmune;
    }

    public String getOpenSound() {
        return openSound;
    }

    public void setOpenSound(String sound) {
        this.openSound = sound;
    }

    public boolean isDyeable() {
        return dyeable;
    }
}
