package com.teamremastered.tlc.config;

/**
 * Lazily reads the stronghold setting.
 *
 * <p>Upstream captured the config in a static final field, which made the
 * value depend on class-load order (the mixin could observe a half
 * initialised config and NPE).  Reading through {@link ConfigOptions#get()}
 * on every call keeps the value correct no matter when it is first asked
 * for; it is a cheap field read after the first load.
 */
public final class ConfigHandler {
    private ConfigHandler() {
    }

    public static boolean isDisableVanillaStronghold() {
        return ConfigOptions.get().isDisableVanillaStronghold();
    }
}
