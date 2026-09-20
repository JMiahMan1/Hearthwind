package net.dungeonz.block.render;

import net.dungeonz.block.DungeonGateBlock;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class DungeonGateRenderer implements BlockEntityRenderer<DungeonGateEntity, DungeonGateRenderer.GateRenderState> {
    public DungeonGateRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public GateRenderState createRenderState() {
        return new GateRenderState();
    }

    @Override
    public void extractRenderState(DungeonGateEntity blockEntity, GateRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        BlockState gateState = blockEntity.getBlockState();
        state.enabled = gateState.getValue(DungeonGateBlock.ENABLED) && blockEntity.getLevel() != null;
        state.model.clear();
        if (state.enabled) {
            BlockState disguise = blockEntity.getDisguiseBlockState();
            Minecraft.getInstance().getModelManager().getBlockModelSet().get(disguise)
                    .update(state.model, disguise, BlockDisplayContext.create(), disguise.getSeed(blockEntity.getBlockPos()));
            state.model.blockLightCoords = disguise.emissiveRendering() ? 15728880 : LightCoordsUtil.pack(disguise.getLightEmission(), 0);
        }
    }

    @Override
    public void submit(GateRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.enabled) {
            state.model.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }
    }

    public static class GateRenderState extends BlockEntityRenderState {
        public boolean enabled;
        public final BlockModelRenderState model = new BlockModelRenderState();
    }

}
