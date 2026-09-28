package draylar.inmis.config;

import com.google.gson.annotations.SerializedName;

/**
 * One backpack tier: registry name ({@code <name>_backpack}), row width,
 * number of rows, fire immunity, opening sound id and whether the item can
 * be dyed. Gson reads and writes these fields directly.
 */
public final class BackpackInfo {

    private final String name;
    private final int rowWidth;
    private final int numberOfRows;
    // Upstream's config (and Aged's) spells this `isFireImmune`, so the plain
    // field name would leave every tier non-fireproof when the shipped
    // config/inmis.json is read back.
    @SerializedName("isFireImmune")
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
