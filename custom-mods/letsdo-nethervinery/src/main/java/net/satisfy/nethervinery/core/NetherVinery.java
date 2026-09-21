package net.satisfy.nethervinery.core;

import net.satisfy.nethervinery.core.event.CommonEvents;
import net.satisfy.nethervinery.core.registry.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetherVinery {
    public static final String MODID = "nethervinery";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static void init() {
        NetherObjectRegistry.init();
        NetherEntityTypeRegistry.init();
        NetherTabRegistry.init();
        NetherGrapeTypes.addGrapeAttributes();
        NetherEffects.init();
        CommonEvents.init();
    }
}
