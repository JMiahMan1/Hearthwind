package dev.jmiahman.hearthwind.flora.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class StorageBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> items;

    public StorageBlockEntity(BlockPos pos, BlockState state) {
        super(FloraBlockEntities.STORAGE, pos, state);
        this.items = NonNullList.withSize(4, ItemStack.EMPTY);
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // Take item from top filled slot
        if (held.isEmpty() || player.isShiftKeyDown()) {
            for (int i = items.size() - 1; i >= 0; i--) {
                if (!items.get(i).isEmpty()) {
                    ItemStack extracted = items.get(i).copy();
                    items.set(i, ItemStack.EMPTY);
                    if (!player.getInventory().add(extracted)) player.drop(extracted, false);
                    setChanged();
                    if (level != null) level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.2f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // Place bottle/item into first free slot
        if (!held.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).isEmpty()) {
                    items.set(i, held.split(1));
                    setChanged();
                    if (level != null) level.playSound(null, worldPosition, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 0.8f, 1.4f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }
}
