package dev.jmiahman.hearthwind.skills.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import dev.jmiahman.hearthwind.skills.SkillProcs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

/**
 * Scales damage through the skill procs:
 * <ul>
 * <li>outgoing melee: LUCK crits and the max-level STRENGTH double hit;</li>
 * <li>incoming falls: AGILITY shaves damage off every fall.</li>
 * </ul>
 * {@code hurtServer} is the single server-side damage entry point for every
 * living entity (players included - they delegate to it), so one hook covers
 * mobs, players and PvP.
 */
@Mixin(LivingEntity.class)
public abstract class SkillProcDamageMixin {

    @ModifyVariable(
            method = "hurtServer",
            at = @At("HEAD"),
            ordinal = 0)
    private float hearthwind$skillProcDamage(float amount, ServerLevel level,
            DamageSource source) {
        float out = amount;
        LivingEntity self = (LivingEntity) (Object) this;

        if (source.is(DamageTypes.PLAYER_ATTACK)
                && source.getEntity() instanceof ServerPlayer attacker) {
            SkillProcs.MeleeRoll roll = SkillProcs.rollMelee(attacker, level.getRandom());
            out = SkillProcs.applyMelee(out, roll);
            if (roll.crit()) {
                attacker.crit(self);
            }
        }

        if (self instanceof ServerPlayer victim && source.is(DamageTypes.FALL)) {
            out = SkillProcs.applyFallProtection(victim, out);
        }
        return out;
    }
}
