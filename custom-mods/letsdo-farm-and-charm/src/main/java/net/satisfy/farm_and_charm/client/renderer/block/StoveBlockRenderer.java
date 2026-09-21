package net.satisfy.farm_and_charm.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.satisfy.farm_and_charm.client.util.ClientUtil;
import net.satisfy.farm_and_charm.core.block.StoveBlock;
import net.satisfy.farm_and_charm.core.block.entity.StoveBlockEntity;

import java.util.Arrays;

// 26.2: same slot/output item layout, submitted through ClientUtil.
public class StoveBlockRenderer implements BlockEntityRenderer<StoveBlockEntity, StoveBlockRenderer.State> {
    private final ItemModelResolver itemResolver;

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public NonNullList<ItemStack> items = NonNullList.create();
        public int[] slots = new int[0];
        public int outputSlot;
        public Level level;
    }

    public StoveBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StoveBlockEntity blockEntity, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);
        state.level = blockEntity.getLevel();
        state.facing = blockEntity.getBlockState().getValue(StoveBlock.FACING);
        state.items = NonNullList.create();
        for (int i = 0; i < 5; i++) state.items.add(blockEntity.getItem(i).copy());
        state.slots = blockEntity.getIngredientSlots();
        state.outputSlot = blockEntity.getOutputSlot();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.level == null) return;
        Direction direction = state.facing;
        Vec3 baseOffset = new Vec3(0.5, 1.0, 0.5);
        Vec3 directionOffset = direction.getUnitVec3().scale(0.3);
        double downOffset = -0.575;
        Vec3 inputSlotOffset = new Vec3(
                direction == Direction.NORTH || direction == Direction.WEST ? 0.15 : -0.15,
                downOffset,
                direction == Direction.NORTH || direction == Direction.EAST ? 0.15 : -0.15
        );

        renderSlots(state, poseStack, baseOffset, directionOffset, inputSlotOffset, collector);
        renderOutput(state, poseStack, baseOffset, directionOffset, collector);
    }

    private void renderSlots(State state, PoseStack poseStack, Vec3 baseOffset, Vec3 directionOffset,
            Vec3 slotOffset, SubmitNodeCollector collector) {
        int nonEmptyCount = (int) Arrays.stream(state.slots).filter(slot -> !state.items.get(slot).isEmpty()).count();
        double ySpacing = 1.0 / 50;

        double yOffset = 0.1;
        for (int slot : state.slots) {
            ItemStack stack = state.items.get(slot);
            if (!stack.isEmpty()) {
                poseStack.pushPose();
                Vec3 position = baseOffset.add(directionOffset).add(slotOffset).add(0, yOffset, 0);
                poseStack.translate(position.x, position.y, position.z);
                poseStack.mulPose(Axis.YP.rotationDegrees(45f * (nonEmptyCount - 1)));
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                poseStack.scale(0.3f, 0.3f, 0.3f);
                ClientUtil.renderItem(stack, poseStack, collector, itemResolver, state.level, state.blockPos, state.lightCoords);
                poseStack.popPose();
                yOffset += ySpacing;
            }
        }
    }

    private void renderOutput(State state, PoseStack poseStack, Vec3 baseOffset, Vec3 directionOffset,
            SubmitNodeCollector collector) {
        ItemStack outputStack = state.items.get(state.outputSlot);
        if (!outputStack.isEmpty()) {
            poseStack.pushPose();
            Vec3 position = baseOffset.add(directionOffset).add(0, -0.49, 0);
            poseStack.translate(position.x, position.y, position.z);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.scale(0.3f, 0.3f, 0.3f);
            ClientUtil.renderItem(outputStack, poseStack, collector, itemResolver, state.level, state.blockPos, state.lightCoords);
            poseStack.popPose();
        }
    }
}
