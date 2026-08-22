package dev.jmiahman.aged.skills;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgedSkills implements ModInitializer {
	public static final String MOD_ID = "aged_skills";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Aged Skills initialized");
	}
}
