package net.satisfy.brewery.core.compat.rei.category;

import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.satisfy.brewery.Brewery;
import net.satisfy.brewery.core.compat.rei.display.BrewingStationDisplay;
import net.satisfy.brewery.core.registry.ObjectRegistry;

import java.util.List;

public class BrewingStationCategory implements DisplayCategory<BrewingStationDisplay> {
    public static final CategoryIdentifier<BrewingStationDisplay> BREWING_STATION_DISPLAY = CategoryIdentifier.of(Brewery.MOD_ID, "brewing_station_display");

    @Override
    public CategoryIdentifier<BrewingStationDisplay> getCategoryIdentifier() {
        return BREWING_STATION_DISPLAY;
    }

    @Override
    public Component getTitle() {
        return ObjectRegistry.WOODEN_BREWINGSTATION.get().getName();
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(ObjectRegistry.WOODEN_BREWINGSTATION.get());
    }

    @Override
    public int getDisplayWidth(BrewingStationDisplay display) {
        return 128;
    }

    @Override
    public int getDisplayHeight() {
        return 64;
    }

    @Override
    public List<Widget> setupDisplay(BrewingStationDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - getDisplayWidth(display) / 2 - 4, bounds.getCenterY() - getDisplayHeight() / 2 + 14);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 54, startPoint.y + 9))
                .animationDurationTicks(50));
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 88, startPoint.y + 9)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 88, startPoint.y + 9)).entries(display.getOutputEntries().get(0)).disableBackground().markOutput());

        if (display.getInputEntries().isEmpty())
            widgets.add(Widgets.createSlotBackground(new Point(startPoint.x + 32, startPoint.y)));
        else
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 12, startPoint.y)).entries(display.getInputEntries().get(0)).markInput());

        if (display.getInputEntries().size() < 2)
            widgets.add(Widgets.createSlotBackground(new Point(startPoint.x + 32, startPoint.y + 20)));
        else
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 12, startPoint.y + 20)).entries(display.getInputEntries().get(1)).markInput());

        if (display.getInputEntries().size() < 3)
            widgets.add(Widgets.createSlotBackground(new Point(startPoint.x + 32, startPoint.y)));
        else
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 32, startPoint.y)).entries(display.getInputEntries().get(2)).markInput());

        if (display.getInputEntries().size() < 4)
            widgets.add(Widgets.createSlotBackground(new Point(startPoint.x + 32, startPoint.y + 20)));
        else
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 32, startPoint.y + 20)).entries(display.getInputEntries().get(3)).markInput());

        return widgets;
    }
}