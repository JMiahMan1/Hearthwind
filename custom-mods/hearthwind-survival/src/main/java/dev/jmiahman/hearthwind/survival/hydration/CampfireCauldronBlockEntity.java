package dev.jmiahman.hearthwind.survival.hydration;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port of Dehydration 1.3.6 {@code CampfireCauldronEntity}: counts ticks
 * while the campfire below is lit and the cauldron holds water, then marks
 * the content boiled (purified). {@code water_boiling_time} comes from the
 * survival config (Aged default 100 ticks).
 */
public class CampfireCauldronBlockEntity extends BlockEntity {
    public static final String BOILED_KEY = "Boiled";

    public boolean isBoiled;
    private int ticker;

    public CampfireCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(HydrationBlocks.CAMPFIRE_CAULDRON_ENTITY, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.isBoiled = input.getBooleanOr(BOILED_KEY, false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean(BOILED_KEY, this.isBoiled);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state,
            CampfireCauldronBlockEntity blockEntity) {
        blockEntity.update();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            CampfireCauldronBlockEntity blockEntity) {
        blockEntity.update();
    }

    public void update() {
        if (this.level == null) {
            return;
        }
        BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof CampfireCauldronBlock block)) {
            return;
        }
        if (block.isFireBurning(this.level, this.worldPosition)
                && state.getValue(CampfireCauldronBlock.LEVEL) > 0
                && !this.isBoiled) {
            this.ticker++;
            if (this.ticker >= HearthwindSurvivalConfig.get().hydration.waterBoilingTime) {
                this.isBoiled = true;
                this.ticker = 0;
                this.setChanged();
            }
        }
    }

    /** Called when new (non-purified) water lands in the cauldron. */
    public void onFillingCauldron() {
        this.isBoiled = false;
        this.ticker = 0;
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
