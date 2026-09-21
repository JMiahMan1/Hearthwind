package net.satisfy.candlelight.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.satisfy.farm_and_charm.client.util.ClientUtil;

@Environment(EnvType.CLIENT)
public class JewelryRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> itemStacks, int light) {
        if (!itemStacks.isEmpty()) {
            ItemStack stack = itemStacks.get(0);
            if (!stack.isEmpty()) {
                matrices.pushPose();
                matrices.mulPose(Axis.ZN.rotationDegrees(45f));
                matrices.translate(-0.1, 0.1, 0);
                matrices.scale(0.25f, 0.25f, 0.25f);
                ClientUtil.renderItem(stack, matrices, collector, itemResolver, level, pos, light);
                matrices.popPose();
            }
        }
    }
}
