package net.satisfy.candlelight.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.satisfy.candlelight.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.Nullable;

public class TypewriterEntity extends BlockEntity {
    public static final String PAPER_KEY = "paper";
    ItemStack paper = ItemStack.EMPTY;
    private int spaceTicks;
    private int enterTicks;
    private int keyBounceTicks;
    private float lineProgress;
    private int rollerSnapTicks;
    private int bouncingKeyIndex = -1;

    public TypewriterEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.TYPE_WRITER_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack getPaper() {
        return paper;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!paper.isEmpty()) {
            output.store(PAPER_KEY, ItemStack.OPTIONAL_CODEC, paper);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        paper = input.read(PAPER_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Nullable
    @SuppressWarnings("unused")
    public Packet<ClientGamePacketListener> toUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @SuppressWarnings("unused")
    public CompoundTag toInitialChunkDataNbt(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    public void addPaper(ItemStack itemStack) {
        paper = itemStack;
        setChanged();
    }

    public void removePaper() {
        paper = ItemStack.EMPTY;
        setChanged();
    }

    public void triggerSpace() {
        this.spaceTicks = 3;
    }

    public void triggerEnter() {
        this.enterTicks = 3;
    }

    public void triggerKeyBounce() {
        this.keyBounceTicks = 2;
        this.bouncingKeyIndex = -1;
    }

    public int getSpaceTicks() {
        return spaceTicks;
    }

    public int getEnterTicks() {
        return enterTicks;
    }

    public int getKeyBounceTicks() {
        return keyBounceTicks;
    }

    public void tickAnimations() {
        if (this.spaceTicks > 0) this.spaceTicks--;
        if (this.enterTicks > 0) this.enterTicks--;
        if (this.keyBounceTicks > 0) this.keyBounceTicks--;
        if (this.rollerSnapTicks > 0) this.rollerSnapTicks--;
        if (this.keyBounceTicks == 0) this.bouncingKeyIndex = -1;
    }

    public void setLineProgress(float v) {
        this.lineProgress = v;
    }

    public float getLineProgress() {
        return lineProgress;
    }

    public void snapRoller() {
        this.rollerSnapTicks = 4;
        this.lineProgress = 0f;
    }

    public int getRollerSnapTicks() {
        return rollerSnapTicks;
    }

    public int getBouncingKeyIndex() {
        return bouncingKeyIndex;
    }

    public void setBouncingKeyIndex(int idx) {
        this.bouncingKeyIndex = idx;
    }
}
