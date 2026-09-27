package dev.jmiahman.hearthwind.survival.hydration;

import dev.jmiahman.hearthwind.survival.FlaskItems;
import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import dev.jmiahman.hearthwind.survival.LeatherFlaskItem;
import dev.jmiahman.hearthwind.survival.PurifiedWater;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Clean-room reimplementation of Dehydration's bamboo pump block entity
 * (upstream is GPLv3; the behaviour was decoded from the shipped bytecode
 * and nothing was copied).
 *
 * <p>The pump holds a single container - a bucket, a glass bottle or a
 * leather flask. Squeezing it adds to {@link #getPumpCount()}; a bucket
 * needs more than three pumps, a bottle fills on the first, and a flask
 * gains two units. Every conversion arms {@link #getCooldown()} for
 * {@code hydration.pumpCooldown} ticks.
 */
public class BambooPumpBlockEntity extends BlockEntity implements Container {
    private static final int SLOT_COUNT = 1;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int pumpCount;
    private int cooldown;

    public BambooPumpBlockEntity(BlockPos pos, BlockState state) {
        super(HydrationBlocks.BAMBOO_PUMP_ENTITY, pos, state);
    }

    /** Pumps performed on the current container. */
    public int getPumpCount() {
        return this.pumpCount;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(int ticks) {
        this.cooldown = Math.max(0, ticks);
        setChanged();
    }

    /** Upstream {@code increasePumpCount}: count a pump and try to convert. */
    public void increasePumpCount(int pumps) {
        this.pumpCount += pumps;
        updateInventory();
    }

    /**
     * Upstream {@code updateInventory}: swap the stored container for its
     * purified form once enough pumps have been spent, then rest.
     */
    public void updateInventory() {
        ItemStack stack = this.inventory.get(0);
        if (stack.isEmpty()) {
            return;
        }
        boolean bucket = stack.is(Items.BUCKET);
        boolean bottle = stack.is(Items.GLASS_BOTTLE);
        boolean flask = stack.getItem() instanceof LeatherFlaskItem;
        if (!bucket && !bottle && !flask) {
            return;
        }
        if (bucket && this.pumpCount <= 3) {
            return;
        }
        if (this.level != null && this.level.isClientSide()) {
            return;
        }
        if (bucket) {
            this.inventory.set(0, new ItemStack(PurifiedWater.BUCKET));
        } else if (bottle) {
            ItemStack potion = new ItemStack(Items.POTION);
            Holder<Potion> purified = PurifiedWater.PURIFIED_POTION;
            potion.set(DataComponents.POTION_CONTENTS, new PotionContents(purified));
            this.inventory.set(0, potion);
        } else {
            var data = stack.get(FlaskItems.FLASK_DATA);
            if (data != null) {
                this.inventory.set(0, FlaskItems.setFill(stack, data.fillLevel() + 2, data.qualityLevel()));
            }
        }
        this.pumpCount = 0;
        this.cooldown = HearthwindSurvivalConfig.get().hydration.pumpCooldown;
        setChanged();
    }

    /** Ticker body: the cooldown simply counts down. */
    public static void tick(Level level, BlockPos pos, BlockState state, BambooPumpBlockEntity entity) {
        if (entity.cooldown > 0) {
            entity.cooldown--;
        }
    }

    private void sendUpdate() {
        if (this.level != null) {
            BlockState current = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, current, current, 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inventory.clear();
        ContainerHelper.loadAllItems(input, this.inventory);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.inventory);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.get(0);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(this.inventory, 0, amount);
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = this.inventory.get(0);
        this.inventory.set(0, ItemStack.EMPTY);
        return removed;
    }

    /** Takes the stored container out (upstream hands the slot-0 stack back). */
    public ItemStack takeStored() {
        ItemStack removed = removeItemNoUpdate(0);
        setChanged();
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.inventory.set(0, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        sendUpdate();
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
        this.pumpCount = 0;
        setChanged();
    }
}
