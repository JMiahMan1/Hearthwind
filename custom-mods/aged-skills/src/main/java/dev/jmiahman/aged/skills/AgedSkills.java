package dev.jmiahman.aged.skills;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgedSkills implements ModInitializer {
	public static final String MOD_ID = "aged_skills";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SkillsConfig.get(); // materialize config/aged_skills.json early
		SkillEvents.register();
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				SkillAttributes.applyAll(handler.getPlayer()));
		LOGGER.info("Aged Skills initialized: 12 skills, XP curve base {}",
				SkillsConfig.get().levels.baseXpPerLevel);
	}
}
