package dev.jmiahman.hearthwind.world;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearthwindWorld implements ModInitializer {
	public static final String MOD_ID = "hearthwind_world";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** Last payload broadcast, so we only send on day/season rollover. */
	private static SeasonSyncPayload lastBroadcast = null;

	@Override
	public void onInitialize() {
		HearthwindWorldConfig.get();
		ServerLifecycleEvents.SERVER_STARTING.register(server -> SeasonCrops.load(server.getResourceManager()));
		LOGGER.info("Hearthwind World initialized: seasons-lite {} days/season, crop x[sp {} su {} au {} wi {}] temp offsets [sp {} su {} wi {}]",
				HearthwindWorldConfig.get().daysPerSeason,
				HearthwindWorldConfig.get().springCropMultiplier,
				HearthwindWorldConfig.get().summerCropMultiplier,
				HearthwindWorldConfig.get().autumnCropMultiplier,
				HearthwindWorldConfig.get().winterCropMultiplier,
				HearthwindWorldConfig.get().springTempOffset,
				HearthwindWorldConfig.get().summerTempOffset,
				HearthwindWorldConfig.get().winterTempOffset);

		PayloadTypeRegistry.clientboundPlay().register(
				SeasonSyncPayload.TYPE, SeasonSyncPayload.CODEC);
		dev.jmiahman.hearthwind.world.herd.HerdPanic.register();
		dev.jmiahman.hearthwind.world.couplings.Couplings.register();
		dev.jmiahman.hearthwind.world.villager.VillagerLeashHandler.register();
		dev.jmiahman.hearthwind.world.log.LogBegone.register();
		dev.jmiahman.hearthwind.world.water.WaterMotion.register();
		dev.jmiahman.hearthwind.world.snow.WinterSnowAccumulation.register();
		dev.jmiahman.hearthwind.world.fauna.WaterfowlFauna.register();
		dev.jmiahman.hearthwind.world.fauna.NaturalistFauna.registerAll();
		dev.jmiahman.hearthwind.world.exploration.ExplorationItems.registerAll();
		dev.jmiahman.hearthwind.world.endrem.EndRemasteredItems.registerAll(msg -> LOGGER.info(msg));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			SeasonSyncPayload payload = SeasonSyncPayload.ofGameTime(
					server.overworld().getGameTime(),
					HearthwindWorldConfig.get().daysPerSeason);
			ServerPlayNetworking.send(handler.getPlayer(), payload);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 100 != 0) {
				return;
			}
			SeasonSyncPayload payload = SeasonSyncPayload.ofGameTime(
					server.overworld().getGameTime(),
					HearthwindWorldConfig.get().daysPerSeason);
			if (lastBroadcast != null
					&& lastBroadcast.seasonOrdinal() == payload.seasonOrdinal()
					&& lastBroadcast.dayOfSeason() == payload.dayOfSeason()) {
				return;
			}
			lastBroadcast = payload;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				ServerPlayNetworking.send(player, payload);
			}
		});
	}

	public static Season currentSeason(net.minecraft.server.level.ServerLevel world) {
		return Season.fromWorldTime(world.getGameTime(), HearthwindWorldConfig.get().daysPerSeason);
	}
}
