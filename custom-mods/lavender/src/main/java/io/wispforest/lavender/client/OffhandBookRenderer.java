package io.wispforest.lavender.client;

import com.google.common.base.Suppliers;
import io.wispforest.lavender.Lavender;
import io.wispforest.lavender.book.Book;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * v1: offhand book live preview is disabled — 26.2 removed the old
 * RenderTarget(int,int,boolean) ctor, getBufferBuilders, and ShaderProgramKey blit path.
 */
public class OffhandBookRenderer {

    public static boolean rendering = false;

    private static LavenderBookScreen cachedScreen = null;
    private static boolean cacheExpired = true;

    public static void initialize() {
        // framebuffer-to-item path not ported for 26.2 v1
    }

    public static void beginFrame(@Nullable Book book) {
        cacheExpired = true;
    }

    public static void render(PoseStack matrices, int light) {
        cacheExpired = false;
        // no-op: offhand preview disabled
    }

    public static void endFrame() {
        if (cacheExpired) cachedScreen = null;
    }
}
