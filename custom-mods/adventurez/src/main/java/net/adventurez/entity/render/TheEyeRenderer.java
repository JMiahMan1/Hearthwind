package net.adventurez.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.adventurez.entity.TheEyeEntity;
import net.adventurez.entity.model.TheEyeModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class TheEyeRenderer extends MobRenderer<TheEyeEntity, AdventureRenderState, TheEyeModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/the_eye.png");
    private static final Identifier BEAM_TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/eye_beam.png");

    public TheEyeRenderer(EntityRendererProvider.Context context) {
        super(context, new TheEyeModel(context.bakeLayer(RenderInit.THE_EYE_LAYER)), 1.0F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(TheEyeEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
        LivingEntity target = entity.getBeamTarget();
        if (target != null && target.isAlive() && entity.isAlive()) {
            Vec3 targetPosition = target.getPosition(partialTicks).add(0.0, target.getBbHeight() * 0.5, 0.0);
            state.beamTarget = targetPosition.subtract(entity.getEyePosition(partialTicks));
            state.beamTime = entity.level().getGameTime() + partialTicks;
        } else {
            state.beamTarget = null;
        }
    }

    @Override
    public boolean shouldRender(TheEyeEntity entity, Frustum culler, double camX, double camY, double camZ) {
        if (super.shouldRender(entity, culler, camX, camY, camZ)) {
            return true;
        }
        LivingEntity target = entity.getBeamTarget();
        if (target == null) {
            return false;
        }
        Vec3 start = entity.getEyePosition(1.0F);
        Vec3 end = target.getPosition(1.0F).add(0.0, target.getBbHeight() * 0.5, 0.0);
        return culler.isVisible(new AABB(start.x, start.y, start.z, end.x, end.y, end.z));
    }

    @Override
    protected void scale(AdventureRenderState state, PoseStack poseStack) {
        poseStack.scale(4.0F, 4.0F, 4.0F);
    }

    @Override
    public void submit(AdventureRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.beamTarget == null) {
            return;
        }
        Vec3 beam = state.beamTarget;
        float length = (float) (beam.length() + 1.0);
        beam = beam.normalize();
        float xRot = (float) Math.acos(beam.y);
        float yRot = (float) (Math.PI / 2.0) - (float) Math.atan2(beam.z, beam.x);
        poseStack.pushPose();
        poseStack.translate(0.0F, state.eyeHeight, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot * (180.0F / (float) Math.PI)));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot * (180.0F / (float) Math.PI)));
        float time = state.beamTime;
        float vOffset = -1.0F + time * 0.5F % 1.0F;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(BEAM_TEXTURE), (pose, buffer) -> {
            float q = time * 0.05F * -1.5F;
            int red = 64 + (int) (time * time * 191.0F);
            int green = 32 + (int) (time * time * 191.0F);
            int blue = 128 - (int) (time * time * 64.0F);
            float wx = Mth.cos(q + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
            float wy = Mth.sin(q + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
            float ex = Mth.cos(q + (float) (Math.PI / 4.0)) * 0.282F;
            float ey = Mth.sin(q + (float) (Math.PI / 4.0)) * 0.282F;
            float sx = Mth.cos(q + (float) (Math.PI * 5.0 / 4.0)) * 0.282F;
            float sy = Mth.sin(q + (float) (Math.PI * 5.0 / 4.0)) * 0.282F;
            float tx = Mth.cos(q + (float) (Math.PI * 7.0 / 4.0)) * 0.282F;
            float ty = Mth.sin(q + (float) (Math.PI * 7.0 / 4.0)) * 0.282F;
            vertex(buffer, pose, wx, length, wy, red, green, blue, 0.4999F, vOffset + length * 2.5F);
            vertex(buffer, pose, wx, 0.0F, wy, red, green, blue, 0.4999F, vOffset);
            vertex(buffer, pose, ex, 0.0F, ey, red, green, blue, 0.0F, vOffset);
            vertex(buffer, pose, ex, length, ey, red, green, blue, 0.0F, vOffset + length * 2.5F);
            vertex(buffer, pose, tx, length, ty, red, green, blue, 0.4999F, vOffset + length * 2.5F);
            vertex(buffer, pose, tx, 0.0F, ty, red, green, blue, 0.4999F, vOffset);
            vertex(buffer, pose, sx, 0.0F, sy, red, green, blue, 0.0F, vOffset);
            vertex(buffer, pose, sx, length, sy, red, green, blue, 0.0F, vOffset + length * 2.5F);
            float vBase = Mth.floor(time) % 2 == 0 ? 0.5F : 0.0F;
            vertex(buffer, pose, wx, length, wy, red, green, blue, 0.5F, vBase + 0.5F);
            vertex(buffer, pose, ex, length, ey, red, green, blue, 1.0F, vBase + 0.5F);
            vertex(buffer, pose, tx, length, ty, red, green, blue, 1.0F, vBase);
            vertex(buffer, pose, sx, length, sy, red, green, blue, 0.5F, vBase);
        });
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, int red, int green, int blue, float u, float v) {
        buffer.addVertex(pose, x, y, z).setColor(red, green, blue, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
