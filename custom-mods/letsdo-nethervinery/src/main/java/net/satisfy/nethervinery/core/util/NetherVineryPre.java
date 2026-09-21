package net.satisfy.nethervinery.core.util;

import net.satisfy.nethervinery.core.registry.NetherGrapeTypes;

public class NetherVineryPre {
    public static void preInit() {
        NetherGrapeTypes.register();
    }
}
