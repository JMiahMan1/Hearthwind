package dev.jmiahman.hearthwind.survival;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;

public final class HearthwindSurvivalThirst {
    public static final double MAX_HYDRATION = 20.0;
    private static final int TICK_INTERVAL = 40;

    private static int warningLevel = -1;
    private static int damageCounter = 0;

    // Thirst bar via bossbar - visible to vanilla clients without mods
    private static final java.util.Map<java.util.UUID, ServerBossEvent> BOSSBARS =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static final AttachmentType<Double> HYDRATION =
            AttachmentRegistry.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    // do not copyOnDeath - respawn with fresh hydration, otherwise
                    // thirst death at 0 loops forever (hit sounds on login)
                    .buildAndRegister(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath(
                                    "dehydration", "hydration"));

    private HearthwindSurvivalThirst() {}

    public static double hydration(net.minecraft.world.entity.Entity entity) {
        Double v = entity.getAttached(HYDRATION);
        return v == null ? MAX_HYDRATION : v;
    }

    public static void addHydration(net.minecraft.world.entity.Entity entity, double amount) {
        setHydration(entity, Math.min(MAX_HYDRATION, hydration(entity) + amount));
    }

    private static void setHydration(net.minecraft.world.entity.Entity entity, double value) {
        entity.setAttached(HYDRATION, value);
    }

    public static void registerTickLoop() {
        // Bossbar lifecycle - add on join, remove on leave
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer p = handler.getPlayer();
            getBoss(p); // create and show
            updateBoss(p, hydration(p));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer p = handler.getPlayer();
            ServerBossEvent e = BOSSBARS.remove(p.getUUID());
            if (e != null) e.removePlayer(p);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % TICK_INTERVAL != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tick(player);
            }
        });
    }

    private static ServerBossEvent getBoss(ServerPlayer player) {
        return BOSSBARS.computeIfAbsent(player.getUUID(), id -> {
            ServerBossEvent e = new ServerBossEvent(
                    java.util.UUID.randomUUID(),
                    Component.literal("Thirst 20.0/20"),
                    BossEvent.BossBarColor.BLUE,
                    BossEvent.BossBarOverlay.PROGRESS);
            e.setVisible(true);
            return e;
        });
    }

    private static void updateBoss(ServerPlayer player, double h) {
        ServerBossEvent e = getBoss(player);
        // Ensure player is tracked (re-add after dimension change etc.)
        if (!e.getPlayers().contains(player)) e.addPlayer(player);
        float progress = (float) (h / MAX_HYDRATION);
        e.setProgress(progress);
        // Color and name by level
        BossEvent.BossBarColor color;
        String name;
        if (h > 12) {
            color = BossEvent.BossBarColor.BLUE;
            name = String.format("Thirst %.1f/20", h);
        } else if (h > 6) {
            color = BossEvent.BossBarColor.YELLOW;
            name = String.format("Thirst %.1f/20 - thirsty", h);
        } else if (h > 3) {
            color = BossEvent.BossBarColor.YELLOW;
            name = String.format("Thirst %.1f/20 - dehydrated!", h);
        } else if (h > 0) {
            color = BossEvent.BossBarColor.RED;
            name = String.format("Thirst %.1f/20 - DANGER", h);
        } else {
            color = BossEvent.BossBarColor.RED;
            name = "Thirst 0/20 - DYING!";
        }
        e.setColor(color);
        e.setName(Component.literal(name));
        e.setVisible(true);
    }

    private static void tick(ServerPlayer player) {
        if (player.getAbilities().invulnerable
                || player.getAbilities().instabuild) {
            return;
        }
        HearthwindSurvivalConfig.Thirst cfg = HearthwindSurvivalConfig.get().thirst;
        double h = hydration(player);
        // TICK_INTERVAL is ticks (40 = 2s), config is per-second so divide by 20
        double seconds = TICK_INTERVAL / 20.0;
        double drain = cfg.baseDrainPerSecond * seconds;
        if (player.isSprinting()) {
            drain *= cfg.sprintMultiplier;
        }
        MobEffectInstance thirst = player.getEffect(ThirstMobEffect.HOLDER);
        if (thirst != null) {
            drain += cfg.thirstEffectDrainPerSecond * seconds
                    * (thirst.getAmplifier() + 1);
        }
        boolean wasAboveRegenFloor = h > cfg.regenHydrationFloor;
        h = Math.max(0.0, h - drain);
        setHydration(player, h);
        updateBoss(player, h);

        long damageIntervalTicks =
                (long) (cfg.damageIntervalSeconds * TICK_INTERVAL / 2);
        if (h <= 0.0) {
            damageCounter += TICK_INTERVAL;
            if (damageCounter >= damageIntervalTicks) {
                damageCounter = 0;
                player.hurt(player.damageSources().magic(),
                        (float) cfg.damageAmount);
            }
        } else {
            damageCounter = 0;
        }
        sendThresholdWarnings(player, h, wasAboveRegenFloor);
    }

    private static void sendThresholdWarnings(ServerPlayer player, double h,
            boolean wasAboveRegenFloor) {
        int level = h > 12 ? -1 : h > 6 ? 0 : h > 3 ? 1 : 2;
        if (!wasAboveRegenFloor && level >= 0) {
            return;
        }
        if (level != warningLevel) {
            warningLevel = level;
            switch (level) {
                case 0 -> warn(player, "You are getting thirsty.", ChatFormatting.YELLOW);
                case 1 -> warn(player, "You are dehydrated! Find water!", ChatFormatting.GOLD);
                case 2 -> warn(player, "You are dying of thirst!", ChatFormatting.RED);
                default -> { }
            }
        }
    }

    private static void warn(ServerPlayer player, String text, ChatFormatting color) {
        player.sendOverlayMessage(
                Component.literal(text).withStyle(color));
    }
}
