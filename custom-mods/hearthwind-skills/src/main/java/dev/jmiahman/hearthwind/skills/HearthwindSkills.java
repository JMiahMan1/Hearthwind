package dev.jmiahman.hearthwind.skills;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
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
		// The levelz corpus lives in the world datapack, so re-read it once the
		// server hands us a ResourceManager (falls back to the bundled digest
		// when the pack is not installed).
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING
				.register(server -> SkillGates.load(server.getResourceManager()));
		SkillGates.register();
		SkillEvents.register();
		SkillProcs.register();
		dev.jmiahman.hearthwind.skills.party.PartyCombat.register();
		dev.jmiahman.hearthwind.skills.party.PartyCommand.register();
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) ->
				MobScaling.apply(entity));

		// On login:
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
				net.minecraft.server.level.ServerPlayer player = handler.getPlayer();
				var xpMap = player.getAttached(SkillXp.XP);
				if ((xpMap == null || xpMap.isEmpty()) && player.experienceLevel == 0 && player.totalExperience == 0) {
					player.setExperienceLevels(2);
				}
				SkillAttributes.applyAll(player);
				SkillsSync.send(player);
				dev.jmiahman.hearthwind.skills.party.PartySync.syncTo(player);
		});

		// On respawn / death clone:
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
				SkillAttributes.applyAll(newPlayer);
				SkillsSync.send(newPlayer);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
				SkillAttributes.applyAll(newPlayer);
				SkillsSync.send(newPlayer);
				if (newPlayer.getHealth() > newPlayer.getMaxHealth()) {
					newPlayer.setHealth(newPlayer.getMaxHealth());
				}
		});

		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.serverboundPlay().register(
				dev.jmiahman.hearthwind.survival.SkillUpPayload.TYPE,
				dev.jmiahman.hearthwind.survival.SkillUpPayload.CODEC);
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.clientboundPlay().register(
				dev.jmiahman.hearthwind.survival.SkillUpPayload.TYPE,
				dev.jmiahman.hearthwind.survival.SkillUpPayload.CODEC);
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.clientboundPlay().register(
				dev.jmiahman.hearthwind.survival.SkillsSyncPayload.TYPE,
				dev.jmiahman.hearthwind.survival.SkillsSyncPayload.CODEC);
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.clientboundPlay().register(
				dev.jmiahman.hearthwind.survival.PartySyncPayload.TYPE,
				dev.jmiahman.hearthwind.survival.PartySyncPayload.CODEC);
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.serverboundPlay().register(
				dev.jmiahman.hearthwind.survival.PartySyncPayload.TYPE,
				dev.jmiahman.hearthwind.survival.PartySyncPayload.CODEC);
		net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
				dev.jmiahman.hearthwind.survival.SkillUpPayload.TYPE, (payload, context) -> {
					net.minecraft.server.level.ServerPlayer player = context.player();
					String skillId = payload.skill();
					try {
						Skill skill = Skill.byId(skillId);
						int currentLvl = SkillXp.level(player, skill);
						if (currentLvl < SkillXp.maxLevel()
								&& (player.experienceLevel > 0 || player.getAbilities().instabuild)) {
							if (!player.getAbilities().instabuild) {
								player.setExperienceLevels(player.experienceLevel - 1);
							}
							SkillXp.setLevel(player, skill, currentLvl + 1);
							SkillsSync.send(player);
							player.level().playSound(null, player.blockPosition(),
									net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
									net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.2f);
						}
					} catch (IllegalArgumentException ignored) {
					}
				});
		LOGGER.info("Hearthwind Skills initialized: 12 skills (base {} xp/level), mob scaling {}, gates {}",
				SkillsConfig.get().levels.baseXpPerLevel,
				SkillsConfig.get().mobScaling.enabled ? "on" : "off",
				SkillsConfig.get().gates.enabled ? "on" : "off");
		LOGGER.info("Skill procs {}", SkillsConfig.get().procs.enabled ? "on" : "off");
	}
}
