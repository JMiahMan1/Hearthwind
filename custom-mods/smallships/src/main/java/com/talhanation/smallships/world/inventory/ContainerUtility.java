package com.talhanation.smallships.world.inventory;

import com.talhanation.smallships.world.entity.ship.ContainerShip;
import com.talhanation.smallships.world.inventory.fabric.ContainerUtilityImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerUtility {
    public static void openShipMenu(Player player, ContainerShip containerShip) {
        ContainerUtilityImpl.openShipMenu(player, containerShip);
    }

    public static ItemStack parse(HolderLookup.Provider provider, Tag tag) {
        if (tag == null) return ItemStack.EMPTY;
        return ItemStack.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
    }

    public static Tag save(HolderLookup.Provider provider, ItemStack stack) {
        if (stack.isEmpty()) return new CompoundTag();
        return ItemStack.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack).result().orElse(new CompoundTag());
    }

    public static void loadAllItems(CompoundTag tag, NonNullList<ItemStack> itemStacks, HolderLookup.Provider levelRegistry) {
        ListTag listTag = tag.getListOrEmpty("Items");

        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i).orElse(new CompoundTag());
            int slot = compoundTag.getByte("Slot").orElse((byte) 0) & 255;
            if (slot < itemStacks.size()) {
                itemStacks.set(slot, parse(levelRegistry, compoundTag));
            }
        }
    }

    public static CompoundTag saveAllItems(CompoundTag tag, NonNullList<ItemStack> itemStacks, HolderLookup.Provider levelRegistry) {
        ListTag listTag = new ListTag();

        for (int i = 0; i < itemStacks.size(); ++i) {
            ItemStack itemStack = itemStacks.get(i);
            if (!itemStack.isEmpty()) {
                Tag itemTag = save(levelRegistry, itemStack);
                if (itemTag instanceof CompoundTag compoundTag) {
                    compoundTag.putByte("Slot", (byte) i);
                    listTag.add(compoundTag);
                }
            }
        }

        tag.put("Items", listTag);
        return tag;
    }
}
