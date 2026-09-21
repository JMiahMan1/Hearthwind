package net.satisfy.brewery.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.satisfy.brewery.Brewery;
import net.satisfy.brewery.core.entity.BeerElementalAttackEntity;
import org.joml.Vector3f;
import org.joml.Vector4f;

// 26.2: billboard quad submits through submitCustomGeometry (mirrors vanilla DragonFireballRenderer).
public class BeerElementalAttackRenderer extends EntityRenderer<BeerElementalAttackEntity, EntityRenderState> {
    private static final Identifier TEXTURE_LOCATION = Brewery.identifier("textures/particle/beer_elemental_attack.png");
    private static final net.minecraft.client.renderer.rendertype.RenderType RENDER_TYPE = RenderTypes.entityTranslucent(TEXTURE_LOCATION);

    private static final float SCALE = 0.4F;
    private static final Vec2[] UVS = new Vec2[]{
            new Vec2(1F, 1F), new Vec2(0F, 1F), new Vec2(0F, 0F), new Vec2(1F, 0F)
    };
    private static final Vector3f[] VERTS = new Vector3f[]{
            new Vector3f(0.5F, -0.5F, 0.0F),
            new Vector3f(-0.5F, -0.5F, 0.0F),
            new Vector3f(-0.5F, 0.5F, 0.0F),
            new Vector3f(0.5F, 0.5F, 0.0F)
    };

    public BeerElementalAttackRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.ageInTicks < 2 && state.distanceToCameraSq < 12.25D)
            return;

        poseStack.pushPose();

        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.mulPose(cameraState.orientation);

        collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, consumer) -> buildQuad(state, pose, consumer, -1));

        poseStack.popPose();

        super.submit(state, poseStack, collector, cameraState);
    }

    private static void buildQuad(EntityRenderState state, PoseStack.Pose pose, VertexConsumer consumer, int color) {
        for (int i = 0; i < 4; i++) {
            Vector3f localPos = VERTS[i];
            Vec2 quadUvs = UVS[i];

            Vector4f pos = new Vector4f(localPos.x(), localPos.y() + 0.5F, localPos.z(), 1.0F);
            pos.mul(pose.pose());

            vertex(consumer, pose, state.lightCoords, pos.x(), pos.y(), pos.z(), quadUvs.x, quadUvs.y, color);
        }
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int light, float x, float y, float z, float u, float v, int color) {
        consumer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    protected int getBlockLightLevel(BeerElementalAttackEntity entity, BlockPos pos) {
        return 15;
    }
}
