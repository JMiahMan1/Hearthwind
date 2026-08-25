package dev.jmiahman.hearthwind.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client companion — presentation only, never authoritative.
 *
 * Server owns thirst/diet/temperature/skills/jobs/seasons and enforces gates.
 * Client just renders HUD bars, instruction toasts, and water-motion previews
 * from {@code hearthwind:*} sync payloads. Vanilla clients without this mod
 * get action-bar fallbacks and remain fully playable.
 *
 * Skeleton: HUD/toast wiring lands behind feature flags in Phase C. See
 * docs/PROJECT_DIRECTION.md Distribution model and ideas/rivers-and-waves.md.
 */
public class HearthwindClient implements ClientModInitializer {
    public static final String MOD_ID = "hearthwind_client";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        // TODO Phase C: register ClientPlayNetworking payload handlers
        // hearthwind:hydration, hearthwind:nutrients, hearthwind:skills,
        // hearthwind:season -> HudOverlay + ToastManager.
        // For now skeleton proves client module compiles and loads.
        LOGGER.info("Hearthwind Client initialized — HUD/toast hooks pending (vanilla fallback active)");
    }
}
