package dev.jmiahman.hearthwind.primitive;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Sieve block entity: one siftable stack (a block item with a drop template),
 * the number of taps performed, and the redstone auto-sift tick counter.
 *
 * Four taps complete a cycle: drops from the matching template are rolled and
 * the stack is consumed (earlystage parity).
 */
public class SieveBlockEntity extends BlockEntity implements Container {

    public static final String SIEVE_COUNT_KEY = "SieveCount";

    private int tick;
    private NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private int sieveCount;

    public SieveBlockEntity(BlockPos pos, BlockState state) {
        super(HearthwindPrimitiveBlocks.SIEVE_ENTITY, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inventory = NonNullList.withSize(1, ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(input, this.inventory);
        this.sieveCount = input.getIntOr(SIEVE_COUNT_KEY, 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        net.minecraft.world.ContainerHelper.saveAllItems(output, this.inventory);
        output.putInt(SIEVE_COUNT_KEY, this.sieveCount);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SieveBlockEntity blockEntity) {
        blockEntity.tickUpdate();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SieveBlockEntity blockEntity) {
        blockEntity.tickUpdate();
    }

    /** Redstone auto-sift driver; only ever ticked by RedstoneSieveBlock. */
    public void tickUpdate() {
        update();
    }

    private void update() {
        if (this.level == null) {
            return;
        }
        if (!this.isEmpty() && this.level.hasNeighborSignal(this.worldPosition)) {
            this.tick++;
            if (this.tick >= HearthwindPrimitiveConfig.get().redstoneSieveTicks) {
                this.sieve();
                this.tick = 0;
            }
        } else if (this.tick != 0) {
            this.tick = 0;
        }
    }

    public int getSieveCount() {
        return this.sieveCount;
    }

    public void refreshSieveCount() {
        this.sieveCount = 0;
    }

    public void sieve() {
        if (this.level == null) {
            return;
        }
        this.sieveCount++;
        if (this.sieveCount > 3) {
            if (!this.level.isClientSide()) {
                List<SieveBlock.SieveDrop> templates = SieveBlock.drops();
                for (SieveBlock.SieveDrop template : templates) {
                    if (SieveBlock.templateItem(template) == this.getItem(0).getItem()) {
                        for (SieveBlock.SieveEntry entry : template.entries()) {
                            for (int k = 0; k < entry.rolls(); k++) {
                                if (this.level.getRandom().nextFloat() <= entry.chance()) {
                                    dropItem(entry.itemId());
                                }
                            }
                        }
                        break;
                    }
                }
                this.clearContent();
            }
            this.level.playSound(null, this.worldPosition, SoundEvents.BRUSH_SAND_COMPLETED,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
        } else {
            this.level.playSound(null, this.worldPosition, SoundEvents.BRUSH_SAND,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    private void dropItem(Identifier itemId) {
        net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            return;
        }
        double d = this.level.getRandom().nextDouble() * 0.7f + 0.15f;
        double e = this.level.getRandom().nextDouble() * 0.7f + 0.06000000238418579 + 0.6;
        double g = this.level.getRandom().nextDouble() * 0.7f + 0.15f;

        ItemEntity itemEntity = new ItemEntity(this.level,
                this.worldPosition.getX() + d,
                this.worldPosition.getY() + e,
                this.worldPosition.getZ() + g,
                new ItemStack(item));
        itemEntity.setDefaultPickUpDelay();
        this.level.addFreshEntity(itemEntity);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        sendUpdate();
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        var pkt = net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
        return pkt;
    }

    private void sendUpdate() {
        if (this.level != null && !this.level.isClientSide()) {
            net.minecraft.world.level.block.state.BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
        setChanged();
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getItem(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.get(0);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = net.minecraft.world.ContainerHelper.removeItem(this.inventory, slot, 1);
        setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        setChanged();
        return net.minecraft.world.ContainerHelper.takeItem(this.inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.inventory.set(0, stack);
        refreshSieveCount();
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (this.isEmpty() && this.level != null
                && this.level.getBlockState(this.worldPosition.above()).isAir()) {
            return SieveBlock.isSiftable(stack);
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
