package dev.jmiahman.hearthwind.jobs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearthwindJobs implements ModInitializer {
    public static final String MOD_ID = "hearthwind_jobs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        HearthwindJobsConfig.get();
        JobDefs.ensureLoaded();
        JobGates.ensureLoaded();
        JobEvents.register();
        JobCommands.register();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            JobCorpus.load(server.getResourceManager());
            for (String line : JobCorpus.summary()) {
                LOGGER.info(line);
            }
        });
        LOGGER.info("Hearthwind Jobs initialized: {} jobs (config {} pts/level), commands /job join|leave|info|age",
                JobDefs.all().size(), HearthwindJobsConfig.get().pointsPerLevel);
    }
}
