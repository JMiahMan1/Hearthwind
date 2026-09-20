package net.dungeonz.block.render;

import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.block.logic.DungeonSpawnerLogic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class DungeonSpawnerRenderer implements BlockEntityRenderer<DungeonSpawnerEntity, SpawnerRenderState> {
    private final EntityRenderDispatcher entityRenderer;

    public DungeonSpawnerRenderer(BlockEntityRendererProvider.Context ctx) {
        this.entityRenderer = ctx.entityRenderer();
    }

    @Override
    public SpawnerRenderState createRenderState() {
        return new SpawnerRenderState();
    }

    @Override
    public void extractRenderState(DungeonSpawnerEntity blockEntity, SpawnerRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        if (blockEntity.getLevel() != null) {
            DungeonSpawnerLogic logic = blockEntity.getLogic();
            Entity entity = logic.getRenderedEntity(blockEntity.getLevel());
            if (entity != null) {
                state.displayEntity = this.entityRenderer.extractEntity(entity, partialTicks);
                state.displayEntity.lightCoords = state.lightCoords;
                state.spin = (float) Mth.lerp(partialTicks, logic.randomParticleValueTwo(), logic.randomParticleValueOne()) * 10.0F;
                state.scale = 0.53125F;
                float maxLength = Math.max(entity.getBbWidth(), entity.getBbHeight());
                if (maxLength > 1.0F) {
                    state.scale /= maxLength;
                }
            }
        }
    }

    @Override
    public void submit(SpawnerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.displayEntity != null) {
            SpawnerRenderer.submitEntityInSpawner(poseStack, submitNodeCollector, state.displayEntity, this.entityRenderer, state.spin, state.scale, camera);
        }
    }
}
