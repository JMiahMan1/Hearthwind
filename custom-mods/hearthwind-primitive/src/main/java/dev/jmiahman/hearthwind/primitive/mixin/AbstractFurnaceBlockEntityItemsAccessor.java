package dev.jmiahman.hearthwind.primitive.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Grants write access to the protected furnace inventory list. */
@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityItemsAccessor {

    @Accessor("items")
    void hearthwind$setItemsList(NonNullList<ItemStack> items);
}
