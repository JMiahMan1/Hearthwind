package dev.jmiahman.hearthwind.survival;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearthwindSurvival implements ModInitializer {
	public static final String MOD_ID = "hearthwind_survival";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HearthwindSurvivalConfig.get(); // materialize config/hearthwind_survival.json early
		PayloadTypeRegistry.clientboundPlay().register(ThirstSyncPayload.TYPE, ThirstSyncPayload.CODEC);
		ThirstMobEffect.register();
		HearthwindSurvivalThirst.registerTickLoop();
		HearthwindSurvivalDiet.registerTickLoop();
		HearthwindSurvivalSpoilage.registerTickLoop();
		DehydrationItems.registerAll(msg -> LOGGER.info(msg));
		EnvironmentzItems.registerAll(msg -> LOGGER.info(msg));
		HearthwindSurvivalTemperature.registerTickLoop();
		HearthwindSurvivalLoot.init();
		LOGGER.info("Hearthwind Survival initialized: thirst + diet + spoilage + temperature systems active");
	}
}
