package net.satisfy.bakery.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.bakery.core.registry.EntityTypeRegistry;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;

public class StorageBlockEntity extends BlockEntity {
    private int size;

    private NonNullList<ItemStack> inventory;

    public StorageBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.STORAGE_ENTITY.get(), pos, state);
    }

    public StorageBlockEntity(BlockPos pos, BlockState state, int size) {
        super(EntityTypeRegistry.STORAGE_ENTITY.get(), pos, state);
        this.size = size;
        this.inventory = NonNullList.withSize(this.size, ItemStack.EMPTY);
    }

    public ItemStack removeStack(int slot) {
        ItemStack stack = inventory.set(slot, ItemStack.EMPTY);
        setChanged();
        return stack;
    }

    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
        if (level != null && !level.isClientSide()) {
            Packet<ClientGamePacketListener> updatePacket = getUpdatePacket();
            for (ServerPlayer player : GeneralUtil.tracking((ServerLevel) level, getBlockPos())) {
                player.connection.send(updatePacket);
            }
        }
        super.setChanged();
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.size = input.getIntOr("size", this.size);
        this.inventory = NonNullList.withSize(this.size, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.inventory);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.inventory);
        output.putInt("size", this.size);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    public void setInventory(NonNullList<ItemStack> inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            this.inventory.set(i, inventory.get(i));
        }
    }

    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }
}
