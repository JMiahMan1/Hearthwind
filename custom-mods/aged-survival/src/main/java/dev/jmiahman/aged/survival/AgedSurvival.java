package dev.jmiahman.aged.survival;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgedSurvival implements ModInitializer {
	public static final String MOD_ID = "aged_survival";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		AgedSurvivalConfig.get(); // materialize config/aged_survival.json early
		ThirstMobEffect.register();
		AgedSurvivalThirst.registerTickLoop();
		AgedSurvivalDiet.registerTickLoop();
		AgedSurvivalSpoilage.registerTickLoop();
		DehydrationItems.registerAll(msg -> LOGGER.info(msg));
		EnvironmentzItems.registerAll(msg -> LOGGER.info(msg));
		AgedSurvivalTemperature.registerTickLoop();
		AgedSurvivalLoot.init();
		LOGGER.info("Aged Survival initialized: thirst + diet + spoilage + temperature systems active");
	}
}
