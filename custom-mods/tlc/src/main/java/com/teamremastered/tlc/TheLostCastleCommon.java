package com.teamremastered.tlc;

import com.teamremastered.tlc.config.ConfigOptions;
import com.teamremastered.tlc.registries.TLCProcessors;
import com.teamremastered.tlc.registries.TLCStructures;

import java.io.IOException;

public final class TheLostCastleCommon {
    private TheLostCastleCommon() {
    }

    public static void init() {
        try {
            ConfigOptions.create();
            Constants.LOG.info("Lost Castle config loaded successfully");
        } catch (IOException e) {
            Constants.LOG.error("Something went wrong with the config", e);
        }
    }

    public static void registerRegistries() {
        TLCStructures.init();
        TLCProcessors.init();
    }
}
