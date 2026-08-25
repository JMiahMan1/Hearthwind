package dev.jmiahman.hearthwind.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
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
        // Thirst sync from server (hearthwind:thirst) -> ClientThirstData for HUD above hunger bar
        try {
            PayloadTypeRegistry.clientboundPlay().register(ThirstSyncPayload.TYPE, ThirstSyncPayload.CODEC);
            ClientPlayNetworking.registerGlobalReceiver(ThirstSyncPayload.TYPE, (payload, context) -> {
                context.client().execute(() -> ClientThirstData.setHydration(payload.hydration()));
            });
            LOGGER.info("Hearthwind Client networking: registered {}", ThirstSyncPayload.TYPE.id());
        } catch (Exception e) {
            LOGGER.warn("Failed to register thirst receiver", e);
        }

        // HUD above hunger bar - vanilla-like, 10 droplets, 8px spacing, same as Thirst Was Taken (MIT)
        try {
            ThirstHud.register();
            LOGGER.info("Hearthwind Client initialized - thirst HUD above hunger bar active (vanilla-like)");
        } catch (Exception e) {
            LOGGER.warn("Failed to register thirst HUD", e);
        }
    }
}
