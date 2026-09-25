package dev.jmiahman.hearthwind.survival;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearthwindSurvival implements ModInitializer {
	public static final String MOD_ID = "hearthwind_survival";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Map<UUID, Integer> lastDietHash = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> lastTempHash = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> lastJobHash = new ConcurrentHashMap<>();
	private static final int SYNC_INTERVAL = 80; // every 4 seconds

	@Override
	public void onInitialize() {
		HearthwindSurvivalConfig.get(); // materialize config/hearthwind_survival.json early
		PayloadTypeRegistry.clientboundPlay().register(ThirstSyncPayload.TYPE, ThirstSyncPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(DietSyncPayload.TYPE, DietSyncPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(NutritionItemMapPayload.TYPE, NutritionItemMapPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(NutritionEffectsPayload.TYPE, NutritionEffectsPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(TempSyncPayload.TYPE, TempSyncPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(JobSyncPayload.TYPE, JobSyncPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(dev.jmiahman.hearthwind.survival.revive.DownedSyncPayload.TYPE, dev.jmiahman.hearthwind.survival.revive.DownedSyncPayload.CODEC);
		ThirstMobEffect.register();
		EnvironmentzEffects.register();
		HearthwindSurvivalThirst.registerTickLoop();
		HearthwindSurvivalDiet.registerTickLoop();
		HearthwindSurvivalSpoilage.registerTickLoop();
		FlaskItems.registerAll(msg -> LOGGER.info(msg));
		PurifiedWater.registerAll(msg -> LOGGER.info(msg));
		EnvironmentzItems.registerAll(msg -> LOGGER.info(msg));
		BareHandDrinkHandler.register();
		CommandRegistrationCallback.EVENT.register((dispatcher, ctx, sel) -> HearthwindDebugCommand.register(dispatcher));
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			EnvironmentCorpus.load(server.getResourceManager());
			for (String line : EnvironmentCorpus.summary()) {
				LOGGER.info(line);
			}
			HydrationCorpus.load(server.getResourceManager());
			for (String line : HydrationCorpus.summary()) {
				LOGGER.info(line);
			}
			HearthwindSurvivalDiet.loadCorpus(server.getResourceManager());
			for (String line : HearthwindSurvivalDiet.summary()) {
				LOGGER.info(line);
			}
			NutritionEffects.load(server.getResourceManager());
		});
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (success) {
				EnvironmentCorpus.load(resourceManager);
				HydrationCorpus.load(resourceManager);
				HearthwindSurvivalDiet.loadCorpus(resourceManager);
				NutritionEffects.load(resourceManager);
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					syncNutritionCorpus(player);
				}
			}
		});
		HearthwindSurvivalTemperature.registerTickLoop();
		HearthwindSurvivalLoot.init();
		dev.jmiahman.hearthwind.survival.revive.ReviveManager.register();
		if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("lavender")) {
			GuideBook.register();
		}
		StarterKit.register();
		registerSyncTick();
		LOGGER.info("Hearthwind Survival initialized: thirst + diet + spoilage + temperature + revive + starter kit systems active");
	}

	static void registerSyncTick() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer p = handler.getPlayer();
			syncDiet(p);
			syncTemp(p);
			syncJob(p);
			syncNutritionCorpus(p);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			UUID id = handler.getPlayer().getUUID();
			lastDietHash.remove(id);
			lastTempHash.remove(id);
			lastJobHash.remove(id);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				dev.jmiahman.hearthwind.survival.revive.ReviveManager.tickPlayer(player);
			}
			if (server.getTickCount() % SYNC_INTERVAL != 0) {
				return;
			}
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				syncDiet(player);
				syncTemp(player);
				syncJob(player);
			}
		});
	}

	private static void syncDiet(ServerPlayer player) {
		try {
			int[] nuts = HearthwindSurvivalDiet.getNutrients(player);
			int hash = java.util.Arrays.hashCode(nuts);
			UUID id = player.getUUID();
			Integer prev = lastDietHash.get(id);
			if (prev == null || prev != hash) {
				lastDietHash.put(id, hash);
				net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
						player, DietSyncPayload.of(nuts));
			}
		} catch (Exception e) {
			// ignore sync failures
		}
	}

	/** Item map + threshold-effect tooltips for the Nutrients panel. */
	private static void syncNutritionCorpus(ServerPlayer player) {
		try {
			net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
					player, new NutritionItemMapPayload(HearthwindSurvivalDiet.itemSnapshot()));
			net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
					player, new NutritionEffectsPayload(
							NutritionEffects.positiveKeys(), NutritionEffects.negativeKeys()));
		} catch (Exception e) {
			// ignore sync failures
		}
	}

	private static void syncTemp(ServerPlayer player) {
		try {
			HearthwindSurvivalTemperature.State state = HearthwindSurvivalTemperature.getState(player);
			int hash = java.util.Objects.hash(state.body(), state.wetness(), state.thermometer());
			UUID id = player.getUUID();
			Integer prev = lastTempHash.get(id);
			if (prev == null || prev != hash) {
				lastTempHash.put(id, hash);
				net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
						player, new TempSyncPayload(state.body(), state.wetness(), state.thermometer()));
			}
		} catch (Exception e) {
			// ignore sync failures
		}
	}

	private static void syncJob(ServerPlayer player) {
		try {
			if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("hearthwind_jobs")) {
				return;
			}
			Class<?> stateCls = Class.forName("dev.jmiahman.hearthwind.jobs.JobState");
			Class<?> configCls = Class.forName("dev.jmiahman.hearthwind.jobs.HearthwindJobsConfig");
			java.lang.reflect.Method jobId = stateCls.getMethod("jobId", net.minecraft.world.entity.Entity.class);
			java.lang.reflect.Method level = stateCls.getMethod("level", net.minecraft.world.entity.Entity.class);
			java.lang.reflect.Method xp = stateCls.getMethod("xp", net.minecraft.world.entity.Entity.class);
			java.lang.reflect.Method getConfig = configCls.getMethod("get");
			Object config = getConfig.invoke(null);
			java.lang.reflect.Field xpPerLevel = config.getClass().getField("pointsPerLevel");
			int xpPerLvl = xpPerLevel.getInt(config);
			String jId = (String) jobId.invoke(null, player);
			int lvl = (int) level.invoke(null, player);
			double xpVal = (double) xp.invoke(null, player);
			int hash = java.util.Objects.hash(jId, lvl, xpVal);
			UUID id = player.getUUID();
			Integer prev = lastJobHash.get(id);
			if (prev == null || prev != hash) {
				lastJobHash.put(id, hash);
				net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
						player, new JobSyncPayload(jId, lvl, xpVal, xpPerLvl));
			}
		} catch (Exception e) {
			// ignore sync failures
		}
	}
}
