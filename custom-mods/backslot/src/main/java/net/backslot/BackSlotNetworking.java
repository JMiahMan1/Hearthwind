package net.backslot;

import net.backslot.network.SwitchSlotPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Server side of the G / G+Shift swap, mirroring upstream's switch packet. */
public final class BackSlotNetworking {

    private BackSlotNetworking() {}

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(SwitchSlotPayload.TYPE, SwitchSlotPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SwitchSlotPayload.TYPE, (payload, context) ->
                context.server().execute(() -> switchSlot(context.player(), payload.slot())));
    }

    public static void switchSlot(Player player, int slot) {
        if (slot != BackSlot.BACK_SLOT && slot != BackSlot.BELT_SLOT) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty() && !BackSlotSlots.isItemAllowed(held, slot)) {
            return;
        }
        ItemStack stored = BackSlotSlots.get(player, slot);
        BackSlotSlots.set(player, slot, held.copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, stored);
    }
}
