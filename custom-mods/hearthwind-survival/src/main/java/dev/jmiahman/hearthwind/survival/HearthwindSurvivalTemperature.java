package dev.jmiahman.hearthwind.survival;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Clean-room port of EnvironmentZ 2.0.8's server temperature model (the
 * version Aged 3.1.2 ships; upstream Globox1997/EnvironmentZ
 * temperature/TemperatureAspects + TemperatureManager, GPL-3.0 studied - no
 * code copied). Behaviour and values mirror the original:
 *
 * <ul>
 *   <li>body temperature is an integer in [-2400, 2400] with bands
 *       -2400/-1800/-240/-0/+240/+1800/+2400;</li>
 *   <li>wetness is an integer in [0, 200] (soaked at 180, water +100,
 *       rain +1, drying -1);</li>
 *   <li>all drivers run once per {@code temperatureCalculationTime + 1}
 *       ticks per player: biome band, dimension standard or day/night,
 *       wetness, shadow, sweat, armor (warm +3 / iced NBT), height,
 *       nearby blocks/fluids with line of sight and max_count, equipped
 *       items, status effects, acclimatization, protection pools;</li>
 *   <li>resistance/protection pools (max 600) are consumed against the
 *       incoming delta before it reaches the body;</li>
 *   <li>band debuffs use transient attribute modifiers with stable ids:
 *       cold -8% speed, freezing -25% speed / -20% attack speed, hot -12%
 *       attack damage, overheating -30% attack damage / -20% attack
 *       speed;</li>
 *   <li>{@code <= -2400} deals 1.0 freezing damage, {@code >= 2400} adds
 *       0.07 exhaustion (Aged's exhaustionInsteadDehydration path).</li>
 * </ul>
 */
public final class HearthwindSurvivalTemperature {

    /** Data-driven freezing damage type (data/environmentz/damage_type/freezing.json). */
    public static final ResourceKey<DamageType> FREEZING = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath("environmentz", "freezing"));

    /** Persistent per-player state; mirrors TemperatureManager NBT. */
    public record State(int body, int wetness, int thermometer,
            int coldProtection, int heatProtection,
            int coldResistance, int heatResistance,
            boolean coldAffected, boolean hotAffected) {

        public static final State DEFAULT = new State(0, 0, 0, 0, 0, 0, 0, true, true);

        public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("player_temperature").forGetter(State::body),
                Codec.INT.fieldOf("player_wetness").forGetter(State::wetness),
                Codec.INT.fieldOf("thermometer").forGetter(State::thermometer),
                Codec.INT.fieldOf("cold_protection").forGetter(State::coldProtection),
                Codec.INT.fieldOf("heat_protection").forGetter(State::heatProtection),
                Codec.INT.fieldOf("cold_resistance").forGetter(State::coldResistance),
                Codec.INT.fieldOf("heat_resistance").forGetter(State::heatResistance),
                Codec.BOOL.fieldOf("cold_affected").forGetter(State::coldAffected),
                Codec.BOOL.fieldOf("hot_affected").forGetter(State::hotAffected)
        ).apply(instance, State::new));

        public State withBody(int value) {
            return new State(value, wetness, thermometer, coldProtection, heatProtection,
                    coldResistance, heatResistance, coldAffected, hotAffected);
        }

        public State withWetness(int value) {
            return new State(body, value, thermometer, coldProtection, heatProtection,
                    coldResistance, heatResistance, coldAffected, hotAffected);
        }

        public State withThermometer(int value) {
            return new State(body, wetness, value, coldProtection, heatProtection,
                    coldResistance, heatResistance, coldAffected, hotAffected);
        }
    }

    public static final AttachmentType<State> STATE = AttachmentRegistry.<State>builder()
            .persistent(State.CODEC)
            // EnvironmentZ's TemperatureManager does NOT copy on death
            // (not a CopyableComponent): respawn resets to the neutral band.
            // copyOnDeath here caused a respawn -> instant re-freeze loop.
            .buildAndRegister(Identifier.fromNamespaceAndPath("environmentz", "temperature_state"));

    private static final Identifier COLD_DEBUFF_ID = id("cold_debuff");
    private static final Identifier FREEZING_DEBUFF_ID = id("freezing_debuff");
    private static final Identifier HOT_DEBUFF_ID = id("hot_debuff");
    private static final Identifier OVERHEATING_DEBUFF_ID = id("overheating_debuff");
    private static final Identifier GENERAL_DEBUFF_ID = id("general_debuff");

    private static final AttributeModifier COLD_DEBUFF =
            new AttributeModifier(COLD_DEBUFF_ID, -0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier FREEZING_DEBUFF =
            new AttributeModifier(FREEZING_DEBUFF_ID, -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier HOT_DEBUFF =
            new AttributeModifier(HOT_DEBUFF_ID, -0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier OVERHEATING_DEBUFF =
            new AttributeModifier(OVERHEATING_DEBUFF_ID, -0.30, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier GENERAL_DEBUFF =
            new AttributeModifier(GENERAL_DEBUFF_ID, -0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final EquipmentSlot[] EQUIPPED_SLOTS = {
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private static final Map<UUID, Integer> TICKERS = new ConcurrentHashMap<>();

    private HearthwindSurvivalTemperature() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("environmentz", path);
    }

    // ---- state access --------------------------------------------------
    public static State getState(net.minecraft.world.entity.Entity entity) {
        State state = entity.getAttached(STATE);
        return state == null ? State.DEFAULT : state;
    }

    public static void setState(net.minecraft.world.entity.Entity entity, State state) {
        entity.setAttached(STATE, state);
    }

    public static int body(net.minecraft.world.entity.Entity entity) {
        return getState(entity).body();
    }

    public static int wetness(net.minecraft.world.entity.Entity entity) {
        return getState(entity).wetness();
    }

    public static int thermometer(net.minecraft.world.entity.Entity entity) {
        return getState(entity).thermometer();
    }

    public static void setBody(net.minecraft.world.entity.Entity entity, int value) {
        setState(entity, getState(entity).withBody(value));
    }

    public static void setWetness(net.minecraft.world.entity.Entity entity, int value) {
        setState(entity, getState(entity).withWetness(value));
    }

    public static void setThermometer(net.minecraft.world.entity.Entity entity, int value) {
        setState(entity, getState(entity).withThermometer(value));
    }

    /** /environment affection parity: zero out unwanted hot/cold body changes. */
    public static void setEnvironmentAffection(ServerPlayer player, boolean hotAffected, boolean coldAffected) {
        State state = getState(player);
        setState(player, new State(state.body(), state.wetness(), state.thermometer(),
                state.coldProtection(), state.heatProtection(),
                state.coldResistance(), state.heatResistance(), coldAffected, hotAffected));
    }

    public static void setProtection(ServerPlayer player, boolean heat, int amount) {
        State s = getState(player);
        if (heat) {
            setState(player, new State(s.body(), s.wetness(), s.thermometer(),
                    s.coldProtection(), amount, s.coldResistance(), s.heatResistance(),
                    s.coldAffected(), s.hotAffected()));
        } else {
            setState(player, new State(s.body(), s.wetness(), s.thermometer(),
                    amount, s.heatProtection(), s.coldResistance(), s.heatResistance(),
                    s.coldAffected(), s.hotAffected()));
        }
    }

    public static void setResistance(ServerPlayer player, boolean heat, int amount) {
        State s = getState(player);
        if (heat) {
            setState(player, new State(s.body(), s.wetness(), s.thermometer(),
                    s.coldProtection(), s.heatProtection(), s.coldResistance(), amount,
                    s.coldAffected(), s.hotAffected()));
        } else {
            setState(player, new State(s.body(), s.wetness(), s.thermometer(),
                    s.coldProtection(), s.heatProtection(), amount, s.heatResistance(),
                    s.coldAffected(), s.hotAffected()));
        }
    }

    // ---- loop ----------------------------------------------------------
    public static void registerTickLoop() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            HearthwindSurvivalConfig.Temperature cfg = HearthwindSurvivalConfig.get().temperature;
            // Reference PlayerManager behaviour: first-time players get a
            // startup comfort effect (we use the absent attachment as the
            // "no player NBT" proxy).
            if (player.getAttached(STATE) == null && !player.isCreative()
                    && cfg.startUpComfortEffectDuration > 0) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        EnvironmentzEffects.COMFORT, cfg.startUpComfortEffectDuration,
                        0, false, false, true));
            }
            sync(player, getState(player));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                TICKERS.remove(handler.getPlayer().getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tickPlayer(player);
            }
        });
    }

    /** Reference TemperatureManager#tick: every temperatureCalculationTime + 1 ticks. */
    static void tickPlayer(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator() || !player.isAlive()) {
            return;
        }
        int period = Math.max(0, HearthwindSurvivalConfig.get().temperature.temperatureCalculationTime);
        int ticks = TICKERS.merge(player.getUUID(), 1, Integer::sum);
        if (ticks <= period) {
            return;
        }
        TICKERS.put(player.getUUID(), 0);
        calculate(player);
    }

    /** One full reference calculation. Package-private for gametests. */
    static State calculate(ServerPlayer player) {
        HearthwindSurvivalConfig.Temperature cfg = HearthwindSurvivalConfig.get().temperature;
        State old = getState(player);

        int calc = 0;
        int thermo = 0;

        int wet = updateWetness(player, old.wetness());
        boolean isSoaked = wet >= EnvironmentCorpus.wetness(1);
        boolean isInShadow = !player.level().canSeeSky(player.blockPosition().above());
        float biomeTemperature = player.level().getBiome(player.blockPosition()).value().getBaseTemperature();

        Identifier dimensionId = player.level().dimension().identifier();
        if (EnvironmentCorpus.shouldUseOverworldTemperatures(dimensionId)) {
            dimensionId = EnvironmentCorpus.OVERWORLD;
        }
        EnvironmentCorpus.DimensionTable dimension = EnvironmentCorpus.dimension(dimensionId);
        if (dimension == null) {
            dimension = EnvironmentCorpus.dimension(EnvironmentCorpus.OVERWORLD);
        }
        int environmentCode = environmentCode(biomeTemperature);

        // standard OR day/night row
        int rowValue;
        if (EnvironmentCorpus.shouldUseStandardTemperatures(dimensionId)) {
            rowValue = dimension.standard(environmentCode);
        } else {
            rowValue = player.level().isBrightOutside()
                    ? dimension.day(environmentCode)
                    : dimension.night(environmentCode);
        }
        calc += rowValue;
        thermo += rowValue;

        // Season climate hook (seasons-lite): the world's seasonal offset
        // shifts both body drift and the thermometer reading.
        try {
            dev.jmiahman.hearthwind.world.Season season =
                    dev.jmiahman.hearthwind.world.Season.fromWorldTime(
                            player.level().getGameTime(),
                            dev.jmiahman.hearthwind.world.HearthwindWorldConfig.get().daysPerSeason);
            int seasonOff = (int) Math.round(season.tempOffset(
                    dev.jmiahman.hearthwind.world.HearthwindWorldConfig.get()));
            calc += seasonOff;
            thermo += seasonOff;
        } catch (Exception ignored) {
        }

        // wetness row
        if (wet > 0) {
            calc += isSoaked ? dimension.soaked(environmentCode) : dimension.wett(environmentCode);
        }

        // shadow
        if (isInShadow) {
            int shadow = dimension.shadow(environmentCode);
            calc += shadow;
            thermo += shadow;
        }

        // sweat
        if (environmentCode > 2) {
            if (cfg.exhaustionInsteadDehydration) {
                if (player.getFoodData().getFoodLevel() > 6) {
                    player.causeFoodExhaustion(cfg.overheatingExhaustion);
                    calc += dimension.sweat(environmentCode - 3);
                }
            } else {
                HearthwindSurvivalThirst.addDehydration(player, cfg.overheatingExhaustion);
                calc += dimension.sweat(environmentCode - 3);
            }
        }

        // armor (warm insulation / iced NBT)
        calc += armorTemperature(player, dimension, environmentCode);

        // height
        int height = dimension.heightAt(player.blockPosition().getY());
        calc += height;
        thermo += height;

        // nearby blocks and fluids
        int blocks = EnvironmentCorpus.blockHeat(player, cfg.heatBlockRadius);
        calc += blocks;
        thermo += blocks;

        // protection pools (index 0 heat, 1 cold)
        int[] pools = {old.heatProtection(), old.coldProtection()};

        // equipped items
        calc += itemTemperature(player, old, pools);

        // status effects
        calc += effectTemperature(player, old, pools);

        // acclimatization
        int acclimatization = acceptanceAdjustment(dimension, environmentCode, old.body());
        calc += acclimatization;

        // resistance then protection, consumed against the incoming delta
        int[] resistances = {old.heatResistance(), old.coldResistance()};
        calc = consumeProtection(calc, environmentCode, pools, resistances);
        int heatResistance = resistances[0];
        int coldResistance = resistances[1];

        // new body temperature + cutoff / strong acclimatization
        int body = applyCutoff(old.body() + calc, environmentCode);

        // environment affection flags
        if (!old.coldAffected() && body < 0) {
            body = 0;
        }
        if (!old.hotAffected() && body > 0) {
            body = 0;
        }

        // band debuffs
        if (body != 0 && body % 2 == 0) {
            applyBandDebuffs(player, body);
        }

        // damage / exhaustion
        if (body <= EnvironmentCorpus.bodyTemperature(0)) {
            player.hurt(createFreezingSource(player), 1.0F);
        } else if (body >= EnvironmentCorpus.bodyTemperature(6)) {
            if (cfg.exhaustionInsteadDehydration) {
                player.causeFoodExhaustion(cfg.overheatingExhaustion);
            } else {
                HearthwindSurvivalThirst.addDehydration(player, cfg.overheatingExhaustion);
            }
        }

        State next = new State(body, wet, thermo, pools[1], pools[0],
                coldResistance, heatResistance, old.coldAffected(), old.hotAffected());
        setState(player, next);
        if (next.body() != old.body() || next.wetness() != old.wetness()
                || next.thermometer() != old.thermometer()) {
            sync(player, next);
        }
        return next;
    }

    /** Reference environmentCode 0 very_cold .. 4 very_hot. */
    static int environmentCode(float biomeTemperature) {
        if (biomeTemperature < EnvironmentCorpus.biomeTemperature(1)) {
            return biomeTemperature < EnvironmentCorpus.biomeTemperature(0) ? 0 : 1;
        }
        if (biomeTemperature > EnvironmentCorpus.biomeTemperature(2)) {
            return biomeTemperature > EnvironmentCorpus.biomeTemperature(3) ? 4 : 3;
        }
        return 2;
    }

    static void sync(ServerPlayer player, State state) {
        try {
            ServerPlayNetworking.send(player,
                    new TempSyncPayload(state.body(), state.wetness(), state.thermometer()));
        } catch (Exception ignored) {
            // client without hearthwind-client simply does not receive the payload
        }
    }

    // ---- drivers -------------------------------------------------------
    /** Reference playerWetness: water/rain add, dry subtract once per calculation. */
    static int updateWetness(ServerPlayer player, int wetness) {
        int max = EnvironmentCorpus.wetness(0);
        if (player.isInWaterOrRain()) {
            if (wetness < max) {
                if (player.isInWater()) {
                    wetness += EnvironmentCorpus.wetness(2);
                } else if (player.level().isRainingAt(player.blockPosition())) {
                    wetness += EnvironmentCorpus.wetness(3);
                }
            }
        } else if (wetness > 0) {
            wetness += EnvironmentCorpus.wetness(4);
        }
        return Math.max(0, Math.min(max, wetness));
    }

    static int armorTemperature(ServerPlayer player, EnvironmentCorpus.DimensionTable dimension, int environmentCode) {
        int total = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag tag = customTag(stack);
            boolean insulated = (tag != null && tag.contains("environmentz"))
                    || stack.is(EnvironmentzItems.WARM_ARMOR);
            if (!stack.is(EnvironmentzItems.NON_AFFECTING_ARMOR)) {
                total += insulated ? dimension.insulatedArmor(environmentCode) : dimension.armor(environmentCode);
            }
            if (tag != null && tag.contains("iced") && !stack.is(EnvironmentzItems.WARM_ARMOR)) {
                total += dimension.icedArmor(environmentCode);
                int iced = tag.getInt("iced").orElse(1) - 1;
                CustomData.update(DataComponents.CUSTOM_DATA, stack, updated -> {
                    if (iced <= 0) {
                        updated.remove("iced");
                    } else {
                        updated.putInt("iced", iced);
                    }
                });
            }
        }
        return total;
    }

    static int itemTemperature(ServerPlayer player, State state, int[] pools) {
        int total = 0;
        for (EquipmentSlot slot : EQUIPPED_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            EnvironmentCorpus.ItemTemp item = EnvironmentCorpus.item(stack.getItem());
            if (item == null) {
                continue;
            }
            if (item.damage() != 0 && stack.isDamageableItem() && !isArmor(stack)) {
                if (stack.getMaxDamage() - stack.getDamageValue() > 1) {
                    if (!player.isCreative()) {
                        int damage = item.damage();
                        if (stack.getMaxDamage() - stack.getDamageValue() - damage <= 0) {
                            stack.setDamageValue(0);
                        } else {
                            stack.hurtAndBreak(damage, player, slot);
                        }
                    }
                } else {
                    continue;
                }
            }
            total += item.temperature();
            if (item.heatProtection() != 0 && pools[0] < EnvironmentCorpus.protection(0)) {
                pools[0] = Math.min(EnvironmentCorpus.protection(0), pools[0] + item.heatProtection());
            }
            if (item.coldProtection() != 0 && pools[1] < EnvironmentCorpus.protection(1)) {
                pools[1] = Math.min(EnvironmentCorpus.protection(1), pools[1] + item.coldProtection());
            }
        }
        return total;
    }

    static int effectTemperature(ServerPlayer player, State state, int[] pools) {
        int total = 0;
        for (MobEffectInstance instance : player.getActiveEffects()) {
            Identifier effectId = instance.getEffect().unwrapKey()
                    .map(ResourceKey::identifier).orElse(null);
            if (effectId == null) {
                continue;
            }
            int[] values = EnvironmentCorpus.effect(effectId);
            if (values == null) {
                continue;
            }
            int temperature = values[0];
            if ((state.body() < EnvironmentCorpus.bodyTemperature(3) && temperature > 0)
                    || (state.body() > EnvironmentCorpus.bodyTemperature(3) && temperature < 0)) {
                total += temperature;
            }
            if (values[1] != 0 && pools[0] < EnvironmentCorpus.protection(0)) {
                pools[0] = Math.min(EnvironmentCorpus.protection(0), pools[0] + values[1]);
            }
            if (values[2] != 0 && pools[1] < EnvironmentCorpus.protection(1)) {
                pools[1] = Math.min(EnvironmentCorpus.protection(1), pools[1] + values[2]);
            }
        }
        return total;
    }

    static int acceptanceAdjustment(EnvironmentCorpus.DimensionTable dimension, int environmentCode, int body) {
        int dimensionAcclimatization = dimension.acclimatization();
        if (dimensionAcclimatization != EnvironmentCorpus.NO_DIMENSION_ACCLIMATIZATION) {
            return dimensionAcclimatization;
        }
        return switch (environmentCode) {
            case 1 -> body < EnvironmentCorpus.acclimatization(6)
                    ? EnvironmentCorpus.acclimatization(7) : 0;
            case 2 -> {
                if (body < EnvironmentCorpus.acclimatization(4)) {
                    yield EnvironmentCorpus.acclimatization(5);
                }
                if (body > EnvironmentCorpus.acclimatization(0)) {
                    yield EnvironmentCorpus.acclimatization(1);
                }
                yield 0;
            }
            case 3 -> body > EnvironmentCorpus.acclimatization(2)
                    ? EnvironmentCorpus.acclimatization(3) : 0;
            default -> 0;
        };
    }

    /**
     * Reference protection/resistance consumption, with the upstream sign
     * slip fixed (leftover delta is reduced, never doubled). {@code pools}
     * is {@code [heatProtection, coldProtection]} and {@code resistances} is
     * {@code [heatResistance, coldResistance]}; both are mutated. Incoming
     * cold only consumes cold pools and incoming heat only hot pools, exactly
     * like the original. Package-private for gametests.
     */
    static int consumeProtection(int calc, int environmentCode, int[] pools, int[] resistances) {
        if (environmentCode < 2 && calc < 0) {
            if (resistances[1] > 0) {
                int difference = -calc;
                if (resistances[1] >= difference) {
                    calc = 0;
                    resistances[1] -= difference;
                } else {
                    calc += resistances[1];
                    resistances[1] = 0;
                }
            }
            if (calc < 0 && pools[1] > 0) {
                int difference = -calc;
                if (pools[1] >= difference) {
                    calc = 0;
                    pools[1] -= difference;
                } else {
                    calc += pools[1];
                    pools[1] = 0;
                }
            }
        } else if (environmentCode > 2 && calc > 0) {
            if (resistances[0] > 0) {
                int difference = calc;
                if (resistances[0] >= difference) {
                    calc = 0;
                    resistances[0] -= difference;
                } else {
                    calc -= resistances[0];
                    resistances[0] = 0;
                }
            }
            if (calc > 0 && pools[0] > 0) {
                int difference = calc;
                if (pools[0] >= difference) {
                    calc = 0;
                    pools[0] -= difference;
                } else {
                    calc -= pools[0];
                    pools[0] = 0;
                }
            }
        }
        return calc;
    }

    /**
     * Reference cutoff/strong acclimatization: in cold environments a body
     * above "+240" is pushed back down by twice the hot acclimatization, in
     * hot environments a body below "-240" is pushed up, and otherwise the
     * absolute +-2400 limits clamp. Package-private for gametests.
     */
    static int applyCutoff(int body, int environmentCode) {
        if (environmentCode < 2 && body > EnvironmentCorpus.bodyTemperature(4)) {
            return body + EnvironmentCorpus.acclimatization(1) * 2;
        }
        if (environmentCode > 2 && body < EnvironmentCorpus.bodyTemperature(2)) {
            return body + EnvironmentCorpus.acclimatization(5) * 2;
        }
        if (body < EnvironmentCorpus.bodyTemperature(0)) {
            return EnvironmentCorpus.bodyTemperature(0);
        }
        if (body > EnvironmentCorpus.bodyTemperature(6)) {
            return EnvironmentCorpus.bodyTemperature(6);
        }
        return body;
    }

    /** Reference band debuffs, with stable modifier ids. */
    static void applyBandDebuffs(ServerPlayer player, int body) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speed == null || damage == null || attackSpeed == null) {
            return;
        }
        int maxCold = EnvironmentCorpus.bodyTemperature(2);
        int maxHot = EnvironmentCorpus.bodyTemperature(4);
        int veryCold = EnvironmentCorpus.bodyTemperature(1);
        int veryHot = EnvironmentCorpus.bodyTemperature(5);

        if (body > maxCold && body < maxHot) {
            speed.removeModifier(COLD_DEBUFF_ID);
            damage.removeModifier(HOT_DEBUFF_ID);
        } else if (body <= maxCold) {
            if (body <= veryCold) {
                if (!speed.hasModifier(FREEZING_DEBUFF_ID)) {
                    speed.addTransientModifier(FREEZING_DEBUFF);
                    if (!attackSpeed.hasModifier(GENERAL_DEBUFF_ID)) {
                        attackSpeed.addTransientModifier(GENERAL_DEBUFF);
                    }
                }
                speed.removeModifier(COLD_DEBUFF_ID);
            } else {
                if (!speed.hasModifier(COLD_DEBUFF_ID)) {
                    speed.addTransientModifier(COLD_DEBUFF);
                }
                if (speed.hasModifier(FREEZING_DEBUFF_ID)) {
                    speed.removeModifier(FREEZING_DEBUFF_ID);
                    attackSpeed.removeModifier(GENERAL_DEBUFF_ID);
                }
            }
        } else if (body >= veryHot) {
            if (!damage.hasModifier(OVERHEATING_DEBUFF_ID)) {
                damage.addTransientModifier(OVERHEATING_DEBUFF);
                if (!attackSpeed.hasModifier(GENERAL_DEBUFF_ID)) {
                    attackSpeed.addTransientModifier(GENERAL_DEBUFF);
                }
            }
            damage.removeModifier(HOT_DEBUFF_ID);
        } else {
            if (!damage.hasModifier(HOT_DEBUFF_ID)) {
                damage.addTransientModifier(HOT_DEBUFF);
            }
            if (damage.hasModifier(OVERHEATING_DEBUFF_ID)) {
                damage.removeModifier(OVERHEATING_DEBUFF_ID);
                attackSpeed.removeModifier(GENERAL_DEBUFF_ID);
            }
        }
    }

    private static DamageSource createFreezingSource(ServerPlayer player) {
        return player.damageSources().source(FREEZING);
    }

    private static CompoundTag customTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    private static boolean isArmor(ItemStack stack) {
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) {
            return false;
        }
        return switch (equippable.slot()) {
            case HEAD, CHEST, LEGS, FEET, BODY -> true;
            default -> false;
        };
    }
}
