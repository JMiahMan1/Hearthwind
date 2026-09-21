package net.satisfy.meadow.client.renderer.block.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.meadow.client.util.ClientUtil;

public class WheelBarrowRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, ItemModelResolver itemResolver,
            BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash,
            NonNullList<ItemStack> itemStacks, int light) {
        if (itemStacks.isEmpty()) return;
        ItemStack stack = itemStacks.get(0);
        if (stack.getItem() instanceof BlockItem blockItem) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            poseStack.translate(-0.5f, 0.5f, -0.5f);
            ClientUtil.renderBlock(state, poseStack, collector, blockResolver, level, pos);
        }
    }
}