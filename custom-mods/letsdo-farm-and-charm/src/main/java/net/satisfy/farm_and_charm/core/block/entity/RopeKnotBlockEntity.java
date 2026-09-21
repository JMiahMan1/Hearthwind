package net.satisfy.farm_and_charm.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.farm_and_charm.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class RopeKnotBlockEntity extends BlockEntity {
    private BlockState held;

    public RopeKnotBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.ROPE_KNOT_BLOCK_ENTITY.get(), pos, state);
    }

    public BlockState getHeldBlock() {
        return held;
    }

    public void setHeldBlock(BlockState state) {
        this.held = state;
        setChanged();
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        if (held != null) {
            output.store("Held", BlockState.CODEC, held);
        }
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.held = input.read("Held", BlockState.CODEC).orElse(null);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }
}
