package dev.jmiahman.hearthwind.survival.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.jmiahman.hearthwind.survival.EnvironmentzItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.component.CustomData;

/**
 * Clean-room port of EnvironmentZ 2.0.8's anvil behaviour (upstream
 * AnvilScreenHandlerMixin, GPL-3.0 studied, reauthored):
 * <ul>
 *   <li>armor + environmentz:insolating_item (polar bear fur etc.) adds the
 *       {@code environmentz} custom-data key - treated as insulated armor (+3);</li>
 *   <li>armor + any environmentz:ice_items adds {@code iced} =
 *       {@link EnvironmentzItems#COOLING_HEATING_VALUE}, cooled armor (-5);</li>
 *   <li>shears strip the {@code environmentz} key back off.</li>
 * </ul>
 * Warm armor (warm_armor tag) is excluded from both, like the reference.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuTemperatureMixin {

    @Unique
    private boolean hearthwind$temperatureResult;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void hearthwind$createTemperatureResult(CallbackInfo ci) {
        AnvilMenu self = (AnvilMenu) (Object) this;
        ItemStack result = temperatureResult(
                self.getSlot(AnvilMenu.INPUT_SLOT).getItem(),
                self.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem());
        if (result != null) {
            self.getSlot(AnvilMenu.RESULT_SLOT).set(result);
            hearthwind$temperatureResult = true;
            ci.cancel();
        } else {
            hearthwind$temperatureResult = false;
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void hearthwind$mayPickup(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (hearthwind$temperatureResult) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void hearthwind$onTake(Player player, ItemStack stack, CallbackInfo ci) {
        if (!hearthwind$temperatureResult) {
            return;
        }
        AnvilMenu self = (AnvilMenu) (Object) this;
        boolean shears = self.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem().getItem() instanceof ShearsItem;
        self.getSlot(AnvilMenu.INPUT_SLOT).set(ItemStack.EMPTY);
        self.getSlot(AnvilMenu.ADDITIONAL_SLOT).set(ItemStack.EMPTY);
        player.level().playSound(null, player.blockPosition(),
                shears ? SoundEvents.SHEEP_SHEAR : SoundEvents.ANVIL_USE,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        hearthwind$temperatureResult = false;
        ci.cancel();
    }

    @Unique
    private static ItemStack temperatureResult(ItemStack base, ItemStack additive) {
        if (base.isEmpty() || additive.isEmpty() || !isArmor(base) || base.is(EnvironmentzItems.WARM_ARMOR)) {
            return null;
        }
        CustomData data = base.get(DataComponents.CUSTOM_DATA);
        boolean insulated = data != null && data.copyTag().contains("environmentz");

        if (additive.is(EnvironmentzItems.INSOLATING_ITEM) && !insulated) {
            ItemStack result = base.copy();
            CustomData.update(DataComponents.CUSTOM_DATA, result,
                    tag -> tag.putString("environmentz", "fur_insolated"));
            return result;
        }
        if (additive.is(EnvironmentzItems.ICE_ITEMS)) {
            ItemStack result = base.copy();
            CustomData.update(DataComponents.CUSTOM_DATA, result,
                    tag -> tag.putInt("iced", EnvironmentzItems.COOLING_HEATING_VALUE));
            return result;
        }
        if (additive.getItem() instanceof ShearsItem && insulated) {
            ItemStack result = base.copy();
            CustomData.update(DataComponents.CUSTOM_DATA, result,
                    tag -> tag.remove("environmentz"));
            return result;
        }
        return null;
    }

    @Unique
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
