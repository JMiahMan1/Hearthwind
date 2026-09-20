package net.dungeonz.block.render;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.EndPortalRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;

@Environment(EnvType.CLIENT)
public class DungeonPortalRenderer extends AbstractEndPortalRenderer<DungeonPortalEntity, EndPortalRenderState> {

    public DungeonPortalRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public EndPortalRenderState createRenderState() {
        return new EndPortalRenderState();
    }

    @Override
    public void submit(EndPortalRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // 26.x: the end-portal squash lives in TheEndPortalRenderer's fixed
        // TRANSFORMATION; the dungeon portal is a full block (old getOffsetUp
        // 1.0 / getOffsetDown 0.0), so submit the cube without that transform.
        submitCube(state.facesToShow, RenderTypes.endPortal(), poseStack, submitNodeCollector);
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

}
