package dev.jmiahman.hearthwind.jobs;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearthwindJobs implements ModInitializer {
    public static final String MOD_ID = "hearthwind_jobs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        HearthwindJobsConfig.get();
        JobDefs.ensureLoaded();
        JobEvents.register();
        JobCommands.register();
        LOGGER.info("Hearthwind Jobs initialized: {} jobs (config {} pts/level), commands /job join|leave|info",
                JobDefs.all().size(), HearthwindJobsConfig.get().pointsPerLevel);
    }
}
