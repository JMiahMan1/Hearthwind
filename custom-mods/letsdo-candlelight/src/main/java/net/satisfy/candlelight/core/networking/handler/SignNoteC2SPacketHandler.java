package net.satisfy.candlelight.core.networking.handler;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.satisfy.candlelight.core.networking.packet.SignNoteC2SPacket;
import net.satisfy.candlelight.core.registry.ObjectRegistry;

public class SignNoteC2SPacketHandler implements NetworkManager.NetworkReceiver<SignNoteC2SPacket>{

    @Override
    public void receive(SignNoteC2SPacket packet, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        CompoundTag stack = packet.stack();
        int slot = packet.slot();
        boolean sign = packet.sign();

        ItemStack itemStack = new ItemStack(ObjectRegistry.NOTE_PAPER_WRITTEN.get());
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(stack));

        if (sign) {
            player.getInventory().setItem(slot, itemStack);
        }
    }
}
