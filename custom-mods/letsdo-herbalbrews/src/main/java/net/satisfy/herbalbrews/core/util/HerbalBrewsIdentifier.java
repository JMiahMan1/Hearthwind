package net.satisfy.herbalbrews.core.util;

import net.minecraft.resources.Identifier;
import net.satisfy.herbalbrews.HerbalBrews;

public class HerbalBrewsIdentifier {

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(HerbalBrews.MOD_ID, path);
    }
}
