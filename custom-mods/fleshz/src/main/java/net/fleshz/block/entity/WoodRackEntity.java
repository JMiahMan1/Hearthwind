package net.fleshz.block.entity;

import org.jetbrains.annotations.Nullable;

import net.fleshz.init.BlockInit;
import net.fleshz.init.RecipeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class WoodRackEntity extends BlockEntity implements Container {
    @Nullable
    public Item result;
    public int index;
    public int dryingTime;
    private int processTime;
    private NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);

    public WoodRackEntity(BlockPos pos, BlockState state) {
        super(BlockInit.WOOD_RACK_ENTITY, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.dryingTime = input.getIntOr("Drying_Time", 10000);
        this.index = input.getIntOr("Rack_Index", 0);
        this.inventory.clear();
        net.minecraft.world.ContainerHelper.loadAllItems(input, this.inventory);
        if (!isEmpty() && !RecipeInit.RACK_RESULT_ITEM_LIST.isEmpty() && RecipeInit.RACK_RESULT_ITEM_LIST.size() > index)
            this.result = RecipeInit.RACK_RESULT_ITEM_LIST.get(index);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Drying_Time", dryingTime);
        output.putInt("Rack_Index", index);
        net.minecraft.world.ContainerHelper.saveAllItems(output, inventory);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WoodRackEntity blockEntity) {
        blockEntity.update();
    }

    private void update() {
        if (this.level != null && !this.level.isClientSide() && !this.isEmpty()
                && RecipeInit.RACK_ITEM_LIST.contains(this.getItem(0).getItem())) {
            ++this.processTime;
            if (this.processTime >= this.dryingTime) {
                this.setItem(0, new ItemStack(this.result != null ? this.result : Items.AIR));
                this.processTime = 0;
            }
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        sendUpdate();
    }

    private void sendUpdate() {
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
        this.setChanged();
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.getItem(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.get(0);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = net.minecraft.world.ContainerHelper.removeItem(this.inventory, slot, 1);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.clearContent();
        this.inventory.set(0, stack);
        if (stack.isEmpty()) {
            this.dryingTime = 10000;
            this.result = null;
            this.index = 0;
        } else if (RecipeInit.RACK_ITEM_LIST.contains(stack.getItem())) {
            int idx = RecipeInit.RACK_ITEM_LIST.indexOf(stack.getItem());
            this.dryingTime = RecipeInit.RACK_RESULT_TIME_LIST.get(idx);
            this.result = RecipeInit.RACK_RESULT_ITEM_LIST.get(idx);
            this.index = idx;
        }
        this.setChanged();
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        this.setChanged();
        return net.minecraft.world.ContainerHelper.takeItem(this.inventory, slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public net.minecraft.network.protocol.Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
