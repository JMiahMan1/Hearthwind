package dev.jmiahman.hearthwind.client;

import dev.jmiahman.hearthwind.survival.DietSyncPayload;
import dev.jmiahman.hearthwind.survival.JobSyncPayload;
import dev.jmiahman.hearthwind.survival.SkillUpPayload;
import dev.jmiahman.hearthwind.survival.SkillsSyncPayload;
import dev.jmiahman.hearthwind.survival.TempSyncPayload;
import dev.jmiahman.hearthwind.survival.ThirstSyncPayload;
import dev.jmiahman.hearthwind.world.SeasonSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client companion - presentation only, never authoritative.
 *
 * Server owns thirst/diet/temperature/skills/jobs/seasons and enforces gates.
 * Client just renders HUD bars, instruction toasts, and water-motion previews
 * from {@code hearthwind:*} sync payloads. Vanilla clients without this mod
 * get action-bar fallbacks and remain fully playable.
 */
public class HearthwindClient implements ClientModInitializer {
    public static final String MOD_ID = "hearthwind_client";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        // Thirst sync from server -> ClientThirstData for HUD above hunger bar.
        try {
            ClientPlayNetworking.registerGlobalReceiver(ThirstSyncPayload.TYPE, (payload, context) -> {
                context.client().execute(() -> ClientThirstData.setHydration(payload.hydration()));
            });
            LOGGER.info("Hearthwind Client networking: receiver for {}", ThirstSyncPayload.TYPE.id());
        } catch (Exception e) {
            LOGGER.warn("Failed to register thirst receiver", e);
        }

        // Diet sync from server -> ClientDietData for HUD left of hunger bar.
        try {
            ClientPlayNetworking.registerGlobalReceiver(DietSyncPayload.TYPE, (payload, context) -> {
                context.client().execute(() -> ClientDietData.set(new int[] {
                        payload.carbohydrates(), payload.protein(), payload.fat(),
                        payload.vitamins(), payload.minerals() }));
            });
            LOGGER.info("Hearthwind Client networking: receiver for {}", DietSyncPayload.TYPE.id());
        } catch (Exception e) {
            LOGGER.warn("Failed to register diet receiver", e);
        }

        // Nutrition item map (Shift tooltip) + threshold effect names (panel).
        try {
            ClientPlayNetworking.registerGlobalReceiver(
                    dev.jmiahman.hearthwind.survival.NutritionItemMapPayload.TYPE, (payload, context) -> {
                        context.client().execute(() -> ClientNutritionData.setItemMap(payload.entries()));
                    });
            ClientPlayNetworking.registerGlobalReceiver(
                    dev.jmiahman.hearthwind.survival.NutritionEffectsPayload.TYPE, (payload, context) -> {
                        context.client().execute(
                                () -> ClientNutritionData.setEffects(payload.positive(), payload.negative()));
                    });
            LOGGER.info("Hearthwind Client networking: receivers for nutrition item map + effects");
        } catch (Exception e) {
            LOGGER.warn("Failed to register nutrition receivers", e);
        }

        // Temperature sync from server -> ClientTempData for HUD right of hunger bar.
        try {
            ClientPlayNetworking.registerGlobalReceiver(TempSyncPayload.TYPE, (payload, context) -> {
                context.client().execute(() -> ClientTempData.set(
                        payload.bodyTemperature(), payload.wetness(), payload.thermometer()));
            });
            LOGGER.info("Hearthwind Client networking: receiver for {}", TempSyncPayload.TYPE.id());
        } catch (Exception e) {
            LOGGER.warn("Failed to register temp receiver", e);
        }

        // Job sync from server -> ClientJobData for HUD above hotbar.
        try {
            ClientPlayNetworking.registerGlobalReceiver(JobSyncPayload.TYPE, (payload, context) -> {
                context.client().execute(() -> ClientJobData.setJob(payload.job(), payload.level(),
                        payload.xp(), payload.xpPerLevel()));
            });
            LOGGER.info("Hearthwind Client networking: receiver for {}", JobSyncPayload.TYPE.id());
        } catch (Exception e) {
            LOGGER.warn("Failed to register job receiver", e);
        }

        // Full multi-job sync (all jobs, employed list, cooldown) for the Jobs screen.
        try {
            ClientPlayNetworking.registerGlobalReceiver(dev.jmiahman.hearthwind.jobs.JobsSyncPayload.TYPE,
                    (payload, context) -> {
                        context.client().execute(() -> ClientJobData.updateAll(
                                payload.employed(), payload.ids(), payload.levels(), payload.xp(),
                                payload.nextCost(), payload.cooldownTicks(), payload.maxEmployed()));
                    });
            LOGGER.info("Hearthwind Client networking: receiver for {}", dev.jmiahman.hearthwind.jobs.JobsSyncPayload.TYPE.id());
        } catch (Exception e) {
            LOGGER.warn("Failed to register multi-job receiver", e);
        }

        // Full skill-state sync from server
        try {
            ClientPlayNetworking.registerGlobalReceiver(SkillsSyncPayload.TYPE, (payload, context) -> {
                java.util.Map<String, Integer> levels = new java.util.HashMap<>();
                for (int i = 0; i < payload.skills().size(); i++) {
                    levels.put(payload.skills().get(i), payload.levels().get(i));
                }
                context.client().execute(() -> ClientSkillData.replaceAll(levels));
            });
            LOGGER.info("Hearthwind Client networking: receiver for {}", SkillsSyncPayload.TYPE.id());
        } catch (Exception e) {
            LOGGER.warn("Failed to register skills sync receiver", e);
        }

        try {
            ClientPlayNetworking.registerGlobalReceiver(
                    dev.jmiahman.hearthwind.skills.SkillGatesSyncPayload.TYPE,
                    (payload, context) -> context.client().execute(() -> ClientSkillGates.replaceFrom(payload)));
            LOGGER.info("Hearthwind Client networking: receiver for {}",
                    dev.jmiahman.hearthwind.skills.SkillGatesSyncPayload.TYPE.id());
        } catch (Exception e) {
            LOGGER.warn("Failed to register skill gate sync receiver", e);
        }

        // Skill level-up toast notification from server
        try {
            ClientPlayNetworking.registerGlobalReceiver(SkillUpPayload.TYPE, (payload, context) -> {
                context.client().execute(() -> ClientSkillData.onSkillUp(payload.skill(), payload.newLevel()));
            });
            LOGGER.info("Hearthwind Client networking: receiver for {}", SkillUpPayload.TYPE.id());
        } catch (Exception e) {
            LOGGER.warn("Failed to register skill level-up receiver", e);
        }

        // Season sync from server -> ClientSeasonData for top-left widget.
        try {
            ClientPlayNetworking.registerGlobalReceiver(SeasonSyncPayload.TYPE, (payload, context) -> {
                context.client().execute(() -> ClientSeasonData.set(
                        payload.seasonOrdinal(), payload.dayOfSeason(),
                        payload.daysPerSeason()));
            });
            LOGGER.info("Hearthwind Client networking: receiver for {}", SeasonSyncPayload.TYPE.id());
        } catch (Exception e) {
            LOGGER.warn("Failed to register season receiver", e);
        }

        // Party sync from server -> ClientPartyData for party HUD
        try {
            ClientPlayNetworking.registerGlobalReceiver(dev.jmiahman.hearthwind.survival.PartySyncPayload.TYPE, (payload, context) -> {
                java.util.List<ClientPartyData.MemberEntry> list = new java.util.ArrayList<>();
                for (dev.jmiahman.hearthwind.survival.PartySyncPayload.MemberInfo m : payload.members()) {
                    list.add(new ClientPartyData.MemberEntry(m.name(), m.health(), m.maxHealth(), m.distance(), m.isLeader()));
                }
                context.client().execute(() -> ClientPartyData.update(payload.partyName(), payload.isLeader(), payload.pvpEnabled(), list));
            });
            LOGGER.info("Hearthwind Client networking: receiver for party_sync");
        } catch (Exception e) {
            LOGGER.warn("Failed to register party receiver", e);
        }

        // Downed sync from server -> DownedHud
        try {
            ClientPlayNetworking.registerGlobalReceiver(dev.jmiahman.hearthwind.survival.revive.DownedSyncPayload.TYPE, (payload, context) -> {
                context.client().execute(() -> DownedHud.update(payload.isDowned(), payload.remainingSeconds(), payload.reviveProgressPercent()));
            });
            LOGGER.info("Hearthwind Client networking: receiver for downed_sync");
        } catch (Exception e) {
            LOGGER.warn("Failed to register downed receiver", e);
        }

        // HUDs & Keybindings
        try {
            ThirstHud.register();
            TempHud.register();
            JobHud.register();
            SeasonHud.register();
            SkillToast.register();
            GateHintHud.register();
            DownedHud.register();
            BlockTargetHud.register();
            NutrientsKey.init();
            dev.jmiahman.hearthwind.client.render.FaunaEntityRenderer.registerAll();
            HydrationBlockTints.register();

            net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
                net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft != null && com.mojang.blaze3d.platform.InputConstants.isKeyDown(
                        minecraft.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    ClientNutritionData.appendShiftTooltip(stack, lines);
                }
                ClientSkillGates.Requirement req = ClientSkillGates.getItemRequirement(stack);
                if (req != null) {
                    int playerLvl = ClientSkillData.knownLevels().getOrDefault(req.skill(), 0);
                    boolean unlocked = playerLvl >= req.level();
                    String skillCap = req.skill().substring(0, 1).toUpperCase() + req.skill().substring(1);
                    if (unlocked) {
                        lines.add(net.minecraft.network.chat.Component.literal("§a✔ " + skillCap + " Level " + req.level()));
                    } else {
                        lines.add(net.minecraft.network.chat.Component.literal("§c✖ Requires " + skillCap + " Level " + req.level()));
                    }
                }
            });

            LOGGER.info("Hearthwind Client initialized - HUDs, block targeting level badge, and item tooltips active");
        } catch (Exception e) {
            LOGGER.warn("Failed to register HUDs", e);
        }
    }
}
