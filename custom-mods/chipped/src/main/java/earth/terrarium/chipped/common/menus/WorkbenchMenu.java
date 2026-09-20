package earth.terrarium.chipped.common.menus;

import earth.terrarium.chipped.common.blocks.WorkbenchBlock;
import earth.terrarium.chipped.common.recipes.ChippedRecipe;
import earth.terrarium.chipped.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkbenchMenu extends AbstractContainerMenu {
    protected final Inventory inventory;
    protected final Level level;
    protected final RecipeType<ChippedRecipe> recipeType;

    private int selectedStackId;
    private ItemStack selectedStack = ItemStack.EMPTY;
    private ItemStack chosenStack = ItemStack.EMPTY;
    @Nullable
    private String filter;
    private final List<ItemStack> results = new ArrayList<>();

    public WorkbenchMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, getRecipeFromPos(inventory.player.level(), pos));
    }

    public WorkbenchMenu(int containerId, Inventory inventory, RecipeType<ChippedRecipe> recipeType) {
        super(ModMenuTypes.WORKBENCH.get(), containerId);
        this.inventory = inventory;
        this.level = inventory.player.level();
        this.recipeType = recipeType;
        addPlayerInvSlots();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    protected void addPlayerInvSlots() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new InventorySlot(inventory, j + i * 9 + 9, getPlayerInvXOffset() + j * 18, getPlayerInvYOffset() + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new InventorySlot(inventory, i, getPlayerInvXOffset() + i * 18, getPlayerInvYOffset() + 58));
        }
    }

    public int getPlayerInvXOffset() {
        return 86;
    }

    public int getPlayerInvYOffset() {
        return 167;
    }


    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        selectStack(slotId);
        super.clicked(slotId, button, clickType, player);
    }

    public void selectStack(int slotId) {
        if (slotId < 0 || slotId >= slots.size()) return;
        selectedStackId = slots.get(slotId).getContainerSlot();
        selectedStack = slots.get(slotId).getItem();
        chosenStack = selectedStack;
        updateResults(filter);
    }

    public void updateResults(@Nullable String filter) {
        if (selectedStack.isEmpty()) return;
        this.filter = filter;
        if (recipeType == null) {
            reset();
            return;
        }
        CraftingInput craftingInput = CraftingInput.of(1, 1, List.of(selectedStack));

        level.recipeAccess().getSynchronizedRecipes()
            .getFirstMatch(recipeType, craftingInput, level).ifPresentOrElse(recipe -> {
                results.clear();
                recipe.value().getResults(craftingInput.getItem(0)).forEach(result -> {
                    if (filter == null
                        || StringUtil.isBlank(filter)
                        || result.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))) {
                        results.add(result);
                    }
                });
            }, this::reset);
    }

    public boolean canCraft() {
        return !selectedStack.isEmpty() && results.stream().anyMatch(result -> ItemStack.isSameItemSameComponents(result, chosenStack));
    }

    public void craft(ItemStack stack, boolean replaceAll) {
        if (WorkbenchCrafting.replace(inventory, selectedStackId, selectedStack, stack, results, replaceAll)) reset();
    }

    public void reset() {
        selectedStackId = 0;
        selectedStack = ItemStack.EMPTY;
        chosenStack = ItemStack.EMPTY;
        results.clear();
    }

    public ItemStack selectedStack() {
        return selectedStack;
    }

    public ItemStack chosenStack() {
        return chosenStack;
    }

    public void setChosenStack(ItemStack stack) {
        chosenStack = stack;
    }

    public List<ItemStack> results() {
        return results;
    }

    public Level level() {
        return level;
    }

    public RecipeType<ChippedRecipe> recipeType() {
        return recipeType;
    }

    public void setFilter(@Nullable String filter) {
        this.filter = filter;
    }

    protected static RecipeType<ChippedRecipe> getRecipeFromPos(Level level, BlockPos pos) {
        if (level == null || pos == null) return null;
        if (level.getBlockState(pos).getBlock() instanceof WorkbenchBlock workbench) {
            return workbench.recipeType();
        }
        return null;
    }

    private static class InventorySlot extends Slot {
        public InventorySlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}