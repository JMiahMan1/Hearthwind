package net.backslot;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** One-item container view over a player's back or belt attachment. */
public class AttachmentContainer implements Container {

    private final Player player;
    private final AttachmentType<ItemStack> type;

    public AttachmentContainer(Player player, AttachmentType<ItemStack> type) {
        this.player = player;
        this.type = type;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getItem(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return player.getAttachedOrElse(type, ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack current = getItem(0);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack split = current.split(count);
        setItem(0, current);
        return split;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack current = getItem(0);
        setItem(0, ItemStack.EMPTY);
        return current;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        player.setAttached(type, stack);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player viewer) {
        return true;
    }

    @Override
    public void clearContent() {
        setItem(0, ItemStack.EMPTY);
    }
}
