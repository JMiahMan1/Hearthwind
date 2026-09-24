package net.adventurez.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.model.RockModel;
import net.adventurez.entity.nonliving.ThrownRockEntity;
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
public class ThrownRockRenderer extends EntityRenderer<ThrownRockEntity, AdventureRenderState> {
    private static final Identifier GOLEM_TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/thrown_golem_rock.png");
    private static final Identifier VOID_TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/thrown_void_rock.png");
    private final RockModel model;

    public ThrownRockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RockModel(context.bakeLayer(RenderInit.THROWN_ROCK_LAYER));
    }

    @Override
    protected int getBlockLightLevel(ThrownRockEntity entity, BlockPos pos) {
        return entity.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(ThrownRockEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
        state.variant = entity.getItem().getItem() == entity.getDefaultItem() ? 0 : 1;
    }

    @Override
    public void submit(AdventureRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(1.5F, 1.5F, 1.5F);
        collector.submitModel(this.model, state, poseStack, RenderTypes.entityCutout(getTextureLocation(state)), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    protected Identifier getTextureLocation(AdventureRenderState state) {
        return state.variant == 0 ? GOLEM_TEXTURE : VOID_TEXTURE;
    }
}
