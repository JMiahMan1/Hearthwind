package net.adventurez.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.model.GildedStoneModel;
import net.adventurez.entity.nonliving.GildedBlackstoneShardEntity;
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
public class GildedStoneRenderer extends EntityRenderer<GildedBlackstoneShardEntity, AdventureRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/item/gilded_stone.png");
    private final GildedStoneModel model;

    public GildedStoneRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new GildedStoneModel(context.bakeLayer(RenderInit.GILDED_STONE_LAYER));
    }

    @Override
    protected int getBlockLightLevel(GildedBlackstoneShardEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(GildedBlackstoneShardEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public void submit(AdventureRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.45F, 0.0F);
        collector.submitModel(this.model, state, poseStack, RenderTypes.entityCutout(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    protected Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
