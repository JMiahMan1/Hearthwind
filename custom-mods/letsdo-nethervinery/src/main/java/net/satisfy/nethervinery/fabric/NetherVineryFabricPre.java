package net.satisfy.nethervinery.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.satisfy.nethervinery.core.util.NetherVineryPre;

public class NetherVineryFabricPre implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        if (!FabricLoader.getInstance().isModLoaded("vinery")) {
            throw new RuntimeException("Vinery must be loaded before NetherVinery!");
        }

        NetherVineryPre.preInit();
    }
}
