package io.wispforest.lavendermd.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TextureSizeLookup {

    private static final Map<Identifier, Size> TEXTURE_SIZES = new Object2ObjectOpenHashMap<>();

    public static @Nullable Size sizeOf(Identifier texture) {
        return TEXTURE_SIZES.get(texture);
    }

    @ApiStatus.Internal
    public static void _registerTextureSize(int textureId, int width, int height) {
        // v1 26.2: vanilla texture-id registration path removed; sizes registered by Identifier only
    }

    @ApiStatus.Internal
    public static void _registerTextureSize(Identifier texture, int width, int height) {
        TEXTURE_SIZES.put(texture, new Size(width, height));
    }

    public record Size(int width, int height) {}
}
