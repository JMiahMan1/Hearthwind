package net.satisfy.brewery.core.block.property;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Heat implements StringRepresentable {
    OFF("off"),
    WEAK("weak"),
    LIT("lit");

    private final String name;

    Heat(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
