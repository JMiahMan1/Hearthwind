package brightspark.asynclocator;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Flattened properties config (replaces upstream SparkConfig reflection).
 */
public final class ALConfig {
	public static int LOCATOR_THREADS = 1;
	public static boolean REMOVE_OFFER = false;
	public static boolean DOLPHIN_TREASURE_ENABLED = true;
	public static boolean EYE_OF_ENDER_ENABLED = true;
	public static boolean EXPLORATION_MAP_ENABLED = true;
	public static boolean LOCATE_COMMAND_ENABLED = true;
	public static boolean VILLAGER_TRADE_ENABLED = true;

	private ALConfig() {}

	public static int locatorThreads() {
		return Math.max(1, LOCATOR_THREADS);
	}

	public static boolean removeOffer() {
		return REMOVE_OFFER;
	}

	public static boolean dolphinTreasureEnabled() {
		return DOLPHIN_TREASURE_ENABLED;
	}

	public static boolean eyeOfEnderEnabled() {
		return EYE_OF_ENDER_ENABLED;
	}

	public static boolean explorationMapEnabled() {
		return EXPLORATION_MAP_ENABLED;
	}

	public static boolean locateCommandEnabled() {
		return LOCATE_COMMAND_ENABLED;
	}

	public static boolean villagerTradeEnabled() {
		return VILLAGER_TRADE_ENABLED;
	}

	public static void init() {
		Path configFile = FabricLoader.getInstance().getConfigDir().resolve(ALConstants.MOD_ID + ".properties");
		Properties props = new Properties();
		if (Files.exists(configFile)) {
			ALConstants.logInfo("Config file found");
			try (InputStream in = Files.newInputStream(configFile)) {
				props.load(in);
			} catch (IOException e) {
				ALConstants.logError(e, "Failed to read config file {}", configFile);
			}
		} else {
			ALConstants.logInfo("No config file found - creating it");
		}

		LOCATOR_THREADS = parseInt(props, "asyncLocatorThreads", LOCATOR_THREADS, 1, Integer.MAX_VALUE);
		REMOVE_OFFER = parseBool(props, "removeMerchantInvalidMapOffer", REMOVE_OFFER);
		DOLPHIN_TREASURE_ENABLED = parseBool(props, "dolphinTreasureEnabled", DOLPHIN_TREASURE_ENABLED);
		EYE_OF_ENDER_ENABLED = parseBool(props, "eyeOfEnderEnabled", EYE_OF_ENDER_ENABLED);
		EXPLORATION_MAP_ENABLED = parseBool(props, "explorationMspEnabled", EXPLORATION_MAP_ENABLED);
		LOCATE_COMMAND_ENABLED = parseBool(props, "locateCommandEnabled", LOCATE_COMMAND_ENABLED);
		VILLAGER_TRADE_ENABLED = parseBool(props, "villagerTradeEnabled", VILLAGER_TRADE_ENABLED);

		if (!Files.exists(configFile)) {
			Properties out = new Properties();
			out.setProperty("asyncLocatorThreads", Integer.toString(LOCATOR_THREADS));
			out.setProperty("removeMerchantInvalidMapOffer", Boolean.toString(REMOVE_OFFER));
			out.setProperty("dolphinTreasureEnabled", Boolean.toString(DOLPHIN_TREASURE_ENABLED));
			out.setProperty("eyeOfEnderEnabled", Boolean.toString(EYE_OF_ENDER_ENABLED));
			out.setProperty("explorationMspEnabled", Boolean.toString(EXPLORATION_MAP_ENABLED));
			out.setProperty("locateCommandEnabled", Boolean.toString(LOCATE_COMMAND_ENABLED));
			out.setProperty("villagerTradeEnabled", Boolean.toString(VILLAGER_TRADE_ENABLED));
			try {
				Files.createDirectories(configFile.getParent());
				try (OutputStream os = Files.newOutputStream(configFile)) {
					out.store(os, "Async Locator config");
				}
			} catch (IOException e) {
				ALConstants.logError(e, "Failed to write config file {}", configFile);
			}
		}

		printConfigs();
	}

	private static int parseInt(Properties props, String key, int def, int min, int max) {
		String raw = props.getProperty(key);
		if (raw == null || raw.isBlank()) return def;
		try {
			return Math.max(min, Math.min(max, Integer.parseInt(raw.trim())));
		} catch (NumberFormatException e) {
			ALConstants.logWarn("Invalid integer for {} - using default {}", key, def);
			return def;
		}
	}

	private static boolean parseBool(Properties props, String key, boolean def) {
		String raw = props.getProperty(key);
		if (raw == null || raw.isBlank()) return def;
		return Boolean.parseBoolean(raw.trim());
	}

	private static void printConfigs() {
		ALConstants.logInfo("Configs:" +
			"\nLocator Threads: " + LOCATOR_THREADS +
			"\nRemove Offer: " + REMOVE_OFFER +
			"\nDolphin Treasure Enabled: " + DOLPHIN_TREASURE_ENABLED +
			"\nEye Of Ender Enabled: " + EYE_OF_ENDER_ENABLED +
			"\nExploration Map Enabled: " + EXPLORATION_MAP_ENABLED +
			"\nLocate Command Enabled: " + LOCATE_COMMAND_ENABLED +
			"\nVillager Trade Enabled: " + VILLAGER_TRADE_ENABLED
		);
	}
}
