package net.satisfy.vinery.client.render.block.storage;

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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.farm_and_charm.client.util.ClientUtil;
import net.satisfy.vinery.core.block.WineBottleBlock;

public class FourBottleRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> itemStacks, int light) {
        matrices.translate(-0.13, 0.335, 0.125);
        matrices.scale(0.9f, 0.9f, 0.9f);
        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack stack = itemStacks.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                matrices.pushPose();
                if(i == 0){
                    matrices.translate(-0.35f, 0, 0f);
                }
                else if(i == 1){
                    matrices.translate(0, -0.33f, 0f);
                }
                else if(i == 2){
                    matrices.translate(-0.7f, -0.33f, 0f);
                }
                else if(i == 3){
                    matrices.translate(-0.35f, -0.66f, 0f);
                }
                else {
                    matrices.popPose();
                    continue;
                }
                matrices.mulPose(Axis.XN.rotationDegrees(90));

                BlockState state = blockItem.getBlock().defaultBlockState();
                if (state.hasProperty(WineBottleBlock.FAKE_MODEL)) {
                    state = state.setValue(WineBottleBlock.FAKE_MODEL, false);
                }
                ClientUtil.renderBlock(state, matrices, collector, blockResolver, level, pos);
                matrices.popPose();
            }
        }
    }
}