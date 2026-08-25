package dev.jmiahman.hearthwind.skills;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearthwindSkills implements ModInitializer {
	public static final String MOD_ID = "hearthwind_skills";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SkillsConfig.get(); // materialize config/hearthwind_skills.json early
		SkillGates.ensureLoaded();
		SkillGates.register();
		SkillEvents.register();
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) ->
				MobScaling.apply(entity));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				SkillAttributes.applyAll(handler.getPlayer()));
		LOGGER.info("Hearthwind Skills initialized: 12 skills (base {} xp/level), mob scaling {}, gates {}",
				SkillsConfig.get().levels.baseXpPerLevel,
				SkillsConfig.get().mobScaling.enabled ? "on" : "off",
				SkillsConfig.get().gates.enabled ? "on" : "off");
	}
}
