package dev.jmiahman.aged.world;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgedWorld implements ModInitializer {
	public static final String MOD_ID = "aged_world";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Aged World initialized");
	}
}
