package dev.jmiahman.hearthwind.client;

import dev.jmiahman.hearthwind.survival.ThirstSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client companion - presentation only, never authoritative.
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
        // Thirst sync from server -> ClientThirstData for HUD above hunger bar.
        // NOTE: the payload Type is registered by hearthwind-survival's common
        // init (which also runs client-side); do NOT register it again here or
        // Fabric throws "already registered" and the receiver never attaches
        // (that caused the "Unknown custom packet payload" spam).
        try {
            ClientPlayNetworking.registerGlobalReceiver(ThirstSyncPayload.TYPE, (payload, context) -> {
                context.client().execute(() -> ClientThirstData.setHydration(payload.hydration()));
            });
            LOGGER.info("Hearthwind Client networking: receiver for {}", ThirstSyncPayload.TYPE.id());
        } catch (Exception e) {
            LOGGER.warn("Failed to register thirst receiver", e);
        }

        // HUD above hunger bar - vanilla-like, 10 droplets, 8px spacing
        try {
            ThirstHud.register();
            LOGGER.info("Hearthwind Client initialized - thirst HUD above hunger bar active (vanilla-like)");
        } catch (Exception e) {
            LOGGER.warn("Failed to register thirst HUD", e);
        }
    }
}
