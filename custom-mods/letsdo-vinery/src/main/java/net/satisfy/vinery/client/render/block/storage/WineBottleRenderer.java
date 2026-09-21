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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.farm_and_charm.client.util.ClientUtil;
import net.satisfy.vinery.core.block.WineBottleBlock;
import net.satisfy.vinery.core.item.DrinkBlockItem;
import net.satisfy.vinery.core.registry.ObjectRegistry;

@Environment(EnvType.CLIENT)
public class WineBottleRenderer implements StorageTypeRenderer {
    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> nonNullList, int light) {
        poseStack.translate(-0.5, 0, -0.5);
        switch (getCount(nonNullList)) {
            case 1 -> renderOne(poseStack, collector, itemResolver, blockResolver, level, pos, posHash, nonNullList, light);
            case 2 -> renderTwo(poseStack, collector, itemResolver, blockResolver, level, pos, posHash, nonNullList, light);
            case 3 -> renderThree(poseStack, collector, itemResolver, blockResolver, level, pos, posHash, nonNullList, light);
        }
    }

    public int getCount(NonNullList<ItemStack> nonNullList){
        int count = 0;
        for(ItemStack stack : nonNullList){
            if(!stack.isEmpty()) count++;
        }
        return count;
    }

    private void renderOne(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> nonNullList, int light) {
        if(nonNullList.get(0).getItem() instanceof DrinkBlockItem item){
            ClientUtil.renderBlock(getState(item), matrices, collector, blockResolver, level, pos);
        }
    }

    private static BlockState getState(DrinkBlockItem item){
        BlockState state = item.getBlock().defaultBlockState();
        if (state.hasProperty(WineBottleBlock.FAKE_MODEL)) {
            state = state.setValue(WineBottleBlock.FAKE_MODEL, false);
        }
        return state;
    }

    private void renderTwo(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> nonNullList, int light) {
        DrinkBlockItem item1 = nonNullList.get(0).getItem() instanceof DrinkBlockItem item ? item : null;
        DrinkBlockItem item2 = nonNullList.get(1).getItem() instanceof DrinkBlockItem item ? item : null;

        matrices.translate(-0.15f, 0f, -0.25f);
        if(item1 != null){
            ClientUtil.renderBlock(getState(item1), matrices, collector, blockResolver, level, pos);
        }
        matrices.translate(.1f, 0f, .8f);
        matrices.mulPose(Axis.YP.rotationDegrees(30));
        if(item2 != null){
            ClientUtil.renderBlock(getState(item2), matrices, collector, blockResolver, level, pos);
        }
    }

    private void renderThree(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> nonNullList, int light) {
        DrinkBlockItem item1 = nonNullList.get(0).getItem() instanceof DrinkBlockItem item ? item : null;
        DrinkBlockItem item2 = nonNullList.get(1).getItem() instanceof DrinkBlockItem item ? item : null;
        DrinkBlockItem item3 = nonNullList.get(2).getItem() instanceof DrinkBlockItem item ? item : null;  // Fixed: was getting index 1 again
        matrices.translate(-0.25f, 0f, -0.25f);
        if(item1 != null){
            ClientUtil.renderBlock(getState(item1), matrices, collector, blockResolver, level, pos);
        }
        matrices.translate(.15f, 0f, .5f);
        if(item2 != null){
            ClientUtil.renderBlock(getState(item2), matrices, collector, blockResolver, level, pos);
        }
        if(item3 == null) return;
        if (item3.asItem().equals(ObjectRegistry.KELP_CIDER.get().asItem())) {
            matrices.translate(.35f, .7f, -.13f);
            matrices.mulPose(Axis.XP.rotationDegrees(90));
            ClientUtil.renderBlock(getState(item3), matrices, collector, blockResolver, level, pos);
            return;
        }
        matrices.translate(.1f, 0f, 0f);
        matrices.mulPose(Axis.YP.rotationDegrees(30));
        ClientUtil.renderBlock(getState(item3), matrices, collector, blockResolver, level, pos);
    }
}