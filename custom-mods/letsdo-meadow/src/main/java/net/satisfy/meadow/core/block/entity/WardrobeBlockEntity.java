package net.satisfy.meadow.core.block.entity;

import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.world.level.storage.ValueInput;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.meadow.core.registry.EntityTypeRegistry;
import net.satisfy.meadow.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;

public class WardrobeBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);

    public static final int SLOT_HEAD = 0;
    public static final int SLOT_CHEST = 1;
    public static final int SLOT_LEGS = 2;
    public static final int SLOT_FEET = 3;

    public WardrobeBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.WARDROBE_BLOCK_ENTITY.get(), pos, state);
    }

    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack.copy());
        this.setChanged();
    }

    public ItemStack getItem(int slot) {
        return this.inventory.get(slot);
    }

    public NonNullList<ItemStack> getInventory() {
        return this.inventory;
    }

    @Override
    public void setChanged() {
        Level level = this.level;
        if (level instanceof ServerLevel serverLevel && !level.isClientSide()) {
            Packet<ClientGamePacketListener> updatePacket = this.getUpdatePacket();
            for (ServerPlayer player : GeneralUtil.tracking(serverLevel, this.getBlockPos())) {
                player.connection.send(updatePacket);
            }
        }
        super.setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.inventory.clear();
        ContainerHelper.loadAllItems(tag, this.inventory);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        ContainerHelper.saveAllItems(tag, this.inventory);
        super.saveAdditional(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }
}
