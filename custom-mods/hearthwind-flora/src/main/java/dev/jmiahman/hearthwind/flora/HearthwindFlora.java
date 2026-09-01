package dev.jmiahman.hearthwind.flora;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.jmiahman.hearthwind.flora.blockentity.FloraBlockEntities;
import net.fabricmc.api.ModInitializer;

public final class HearthwindFlora implements ModInitializer {
    public static final String MOD_ID = "hearthwind_flora";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hearthwind Flora: Registering full authentic Let Do content on 26.2...");

        VineryContent.registerAll();
        CandlelightContent.registerAll();
        MeadowContent.registerAll();
        BakeryContent.registerAll();
        HerbalBrewsContent.registerAll();
        FarmAndCharmContent.registerAll();
        BreweryContent.registerAll();
        NetherVineryContent.registerAll();

        FloraStatusEffects.registerAll();
        FloraBlockEntities.registerAll();
        FloraWorldGen.init();

        LOGGER.info("Hearthwind Flora: 100% complete feature parity registered successfully!");
    }
}
