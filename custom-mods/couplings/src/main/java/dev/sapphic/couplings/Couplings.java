package dev.sapphic.couplings;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Couplings implements ModInitializer {
  public static final int COUPLING_DISTANCE = 64;
  public static final int COUPLING_SIGNAL = 8;

  static final boolean IGNORE_SNEAKING;
  static final boolean COUPLE_DOORS;
  static final boolean COUPLE_FENCE_GATES;
  static final boolean COUPLE_TRAPDOORS;

  static {
    final Path path = net.fabricmc.loader.api.FabricLoader.getInstance()
        .getConfigDir().resolve("couplings.properties");
    final Properties config = new Properties();

    try {
      if (Files.exists(path)) {
        try (var reader = Files.newBufferedReader(path)) {
          config.load(reader);
        }
      }
    } catch (final IOException e) {
      LogManager.getLogger().warn(e.getMessage());
    }

    IGNORE_SNEAKING = Boolean.parseBoolean(
        config.getProperty("ignore_sneaking", "true"));
    COUPLE_DOORS = Boolean.parseBoolean(
        config.getProperty("couple_doors", "true"));
    COUPLE_FENCE_GATES = Boolean.parseBoolean(
        config.getProperty("couple_fence_gates", "true"));
    COUPLE_TRAPDOORS = Boolean.parseBoolean(
        config.getProperty("couple_trapdoors", "true"));

    config.setProperty("ignore_sneaking", Boolean.toString(IGNORE_SNEAKING));
    config.setProperty("couple_doors", Boolean.toString(COUPLE_DOORS));
    config.setProperty("couple_fence_gates", Boolean.toString(COUPLE_FENCE_GATES));
    config.setProperty("couple_trapdoors", Boolean.toString(COUPLE_TRAPDOORS));

    try {
      Files.createDirectories(path.getParent());
      try (var writer = Files.newBufferedWriter(path)) {
        config.store(writer, "Couplings configuration");
      }
    } catch (final IOException e) {
      LogManager.getLogger().warn(e.getMessage());
    }

    if (!COUPLE_DOORS || !COUPLE_FENCE_GATES || !COUPLE_TRAPDOORS) {
      LogManager.getLogger().warn("No features are enabled, this could be a bug!");
    }
  }

  public static boolean ignoresSneaking(final Player player) {
    return IGNORE_SNEAKING;
  }

  public static boolean couplesDoors(final Level level) {
    return COUPLE_DOORS;
  }

  public static boolean couplesFenceGates(final Level level) {
    return COUPLE_FENCE_GATES;
  }

  public static boolean couplesTrapdoors(final Level level) {
    return COUPLE_TRAPDOORS;
  }

  @Override
  public void onInitialize() {
  }
}
