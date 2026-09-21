package net.satisfy.farm_and_charm.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.satisfy.farm_and_charm.FarmAndCharm;
import net.satisfy.farm_and_charm.client.model.WaterSprinklerModel;
import net.satisfy.farm_and_charm.core.block.entity.WaterSprinklerBlockEntity;
import org.joml.Quaternionf;

// 26.2: same rain-driven spin accumulation and texture as before,
// submitted through BakedPartModel wrappers.
public class WaterSprinklerRenderer implements BlockEntityRenderer<WaterSprinklerBlockEntity, WaterSprinklerRenderer.State> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(FarmAndCharm.MOD_ID, "textures/entity/water_sprinkler.png");
    private final ModelPart rotating;
    private final ModelPart basin;
    private final BakedPartModel rotatingModel;
    private final BakedPartModel basinModel;
    private long lastRenderTime = 0;
    private float rotationAngle = 0.0F;

    public static class State extends BlockEntityRenderState {
        public float angle;
    }

    public WaterSprinklerRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(WaterSprinklerModel.LAYER_LOCATION);
        this.rotating = root.getChild("rotating");
        this.basin = root.getChild("basin");
        this.rotatingModel = new BakedPartModel(this.rotating);
        this.basinModel = new BakedPartModel(this.basin);
    }

    private float updateRotationAngle(WaterSprinklerBlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        assert level != null;
        boolean isRaining = level.isRaining();
        boolean isThundering = level.isThundering();
        float rotationSpeed = isRaining || isThundering ? 2.0F : 1.0F;

        long currentTime = System.currentTimeMillis();
        if (lastRenderTime == 0) {
            lastRenderTime = currentTime;
        }
        float deltaTime = (currentTime - lastRenderTime) / 1000.0F;
        lastRenderTime = currentTime;

        rotationAngle += rotationSpeed * deltaTime * 40.0F;
        rotationAngle %= 360;

        blockEntity.setRotationAngle(rotationAngle);
        return rotationAngle;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WaterSprinklerBlockEntity blockEntity, State state, float partialTick,
            Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);
        state.angle = updateRotationAngle(blockEntity);
    }

    @Override
    public void submit(State state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0, 0.5);
        matrixStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(-state.angle)));
        matrixStack.translate(-0.5, 0, -0.5);
        collector.submitModel(rotatingModel, state, matrixStack, RenderTypes.entitySolid(TEXTURE),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
        matrixStack.popPose();
        collector.submitModel(basinModel, state, matrixStack, RenderTypes.entitySolid(TEXTURE),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
    }
}
