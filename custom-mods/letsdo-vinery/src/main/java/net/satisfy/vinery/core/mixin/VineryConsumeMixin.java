package net.satisfy.vinery.core.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import net.satisfy.vinery.core.registry.DataComponentRegistry;
import net.satisfy.vinery.core.util.FoodComponent;
import net.satisfy.vinery.core.util.WineYears;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

// 26.2: consumption runs through Consumable.onConsume. Applies CUSTOM_FOOD
// component effects the way the old LivingEntity.eat hook did.
@Mixin(Consumable.class)
public abstract class VineryConsumeMixin {
	@Inject(method = "onConsume", at = @At("TAIL"))
	private void vinery$applyCustomFoodEffects(Level level, LivingEntity entity, ItemStack stack,
			CallbackInfoReturnable<ItemStack> cir) {
		if (!stack.has(DataComponentRegistry.CUSTOM_FOOD.get())) {
			return;
		}
		FoodComponent foodComponent = stack.get(DataComponentRegistry.CUSTOM_FOOD.get());
		if (foodComponent == null) {
			return;
		}
		List<FoodComponent.EffectEntry> list = foodComponent.getEffects();
		for (FoodComponent.EffectEntry effect : list) {
			if (level.isClientSide() || !(level.getRandom().nextFloat() < effect.probability())) continue;
			MobEffectInstance base = effect.instance();
			int amplifier = WineYears.getEffectLevel(stack, level);
			int duration = base.getDuration();
			if (base.getEffect().equals(MobEffects.INSTANT_HEALTH) || base.getEffect().equals(MobEffects.INSTANT_DAMAGE)) {
				duration = 1;
			}
			entity.addEffect(new MobEffectInstance(base.getEffect(), duration, amplifier));
		}
	}
}
