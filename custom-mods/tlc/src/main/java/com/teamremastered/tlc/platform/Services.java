package com.teamremastered.tlc.platform;

import com.teamremastered.tlc.Constants;
import com.teamremastered.tlc.platform.services.IConfigHelper;
import com.teamremastered.tlc.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public final class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IConfigHelper CONFIG_HELPER = load(IConfigHelper.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        T loadedService = ServiceLoader.load(clazz).findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
