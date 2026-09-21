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
import net.satisfy.meadow.client.util.ClientUtil;

public class CheeseRackRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, ItemModelResolver itemResolver,
            BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash,
            NonNullList<ItemStack> nonNullList, int light) {
        poseStack.translate(-0.5f, 0.05f, -0.5f);
        if (!nonNullList.isEmpty()) {
            ItemStack itemStack1 = nonNullList.get(0);
            if (itemStack1.getItem() instanceof BlockItem blockItem) {
                ClientUtil.renderBlockFromItem(blockItem, poseStack, collector, blockResolver, level, pos);
            }
        }
        if (nonNullList.size() > 1) {
            ItemStack itemStack2 = nonNullList.get(1);
            if (itemStack2.getItem() instanceof BlockItem blockItem) {
                poseStack.translate(0f, 0.4f, 0f);
                ClientUtil.renderBlockFromItem(blockItem, poseStack, collector, blockResolver, level, pos);
            }
        }
    }
}
