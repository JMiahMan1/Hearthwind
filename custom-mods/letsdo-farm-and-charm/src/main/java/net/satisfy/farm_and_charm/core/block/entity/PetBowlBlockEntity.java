package net.satisfy.farm_and_charm.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.farm_and_charm.core.block.PetBowlBlock;
import net.satisfy.farm_and_charm.core.registry.EntityTypeRegistry;
import net.satisfy.farm_and_charm.core.registry.ObjectRegistry;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PetBowlBlockEntity extends BlockEntity implements WorldlyContainer, TextEditableBlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private boolean updatingState = false;
    private boolean fedDog;
    private boolean fedCat;
    private Component text = Component.literal("");

    public PetBowlBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.PET_BOWL_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return (stack.is(ObjectRegistry.DOG_FOOD.get()) || stack.is(ObjectRegistry.CAT_FOOD.get()))
                && getItem(slot).getCount() < 4;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        setChanged();
        return stack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = ContainerHelper.takeItem(items, slot);
        setChanged();
        return stack;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        } else {
            ItemStack current = items.get(slot);
            if (current.isEmpty()) {
                items.set(slot, stack.copy());
            } else if (ItemStack.isSameItemSameComponents(current, stack)) {
                int newCount = Math.min(4, current.getCount() + stack.getCount());
                current.setCount(newCount);
                items.set(slot, current);
            }
        }
        setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);
        fedDog = input.getBooleanOr("FedDog", false);
        fedCat = input.getBooleanOr("FedCat", false);
        if (input.read("Text0", com.mojang.serialization.Codec.STRING).isPresent()) {
            text = net.minecraft.network.chat.ComponentSerialization.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, com.google.gson.JsonParser.parseString(input.getStringOr("Text0", "\"\""))).result().orElse(net.minecraft.network.chat.Component.empty());
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putBoolean("FedDog", fedDog);
        output.putBoolean("FedCat", fedCat);
        output.putString("Text0", net.minecraft.network.chat.ComponentSerialization.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, text).result().map(Object::toString).orElse(""));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!updatingState) {
            updatingState = true;
            updateBlockState();
            updatingState = false;
        }
    }

    private void updateBlockState() {
        if (level != null && !level.isClientSide()) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getBlock() instanceof PetBowlBlock) {
                GeneralUtil.FoodType newType = GeneralUtil.FoodType.NONE;
                ItemStack stack = items.get(0);
                if (!stack.isEmpty()) {
                    if (stack.is(ObjectRegistry.CAT_FOOD.get())) newType = GeneralUtil.FoodType.CAT;
                    else if (stack.is(ObjectRegistry.DOG_FOOD.get())) newType = GeneralUtil.FoodType.DOG;
                }

                if (state.getValue(PetBowlBlock.FOOD_TYPE) != newType) {
                    BlockState newState = state.setValue(PetBowlBlock.FOOD_TYPE, newType);
                    level.setBlock(worldPosition, newState, 3);
                    level.sendBlockUpdated(worldPosition, state, newState, 3);
                }
            }
        }
    }

    public void onFed(ItemStack stack) {
        if (stack.is(ObjectRegistry.DOG_FOOD.get())) {
            fedDog = true;
        } else if (stack.is(ObjectRegistry.CAT_FOOD.get())) {
            fedCat = true;
        }
    }

    public void decreaseFood() {
        ItemStack stack = items.get(0);
        if (!stack.isEmpty()) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                items.set(0, ItemStack.EMPTY);
            }
            setChanged();
        }
    }

    public boolean wasCatFed() {
        return fedCat;
    }

    public void resetFedFlags() {
        fedDog = false;
        fedCat = false;
        setChanged();
    }

    public Component getText() {
        return text;
    }

    @Override
    public void setText(int line, Component text) {
        if (line == 0) {
            this.text = text;
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public int getTextLineCount() {
        return 1;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        var out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, provider);
        saveAdditional(out);
        CompoundTag extra = out.buildResult();
        for (String key : extra.keySet()) {
            net.minecraft.nbt.Tag v = extra.get(key);
            if (v != null) tag.put(key, v);
        }
        return tag;
    }

    public boolean canBeUsedBy(Animal animal) {
        if (text.getString().isBlank()) return true;
        return animal.hasCustomName() && animal.getName().getString().equals(text.getString());
    }
}