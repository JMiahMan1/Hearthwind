package dev.jmiahman.hearthwind.primitive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Crafting rock block entity: a 3x3 "pot" of ingredients sitting on the rock,
 * plus the remaining hits before the next craft attempt and the lifetime hit
 * counter that eventually wears the rock away (earlystage parity).
 */
public class CraftingRockBlockEntity extends BlockEntity implements Container {

    public static final String CRAFT_HITS_KEY = "CraftHits";
    public static final String TOTAL_HITS_KEY = "TotalHits";

    private NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);
    private int craftHits = 0;
    private int totalHits = 0;

    public CraftingRockBlockEntity(BlockPos pos, BlockState state) {
        super(HearthwindPrimitiveBlocks.CRAFTING_ROCK_ENTITY, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inventory = NonNullList.withSize(9, ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(input, this.inventory);
        this.craftHits = input.getIntOr(CRAFT_HITS_KEY, 0);
        this.totalHits = input.getIntOr(TOTAL_HITS_KEY, 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        net.minecraft.world.ContainerHelper.saveAllItems(output, this.inventory);
        output.putInt(CRAFT_HITS_KEY, this.craftHits);
        output.putInt(TOTAL_HITS_KEY, this.totalHits);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        sendUpdate();
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    private void sendUpdate() {
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    public void setCraftHits(int hits) {
        this.craftHits = hits;
    }

    public int getCraftHits() {
        return this.craftHits;
    }

    public int getTotalHits() {
        return this.totalHits;
    }

    public void decreaseCraftHits(Entity entity) {
        this.craftHits--;
        this.totalHits++;
        if (this.level != null && !this.level.isClientSide()
                && HearthwindPrimitiveConfig.get().craftRockMaxCraftHits <= this.totalHits) {
            this.level.destroyBlock(this.worldPosition, false, entity, 512);
        }
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
        setChanged();
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = net.minecraft.world.ContainerHelper.removeItem(this.inventory, slot, amount);
        setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        setChanged();
        return net.minecraft.world.ContainerHelper.takeItem(this.inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return true;
    }
}
