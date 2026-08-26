package dev.jmiahman.hearthwind.survival;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Temperature model replacing environmentz (parity-first):
 * body temperature drifts toward a biome-derived target; armor and carried
 * items from the migrated environmentz tags bias the drift; extremes cause
 * freezing damage / overheating exhaustion. Scale is -10..+10.
 */
public final class HearthwindSurvivalTemperature {
    public static final double MIN = -10.0;
    public static final double MAX = 10.0;

    public static final AttachmentType<Double> TEMPERATURE =
            AttachmentRegistry.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .copyOnDeath()
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("environmentz", "temperature"));

    private static int warningLevel = 0;
    private static final Map<UUID, Long> freezeCooldowns = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> heatCooldowns = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> coldWaterCooldowns = new ConcurrentHashMap<>();

    private HearthwindSurvivalTemperature() {}

    public static double get(ServerPlayer player) {
        Double v = player.getAttached(TEMPERATURE);
        return v == null ? 0.0 : v;
    }

    /** Apply an external delta (items); returns the new temperature. */
    public static double shift(ServerPlayer player, double delta) {
        double next = clamp(get(player) + delta);
        player.setAttached(TEMPERATURE, next);
        return next;
    }

    /** Drinking cold/normal water gives a timed cooling window vs overheating. */
    public static void applyColdCooldown(ServerPlayer player, long ticks) {
        long until = player.level().getGameTime() + ticks;
        coldWaterCooldowns.put(player.getUUID(), until);
        // also nudge now for immediate feedback
        shift(player, -0.5);
    }

    public static long coldCooldownRemaining(ServerPlayer player) {
        long until = coldWaterCooldowns.getOrDefault(player.getUUID(), 0L);
        long now = player.level().getGameTime();
        return Math.max(0, until - now);
    }

    public static void sendFeedback(ServerPlayer player, double newTemp) {
        player.sendSystemMessage(Component.literal(String.format(
                "You feel %s (%.1f)", newTemp < 0 ? "cooler" : "warmer", newTemp)));
    }

    private static double clamp(double v) {
        return Math.max(MIN, Math.min(MAX, v));
    }

    private static int countTag(ItemStack[] stacks, TagKey<Item> tag) {
        int n = 0;
        for (ItemStack s : stacks) {
            if (!s.isEmpty() && s.is(tag)) {
                n++;
            }
        }
        return n;
    }

    /** Biome-derived equilibrium temperature on the -10..10 scale. */
    static double targetFor(ServerPlayer player) {
        Holder<net.minecraft.world.level.biome.Biome> biome =
                player.level().getBiome(player.blockPosition());
        float base = biome.value().getBaseTemperature();
        // vanilla range ~[-0.7 .. 2.0]: plains .8, desert 2.0, snowy taiga -.5,
        // frozen peaks -.7 -> map to [-9..+9]
        double t = (base - 0.6) * 6.5;
        return Math.max(-9.0, Math.min(9.0, t));
    }

    public static void registerTickLoop() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 40 != 0) {
                return; // once per second
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                DamageSource generic = player.damageSources().generic();
                if (player.isInvulnerableTo(player.level(), generic)
                        || player.getAbilities().instabuild) {
                    continue;
                }
                tick(player);
            }
        });
    }

    private static void tick(ServerPlayer player) {
        double target = targetFor(player);
        double current = get(player);

        ItemStack[] armor = new ItemStack[]{
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET)};
        int warmPieces = countTag(armor, EnvironmentzItems.WARM_ARMOR);
        int neutralPieces = countTag(armor, EnvironmentzItems.NON_AFFECTING_ARMOR);

        // cold protection: warm pieces pull the effective target back up
        if (target < current && warmPieces > 0) {
            target += warmPieces * 1.2 + warmPieces * warmPieces * 0.3;
        }
        // non-affecting armor dampens both directions slightly
        target *= 1.0 - 0.08 * neutralPieces;

        // inventory insulation / ice bias toward comfort
        boolean hasInsulation = player.getInventory().hasAnyMatching(
                s -> !s.isEmpty() && s.is(EnvironmentzItems.INSOLATING_ITEM));
        boolean hasIce = player.getInventory().hasAnyMatching(
                s -> !s.isEmpty() && s.is(EnvironmentzItems.ICE_ITEMS));
        if (current > 0 && hasIce) {
            target -= 1.5;
        }
        if (current < 0 && hasInsulation) {
            target += 1.5;
        }
        // cold-water drunk cooldown - extra -1.5 target dampening for 30-60s
        // (normal water 30s, cold 60s). Hot water does NOT grant this.
        Long coldUntil = coldWaterCooldowns.get(player.getUUID());
        if (coldUntil != null) {
            if (player.level().getGameTime() < coldUntil) {
                target -= 1.5;
            } else {
                coldWaterCooldowns.remove(player.getUUID());
            }
        }
        // sprinting builds heat quickly
        if (player.isSprinting()) {
            target += 1.8;
        }

        // day/night - heat much less at night, cold much worse at night (when outside)
        long timeOfDay;
        try {
            timeOfDay = (Long) player.level().getClass().getMethod("getDayTime").invoke(player.level()) % 24000L;
        } catch (Exception e) {
            timeOfDay = player.level().getGameTime() % 24000L;
        }
        boolean isNight = timeOfDay > 13000 && timeOfDay < 23000;
        boolean isOutside = player.level().canSeeSky(player.blockPosition());
        if (isNight && isOutside) {
            target -= 3.5; // desert 9 -> 5.5 at night (no longer extreme), snowy -8.5 -> -12 (worse)
        }
        // environmental modifiers
        if (player.isOnFire()) {
            target = Math.max(target, 9.5);
        }
        if (player.isInWater()) {
            // In water, temperature quickly moves toward water-cold.
            // For heat, this is a reset (cools you); for cold, it makes it worse (even colder when wet at night outside).
            target -= 2.0; // water always chills
            if (target > 0) {
                // was hot, water resets strongly to cool
                target = Math.min(target, -1.0);
            }
            // also clear heat-era cooldowns so you don't immediately reburn on exit
            heatCooldowns.remove(player.getUUID());
            // if was overheated, give a brief cooled window after exit
            if (current >= 7.0) {
                applyColdCooldown(player, 200); // 10s extra after water exit
            }
            // if was already freezing, water makes it worse - accelerate freeze
            if (current <= -6.0) {
                target -= 1.5; // extra chill when already cold and wet
            }
        } else if (player.level().isRainingAt(player.blockPosition())) {
            // Rain cools heat and gives relief - stronger than before
            target -= 2.0;
            if (isNight && isOutside) target -= 1.0; // rain + night outside is extra cold
            // rain when cold and outside also worsens (wet cold)
            if (current <= -6.0 && isOutside) target -= 1.0;
            else if (current >= 6.0) {
                // rain relief for heat - brief cooled window like drinking water
                applyColdCooldown(player, 100); // 5s relief
            }
        }
        if (player.blockPosition().getY() > 128) {
            target += 1.0;
        } else if (player.blockPosition().getY() < 0) {
            target -= 1.5;
        }
        // water reset: if just exited water, keep target low for a bit via cold cooldown
        // (already applied above)

        HearthwindSurvivalConfig.Temperature cfg = HearthwindSurvivalConfig.get().temperature;
        // drift is per-second, tick is 40 ticks = 2s
        double seconds = 40 / 20.0;
        double perTickDrift = cfg.driftPerSecond * seconds;
        double next;
        if (player.isInWater()) {
            // In water, heat is quickly washed away - reset to water target
            next = clamp(target);
        } else {
            double step = Math.signum(target - current)
                    * Math.min(perTickDrift, Math.abs(target - current));
            next = clamp(current + step);
        }
        player.setAttached(TEMPERATURE, next);

        long now = player.level().getGameTime();
        long cooldownTicks = (long) (cfg.hurtCooldownSeconds * 20);
        if (next <= cfg.freezeHurtAt && cooldown(freezeCooldowns, player.getUUID(), now, cooldownTicks)) {
            player.hurt(player.damageSources().freeze(), 1.0f);
        }
        if (next >= cfg.heatExhaustAt) {
            player.getFoodData().addExhaustion(0.02f);
        }
        if (next >= cfg.heatHurtAt && cooldown(heatCooldowns, player.getUUID(), now, cooldownTicks)) {
            player.hurt(player.damageSources().hotFloor(), 1.0f);
        }

        warn(player, next);
    }

    private static boolean cooldown(Map<UUID, Long> map, UUID id, long now,
            long cooldownTicks) {
        Long last = map.get(id);
        if (last != null && now - last < cooldownTicks) {
            return false;
        }
        map.put(id, now);
        return true;
    }

    private static void warn(ServerPlayer player, double temp) {
        int level = temp <= -9 || temp >= 9 ? 2 : temp <= -6 || temp >= 6 ? 1 : 0;
        if (level == warningLevel) {
            return;
        }
        warningLevel = level;
        if (level == 2) {
            player.sendOverlayMessage(Component.literal(
                    "\u26a0 Extreme temperature! Find shelter!"));
        } else if (level == 1) {
            player.sendOverlayMessage(Component.literal(
                    "You feel very " + (temp < 0 ? "cold" : "hot")));
        }
    }
}
