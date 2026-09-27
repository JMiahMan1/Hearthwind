package draylar.inmis.network;

import draylar.inmis.Inmis;
import draylar.inmis.item.BackpackItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class InmisNetworking {

    private InmisNetworking() {
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(BackpackOpenPacket.TYPE, BackpackOpenPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BackpackOpenPacket.TYPE,
                (payload, context) -> context.server().execute(() -> openFirstBackpack(context.player())));
    }

    /** The B key opens the chest-slot backpack, else the first carried one. */
    public static void openFirstBackpack(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof BackpackItem) {
            BackpackItem.openScreen(player, chest);
            return;
        }

        Inventory inventory = player.getInventory();
        int until = Inmis.CONFIG.requireArmorTrinketToOpen ? 0 : inventory.getContainerSize();
        for (int i = 0; i < until; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof BackpackItem) {
                BackpackItem.openScreen(player, stack);
                return;
            }
        }

        ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (!Inmis.CONFIG.requireArmorTrinketToOpen && offhand.getItem() instanceof BackpackItem) {
            BackpackItem.openScreen(player, offhand);
            return;
        }

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(Inmis.ENDER_POUCH)) {
                Inmis.openEnderChest(player);
                return;
            }
        }
    }
}
