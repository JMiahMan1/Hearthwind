package dev.jmiahman.aged.primitive;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgedPrimitive implements ModInitializer {
	public static final String MOD_ID = "aged_primitive";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Aged Primitive initialized");
	}
}
