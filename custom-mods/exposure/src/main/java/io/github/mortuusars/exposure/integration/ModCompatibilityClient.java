package io.github.mortuusars.exposure.integration;

public class ModCompatibilityClient {
    public static void handle() {
        // Fabric-only port: RealCamera compat dropped (mod not in the pack;
        // upstream guards it behind isModLoaded("realcamera") the same way).
    }
}
