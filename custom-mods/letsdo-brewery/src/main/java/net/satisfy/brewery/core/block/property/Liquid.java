package net.satisfy.brewery.core.block.property;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Liquid implements StringRepresentable {
    EMPTY("empty"),
    DRAINED("drained"),
    FILLED("filled"),
    OVERFLOWING("overflowing"),
    BEER("beer");

    private final String name;

    Liquid(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
