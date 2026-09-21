package net.satisfy.farm_and_charm.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.satisfy.farm_and_charm.core.block.entity.StorageBlockEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class ChickenNestRenderer implements StorageTypeRenderer {

    private record TransformData(Vector3f position, float rotYDeg, float rotXDeg) {}

    private static final TransformData[] TRANSFORMS = new TransformData[] {
            new TransformData(new Vector3f(-0.2f, 0.3f, -0.2f), 45f, 65f),
            new TransformData(new Vector3f(0.2f, 0.3f, -0.2f), 20f, 60f),
    };

    @Override
    public void render(PoseStack matrixStack, SubmitNodeCollector collector, ItemModelResolver itemResolver,
            BlockModelResolver blockResolver, Level level, BlockPos pos, int posHash,
            NonNullList<ItemStack> itemStacks, int light) {
        if (itemStacks.isEmpty()) return;

        matrixStack.pushPose();
        setupInitialTransform(matrixStack);

        Random random = new Random(posHash);

        for (int index = 0; index < itemStacks.size(); index++) {
            ItemStack stack = itemStacks.get(index);
            if (stack.isEmpty()) continue;

            matrixStack.pushPose();
            TransformData transform = getTransform(index);
            applyItemTransform(matrixStack, transform);
            renderItems(matrixStack, collector, itemResolver, level, pos, light, stack, random);
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    private void setupInitialTransform(PoseStack matrixStack) {
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        matrixStack.translate(0.1f, 0.0F, 0.3f);
    }

    private TransformData getTransform(int index) {
        return index < TRANSFORMS.length ? TRANSFORMS[index] : new TransformData(new Vector3f(0f, 0.3f, 0f), 0f, 0f);
    }

    private void applyItemTransform(PoseStack matrixStack, TransformData data) {
        matrixStack.translate(data.position.x, data.position.y, data.position.z);
        Quaternionf rotation = new Quaternionf()
                .rotateY((float) Math.toRadians(data.rotYDeg))
                .rotateX((float) Math.toRadians(data.rotXDeg));
        matrixStack.mulPose(rotation);
    }

    private void renderItems(PoseStack matrixStack, SubmitNodeCollector collector, ItemModelResolver itemResolver,
            Level level, BlockPos pos, int light, ItemStack stack, Random random) {
        for (int i = 0; i <= stack.getCount() / 8; i++) {
            matrixStack.pushPose();
            Vector3f offset = offsetRandomly(random);
            matrixStack.translate(offset.x, offset.y, offset.z);
            ClientUtil.renderItem(stack, matrixStack, collector, itemResolver, level, pos, light);
            matrixStack.popPose();
        }
    }

    private Vector3f offsetRandomly(Random random) {
        return new Vector3f(
                (random.nextFloat() - 0.5f) * 0.125f,
                (random.nextFloat() - 0.5f) * 0.125f,
                (random.nextFloat() - 0.5f) * 0.125f
        );
    }
}
