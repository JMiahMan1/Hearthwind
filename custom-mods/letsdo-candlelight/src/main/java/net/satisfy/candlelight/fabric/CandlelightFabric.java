package net.satisfy.candlelight.fabric;

import net.fabricmc.api.ModInitializer;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.core.registry.CompostableRegistry;
import net.satisfy.candlelight.fabric.world.CandlelightBiomeModification;

public class CandlelightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Candlelight.init();
        CompostableRegistry.init();
        CandlelightBiomeModification.init();
    }
}
