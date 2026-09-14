package com.teamresourceful.resourcefullib.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.Objects;

public class RenderUtils {
    public static AutoCloseable createScissorBox(Minecraft mc, GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        return () -> {};
    }
}
