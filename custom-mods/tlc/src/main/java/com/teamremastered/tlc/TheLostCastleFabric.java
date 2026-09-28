package com.teamremastered.tlc;

import net.fabricmc.api.ModInitializer;

public class TheLostCastleFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TheLostCastleCommon.registerRegistries();
        TheLostCastleCommon.init();
    }
}
