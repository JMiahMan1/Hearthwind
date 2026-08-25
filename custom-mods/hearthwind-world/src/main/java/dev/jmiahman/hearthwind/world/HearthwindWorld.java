package dev.jmiahman.hearthwind.world;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearthwindWorld implements ModInitializer {
	public static final String MOD_ID = "hearthwind_world";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HearthwindWorldConfig.get();
		LOGGER.info("Hearthwind World initialized: seasons-lite {} days/season, crop x[sp {} su {} au {} wi {}] temp offsets [sp {} su {} wi {}]",
				HearthwindWorldConfig.get().daysPerSeason,
				HearthwindWorldConfig.get().springCropMultiplier,
				HearthwindWorldConfig.get().summerCropMultiplier,
				HearthwindWorldConfig.get().autumnCropMultiplier,
				HearthwindWorldConfig.get().winterCropMultiplier,
				HearthwindWorldConfig.get().springTempOffset,
				HearthwindWorldConfig.get().summerTempOffset,
				HearthwindWorldConfig.get().winterTempOffset);
	}

	public static Season currentSeason(net.minecraft.server.level.ServerLevel world) {
		return Season.fromWorldTime(world.getGameTime(), HearthwindWorldConfig.get().daysPerSeason);
	}
}
