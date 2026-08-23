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
    private static final double BASE_DRAIN_PER_TICK = 0.02;
    private static final double SPRINT_DRAIN_MULTIPLIER = 2.0;
    private static final double THIRST_EFFECT_DRAIN_PER_TICK = 0.03;
    private static final double REGEN_HYDRATION_FLOOR = 6.0;
    private static final int DAMAGE_INTERVAL_TICKS = 80;
    private static final float DAMAGE_AMOUNT = 1.0f;
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

    public static double hydration(ServerPlayer player) {
        Double v = player.getAttached(HYDRATION);
        return v == null ? MAX_HYDRATION : v;
    }

    public static void addHydration(ServerPlayer player, double amount) {
        setHydration(player, Math.min(MAX_HYDRATION, hydration(player) + amount));
    }

    private static void setHydration(ServerPlayer player, double value) {
        player.setAttached(HYDRATION, value);
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
        double h = hydration(player);
        double drain = BASE_DRAIN_PER_TICK * TICK_INTERVAL;
        if (player.isSprinting()) {
            drain *= SPRINT_DRAIN_MULTIPLIER;
        }
        MobEffectInstance thirst = player.getEffect(ThirstMobEffect.HOLDER);
        if (thirst != null) {
            drain += THIRST_EFFECT_DRAIN_PER_TICK * TICK_INTERVAL * (thirst.getAmplifier() + 1);
        }
        boolean wasAboveRegenFloor = h > REGEN_HYDRATION_FLOOR;
        h = Math.max(0.0, h - drain);
        setHydration(player, h);

        if (h <= 0.0) {
            damageCounter += TICK_INTERVAL;
            if (damageCounter >= DAMAGE_INTERVAL_TICKS) {
                damageCounter = 0;
                player.hurt(player.damageSources().magic(), DAMAGE_AMOUNT);
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
