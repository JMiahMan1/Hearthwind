package net.satisfy.nethervinery.core.util;

import net.minecraft.resources.Identifier;
import net.satisfy.nethervinery.core.NetherVinery;

// 26.2: Identifier is final (ResourceLocation is gone), so this can no longer
// be an Identifier subclass. Mirrors Vinery.identifier(): a static factory.
public final class NetherVineryIdentifier {
    private NetherVineryIdentifier() {
    }

    public static Identifier of(String path) {
        return Identifier.fromNamespaceAndPath(NetherVinery.MODID, path);
    }
}
