package net.adventurez.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.DragonEntity;
import net.adventurez.entity.model.DragonModel;
import net.adventurez.entity.render.feature.DragonEyesFeatureRenderer;
import net.adventurez.entity.render.feature.DragonSaddleFeatureRenderer;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class DragonRenderer extends MobRenderer<DragonEntity, AdventureRenderState, DragonModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/dragon.png");
    private static final Identifier RED_DRAGON_TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/red_dragon.png");

    public DragonRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonModel(context.bakeLayer(RenderInit.DRAGON_LAYER)), 0.4F);
        this.addLayer(new DragonEyesFeatureRenderer(this));
        this.addLayer(new DragonSaddleFeatureRenderer(this));
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(DragonEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
        state.size = entity.getSize();
        state.sitting = entity.isInSittingPose();
        state.variant = entity.getEntityData().get(DragonEntity.RED_DRAGON) ? 1 : 0;
        state.fireBreathing = entity.getEntityData().get(DragonEntity.FIRE_BREATH);
        state.clientStartFlying = entity.getEntityData().get(DragonEntity.CLIENT_START_FLYING);
        state.startFlying = entity.getEntityData().get(DragonEntity.IS_START_FLYING);
        state.flying = entity.getEntityData().get(DragonEntity.IS_FLYING);
        state.clientEndFlying = entity.getEntityData().get(DragonEntity.CLIENT_END_FLYING);
        state.hasSaddle = entity.getEntityData().get(DragonEntity.HAS_SADDLE);
        state.hasChest = entity.getEntityData().get(DragonEntity.HAS_CHEST);
        state.otherEyes = entity.getEntityData().get(DragonEntity.OTHER_EYES);
        state.tamed = entity.isTamed();
    }

    @Override
    protected float getShadowRadius(AdventureRenderState state) {
        return 0.6F * state.size;
    }

    @Override
    protected void scale(AdventureRenderState state, PoseStack poseStack) {
        float scale = state.size / 3.0F;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return state.variant == 1 ? RED_DRAGON_TEXTURE : TEXTURE;
    }
}
