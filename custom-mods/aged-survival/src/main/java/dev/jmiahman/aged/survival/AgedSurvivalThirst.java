package dev.jmiahman.aged.survival;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public final class AgedSurvivalThirst {
    public static final double MAX_HYDRATION = 20.0;
    private static final int TICK_INTERVAL = 40;

    private static int warningLevel = -1;
    private static int damageCounter = 0;

    public static final AttachmentType<Double> HYDRATION =
            AttachmentRegistry.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .copyOnDeath()
                    .buildAndRegister(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath(
                                    "dehydration", "hydration"));

    private AgedSurvivalThirst() {}

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
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % TICK_INTERVAL != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tick(player);
            }
        });
    }

    private static void tick(ServerPlayer player) {
        if (player.getAbilities().invulnerable
                || player.getAbilities().instabuild) {
            return;
        }
        AgedSurvivalConfig.Thirst cfg = AgedSurvivalConfig.get().thirst;
        double h = hydration(player);
        double drain = cfg.baseDrainPerSecond * TICK_INTERVAL;
        if (player.isSprinting()) {
            drain *= cfg.sprintMultiplier;
        }
        MobEffectInstance thirst = player.getEffect(ThirstMobEffect.HOLDER);
        if (thirst != null) {
            drain += cfg.thirstEffectDrainPerSecond * TICK_INTERVAL
                    * (thirst.getAmplifier() + 1);
        }
        boolean wasAboveRegenFloor = h > cfg.regenHydrationFloor;
        h = Math.max(0.0, h - drain);
        setHydration(player, h);

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
