package dev.jmiahman.hearthwind.survival.hydration;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
            bubble(this.level, this.worldPosition);
        }
    }

    /**
     * The boil makes noise. Dehydration 1.3.6 plays its own
     * {@code cauldron_bubble} sound on a 1-in-12 display tick while the fire is
     * burning and the cauldron holds water, at volume {@code 0.5 + random * 0.4}
     * and pitch 1.0.
     *
     * <p>Two deliberate differences. We play vanilla's
     * {@code BUBBLE_COLUMN_BUBBLE_POP} instead of Dehydration's own sound file,
     * because this pack does not redistribute another pack's audio and the
     * vanilla bubble pop is the same idea. And we play it from the server tick
     * rather than the display tick, because 26.x's {@code animateTick} is
     * client-only - the reference ran on both sides in 1.20.1, and from the
     * server every player near the fire hears the boil instead of only the one
     * looking at it.
     */
    private void bubble(Level level, BlockPos pos) {
        // Level.random is protected, and serverTick is handed a plain Level, so
        // the roll comes from the world's own public accessor.
        RandomSource random = level.getRandom();
        if (random.nextInt(12) != 0) {
            return;
        }
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS,
                0.5F + random.nextFloat() * 0.4F, 1.0F);
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
