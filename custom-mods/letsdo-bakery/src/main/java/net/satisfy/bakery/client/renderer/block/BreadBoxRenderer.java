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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.satisfy.bakery.core.registry.ObjectRegistry;
import net.satisfy.farm_and_charm.client.util.ClientUtil;

@Environment(EnvType.CLIENT)
public class BreadBoxRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> itemStacks, int light) {
        matrices.translate(-0.25, 0.3, 0);
        matrices.scale(0.5f, 0.5f, 0.5f);
        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack stack = itemStacks.get(i);
            if (!stack.isEmpty()) {
                matrices.pushPose();
                if (stack.getItem() instanceof BlockItem blockItem) {
                    if (blockItem.getBlock() == ObjectRegistry.CRUSTY_BREAD_BLOCK.get()) {
                        matrices.translate(-0.5f, -0.5f, -0.9f);
                        matrices.scale(2f, 2f, 2f);
                        ClientUtil.renderBlockFromItem(blockItem, matrices, collector, blockResolver, level, pos);
                    } else if (blockItem.getBlock() == ObjectRegistry.BUN_BLOCK.get()) {
                        matrices.translate(-0.5f, -0.5f, -0.9f);
                        matrices.scale(2f, 2f, 2f);
                        ClientUtil.renderBlockFromItem(blockItem, matrices, collector, blockResolver, level, pos);
                    } else if (blockItem.getBlock() == ObjectRegistry.BREAD_BLOCK.get()) {
                        matrices.translate(-0.5f, -0.5f, 0.93f);
                        matrices.scale(2f, 2f, 2f);
                        matrices.mulPose(Axis.YP.rotationDegrees(90.0f));
                        ClientUtil.renderBlockFromItem(blockItem, matrices, collector, blockResolver, level, pos);
                    } else if (blockItem.getBlock() == ObjectRegistry.BRAIDED_BREAD_BLOCK.get()) {
                        matrices.translate(-0.5f, -0.5f, 0.93f);
                        matrices.scale(2f, 2f, 2f);
                        matrices.mulPose(Axis.YP.rotationDegrees(90.0f));
                        ClientUtil.renderBlockFromItem(blockItem, matrices, collector, blockResolver, level, pos);
                    } else if (blockItem.getBlock() == ObjectRegistry.TOAST_BLOCK.get()) {
                        matrices.translate(-0.5f, -0.5f, 0.93f);
                        matrices.scale(2f, 2f, 2f);
                        matrices.mulPose(Axis.YP.rotationDegrees(90.0f));
                        ClientUtil.renderBlockFromItem(blockItem, matrices, collector, blockResolver, level, pos);
                    } else if (blockItem.getBlock() == ObjectRegistry.BAGUETTE_BLOCK.get()) {
                        matrices.translate(-0.28f, -0.48f, 0.6f);
                        matrices.scale(1.5f, 1.5f, 1.5f);
                        matrices.mulPose(Axis.YP.rotationDegrees(90.0f));
                        ClientUtil.renderBlockFromItem(blockItem, matrices, collector, blockResolver, level, pos);
                    } else {
                        matrices.translate(0.2, -0.5f, -0.3f);
                        matrices.scale(0.6f, 0.6f, 0.6f);
                        ClientUtil.renderBlockFromItem(blockItem, matrices, collector, blockResolver, level, pos);
                    }
                } else {
                    matrices.translate(0.3f * i, 0, 0);
                    matrices.mulPose(Axis.YN.rotationDegrees(45.0f));
                    ClientUtil.renderItem(stack, matrices, collector, itemResolver, level, pos, light);
                }

                matrices.popPose();
            }
        }
    }
}