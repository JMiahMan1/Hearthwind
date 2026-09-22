package dev.jmiahman.hearthwind.primitive.mixin;

import dev.jmiahman.hearthwind.primitive.HearthwindPrimitiveItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Earlystage parity: force a ~100-tick disable cooldown on the wooden
 * shield whenever vanilla disables blocking (26.x Player#blockUsingItem
 * path; damageShield/disableShield methods are gone).
 *
 * BLOCKS_ATTACKS on the item handles damage/break; this only pins the
 * cooldown to earlystage's 100 ticks via disableCooldownScale=5 on a
 * 0.25s base (vanilla axe), plus an explicit floor of 100 after disable.
 */
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(net.minecraft.world.entity.EntityType<? extends LivingEntity> type,
            net.minecraft.world.level.Level level) {
        super(type, level);
    }

    @Inject(method = "blockUsingItem", at = @At("TAIL"))
    private void hearthwind$woodenShieldDisableCooldown(ServerLevel level, LivingEntity attacker,
            DamageSource source, float damage, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        ItemStack blocking = self.getItemBlockingWith();
        // After vanilla disable() ran, ensure wooden shield is on a 100-tick
        // cooldown when the attacker can disable blocking at all.
        if (blocking != null
                && blocking.is(HearthwindPrimitiveItems.WOODEN_SHIELD)
                && attacker.getSecondsToDisableBlocking() > 0.0F
                && !self.getAbilities().instabuild) {
            self.getCooldowns().addCooldown(blocking, 100);
        }
    }
}
