package dev.jmiahman.hearthwind.world;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearthwindWorld implements ModInitializer {
	public static final String MOD_ID = "hearthwind_world";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hearthwind World initialized");
	}
}
