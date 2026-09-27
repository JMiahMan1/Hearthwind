package net.backslot.mixin;

import net.backslot.AttachmentContainer;
import net.backslot.BackSlot;
import net.backslot.slot.BackItemSlot;
import net.backslot.slot.BeltItemSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds the back and belt slots to the player inventory menu at the Aged
 * positions (vanilla menu ids 46 and 47, right of the shield slot).
 */
@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends net.minecraft.world.inventory.AbstractContainerMenu {

    /**
     * Shape-only constructor: the mixin extends the target so {@code addSlot}
     * is reachable (it is protected), and the real constructor never runs.
     */
    private InventoryMenuMixin() {
        super(null, 0);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void backslot$addSlots(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        this.addSlot(new BackItemSlot(new AttachmentContainer(owner, BackSlot.BACK_ITEM),
                77 + BackSlot.CONFIG.backSlotX, 44 + BackSlot.CONFIG.backSlotY));
        this.addSlot(new BeltItemSlot(new AttachmentContainer(owner, BackSlot.BELT_ITEM),
                77 + BackSlot.CONFIG.beltSlotX, 26 + BackSlot.CONFIG.beltSlotY));
    }

    /** Our two slots sit past the vanilla shift-click ranges; leave them alone. */
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void backslot$guardQuickMove(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return;
        }
        var slot = this.slots.get(slotIndex);
        if (slot instanceof BackItemSlot || slot instanceof BeltItemSlot) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
