package snownee.passablefoliage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Upstream {@code PassableFoliageCommonConfig} was a Kiwi {@code @KiwiConfig}
 * (TOML at {@code config/passablefoliage-common.toml}). Kiwi has no 26.2
 * build, so this port keeps the exact same field names, defaults and ranges
 * but persists them house-style as {@code config/passablefoliage.json}
 * (created with defaults on first boot; edit + restart to apply).
 */
public final class PassableFoliageCommonConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "passablefoliage.json";

	// Range 0..1
	public static float fallDamageMultiplier = .5f;
	// Range 5..255
	public static int fallDamageThreshold = 20;
	// Range 0..1
	public static float speedMultiplierHorizontal = .9f;
	// Range 0..1
	public static float speedMultiplierVertical = .9f;

	public static boolean modifyPathFinding = true;

	public static boolean playerOnly = false;

	public static boolean alwaysNotViewBlocking = true;

	public static boolean alwaysLeafWalking = false;

	public static boolean headHitter = false;

	public static boolean soundsPlayerOnly = false;

	// Range 0..10
	public static float soundVolume = 1;

	private PassableFoliageCommonConfig() {}

	/** Gson bean mirroring the static fields (Gson cannot persist statics). */
	@SuppressWarnings("unused")
	private static final class Data {
		float fallDamageMultiplier = .5f;
		int fallDamageThreshold = 20;
		float speedMultiplierHorizontal = .9f;
		float speedMultiplierVertical = .9f;
		boolean modifyPathFinding = true;
		boolean playerOnly = false;
		boolean alwaysNotViewBlocking = true;
		boolean alwaysLeafWalking = false;
		boolean headHitter = false;
		boolean soundsPlayerOnly = false;
		float soundVolume = 1;
	}

	public static void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		Data data = new Data();
		try {
			if (Files.exists(path)) {
				Data read = GSON.fromJson(Files.readString(path), Data.class);
				if (read != null) {
					data = read;
				}
			}
			Files.writeString(path, GSON.toJson(data));
		} catch (IOException e) {
			PassableFoliage.LOGGER.warn("Could not read/write {}: using defaults", path, e);
		}
		fallDamageMultiplier = clamp01(data.fallDamageMultiplier);
		fallDamageThreshold = Math.min(255, Math.max(5, data.fallDamageThreshold));
		speedMultiplierHorizontal = clamp01(data.speedMultiplierHorizontal);
		speedMultiplierVertical = clamp01(data.speedMultiplierVertical);
		modifyPathFinding = data.modifyPathFinding;
		playerOnly = data.playerOnly;
		alwaysNotViewBlocking = data.alwaysNotViewBlocking;
		alwaysLeafWalking = data.alwaysLeafWalking;
		headHitter = data.headHitter;
		soundsPlayerOnly = data.soundsPlayerOnly;
		soundVolume = Math.min(10, Math.max(0, data.soundVolume));
	}

	private static float clamp01(float v) {
		return Math.min(1, Math.max(0, v));
	}
}
