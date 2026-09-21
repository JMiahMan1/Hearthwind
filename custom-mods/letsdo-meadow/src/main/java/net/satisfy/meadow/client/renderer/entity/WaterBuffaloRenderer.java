package net.satisfy.meadow.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.satisfy.meadow.Meadow;
import net.satisfy.meadow.client.MeadowClient;
import net.satisfy.meadow.client.model.WaterBuffaloModel;
import net.satisfy.meadow.core.entity.WaterBuffaloEntity;
import org.jetbrains.annotations.NotNull;

public class WaterBuffaloRenderer extends MobRenderer<WaterBuffaloEntity, LivingEntityRenderState, WaterBuffaloModel> {

    private static final Identifier TEXTURE = Meadow.identifier("textures/entity/buffalo/water_buffalo.png");

    public WaterBuffaloRenderer(EntityRendererProvider.Context context) {
        super(context, new WaterBuffaloModel(context.bakeLayer(MeadowClient.WATER_BUFFALO_MODEL_LAYER)), 1.0f);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public @NotNull Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        if (state.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        super.scale(state, poseStack);
    }
}
