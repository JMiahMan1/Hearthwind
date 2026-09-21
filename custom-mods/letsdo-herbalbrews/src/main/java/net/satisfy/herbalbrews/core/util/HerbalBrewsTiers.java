package net.satisfy.herbalbrews.core.util;

import com.google.common.base.Suppliers;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum HerbalBrewsTiers {
    GLASS(2, 5.0f, 2.0f, 12, () -> Ingredient.of(Items.GLASS));

    private final int maxUses;
    private final float miningSpeedMultiplier;
    private final float attackDamage;
    private final int enchantability;
    private final Supplier<Ingredient> repairIngredient;

    HerbalBrewsTiers(int maxUses, float miningSpeedMultiplier, float attackDamage, int enchantability, Supplier<Ingredient> repairIngredient) {
        this.maxUses = maxUses;
        this.miningSpeedMultiplier = miningSpeedMultiplier;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairIngredient = Suppliers.memoize(repairIngredient::get);
    }

    public int getUses() {
        return this.maxUses;
    }

    public float getSpeed() {
        return this.miningSpeedMultiplier;
    }

    public float getAttackDamageBonus() {
        return this.attackDamage;
    }

    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_STONE_TOOL;
    }

    public int getEnchantmentValue() {
        return this.enchantability;
    }

    public @NotNull Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
