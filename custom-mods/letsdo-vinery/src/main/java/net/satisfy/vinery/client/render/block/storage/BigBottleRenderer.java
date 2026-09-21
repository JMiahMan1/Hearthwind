package net.satisfy.vinery.client.render.block.storage;

import com.mojang.blaze3d.vertex.PoseStack;
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

public class BigBottleRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> itemStacks, int light) {
        matrices.translate(-0.4, 0.07, -0.5);
        matrices.scale(0.8f, 0.8f, 0.9f);
        ItemStack stack = itemStacks.get(0);
        if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            if (state.hasProperty(WineBottleBlock.FAKE_MODEL)) {
                state = state.setValue(WineBottleBlock.FAKE_MODEL, false);
            }
            ClientUtil.renderBlock(state, matrices, collector, blockResolver, level, pos);
        }
    }
}