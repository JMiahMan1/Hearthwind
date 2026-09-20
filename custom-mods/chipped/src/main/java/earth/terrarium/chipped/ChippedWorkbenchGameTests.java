package earth.terrarium.chipped;

import earth.terrarium.chipped.client.screens.WorkbenchGrid;
import earth.terrarium.chipped.common.menus.WorkbenchCrafting;
import earth.terrarium.chipped.common.menus.WorkbenchMenu;
import earth.terrarium.chipped.common.recipes.ChippedRecipe;
import earth.terrarium.chipped.common.registry.ModBlocks;
import earth.terrarium.chipped.common.registry.ModRecipeTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameType;

import java.util.List;

public final class ChippedWorkbenchGameTests {
    public ChippedWorkbenchGameTests() {}

    @GameTest
    public void workbenchCraftReplacesSelectedAndAll(GameTestHelper helper) {
        replace(true, helper);
        replace(false, helper);
        helper.succeed();
    }

    private static void replace(boolean all, GameTestHelper helper) {
        SimpleContainer inventory = new SimpleContainer(41);
        inventory.setItem(9, new ItemStack(Items.STONE, 17));
        inventory.setItem(0, new ItemStack(Items.STONE, 64));
        inventory.setItem(5, new ItemStack(Items.STONE, 1));
        inventory.setItem(40, new ItemStack(Items.STONE, 3));
        inventory.getItem(40).set(DataComponents.CUSTOM_NAME, Component.literal("named input"));
        inventory.setItem(1, new ItemStack(Items.DIRT, 8));
        inventory.setItem(2, anExistingResult().copyWithCount(5));
        ItemStack output = new ItemStack(Items.COBBLESTONE, 99);
        helper.assertTrue(WorkbenchCrafting.replace(inventory, 9, inventory.getItem(9).copy(), output, List.of(anExistingResult()), all), "craft accepted");
        helper.assertTrue(inventory.getItem(9).is(Items.COBBLESTONE) && inventory.getItem(9).getCount() == 17, "selected entire stack");
        helper.assertTrue(inventory.getItem(0).is(all ? Items.COBBLESTONE : Items.STONE) && inventory.getItem(0).getCount() == 64, "hotbar count retained");
        helper.assertTrue(inventory.getItem(5).is(all ? Items.COBBLESTONE : Items.STONE) && inventory.getItem(5).getCount() == 1, "second stack count retained");
        helper.assertTrue(inventory.getItem(40).is(all ? Items.COBBLESTONE : Items.STONE) && inventory.getItem(40).getCount() == 3, "offhand item-only matching");
        helper.assertTrue(inventory.getItem(1).is(Items.DIRT) && inventory.getItem(1).getCount() == 8, "unrelated untouched");
        helper.assertTrue(inventory.getItem(2).getCount() == 5 && inventory.getItem(3).isEmpty(), "existing result and empty slots untouched");
        helper.assertTrue(output.getCount() == 99, "request not mutated or trusted for count");
    }

    @GameTest
    public void workbenchCraftRejectsInvalidRequests(GameTestHelper helper) {
        SimpleContainer inventory = new SimpleContainer(2);
        ItemStack selected = new ItemStack(Items.STONE, 12);
        inventory.setItem(0, selected.copy());
        List<ItemStack> results = List.of(anExistingResult());
        helper.assertTrue(!WorkbenchCrafting.replace(inventory, 0, selected, new ItemStack(Items.DIAMOND), results, true), "non-result rejected");
        ItemStack decorated = anExistingResult();
        decorated.set(DataComponents.CUSTOM_NAME, Component.literal("not in results"));
        helper.assertTrue(!WorkbenchCrafting.replace(inventory, 0, selected, decorated, results, true), "non-result components rejected");
        helper.assertTrue(!WorkbenchCrafting.replace(inventory, -1, selected, anExistingResult(), results, true), "negative selection rejected");
        helper.assertTrue(!WorkbenchCrafting.replace(inventory, 2, selected, anExistingResult(), results, true), "out of bounds rejected");
        inventory.setItem(0, new ItemStack(Items.DIRT, 12));
        helper.assertTrue(!WorkbenchCrafting.replace(inventory, 0, selected, anExistingResult(), results, true), "stale selection rejected");
        helper.assertTrue(inventory.getItem(0).is(Items.DIRT) && inventory.getItem(0).getCount() == 12, "rejection leaves inventory intact");
        inventory.setItem(0, ItemStack.EMPTY);
        helper.assertTrue(!WorkbenchCrafting.replace(inventory, 0, selected, anExistingResult(), results, true), "empty source rejected");
        helper.succeed();
    }

    private static ItemStack anExistingResult() {
        return new ItemStack(Items.COBBLESTONE);
    }

    @GameTest
    public void chippedRecipeExpandsMatchingIngredientsExcludingInput(GameTestHelper helper) {
        ChippedRecipe recipe = new ChippedRecipe(List.of(
            Ingredient.of(Items.STONE, Items.COBBLESTONE, Items.DEEPSLATE),
            Ingredient.of(Items.DIRT)));
        List<ItemStack> stoneResults = recipe.getResults(new ItemStack(Items.STONE)).toList();
        helper.assertTrue(stoneResults.size() == 2, "matching ingredient expands to its mates");
        helper.assertTrue(stoneResults.stream().anyMatch(stack -> stack.is(Items.COBBLESTONE)), "cobble variant present");
        helper.assertTrue(stoneResults.stream().anyMatch(stack -> stack.is(Items.DEEPSLATE)), "deepslate variant present");
        helper.assertTrue(stoneResults.stream().noneMatch(stack -> stack.is(Items.STONE)), "input excluded");
        helper.assertTrue(recipe.getResults(new ItemStack(Items.DIRT)).toList().isEmpty(), "lone variant expands to nothing");
        helper.assertTrue(recipe.getResults(new ItemStack(Items.DIAMOND)).toList().isEmpty(), "unmatched input empty");
        helper.assertTrue(recipe.getResults(ItemStack.EMPTY).toList().isEmpty(), "empty input empty");
        helper.succeed();
    }

    @GameTest
    public void workbenchMenuResolvesStationResultsFiltersAndCrafts(GameTestHelper helper) {
        Player player = helper.makeMockServerPlayer(GameType.CREATIVE);
        Inventory inventory = player.getInventory();
        BlockPos station = helper.absolutePos(new BlockPos(0, 2, 0));
        helper.getLevel().setBlock(station, ModBlocks.CARPENTERS_TABLE.get().defaultBlockState(), 3);
        WorkbenchMenu menu = new WorkbenchMenu(0, inventory, station);
        helper.assertTrue(menu.recipeType() == ModRecipeTypes.CARPENTERS_TABLE.get(), "station resolves recipe type");

        inventory.setItem(9, new ItemStack(Items.OAK_PLANKS, 5));
        inventory.setItem(10, new ItemStack(Items.OAK_PLANKS, 3));
        menu.selectStack(0);
        helper.assertTrue(menu.selectedStack().is(Items.OAK_PLANKS), "selection tracks slot");
        helper.assertTrue(menu.chosenStack().is(Items.OAK_PLANKS), "choice defaults to selection");
        helper.assertTrue(!menu.results().isEmpty(), "station recipe populates results");
        helper.assertTrue(menu.results().stream().noneMatch(stack -> stack.is(Items.OAK_PLANKS)), "input excluded from results");
        helper.assertTrue(!menu.canCraft(), "craft gated until variant chosen");

        ItemStack chosen = menu.results().get(0).copy();
        menu.setChosenStack(chosen);
        helper.assertTrue(menu.canCraft(), "craft enabled after choice");
        menu.craft(chosen, false);
        helper.assertTrue(inventory.getItem(9).is(chosen.getItem()) && inventory.getItem(9).getCount() == 5, "single craft swaps count kept");
        helper.assertTrue(inventory.getItem(10).is(Items.OAK_PLANKS), "single craft leaves mates");
        helper.assertTrue(menu.results().isEmpty() && menu.selectedStack().isEmpty() && !menu.canCraft(), "craft resets menu");

        inventory.setItem(9, new ItemStack(Items.OAK_PLANKS, 5));
        menu.selectStack(0);
        ItemStack chosenAll = menu.results().get(0).copy();
        menu.setChosenStack(chosenAll);
        menu.craft(chosenAll, true);
        helper.assertTrue(inventory.getItem(9).is(chosenAll.getItem()) && inventory.getItem(9).getCount() == 5, "craft-all swaps selected");
        helper.assertTrue(inventory.getItem(10).is(chosenAll.getItem()) && inventory.getItem(10).getCount() == 3, "craft-all swaps mates");

        inventory.setItem(9, new ItemStack(Items.OAK_PLANKS, 1));
        menu.selectStack(0);
        menu.updateResults("zzz-no-such-variant-zzz");
        helper.assertTrue(menu.results().isEmpty(), "filter hides all");
        helper.assertTrue(menu.selectedStack().is(Items.OAK_PLANKS), "filter keeps selection");
        menu.updateResults(null);
        helper.assertTrue(!menu.results().isEmpty(), "cleared filter restores");

        inventory.setItem(9, new ItemStack(Items.DIAMOND_SWORD, 1));
        menu.selectStack(0);
        helper.assertTrue(menu.results().isEmpty() && menu.selectedStack().isEmpty(), "unstationed input resets");

        inventory.setItem(9, new ItemStack(Items.STONE, 1));
        menu.selectStack(0);
        helper.assertTrue(menu.results().isEmpty() && menu.selectedStack().isEmpty(), "wrong station resets");
        helper.succeed();
    }

    @GameTest
    public void workbenchGridMathMatchesRenderedRows(GameTestHelper helper) {
        helper.assertTrue(WorkbenchGrid.COLUMNS == 9, "columns");
        helper.assertTrue(WorkbenchGrid.VISIBLE_ROWS == 6, "visible rows");
        helper.assertTrue(WorkbenchGrid.CELL_SIZE == 18, "cell size");
        helper.assertTrue(WorkbenchGrid.TOP == 41, "grid top");
        helper.assertTrue(WorkbenchGrid.CLIP_TOP == 40, "clip top");
        helper.assertTrue(WorkbenchGrid.CLIP_BOTTOM == 149, "clip bottom");
        helper.assertTrue(WorkbenchGrid.CLIP_BOTTOM - WorkbenchGrid.CLIP_TOP == 109, "clip height");
        for (int count : new int[]{0, 1, 9, 53, 54, 55, 63, 64, 137}) {
            int rows = Math.max(6, (count + 8) / 9);
            helper.assertTrue(WorkbenchGrid.rows(count) == rows, "rows " + count);
            double maximum = Math.max(0, (rows - 6) * 18);
            for (double amount : new double[]{-10, 0, 8, 17.9, 18, 27, 10000, 0}) {
                double scroll = WorkbenchGrid.clampScroll(amount, count);
                helper.assertTrue(scroll == Math.clamp(amount, 0, maximum), "scroll clamp " + count);
                for (int top : new int[]{0, 37}) {
                    helper.assertTrue(WorkbenchGrid.gridY(top, scroll) == top + 41 - (int) scroll, "grid y");
                    for (int column = 0; column < 9; column++) {
                        for (int row = 0; row < rows; row++) {
                            helper.assertTrue(WorkbenchGrid.index(column, row) == column + row * 9, "index map");
                        }
                    }
                    for (int y = top + 39; y <= top + 150; y++) {
                        helper.assertTrue(WorkbenchGrid.containsY(y, top + 40, top + 149) == (y >= top + 40 && y < top + 149), "clip window");
                    }
                }
            }
        }
        helper.succeed();
    }
}
