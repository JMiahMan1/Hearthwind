package net.satisfy.brewery.core.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.satisfy.brewery.core.registry.MobEffectRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Villager.class)
public class VillagerDiscountMixin {

    // 26.2: Villager.updateSpecialPrices(Player) is gone (trades now refresh
    // via updateTrades(ServerLevel), which has no player). Re-hooked onto
    // mobInteract so the Pintcharisma discount is (re)computed fresh every
    // time trading opens: reset first (no stacking across opens), then apply.
    @Inject(method = "mobInteract", at = @At("HEAD"))
    private void applyHasteDiscount(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffectRegistry.PINTCHARISMA.get()))) {
            Villager villager = (Villager) (Object) this;
            int discountLevel = Objects.requireNonNull(player.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffectRegistry.PINTCHARISMA.get()))).getAmplifier();
            double discountForEffect = 0.1 * (discountLevel + 1);
            for (MerchantOffer offer : villager.getOffers()) {
                offer.resetSpecialPriceDiff();
                int discount = (int) Math.floor(discountForEffect * offer.getBaseCostA().getCount());
                offer.addToSpecialPriceDiff(-Math.max(discount, 1));
            }
        }
    }
}