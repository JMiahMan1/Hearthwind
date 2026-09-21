package net.satisfy.candlelight.core.block.entity;

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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.satisfy.candlelight.core.registry.EntityTypeRegistry;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;

public class StorageBlockEntity extends BlockEntity {
    private int size;
    private NonNullList<ItemStack> inventory;

    public StorageBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.STORAGE_BLOCK_ENTITY.get(), pos, state);
        this.size = 0;
        this.inventory = NonNullList.withSize(this.size, ItemStack.EMPTY);
    }

    public StorageBlockEntity(BlockPos pos, BlockState state, int size) {
        super(EntityTypeRegistry.STORAGE_BLOCK_ENTITY.get(), pos, state);
        this.size = size;
        this.inventory = NonNullList.withSize(this.size, ItemStack.EMPTY);
    }

    public ItemStack removeStack(int slot) {
        ItemStack stack = this.inventory.set(slot, ItemStack.EMPTY);
        this.setChanged();
        return stack;
    }

    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        this.setChanged();
    }

    @Override
    public void setChanged() {
        if (level != null && !level.isClientSide()) {
            Packet<ClientGamePacketListener> updatePacket = this.getUpdatePacket();
            for (ServerPlayer player : GeneralUtil.tracking((ServerLevel) level, this.getBlockPos())) {
                player.connection.send(updatePacket);
            }
        }

        super.setChanged();
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.size = input.getIntOr("size", this.size);
        this.inventory = NonNullList.withSize(this.size, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.inventory);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        ContainerHelper.saveAllItems(output, this.inventory);
        output.putInt("size", this.size);
        super.saveAdditional(output);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    public NonNullList<ItemStack> getInventory() {
        return this.inventory;
    }
}
