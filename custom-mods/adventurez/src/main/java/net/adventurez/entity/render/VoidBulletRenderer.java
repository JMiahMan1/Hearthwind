package net.adventurez.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.adventurez.entity.model.VoidBulletModel;
import net.adventurez.entity.nonliving.VoidBulletEntity;
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
public class VoidBulletRenderer extends EntityRenderer<VoidBulletEntity, AdventureRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/void_bullet.png");
    private final VoidBulletModel model;

    public VoidBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new VoidBulletModel(context.bakeLayer(RenderInit.VOID_BULLET_LAYER));
    }

    @Override
    protected int getBlockLightLevel(VoidBulletEntity entity, BlockPos pos) {
        return entity.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(VoidBulletEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public void submit(AdventureRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.55F, 0.0F);
        collector.submitModel(this.model, state, poseStack, RenderTypes.entityCutout(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    protected Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
