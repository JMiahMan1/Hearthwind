package dev.jmiahman.hearthwind.survival;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Dehydration 1.3.6 thirst engine parity (Aged 3.1.2 replacement).
 *
 * <p>The upstream {@code ThirstManager} keeps an integer thirst level
 * (0..20, half-droplet scale) plus a float "dehydration" buffer (0..40)
 * that only fills from vanilla {@code Player.causeFoodExhaustion} divided
 * by {@code hydrating_factor} (2.0). There is NO passive drain: every 4.0
 * points of buffer cost one thirst level, and at level 0 a 90-tick timer
 * deals {@code thirst_damage} with the same difficulty/health gate as
 * vanilla starvation.
 *
 * <p>Peaceful with natural regeneration regenerates 1 level every 10
 * ticks (and runs the manager update twice per tick, matching the two
 * upstream injection points). Waking up after a real sleep costs
 * {@code sleep_thirst_consumption} thirst and
 * {@code sleep_hunger_consumption} hunger.
 */
public final class HearthwindSurvivalThirst {
    /** Legacy 0..20 display scale, kept as a double for the HUD payload. */
    public static final double MAX_HYDRATION = 20.0;
    public static final int MAX_LEVEL = 20;
    public static final float MAX_DEHYDRATION = 40.0F;
    /** Upstream hard-coded starvation-style tick cadence. */
    public static final int DAMAGE_INTERVAL_TICKS = 90;

    /** Data-driven damage type (data/dehydration/damage_type/thirst.json). */
    public static final ResourceKey<net.minecraft.world.damagesource.DamageType> THIRST =
            ResourceKey.create(
                    net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(
                            "dehydration", "thirst"));

    /** Immutable copy of the upstream ThirstManager fields. */
    public record ThirstState(int level, float dehydration, int damageTimer, boolean hasThirst) {
        public static final ThirstState DEFAULT = new ThirstState(MAX_LEVEL, 0.0F, 0, true);

        public ThirstState withLevel(int newLevel) {
            return new ThirstState(Math.max(0, Math.min(MAX_LEVEL, newLevel)), dehydration, damageTimer, hasThirst);
        }

        public ThirstState withDehydration(float newDehydration) {
            return new ThirstState(level, Math.max(0.0F, Math.min(MAX_DEHYDRATION, newDehydration)), damageTimer, hasThirst);
        }

        public ThirstState withDamageTimer(int newTimer) {
            return new ThirstState(level, dehydration, Math.max(0, newTimer), hasThirst);
        }

        public ThirstState withHasThirst(boolean value) {
            return new ThirstState(level, dehydration, damageTimer, value);
        }
    }

    public static final Codec<ThirstState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("thirst_level").forGetter(ThirstState::level),
            Codec.FLOAT.fieldOf("dehydration").forGetter(ThirstState::dehydration),
            Codec.INT.fieldOf("damage_timer").forGetter(ThirstState::damageTimer),
            Codec.BOOL.fieldOf("has_thirst").forGetter(ThirstState::hasThirst))
            .apply(instance, ThirstState::new));

    public static final AttachmentType<ThirstState> THIRST_STATE =
            AttachmentRegistry.<ThirstState>builder()
                    .persistent(CODEC)
                    .buildAndRegister(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath(
                                    "dehydration", "thirst_state"));

    // Last level sent per player so the HUD only receives real changes.
    private static final Map<UUID, Integer> lastSentLevel = new ConcurrentHashMap<>();

    private HearthwindSurvivalThirst() {}

    public static ThirstState state(net.minecraft.world.entity.Entity entity) {
        ThirstState value = entity.getAttached(THIRST_STATE);
        return value == null ? ThirstState.DEFAULT : value;
    }

    public static void setState(net.minecraft.world.entity.Entity entity, ThirstState value) {
        entity.setAttached(THIRST_STATE, value);
    }

    public static int level(net.minecraft.world.entity.Entity entity) {
        return state(entity).level();
    }

    public static float dehydration(net.minecraft.world.entity.Entity entity) {
        return state(entity).dehydration();
    }

    /** Legacy 0..20 double view: whole levels plus the buffer's quarter value. */
    public static double hydration(net.minecraft.world.entity.Entity entity) {
        ThirstState s = state(entity);
        return s.level() + s.dehydration() / 4.0;
    }

    /** Legacy setter: clamps to a whole level and clears the buffer. */
    public static void setHydration(net.minecraft.world.entity.Entity entity, double value) {
        setState(entity, state(entity).withLevel((int) Math.round(value)).withDehydration(0.0F));
    }

    /** Upstream {@code add(int)}: whole quench points, capped at 20. */
    public static void addThirst(net.minecraft.world.entity.Entity entity, int amount) {
        setState(entity, state(entity).withLevel(state(entity).level() + amount));
    }

    /** Upstream {@code addDehydration(float)}: exhaustion buffer, capped at 40. */
    public static void addDehydration(net.minecraft.world.entity.Entity entity, float amount) {
        setState(entity, state(entity).withDehydration(state(entity).dehydration() + amount));
    }

    /** Legacy double add: whole points in either direction. */
    public static void addHydration(net.minecraft.world.entity.Entity entity, double amount) {
        addThirst(entity, (int) Math.round(amount));
    }

    public static boolean hasThirst(net.minecraft.world.entity.Entity entity) {
        return state(entity).hasThirst();
    }

    public static void setHasThirst(net.minecraft.world.entity.Entity entity, boolean value) {
        setState(entity, state(entity).withHasThirst(value));
    }

    public static void registerTickLoop() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            syncToClient(player, level(player), true);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                updatePlayer(player);
            }
        });
    }

    /**
     * One upstream tick: manager update from the player tick, plus the
     * peaceful regeneration pass from tickMovement (second update + regen).
     */
    public static void updatePlayer(ServerPlayer player) {
        ThirstState s = state(player);
        if (!s.hasThirst()) {
            return;
        }
        s = update(player, s);
        if (player.level().getDifficulty() == Difficulty.PEACEFUL
                && Boolean.TRUE.equals(player.level().getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION))) {
            s = update(player, s);
            if (s.level() < MAX_LEVEL && player.tickCount % 10 == 0) {
                s = s.withLevel(s.level() + 1);
            }
        }
        setState(player, s);
        syncToClient(player, s.level(), false);
    }

    /** Verbatim upstream {@code ThirstManager.update} math. */
    private static ThirstState update(ServerPlayer player, ThirstState s) {
        HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
        Difficulty difficulty = player.level().getDifficulty();
        int level = s.level();
        float dehydration = s.dehydration();
        int timer = s.damageTimer();

        if (dehydration > 4.0F) {
            dehydration -= 4.0F;
            if (difficulty != Difficulty.PEACEFUL) {
                level = Math.max(level - 1, 0);
            }
        }
        if (level <= 0) {
            timer++;
            if (timer >= DAMAGE_INTERVAL_TICKS) {
                if (player.getHealth() > 10.0F
                        || difficulty == Difficulty.HARD
                        || (player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL)) {
                    if (player.level() instanceof ServerLevel serverLevel) {
                        player.hurtServer(serverLevel,
                                player.damageSources().source(THIRST),
                                (float) cfg.thirst.thirstDamage);
                    }
                }
                timer = 0;
            }
        } else {
            timer = 0;
        }
        return new ThirstState(level, dehydration, timer, s.hasThirst());
    }

    /** Skips the payload unless the rendered level actually changed. */
    private static void syncToClient(ServerPlayer player, int currentLevel, boolean force) {
        UUID id = player.getUUID();
        Integer previous = lastSentLevel.get(id);
        if (!force && previous != null && previous == currentLevel) {
            return;
        }
        lastSentLevel.put(id, currentLevel);
        try {
            ServerPlayNetworking.send(player, new ThirstSyncPayload((float) currentLevel));
        } catch (Exception ignored) {
            // Client without hearthwind-client will just ignore.
        }
    }

    public static void forget(ServerPlayer player) {
        lastSentLevel.remove(player.getUUID());
    }
}
