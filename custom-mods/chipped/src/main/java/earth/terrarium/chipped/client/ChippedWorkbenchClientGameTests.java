package earth.terrarium.chipped.client;

import earth.terrarium.chipped.client.screens.SlotWidget;
import earth.terrarium.chipped.client.screens.WorkbenchGrid;
import earth.terrarium.chipped.client.screens.WorkbenchScreen;
import earth.terrarium.chipped.common.menus.WorkbenchMenu;
import earth.terrarium.chipped.common.registry.ModRecipeTypes;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class ChippedWorkbenchClientGameTests implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender(20 * 300);
            context.waitTicks(20);
            context.computeOnClient(minecraft -> {
                gridSelection();
                return true;
            });
            boolean synced = false;
            for (int i = 0; i < 60 && !synced; i++) {
                synced = context.computeOnClient(ChippedWorkbenchClientGameTests::clientRecipesSynced);
                if (!synced) context.waitTicks(20);
            }
            if (!synced) throw new AssertionError("client recipe sync");
            context.computeOnClient(minecraft -> {
                menuPipeline(minecraft);
                return true;
            });
            // Workbench 3D preview parity (Aged 3.0.7): open the real screen
            // with a selected variant so RenderWindowWidget submits its PIP
            // state (-30deg X / 45deg Y, mode offsets, door halves), then
            // screenshot. Planks covers TWO_BY_TWO (-7,5); door covers the
            // 5,12 lower+upper path.
            openWorkbenchPreview(context, Items.OAK_PLANKS, "chipped_workbench_preview_planks");
            // Prefer a solid door (barred/glass/screen are cutout and nearly
            // invisible at preview scale against the brown background).
            openWorkbenchPreview(context, Items.OAK_DOOR, "paneled_oak_door", "chipped_workbench_preview_door");
            context.takeScreenshot("chipped_workbench_grid_selection");
        }
    }

    private static void gridSelection() {
        for (int count : new int[]{0, 1, 9, 53, 54, 55, 63, 64, 137}) {
            for (int top : new int[]{0, 37}) {
                AtomicReference<ItemStack> chosen = new AtomicReference<>(ItemStack.EMPTY);
                List<SlotWidget> widgets = new ArrayList<>();
                List<ItemStack> results = new ArrayList<>();
                GridLayout grid = new GridLayout(85, top + WorkbenchGrid.TOP);
                for (int i = 0; i < count; i++) {
                    ItemStack result = new ItemStack(Items.STONE);
                    result.set(DataComponents.CUSTOM_NAME, Component.literal("result " + i));
                    results.add(result);
                }
                for (int column = 0; column < 9; column++) {
                    for (int row = 0; row < WorkbenchGrid.rows(count); row++) {
                        int index = WorkbenchGrid.index(column, row);
                        ItemStack stack = index < count ? results.get(index) : ItemStack.EMPTY;
                        SlotWidget widget = new SlotWidget(stack, chosen::set, top + WorkbenchGrid.CLIP_TOP, top + WorkbenchGrid.CLIP_BOTTOM) {
                            @Override
                            protected void playSelectionSound() {}
                        };
                        grid.addChild(widget, row, column);
                        widgets.add(widget);
                    }
                }
                grid.arrangeElements();
                for (double amount : new double[]{-10, 0, 8, 17.9, 18, 27, 10000, 0}) {
                    double scroll = WorkbenchGrid.clampScroll(amount, count);
                    int maximum = Math.max(0, ((count + 8) / 9 - 6) * 18);
                    if (scroll != Math.clamp(amount, 0, maximum)) throw new AssertionError("scroll clamp " + count);
                    grid.setY(WorkbenchGrid.gridY(top, scroll));
                    for (int y = top + 39; y <= top + 150; y++) {
                        for (int column = 0; column < 9; column++) {
                            int x = 85 + column * 18 + 8;
                            int row = Math.floorDiv(y - (top + 41 - (int) scroll), 18);
                            int index = column + row * 9;
                            boolean expected = y >= top + 40 && y < top + 149 && row >= 0 && index >= 0 && index < count;
                            chosen.set(ItemStack.EMPTY);
                            int accepted = 0;
                            for (SlotWidget widget : widgets) {
                                if (widget.mouseClicked(click(x, y, 0), false)) accepted++;
                            }
                            if (accepted != (expected ? 1 : 0)) throw new AssertionError("single visible hit at " + count + "/" + scroll + "/" + y);
                            if (chosen.get() != (expected ? results.get(index) : ItemStack.EMPTY)) throw new AssertionError("clicked rendered result");
                            for (SlotWidget widget : widgets) {
                                if (widget.mouseClicked(click(x, y, 1), false)) throw new AssertionError("right click rejected");
                            }
                            if (chosen.get() != (expected ? results.get(index) : ItemStack.EMPTY)) throw new AssertionError("right click cannot change selection");
                        }
                    }
                }
                for (SlotWidget widget : widgets) {
                    widget.active = false;
                    if (widget.mouseClicked(click(widget.getX() + 1, widget.getY() + 1, 0), false)) throw new AssertionError("inactive rejected");
                }
            }
        }
    }

    private static void openWorkbenchPreview(ClientGameTestContext context, net.minecraft.world.item.Item input, String shotName) {
        openWorkbenchPreview(context, input, null, shotName);
    }

    private static void openWorkbenchPreview(ClientGameTestContext context, net.minecraft.world.item.Item input, String preferContains, String shotName) {
        context.setScreen(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) throw new AssertionError("client player present for preview");
            Inventory inv = mc.player.getInventory();
            inv.setItem(9, new ItemStack(input, 4));
            WorkbenchMenu menu = new WorkbenchMenu(0, inv, ModRecipeTypes.CARPENTERS_TABLE.get());
            menu.selectStack(0);
            if (menu.results().isEmpty()) throw new AssertionError("preview results for " + input);
            ItemStack pick = menu.results().get(0);
            if (preferContains != null) {
                for (ItemStack cand : menu.results()) {
                    String id = cand.getItem().toString();
                    if (id.contains(preferContains)) {
                        pick = cand;
                        break;
                    }
                }
            }
            menu.setChosenStack(pick);
            if (menu.chosenStack().isEmpty()) throw new AssertionError("preview chosen for " + input);
            System.out.println("PROBE preview input=" + input + " chosen=" + menu.chosenStack().getItem() + " results=" + menu.results().size());
            return new WorkbenchScreen(menu, inv, Component.literal("Chipped Workbench"));
        });
        context.waitFor(minecraft -> minecraft.gui.screen() instanceof WorkbenchScreen, 20 * 300);
        context.waitTicks(20);
        context.takeScreenshot(shotName);
    }

    private static MouseButtonEvent click(int x, int y, int button) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0));
    }

    private static boolean clientRecipesSynced(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) return false;
        Inventory probeInventory = minecraft.player.getInventory();
        probeInventory.setItem(9, new ItemStack(Items.OAK_PLANKS, 4));
        WorkbenchMenu probe = new WorkbenchMenu(0, probeInventory, ModRecipeTypes.CARPENTERS_TABLE.get());
        probe.selectStack(0);
        return !probe.selectedStack().isEmpty();
    }

    private static void menuPipeline(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null) throw new AssertionError("client player present");
        Inventory inventory = player.getInventory();
        inventory.setItem(9, new ItemStack(Items.OAK_PLANKS, 4));
        WorkbenchMenu menu = new WorkbenchMenu(0, inventory, ModRecipeTypes.CARPENTERS_TABLE.get());
        if (menu.recipeType() == null) throw new AssertionError("default station type");
        menu.selectStack(0);
        if (!menu.selectedStack().is(Items.OAK_PLANKS)) throw new AssertionError("client selection");
        if (menu.results().isEmpty()) throw new AssertionError("client results sync");
        if (menu.results().stream().anyMatch(stack -> stack.is(Items.OAK_PLANKS))) throw new AssertionError("input excluded");
        if (menu.canCraft()) throw new AssertionError("craft gated until variant chosen");
        AtomicReference<ItemStack> chosen = new AtomicReference<>(ItemStack.EMPTY);
        SlotWidget widget = new SlotWidget(menu.results().get(0), chosen::set, 0, Integer.MAX_VALUE) {
            @Override
            protected void playSelectionSound() {}
        };
        widget.setX(100);
        widget.setY(60);
        if (!widget.mouseClicked(click(108, 68, 0), false)) throw new AssertionError("result click selects");
        menu.setChosenStack(chosen.get());
        if (!menu.canCraft()) throw new AssertionError("craft enabled after choice");
        int rows = WorkbenchGrid.rows(menu.results().size());
        double maximum = Math.max(0, (rows - WorkbenchGrid.VISIBLE_ROWS) * WorkbenchGrid.CELL_SIZE);
        if (WorkbenchGrid.clampScroll(10000, menu.results().size()) != maximum) throw new AssertionError("scroll clamps to content");
        if (WorkbenchGrid.gridY(37, maximum) != 37 + WorkbenchGrid.TOP - (int) maximum) throw new AssertionError("scroll offsets grid");
        menu.reset();
        if (menu.canCraft() || !menu.results().isEmpty() || !menu.selectedStack().isEmpty()) throw new AssertionError("craft resets gate");
    }
}
