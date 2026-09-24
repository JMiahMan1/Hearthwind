package net.fleshz.block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fleshz.block.WoodRack;
import net.fleshz.block.entity.WoodRackEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class WoodRackRenderer implements BlockEntityRenderer<WoodRackEntity, WoodRackRenderer.State> {

    private final ItemModelResolver itemModelResolver;

    public WoodRackRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState stackState = new ItemStackRenderState();
        public boolean hasItem;
        public Direction facing = Direction.NORTH;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WoodRackEntity entity, State state, float partialTick, Vec3 camera,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(entity, state, overlay);
        ItemStack stack = entity.getItem(0);
        state.hasItem = !stack.isEmpty();
        if (state.hasItem) {
            Level level = entity.getLevel();
            BlockState blockState = entity.getBlockState();
            state.facing = blockState.getValue(WoodRack.FACING);
            this.itemModelResolver.updateForTopItem(state.stackState, stack,
                    ItemDisplayContext.GROUND, level, null,
                    (int) (level == null ? 0 : level.getGameTime()));
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.hasItem || state.stackState.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        switch (state.facing) {
            case NORTH -> {
                poseStack.scale(1.8F, 1.8F, 1.8F);
                poseStack.translate(0.28D, 0.12D, 0.5D);
            }
            case SOUTH -> {
                poseStack.scale(1.8F, 1.8F, 1.8F);
                poseStack.translate(0.28D, 0.12D, 0.05D);
            }
            case EAST -> {
                poseStack.scale(1.8F, 1.8F, 1.8F);
                poseStack.translate(0.05D, 0.12D, 0.28D);
                poseStack.mulPose(Axis.YP.rotationDegrees(90F));
            }
            case WEST -> {
                poseStack.scale(1.8F, 1.8F, 1.8F);
                poseStack.translate(0.5D, 0.12D, 0.28D);
                poseStack.mulPose(Axis.YP.rotationDegrees(90F));
            }
            default -> {
            }
        }
        state.stackState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
