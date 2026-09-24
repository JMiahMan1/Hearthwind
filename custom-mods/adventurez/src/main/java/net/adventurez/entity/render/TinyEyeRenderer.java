package net.adventurez.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.model.TinyEyeModel;
import net.adventurez.entity.nonliving.TinyEyeEntity;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class TinyEyeRenderer extends EntityRenderer<TinyEyeEntity, AdventureRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/the_eye.png");
    private final TinyEyeModel model;

    public TinyEyeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TinyEyeModel(context.bakeLayer(RenderInit.TINY_EYE_LAYER));
    }

    @Override
    protected int getBlockLightLevel(TinyEyeEntity entity, BlockPos pos) {
        return entity.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(TinyEyeEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public void submit(AdventureRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(-0.5F, -0.5F, 0.5F);
        poseStack.translate(0.0F, -1.4F, 0.0F);
        collector.submitModel(this.model, state, poseStack, RenderTypes.entityCutout(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    protected Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
