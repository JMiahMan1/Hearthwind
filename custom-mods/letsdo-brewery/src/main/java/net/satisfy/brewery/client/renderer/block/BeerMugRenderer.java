package net.satisfy.brewery.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.brewery.core.block.BeerMugFlowerPotBlock;
import net.satisfy.brewery.core.block.entity.BeerMugBlockEntity;
import net.satisfy.farm_and_charm.client.util.ClientUtil;

// 26.2: same mug-flower pose as before; the flower block submits through
// the collector with the block resolver from the render context.
@SuppressWarnings("unused")
public class BeerMugRenderer implements BlockEntityRenderer<BeerMugBlockEntity, BeerMugRenderer.State> {
    private final BlockModelResolver blockResolver;

    public static class State extends BlockEntityRenderState {
        public Level level;
        public boolean isMugPot;
        public Item flower;
    }

    public BeerMugRenderer(BlockEntityRendererProvider.Context ctx) {
        this.blockResolver = ctx.blockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BeerMugBlockEntity entity, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(entity, state, crumbling);
        state.level = entity.hasLevel() ? entity.getLevel() : null;
        if (state.level == null) return;
        BlockState selfState = entity.getBlockState();
        state.isMugPot = selfState.getBlock() instanceof BeerMugFlowerPotBlock;
        state.flower = entity.getFlower();
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.level == null || !state.isMugPot || !(state.flower instanceof BlockItem blockItem)) {
            return;
        }
        matrices.pushPose();
        BlockState flowerState = blockItem.getBlock().defaultBlockState();
        matrices.translate(0f, 0.4f, 0f);
        ClientUtil.renderBlock(flowerState, matrices, collector, blockResolver, state.level, state.blockPos);
        matrices.popPose();
    }
}
