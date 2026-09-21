package net.satisfy.meadow.client.renderer.block.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.satisfy.meadow.client.util.ClientUtil;

public class FlowerPotBigRenderer implements StorageTypeRenderer {

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver,
            BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash,
            NonNullList<ItemStack> itemStacks, int light) {
        if (!itemStacks.isEmpty()) {
            ItemStack itemStack = itemStacks.get(0);
            if (itemStack.getItem() instanceof BlockItem blockItem) {
                BlockState state = blockItem.getBlock().defaultBlockState();
                matrices.translate(-0.5f, 0.4f, -0.5f);
                ClientUtil.renderBlock(state, matrices, collector, blockResolver, level, pos);
                state = blockItem.getBlock().defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
                matrices.translate(0f, 1f, 0f);
                ClientUtil.renderBlock(state, matrices, collector, blockResolver, level, pos);
            }
        }
    }
}
