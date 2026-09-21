package net.satisfy.bakery.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.satisfy.farm_and_charm.client.util.ClientUtil;

public class WallDisplayRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> items, int light) {
        float[][] itemPositions = {
                {-0.5f, 0.7f},
                {0.5f, 0.7f},
                {-0.5f, 1.8f},
                {0.5f, 1.8f}
        };

        float scale = 0.4f;

        for (int i = 0; i < Math.min(items.size(), 4); i++) {
            ItemStack stack = items.get(i);

            if (!stack.isEmpty()) {
                poseStack.pushPose();

                poseStack.scale(scale, scale, scale);

                float xOffset = itemPositions[i][0];
                float yOffset = itemPositions[i][1];
                float zBaseOffset = 0.5f;

                poseStack.translate(xOffset, yOffset, zBaseOffset);

                ClientUtil.renderItem(stack, poseStack, collector, itemResolver, level, pos, light);

                poseStack.popPose();
            }
        }
    }
}
