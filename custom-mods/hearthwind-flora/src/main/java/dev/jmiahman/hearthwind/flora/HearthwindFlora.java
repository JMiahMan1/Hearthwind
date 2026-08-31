package dev.jmiahman.hearthwind.flora;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearthwindFlora implements ModInitializer {
    public static final String MOD_ID = "hearthwind_flora";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        VineryContent.registerAll();
        CandlelightContent.registerAll();
        MeadowContent.registerAll();
        BakeryContent.registerAll();
        HerbalBrewsContent.registerAll();
        FarmAndCharmContent.registerAll();
        BreweryContent.registerAll();
        NetherVineryContent.registerAll();
        FloraWorldGen.init();

        LOGGER.info("Hearthwind Flora initialized: Vinery ({} items, {} blocks), Candlelight ({} items, {} blocks), Meadow ({} items, {} blocks), Bakery ({} items, {} blocks), HerbalBrews ({} items, {} blocks), Farm&Charm ({} items, {} blocks), Brewery ({} items, {} blocks), NetherVinery ({} items, {} blocks)",
                VineryContent.ITEMS.size(), VineryContent.BLOCKS.size(),
                CandlelightContent.ITEMS.size(), CandlelightContent.BLOCKS.size(),
                MeadowContent.ITEMS.size(), MeadowContent.BLOCKS.size(),
                BakeryContent.ITEMS.size(), BakeryContent.BLOCKS.size(),
                HerbalBrewsContent.ITEMS.size(), HerbalBrewsContent.BLOCKS.size(),
                FarmAndCharmContent.ITEMS.size(), FarmAndCharmContent.BLOCKS.size(),
                BreweryContent.ITEMS.size(), BreweryContent.BLOCKS.size(),
                NetherVineryContent.ITEMS.size(), NetherVineryContent.BLOCKS.size());
    }
}
