package net.satisfy.herbalbrews.core.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.herbalbrews.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class StoveBlockEntity extends AbstractFurnaceBlockEntity {
    public StoveBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.STOVE_BLOCK_ENTITY.get(), pos, state, RecipeType.SMOKING);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.smoker");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SmokerMenu(id, inventory, this, this.dataAccess);
    }
}
