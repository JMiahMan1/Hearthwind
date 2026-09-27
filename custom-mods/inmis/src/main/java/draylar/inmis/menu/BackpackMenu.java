package draylar.inmis.menu;

import draylar.inmis.Inmis;
import draylar.inmis.config.BackpackInfo;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.item.component.BackpackComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/**
 * The backpack container. Geometry mirrors upstream exactly: the panel is
 * sized from the tier, slots carry the +1 pixel shift, and the locked slot
 * rules keep backpacks and (optionally) shulkers out while honouring the
 * unstackablesOnly and blacklist config.
 */
public class BackpackMenu extends AbstractContainerMenu {

    private static final int PADDING = 8;
    private static final int TITLE_SPACE = 10;

    private final ItemStack backpackStack;

    public BackpackMenu(int containerId, Inventory playerInventory, ItemStack backpackStack) {
        super(Inmis.BACKPACK_MENU, containerId);
        this.backpackStack = backpackStack;

        if (!(backpackStack.getItem() instanceof BackpackItem backpack)) {
            return;
        }

        BackpackInfo tier = backpack.getTier();
        int rowWidth = tier.getRowWidth();
        int numberOfRows = tier.getNumberOfRows();

        BackpackComponent component = backpackStack.getOrDefault(Inmis.BACKPACK_COMPONENT,
                new BackpackComponent(new SimpleContainer(rowWidth * numberOfRows)));

        for (int y = 0; y < numberOfRows; y++) {
            for (int x = 0; x < rowWidth; x++) {
                Pos backpackSlot = backpackSlotPosition(x, y);
                addSlot(new BackpackLockedSlot(component.getContainer(), y * rowWidth + x,
                        backpackSlot.x + 1, backpackSlot.y + 1));
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                Pos playerSlot = playerSlotPosition(x, y);
                addSlot(new BackpackLockedSlot(playerInventory, x + y * 9 + 9,
                        playerSlot.x + 1, playerSlot.y + 1));
            }
        }

        for (int x = 0; x < 9; x++) {
            Pos playerSlot = playerSlotPosition(x, 3);
            addSlot(new BackpackLockedSlot(playerInventory, x, playerSlot.x + 1, playerSlot.y + 1));
        }

        backpackStack.set(Inmis.BACKPACK_COMPONENT, component);
    }

    public BackpackInfo getTier() {
        return ((BackpackItem) backpackStack.getItem()).getTier();
    }

    public ItemStack getBackpackStack() {
        return backpackStack;
    }

    public int getImageWidth() {
        return PADDING * 2 + Math.max(getTier().getRowWidth(), 9) * 18;
    }

    public int getImageHeight() {
        return PADDING * 2 + TITLE_SPACE * 2 + 8 + (getTier().getNumberOfRows() + 4) * 18;
    }

    public int getPlayerInvSlotX() {
        return playerSlotPosition(0, 0).x;
    }

    private Pos backpackSlotPosition(int x, int y) {
        BackpackInfo tier = getTier();
        return new Pos(getImageWidth() / 2 - tier.getRowWidth() * 9 + x * 18, PADDING + TITLE_SPACE + y * 18);
    }

    private Pos playerSlotPosition(int x, int y) {
        return new Pos(getImageWidth() / 2 - 9 * 9 + x * 18,
                getImageHeight() - PADDING - 4 * 18 - 3 + y * 18 + (y == 3 ? 4 : 0));
    }

    @Override
    public boolean stillValid(Player player) {
        return backpackStack.getItem() instanceof BackpackItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            BackpackInfo tier = getTier();
            int backpackSlots = tier.getNumberOfRows() * tier.getRowWidth();
            if (index < backpackSlots) {
                if (!moveItemStackTo(stack, backpackSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, backpackSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    private record Pos(int x, int y) {
    }

    private final class BackpackLockedSlot extends Slot {

        BackpackLockedSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return stackMovementIsAllowed(getItem());
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (Inmis.CONFIG.unstackablesOnly && stack.getMaxStackSize() > 1) {
                return false;
            }

            // Locked rules only apply inside the backpack itself.
            if (container instanceof SimpleContainer) {
                if (Inmis.CONFIG.disableShulkers && stack.getItem() instanceof BlockItem blockItem
                        && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                    return false;
                }
                Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (Inmis.CONFIG.blacklist.stream().map(Identifier::parse).toList().contains(id)) {
                    return false;
                }
            }

            return stackMovementIsAllowed(stack);
        }

        private boolean stackMovementIsAllowed(ItemStack stack) {
            return !(stack.getItem() instanceof BackpackItem) && stack != backpackStack;
        }
    }
}
