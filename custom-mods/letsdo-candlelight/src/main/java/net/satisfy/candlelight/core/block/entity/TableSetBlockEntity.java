package net.satisfy.candlelight.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TableSetBlockEntity extends StorageBlockEntity {
    private ItemStack effectStack = ItemStack.EMPTY;
    private int effectDuration = 0;

    public TableSetBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, 1);
    }

    public TableSetBlockEntity(BlockPos pos, BlockState state, int size) {
        super(pos, state, size);
    }

    public ItemStack getEffectStack() {
        return this.effectStack;
    }

    public int getEffectDuration() {
        return this.effectDuration;
    }

    public void setEffectStack(ItemStack stack, int duration) {
        this.effectStack = stack;
        this.effectDuration = duration;
        setChanged();
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.effectStack = input.read("EffectStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.effectDuration = input.getIntOr("EffectDuration", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.effectStack.isEmpty()) {
            output.store("EffectStack", ItemStack.OPTIONAL_CODEC, this.effectStack);
            output.putInt("EffectDuration", this.effectDuration);
        }
    }
}
