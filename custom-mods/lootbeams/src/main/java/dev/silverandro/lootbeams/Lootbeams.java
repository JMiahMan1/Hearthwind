package dev.silverandro.lootbeams;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lootbeams port for 26.2. Behaviour reference: Lootbeams 2.1.1+1.20.1
 * (MPL-2.0) by SilverAndro. Aged 3.1.2 ships no config, so upstream
 * defaults apply (white items hidden, one dust particle per drop, beams
 * within 64 blocks, minimum age 12 ticks).
 */
public final class Lootbeams implements ModInitializer {
    public static final String MOD_ID = "lootbeams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static LootbeamsConfig CONFIG;

    @Override
    public void onInitialize() {
        CONFIG = LootbeamsConfig.load(FabricLoader.getInstance().getConfigDir());
    }
}
