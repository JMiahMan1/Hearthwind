package dev.jmiahman.hearthwind.survival.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Private campfire cooking state needed to boil water bottles (Dehydration parity). */
@Mixin(CampfireBlockEntity.class)
public interface CampfireBlockEntityAccessor {
    @Accessor("items")
    NonNullList<ItemStack> hearthwind$items();

    @Accessor("cookingProgress")
    int[] hearthwind$cookingProgress();

    @Accessor("cookingTime")
    int[] hearthwind$cookingTime();
}
