package net.adventurez.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.adventurez.init.ItemInit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @Unique
    private int repairedAmount;

    @Shadow
    @Final
    private DataSlot cost;

    @Unique
    private ItemStack getInput(int index) {
        return ((AnvilMenu) (Object) this).getSlot(index).getItem();
    }

    @Unique
    private void setResult(ItemStack stack) {
        ((AnvilMenu) (Object) this).getSlot(2).container.setItem(0, stack);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void createResultMixin(CallbackInfo info) {
        ItemStack itemStack = getInput(0);
        if (itemStack.getItem() == ItemInit.PRIME_EYE && itemStack.getDamageValue() != 0) {
            ItemStack itemStack2 = getInput(1);
            if (itemStack2.getItem() == Items.ENDER_PEARL) {
                ItemStack itemStack3 = new ItemStack(ItemInit.PRIME_EYE);
                repairedAmount = itemStack2.getCount() > itemStack.getDamageValue() ? itemStack.getDamageValue() : itemStack2.getCount();
                itemStack3.setDamageValue(itemStack.getDamageValue() - repairedAmount);
                setResult(itemStack3);
                info.cancel();
            }
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void mayPickupMixin(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> info) {
        ItemStack itemStack = getInput(0);
        if (itemStack.getItem() == ItemInit.PRIME_EYE && itemStack.getDamageValue() != 0) {
            ItemStack itemStack2 = getInput(1);
            if (itemStack2.getItem() == Items.ENDER_PEARL) {
                info.setReturnValue(true);
            }
        }
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    public void onTakeMixin(Player player, ItemStack stack, CallbackInfo info) {
        ItemStack itemStack = getInput(0);
        if (itemStack.getItem() == ItemInit.PRIME_EYE) {
            ItemStack itemStack2 = getInput(1);
            if (itemStack2.getItem() == Items.ENDER_PEARL) {
                itemStack2.shrink(repairedAmount - 1);
                setResult(itemStack2);
                this.cost.set(0);
            }
        }
    }

}
