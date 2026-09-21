package net.satisfy.nethervinery.client.render.block.storage;

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
import net.satisfy.nethervinery.core.block.NetherWineBottleBlock;

@Environment(EnvType.CLIENT)
public class NetherNineBottleRenderer implements NetherStorageTypeRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemModelResolver itemResolver, BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash, NonNullList<ItemStack> itemStacks, int light) {
        matrices.translate(-0.13, 0.335, 0.125);
        matrices.scale(0.9f, 0.9f, 0.9f);

        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack stack = itemStacks.get(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
                continue;
            }

            matrices.pushPose();

            int line = i >= 6 ? 3 : i >= 3 ? 2 : 1;
            float x;
            float y;

            if (line == 1) {
                x = -0.35f * i;
                y = 0f;
            } else if (line == 2) {
                x = -0.35f * (i - 3);
                y = -0.33f;
            } else {
                x = -0.35f * (i - 6);
                y = -0.66f;
            }

            matrices.translate(x, y, 0f);
            matrices.mulPose(Axis.XN.rotationDegrees(90f));

            BlockState state = blockItem.getBlock().defaultBlockState();
            if (state.hasProperty(NetherWineBottleBlock.FAKE_MODEL)) {
                state = state.setValue(NetherWineBottleBlock.FAKE_MODEL, false);
            }

            ClientUtil.renderBlock(state, matrices, collector, blockResolver, level, pos);
            matrices.popPose();
        }
    }
}
