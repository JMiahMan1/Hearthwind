package net.satisfy.brewery.core.block.property;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum BrewMaterial implements StringRepresentable {
    WOOD("wood", 1),
    COPPER("copper", 2),
    NETHERITE("netherite", 3);

    private final String name;
    private final int level;

    BrewMaterial(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
