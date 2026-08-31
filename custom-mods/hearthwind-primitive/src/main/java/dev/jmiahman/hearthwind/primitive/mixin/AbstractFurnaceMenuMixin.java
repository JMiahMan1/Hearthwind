package dev.jmiahman.hearthwind.primitive.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Menu changes for the 4-slot blast furnace:
 *  - container-size checks 3 -> 4 when the menu is a blast furnace
 *  - a 4th Slot (index 3) at (76,17) holding the extra ingredient
 *  - quickMoveStack with shifted player-inventory ranges
 *
 * Extends AbstractContainerMenu so the inherited slots field and
 * moveItemStackTo/addSlot helpers are directly reachable (shadows only
 * resolve in the target class itself).
 */
@Mixin(AbstractFurnaceMenu.class)
public abstract class AbstractFurnaceMenuMixin extends AbstractContainerMenu {

    public AbstractFurnaceMenuMixin(MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow
    @Final
    private Container container;

    @Shadow
    protected abstract boolean canSmelt(ItemStack stack);

    @Shadow
    protected abstract boolean isFuel(ItemStack stack);

    private static final String SERVER_CTOR = "<init>(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/inventory/RecipeBookType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V";
    private static final String CLIENT_CTOR = "<init>(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/inventory/RecipeBookType;ILnet/minecraft/world/entity/player/Inventory;)V";

    @ModifyConstant(method = SERVER_CTOR, constant = @Constant(intValue = 3))
    private static int hearthwind$serverContainerSize(int original, MenuType<?> type) {
        return type == MenuType.BLAST_FURNACE ? 4 : original;
    }

    @ModifyConstant(method = CLIENT_CTOR, constant = @Constant(intValue = 3))
    private static int hearthwind$clientContainerSize(int original, MenuType<?> type) {
        return type == MenuType.BLAST_FURNACE ? 4 : original;
    }

    @Inject(method = SERVER_CTOR, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractFurnaceMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;", ordinal = 2, shift = At.Shift.AFTER))
    private void hearthwind$addExtraSlot(CallbackInfo ci) {
        if ((Object) this instanceof BlastFurnaceMenu) {
            this.addSlot(new Slot(this.container, 3, 76, 17));
        }
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void hearthwind$quickMove(net.minecraft.world.entity.player.Player player, int index,
            CallbackInfoReturnable<ItemStack> cir) {
        if (!((Object) this instanceof BlastFurnaceMenu)) {
            return;
        }
        // Slots: 0 input, 1 fuel, 2 result, 3 extra; player inventory 4..39
        // (main 4..30, hotbar 31..39). Vanilla layout shifted by the extra slot.
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        ItemStack original = slot.getItem();
        ItemStack stack = original.copy();
        if (index == 2) {
            if (!this.moveItemStackTo(stack, 4, 39, true)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
            slot.onQuickCraft(stack, original);
        } else if (index == 0 || index == 1 || index == 3) {
            if (!this.moveItemStackTo(stack, 4, 39, false)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else {
            if (this.canSmelt(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
            } else if (this.isFuel(stack)) {
                if (!this.moveItemStackTo(stack, 1, 2, false)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
            } else if (index < 31) {
                if (!this.moveItemStackTo(stack, 31, 39, false)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
            } else if (index < 39) {
                if (!this.moveItemStackTo(stack, 4, 31, false)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
            } else {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        slot.onTake(player, stack);
        cir.setReturnValue(stack);
    }
}
