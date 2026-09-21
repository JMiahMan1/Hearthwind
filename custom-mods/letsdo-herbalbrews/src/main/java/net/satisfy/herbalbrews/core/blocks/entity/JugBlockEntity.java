package net.satisfy.herbalbrews.core.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.satisfy.herbalbrews.core.items.DrinkBlockItem;
import net.satisfy.herbalbrews.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JugBlockEntity extends BlockEntity {
    private final List<ItemStack> drinks = new ArrayList<>();

    public JugBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.JUG_BLOCK_ENTITY.get(), pos, state);
    }

    public void addDrink(ItemStack drink) {
        if (drinks.size() < 3 && drink.getItem() instanceof DrinkBlockItem) {
            drinks.add(drink.copy());
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public List<ItemStack> getDrinks() {
        return drinks;
    }

    public void clearDrinks() {
        drinks.clear();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Drinks", ItemStack.OPTIONAL_CODEC.listOf(), List.copyOf(drinks));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        drinks.clear();
        drinks.addAll(input.read("Drinks", ItemStack.OPTIONAL_CODEC.listOf()).orElse(List.of()));
    }

    public void applyEffects(LivingEntity user, int durationTicks) {
        for (ItemStack drink : drinks) {
            if (drink.getItem() instanceof DrinkBlockItem) {
                PotionContents data = drink.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                if (data.hasEffects()) {
                    data.forEachEffect(mobEffectInstance -> user.addEffect(new MobEffectInstance(
                            mobEffectInstance.getEffect(),
                            durationTicks,
                            mobEffectInstance.getAmplifier(),
                            mobEffectInstance.isAmbient(),
                            mobEffectInstance.isVisible())), 1.0F);
                }
            }
        }
    }
}
