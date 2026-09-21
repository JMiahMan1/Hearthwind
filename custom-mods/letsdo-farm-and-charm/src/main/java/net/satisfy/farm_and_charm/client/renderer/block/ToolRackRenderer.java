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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.satisfy.farm_and_charm.client.util.ClientUtil;
import net.satisfy.farm_and_charm.core.registry.TagRegistry;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ToolRackRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver,
            BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash,
            NonNullList<ItemStack> itemStacks, int light) {
        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack stack = itemStacks.get(i);

            if (!stack.isEmpty()) {
                matrices.pushPose();
                Item item = stack.getItem();
                double translate = (i + 1) * (1D / 3);

                if (stack.is(TagRegistry.HANGABLE)) {
                    matrices.translate(translate - (2D / 3.2), 0.675f, 0.38f);
                    matrices.scale(0.5f, 0.5f, 0.5f);
                    matrices.mulPose(Axis.ZN.rotationDegrees(45f));
                    matrices.mulPose(Axis.YN.rotationDegrees(-180f));
                    ClientUtil.renderItem(stack, matrices, collector, itemResolver, level, pos, light);
                } else {
                    matrices.translate(translate - (2D / 3), 0.6f, 0.38f);
                    matrices.scale(0.6f, 0.6f, 0.6f);
                    matrices.mulPose(Axis.ZN.rotationDegrees(135f));
                    matrices.mulPose(Axis.YN.rotationDegrees(0f));
                    ClientUtil.renderItem(stack, matrices, collector, itemResolver, level, pos, light);
                }
                matrices.popPose();
            }
        }
    }
}
