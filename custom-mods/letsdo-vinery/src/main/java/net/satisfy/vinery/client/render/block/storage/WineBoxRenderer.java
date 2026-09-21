package net.satisfy.vinery.client.render.block.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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

public class WineBoxRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> itemStacks, int light) {
        matrices.translate(0.35, 0.6, -0.35);
        matrices.scale(0.7f, 0.7f, 0.7f);

        ItemStack stack = itemStacks.get(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        matrices.mulPose(Axis.ZP.rotationDegrees(90f));
        matrices.mulPose(Axis.YN.rotationDegrees(90f));

        BlockState renderState = blockItem.getBlock().defaultBlockState();
        if (renderState.hasProperty(WineBottleBlock.FAKE_MODEL)) {
            renderState = renderState.setValue(WineBottleBlock.FAKE_MODEL, false);
        }

        ClientUtil.renderBlock(renderState, matrices, collector, blockResolver, level, pos);
    }
}
