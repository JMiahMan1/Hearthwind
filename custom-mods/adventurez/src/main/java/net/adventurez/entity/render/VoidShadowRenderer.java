package net.adventurez.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.adventurez.entity.VoidShadowEntity;
import net.adventurez.entity.model.VoidShadowModel;
import net.adventurez.entity.render.feature.VoidShadowBlackFeatureRenderer;
import net.adventurez.entity.render.feature.VoidShadowEyesFeatureRenderer;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class VoidShadowRenderer extends MobRenderer<VoidShadowEntity, AdventureRenderState, VoidShadowModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/void_shadow.png");

    public VoidShadowRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidShadowModel(context.bakeLayer(RenderInit.VOID_SHADOW_LAYER)), 1.7F);
        this.addLayer(new VoidShadowEyesFeatureRenderer(this));
        this.addLayer(new VoidShadowBlackFeatureRenderer(this));
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(VoidShadowEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
        if (entity.ticksSinceDeath > 40) {
            state.deathProgress = (entity.ticksSinceDeath + partialTicks) / 200.0F;
        }
    }

    @Override
    protected boolean isBodyVisible(AdventureRenderState state) {
        return state.deathProgress <= 0.0F;
    }

    @Override
    protected void scale(AdventureRenderState state, PoseStack poseStack) {
        poseStack.scale(9.0F, 9.0F, 9.0F);
    }

    @Override
    public void submit(AdventureRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.deathProgress > 0.0F) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 8.0F, -2.0F);
            submitDeathAnimation(poseStack, state.deathProgress, collector);
            poseStack.popPose();
        }
    }

    private static void submitDeathAnimation(PoseStack poseStack, float progress, SubmitNodeCollector collector) {
        collector.submitCustomGeometry(poseStack, RenderTypes.dragonRays(), (pose, buffer) -> {
            float overDrive = Mth.clamp(progress > 0.8F ? (progress - 0.8F) / 0.2F : 0.0F, 0.0F, 1.0F);
            int innerColor = ARGB.colorFromFloat(1.0F - overDrive, 1.0F, 1.0F, 1.0F);
            RandomSource random = RandomSource.create(432L);
            Vector3f origin = new Vector3f();
            Vector3f left = new Vector3f();
            Vector3f right = new Vector3f();
            Vector3f bottom = new Vector3f();
            Quaternionf rotation = new Quaternionf();
            int count = Mth.floor((progress + progress * progress) / 2.0F * 60.0F);
            for (int i = 0; i < count; i++) {
                rotation.rotationXYZ(random.nextFloat() * (float) (Math.PI * 2.0), random.nextFloat() * (float) (Math.PI * 2.0), random.nextFloat() * (float) (Math.PI * 2.0))
                    .rotateXYZ(random.nextFloat() * (float) (Math.PI * 2.0), random.nextFloat() * (float) (Math.PI * 2.0), random.nextFloat() * (float) (Math.PI * 2.0) + progress * (float) (Math.PI / 2.0));
                pose.rotate(rotation);
                float length = random.nextFloat() * 20.0F + 5.0F + overDrive * 10.0F;
                float width = random.nextFloat() * 2.0F + 1.0F + overDrive * 2.0F;
                left.set(-(float) (Math.sqrt(3.0) / 2.0) * width, length, -0.5F * width);
                right.set((float) (Math.sqrt(3.0) / 2.0) * width, length, -0.5F * width);
                bottom.set(0.0F, length, width);
                buffer.addVertex(pose, origin).setColor(innerColor);
                buffer.addVertex(pose, left).setColor(-1);
                buffer.addVertex(pose, right).setColor(-1);
                buffer.addVertex(pose, origin).setColor(innerColor);
                buffer.addVertex(pose, right).setColor(-1);
                buffer.addVertex(pose, bottom).setColor(-1);
                buffer.addVertex(pose, origin).setColor(innerColor);
                buffer.addVertex(pose, bottom).setColor(-1);
                buffer.addVertex(pose, left).setColor(-1);
            }
        });
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
