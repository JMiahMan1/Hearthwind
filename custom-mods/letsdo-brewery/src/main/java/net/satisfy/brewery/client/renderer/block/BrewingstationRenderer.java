package net.satisfy.brewery.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.satisfy.brewery.core.block.BrewingstationBlock;
import net.satisfy.brewery.core.block.entity.BrewstationBlockEntity;
import net.satisfy.farm_and_charm.client.util.ClientUtil;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// 26.2: same orbit poses and jitter as before; items submit through the
// collector with the item resolver from the render context.
@SuppressWarnings("unused")
public class BrewingstationRenderer implements BlockEntityRenderer<BrewstationBlockEntity, BrewingstationRenderer.State> {
    private final ItemModelResolver itemResolver;

    public static class State extends BlockEntityRenderState {
        public Level level;
        public List<ItemStack> ingredients = List.of();
    }

    public BrewingstationRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemResolver = ctx.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BrewstationBlockEntity entity, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(entity, state, crumbling);
        if (!entity.hasLevel() || !(entity.getBlockState().getBlock() instanceof BrewingstationBlock)) {
            state.level = null;
            state.ingredients = List.of();
            return;
        }
        state.level = entity.getLevel();
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack stack : entity.getIngredient()) copy.add(stack.copy());
        state.ingredients = copy;
    }

    @Override
    public void submit(State state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.level == null || state.ingredients.isEmpty()) return;

        List<ItemStack> ingredients = state.ingredients;
        matrixStack.pushPose();
        setupInitialTransform(matrixStack);

        Random random = new Random(state.blockPos.hashCode());
        float angleOffset = 360f / ingredients.size();

        for (int index = 0; index < ingredients.size(); index++) {
            ItemStack stack = ingredients.get(index);
            if (stack.isEmpty()) continue;
            matrixStack.pushPose();
            Vector3f position = calculateItemPosition(index, angleOffset, ingredients.size());
            applyItemTransform(matrixStack, position, angleOffset * index);
            renderItems(matrixStack, collector, state, stack, random);
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    private void setupInitialTransform(PoseStack matrixStack) {
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        matrixStack.translate(1.0f, 0.3F, 1.0f);
    }

    private Vector3f calculateItemPosition(int index, float angleOffset, int itemCount) {
        if (itemCount == 1) return new Vector3f(0, 0.3f, 0);
        double angleRad = Math.toRadians(angleOffset * index);
        return new Vector3f((float) (0.125 * Math.cos(angleRad)), 0.3f, (float) (0.125 * Math.sin(angleRad)));
    }

    private void applyItemTransform(PoseStack matrixStack, Vector3f position, float angle) {
        Quaternionf rotation = new Quaternionf().rotateY(angle + 35).rotateX(65);
        matrixStack.translate(position.x, position.y, position.z);
        matrixStack.mulPose(rotation);
    }

    private void renderItems(PoseStack matrixStack, SubmitNodeCollector collector, State state, ItemStack stack, Random random) {
        for (int i = 0; i <= stack.getCount() / 8; i++) {
            matrixStack.pushPose();
            Vector3f offset = offsetRandomly(random);
            matrixStack.translate(offset.x, offset.y, offset.z);
            ClientUtil.renderItem(stack, matrixStack, collector, itemResolver, state.level, state.blockPos, state.lightCoords);
            matrixStack.popPose();
        }
    }

    private Vector3f offsetRandomly(Random random) {
        return new Vector3f((random.nextFloat() - 0.5f) * 2 * (float) 0.0625, (random.nextFloat() - 0.5f) * 2 * (float) 0.0625, (random.nextFloat() - 0.5f) * 2 * (float) 0.0625);
    }
}
