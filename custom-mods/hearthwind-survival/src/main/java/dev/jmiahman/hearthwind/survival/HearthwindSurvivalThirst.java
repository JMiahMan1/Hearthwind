package dev.jmiahman.hearthwind.survival;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public final class HearthwindSurvivalThirst {
    public static final double MAX_HYDRATION = 20.0;
    private static final int TICK_INTERVAL = 40;

    /** Data-driven damage type (data/hearthwind/damage_type/dehydration.json)
     *  so thirst deaths read "died of thirst", not vanilla's "killed by magic". */
    public static final net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> DEHYDRATION =
            net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(
                            "hearthwind", "dehydration"));

    // Per-player damage counter and warning state to avoid cross-player contamination
    private static final Map<UUID, Integer> damageCounters = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> warningLevels = new ConcurrentHashMap<>();

    // payload type is ThirstSyncPayload.TYPE

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

    public static void setHydration(net.minecraft.world.entity.Entity entity, double value) {
        entity.setAttached(HYDRATION, value);
    }

    public static void registerTickLoop() {
        // Sync thirst to client for HUD (above hunger bar) - vanilla gets bossbar fallback via action bar
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer p = handler.getPlayer();
            syncToClient(p, hydration(p));
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

    private static void syncToClient(ServerPlayer player, double h) {
        try {
            ServerPlayNetworking.send(player, new ThirstSyncPayload((float) h));
        } catch (Exception ignored) {
            // Client without hearthwind-client will just ignore - fallback is overlay messages
        }
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
        syncToClient(player, h);

        long damageIntervalTicks = (long) (cfg.damageIntervalSeconds * 20.0);
        UUID id = player.getUUID();
        if (h <= 0.0) {
            int currentDmgCount = damageCounters.getOrDefault(id, 0) + TICK_INTERVAL;
            if (currentDmgCount >= damageIntervalTicks) {
                damageCounters.put(id, 0);
                player.hurt(player.damageSources().source(DEHYDRATION),
                        (float) cfg.damageAmount);
            } else {
                damageCounters.put(id, currentDmgCount);
            }
        } else {
            damageCounters.remove(id);
        }
        sendThresholdWarnings(player, h, wasAboveRegenFloor);
    }

    private static void sendThresholdWarnings(ServerPlayer player, double h,
            boolean wasAboveRegenFloor) {
        int level = h > 12 ? -1 : h > 6 ? 0 : h > 3 ? 1 : 2;
        if (!wasAboveRegenFloor && level >= 0) {
            return;
        }
        // Per-player warning state
        UUID id = player.getUUID();
        Integer prevLevel = warningLevels.get(id);
        if (prevLevel == null || level != prevLevel) {
            warningLevels.put(id, level);
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
