package net.satisfy.candlelight.client.renderer.block;

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
public class ShelfRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> itemStacks, int light) {

        matrices.translate(-0.4, 0.5, 0.25);
        matrices.mulPose(Axis.YP.rotationDegrees(90));
        matrices.scale(0.5f, 0.5f, 0.5f);

        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack stack = itemStacks.get(i);
            if (!stack.isEmpty()) {
                matrices.pushPose();
                matrices.translate(0f, 0f, 0.2f * i);
                matrices.mulPose(Axis.YN.rotationDegrees(22.5f));
                ClientUtil.renderItem(stack, matrices, collector, itemResolver, level, pos, light);
                matrices.popPose();
            }
        }
    }
}
