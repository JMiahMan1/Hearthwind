package net.satisfy.bakery.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface StorageTypeRenderer {
    void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver,
            BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash,
            NonNullList<ItemStack> itemStacks, int light);
}
