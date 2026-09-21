package net.satisfy.farm_and_charm.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.satisfy.farm_and_charm.client.util.ClientUtil;

@Environment(EnvType.CLIENT)
public class WindowSillRenderer implements StorageTypeRenderer {

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver,
            BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash,
            NonNullList<ItemStack> itemStacks, int light) {
        matrices.translate(-0.25f, 0.25f, 0.75f);
        matrices.mulPose(Axis.YP.rotationDegrees(90));

        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack stack = itemStacks.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                matrices.pushPose();
                matrices.translate(0f, 0f, -0.5f * i);
                ClientUtil.renderBlockFromItem(blockItem, matrices, collector, blockResolver, level, pos);
                matrices.popPose();
            }
        }
    }
}
