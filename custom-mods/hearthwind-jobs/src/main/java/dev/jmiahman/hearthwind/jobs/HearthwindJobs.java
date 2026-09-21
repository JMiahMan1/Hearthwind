package dev.jmiahman.hearthwind.jobs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
        PayloadTypeRegistry.clientboundPlay().register(
                JobsSyncPayload.TYPE, JobsSyncPayload.CODEC);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            JobCorpus.load(server.getResourceManager());
            for (String line : JobCorpus.summary()) {
                LOGGER.info(line);
            }
        });
        // Push the full multi-job state on login so the Jobs screen is
        // populated before any XP event fires.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            JobDefs.ensureLoaded();
            JobsSync.send(handler.getPlayer());
        });
        LOGGER.info("Hearthwind Jobs initialized: {} jobs ({} employed slots, cooldown {} ticks, curve {}+{}*L)",
                JobDefs.all().size(), HearthwindJobsConfig.get().employedJobs,
                HearthwindJobsConfig.get().jobChangeTime,
                HearthwindJobsConfig.get().jobXPBaseCost,
                HearthwindJobsConfig.get().jobXPCostMultiplicator);
    }
}
