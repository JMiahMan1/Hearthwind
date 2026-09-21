package net.satisfy.bakery.core.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.satisfy.bakery.platform.PlatformHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VitalityEffect extends MobEffect {

    // 26.2: FoodData no longer exposes accumulated exhaustion, so the original
    // "reduce down to zero, never below" cap cannot be read directly. Instead we
    // track banked (possibly negative) exhaustion per player, capped at one full
    // saturation cycle (4.0). The bank resets whenever food or saturation actually
    // drops, since only real exhaustion burn can consume it - so idle players can
    // bank at most 4.0, while active players see the full original rate.
    private static final float MAX_BANKED_EXHAUSTION = 4.0F;
    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private record State(float banked, int food, float saturation) {
    }

    public VitalityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFBFA34A);
    }

    @Override
    public boolean applyEffectTick(net.minecraft.server.level.ServerLevel level, LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) {
            return true;
        }

        if (!player.isAlive() || player.isSpectator()) {
            return true;
        }

        float baseReduction = PlatformHelper.getVitalityEffectExhaustionReduction();
        if (!Float.isFinite(baseReduction) || baseReduction <= 0.0F) {
            return true;
        }

        int safeAmplifier = Math.max(0, amplifier);
        float scaledReduction = baseReduction * (safeAmplifier + 1);
        if (!Float.isFinite(scaledReduction) || scaledReduction <= 0.0F) {
            return true;
        }

        // 26.2: FoodData no longer exposes accumulated exhaustion, so bleed it off
        // whenever the player is not fully fed. Same rate as before, with the
        // banked buffer capped (see STATES) to preserve the original never-below-zero cap.
        FoodData foodData = player.getFoodData();
        if (!foodData.needsFood() && foodData.getSaturationLevel() <= 0.0F) {
            return true;
        }

        UUID id = player.getUUID();
        State state = STATES.get(id);
        float banked = state == null ? 0.0F : state.banked;
        if (state != null && (foodData.getFoodLevel() < state.food || foodData.getSaturationLevel() < state.saturation)) {
            banked = 0.0F;
        }
        float delta = Math.min(scaledReduction, Math.max(0.0F, MAX_BANKED_EXHAUSTION - banked));
        if (delta > 0.0F) {
            foodData.addExhaustion(-delta);
            banked += delta;
        }
        if (banked > 0.0F) {
            STATES.put(id, new State(banked, foodData.getFoodLevel(), foodData.getSaturationLevel()));
        } else {
            STATES.remove(id);
        }
        return true;
    }

    @Override
    public void onMobRemoved(net.minecraft.server.level.ServerLevel level, LivingEntity entity, int amplifier, net.minecraft.world.entity.Entity.RemovalReason removalReason) {
        if (entity instanceof Player player) {
            STATES.remove(player.getUUID());
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = Math.max(1, PlatformHelper.getVitalityEffectInterval());
        return duration % interval == 0;
    }
}
