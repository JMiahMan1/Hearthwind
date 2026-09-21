package net.satisfy.herbalbrews.core.compat.rei.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.herbalbrews.HerbalBrews;
import net.satisfy.herbalbrews.core.recipe.CauldronRecipe;
import net.satisfy.herbalbrews.core.registry.ObjectRegistry;

import java.util.ArrayList;
import java.util.List;

public class CauldronDisplay extends BasicDisplay {

    public static final CategoryIdentifier<CauldronDisplay> CAULDRON_DISPLAY = CategoryIdentifier.of(HerbalBrews.MOD_ID, "cauldron_display");

    public CauldronDisplay(RecipeHolder<CauldronRecipe> recipe) {
        this(createInputs(), createOutputs(), java.util.Optional.of(recipe.id().identifier()));
    }

    public CauldronDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        this(inputs, outputs, java.util.Optional.empty());
    }

    public CauldronDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, java.util.Optional<Identifier> location) {
        super(inputs, outputs, location);
    }

    private static List<EntryIngredient> createInputs() {
        List<EntryIngredient> inputs = new ArrayList<>();
        inputs.add(EntryIngredients.of(createPotionStack(Potions.SWIFTNESS)));
        inputs.add(EntryIngredients.of(createPotionStack(Potions.HEALING)));
        inputs.add(EntryIngredients.of(createPotionStack(Potions.STRENGTH)));
        inputs.add(EntryIngredients.of(new ItemStack(ObjectRegistry.HERBAL_INFUSION.get())));

        return inputs;
    }


    private static List<EntryIngredient> createOutputs() {
        List<EntryIngredient> outputs = new ArrayList<>();
        outputs.add(EntryIngredients.of(new ItemStack(ObjectRegistry.FLASK.get())));
        return outputs;
    }

    private static ItemStack createPotionStack(Holder<Potion> potionType) {
        ItemStack potion = new ItemStack(Items.POTION);
        potion.set(DataComponents.POTION_CONTENTS, new PotionContents(potionType));
        return potion;
    }

    @Override
    public me.shedaniel.rei.api.common.display.DisplaySerializer<? extends me.shedaniel.rei.api.common.display.Display> getSerializer() {
        return me.shedaniel.rei.api.common.display.DisplaySerializer.of(
                com.mojang.serialization.MapCodec.unit(() -> this),
                net.minecraft.network.codec.StreamCodec.unit(this));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return CAULDRON_DISPLAY;
    }
}
