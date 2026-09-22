package io.wispforest.lavender.client;

import io.wispforest.lavender.book.Entry;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Delta;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import net.minecraft.client.Minecraft;

public class AssociatedEntryTooltipComponent implements ClientTooltipComponent {

    public static float entryTriggerProgress = 0f;
    public static @Nullable WeakReference<ItemStack> tooltipStack = null;

    private final FlowLayout layout;

    public AssociatedEntryTooltipComponent(ItemStack book, Entry entry, float progress) {
        this.layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content()).gap(2);

        this.layout.child(UIContainers.verticalFlow(Sizing.content(), Sizing.content())
            .child(entry.iconFactory().apply(Sizing.fixed(16)).margins(Insets.of(2)))
            .child(UIComponents.item(book).sizing(Sizing.fixed(8)).positioning(Positioning.absolute(11, 11))));

        this.layout.child(UIContainers.verticalFlow(Sizing.content(), Sizing.content())
            .child(UIComponents.label(Component.literal(entry.title()).withStyle(ChatFormatting.GRAY)))
            .child(UIComponents.label(progress >= .05f
                ? Component.translatable("text.lavender.entry_tooltip.progress", "|".repeat((int) (30 * progress)), "|".repeat((int) Math.ceil(30 * (1 - progress))))
                : Component.translatable("text.lavender.entry_tooltip"))));

        this.layout.verticalAlignment(VerticalAlignment.CENTER);

        this.layout.inflate(Size.of(1000, 1000));
        this.layout.mount(null, 0, 0);
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor context) {
        var g = OwoUIGraphics.of(context);
        g.pose().pushMatrix();
        g.pose().translate(0, 0);

        this.layout.moveTo(x, y);
        this.layout.draw(g, 0, 0, 0, 0);

        g.pose().popMatrix();
    }

    @Override
    public int getHeight(Font font) {
        return this.layout.height();
    }

    @Override
    public int getWidth(Font font) {
        return this.layout.width();
    }

    static {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Minecraft.getInstance().hasShiftDown()) return;
            entryTriggerProgress += Delta.compute(entryTriggerProgress, 0f, .125f);
        });
    }
}
