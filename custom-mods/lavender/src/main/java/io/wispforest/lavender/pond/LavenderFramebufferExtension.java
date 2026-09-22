package io.wispforest.lavender.pond;

public interface LavenderFramebufferExtension {
    void lavender$enableDepthTest();

    static void setDepthTestEnabled(boolean enabled) {
        // no-op on 26.2: custom blit programs are disabled
    }
}
