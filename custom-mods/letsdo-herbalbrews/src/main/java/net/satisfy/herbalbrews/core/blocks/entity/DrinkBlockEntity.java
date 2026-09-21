package net.satisfy.herbalbrews.core.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.satisfy.herbalbrews.core.registry.EntityTypeRegistry;

public class DrinkBlockEntity extends BlockEntity {
    
    private CompoundTag storedNbt;

    public DrinkBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.DRINK_BLOCK_ENTITY.get(), pos, state);
    }

    public void setStoredNbt(CompoundTag tag) {
        this.storedNbt = tag;
    }

    public CompoundTag getStoredNbt() {
        return storedNbt;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedNbt = input.read("StoredNbt", CompoundTag.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (storedNbt != null) {
            output.store("StoredNbt", CompoundTag.CODEC, storedNbt);
        }
    }
}
