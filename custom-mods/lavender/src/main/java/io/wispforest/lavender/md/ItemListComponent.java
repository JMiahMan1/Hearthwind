package io.wispforest.lavender.md;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class ItemListComponent extends ItemComponent {

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    private @Nullable ImmutableList<ItemStack> items;

    private float time = 0f;
    private List<ClientTooltipComponent> extraTooltipSection = List.of();
    private int currentStackIndex;

    public ItemListComponent() {
        super(ItemStack.EMPTY);
        this.setTooltipFromStack(true);
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);

        this.time += delta;
        if (this.time >= 20) {
            this.time -= 20;
            this.updateForItems();
        }
    }

    @Override
    public UIComponent tooltip(List<ClientTooltipComponent> tooltip) {
        if (tooltip == null) return super.tooltip((List<ClientTooltipComponent>) null);

        tooltip = new ArrayList<>(tooltip);
        tooltip.addAll(this.extraTooltipSection);

        this.tooltip = tooltip;
        return this;
    }

    private void updateForItems() {
        if (this.items != null && !this.items.isEmpty()) {
            this.currentStackIndex = (this.currentStackIndex + 1) % this.items.size();
            this.stack(this.items.get(this.currentStackIndex));
        } else {
            this.currentStackIndex = 0;
            this.stack(ItemStack.EMPTY);
        }
    }

    public ItemListComponent slotDisplay(SlotDisplay display) {
        this.items = ImmutableList.copyOf(display.resolveForStacks(SlotDisplayContext.fromLevel(Minecraft.getInstance().level)));
        this.updateForItems();

        return this;
    }

    public ItemListComponent ingredient(Ingredient ingredient) {
        return this.slotDisplay(ingredient.display());
    }

    public ItemListComponent tag(TagKey<Item> tag) {
        var holders = BuiltInRegistries.ITEM.getTagOrEmpty(tag);
        var built = ImmutableList.<ItemStack>builder();
        for (var holder : holders) built.add(holder.value().getDefaultInstance());
        this.items = built.build();
        this.updateForItems();

        return this;
    }

    public void extraTooltipSection(List<ClientTooltipComponent> section) {
        this.extraTooltipSection = section;
        this.updateTooltipForStack();
    }

    @Override
    public void parseProperties(UIModel model, org.w3c.dom.Element element, Map<String, org.w3c.dom.Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "tag", tagElement -> TagKey.create(net.minecraft.core.registries.Registries.ITEM, UIParsing.parseIdentifier(tagElement)), this::tag);
        UIParsing.apply(
            children,
            "ingredient",
            ingredientElement -> Ingredient.CODEC.parse(JsonOps.INSTANCE, GSON.fromJson(ingredientElement.getTextContent().strip(), JsonElement.class)).getOrThrow(),
            this::ingredient
        );
    }
}
