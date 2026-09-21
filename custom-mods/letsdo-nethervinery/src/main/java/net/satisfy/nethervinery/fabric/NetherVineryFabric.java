package net.satisfy.nethervinery.fabric;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.satisfy.nethervinery.core.NetherVinery;
import net.satisfy.nethervinery.fabric.world.NetherVineryBiomeModification;
import net.satisfy.vinery.fabric.config.VineryFabricConfig;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class NetherVineryFabric implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(NetherVinery.MODID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing NetherVineryFabric...");

        if (!FabricLoader.getInstance().isModLoaded("vinery")) {
            LOGGER.error("Vinery is required for NetherVinery to load! Disabling mod.");
            return;
        }

        try {
            NetherVinery.init();

            // Fabric does not order entrypoints by dependency (nethervinery
            // sorts before vinery), so the Vinery config may not be
            // registered yet - register it ourselves in that case.
            try {
                AutoConfig.getConfigHolder(VineryFabricConfig.class);
            } catch (Exception ignored) {
                AutoConfig.register(VineryFabricConfig.class, GsonConfigSerializer::new);
            }
            VineryFabricConfig config = AutoConfig.getConfigHolder(VineryFabricConfig.class).getConfig();
            LOGGER.info("Vinery configuration loaded successfully: {}", config);
        } catch (Exception e) {
            LOGGER.error("Failed to register NetherVinery registries! Disabling mod.", e);
            return;
        }

        NetherVineryBiomeModification.init();

        LOGGER.info("NetherVineryFabric has been initialized successfully.");
    }
}
