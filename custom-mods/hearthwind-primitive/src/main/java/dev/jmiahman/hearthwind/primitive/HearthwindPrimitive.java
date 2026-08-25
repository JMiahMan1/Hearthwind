package dev.jmiahman.hearthwind.primitive;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearthwindPrimitive implements ModInitializer {
	public static final String MOD_ID = "hearthwind_primitive";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HearthwindPrimitiveItems.init();
		HearthwindPrimitiveLoot.init();
	}
}
