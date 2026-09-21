package net.satisfy.bakery.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.satisfy.farm_and_charm.client.util.ClientUtil;

@Environment(EnvType.CLIENT)
public class CupcakeDisplayRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> itemStacks, int light) {
        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack stack = itemStacks.get(i);

            if (stack.isEmpty()) continue;

            matrices.pushPose();

            matrices.scale(0.3f, 0.3f, 0.3f);

            float xOffset = (i % 3) * 2.1f;
            float yOffset = i < 3 ? 1.7f : 0.45f;
            float zOffset = -1f;

            xOffset += i < 3 ? -0.9f : 2.7f;

            matrices.translate(xOffset, yOffset, zOffset);
            matrices.mulPose(Axis.XP.rotationDegrees(90f));
            matrices.translate(-1.2f * i, 1, 0);

            ClientUtil.renderItem(stack, matrices, collector, itemResolver, level, pos, light);

            matrices.popPose();
        }
    }
}
