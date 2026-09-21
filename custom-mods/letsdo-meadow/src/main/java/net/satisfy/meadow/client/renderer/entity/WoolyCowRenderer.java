package net.satisfy.meadow.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.satisfy.meadow.Meadow;
import net.satisfy.meadow.client.MeadowClient;
import net.satisfy.meadow.client.model.WoolyCowModel;
import net.satisfy.meadow.core.entity.WoolyCowEntity;
import org.jetbrains.annotations.NotNull;

public class WoolyCowRenderer extends MobRenderer<WoolyCowEntity, WoolyCowRenderState, WoolyCowModel> {

    public WoolyCowRenderer(EntityRendererProvider.Context context) {
        super(context, new WoolyCowModel(context.bakeLayer(MeadowClient.SHEARABLE_MEADOW_COW_MODEL_LAYER)), 0.7f);
    }

    @Override
    public WoolyCowRenderState createRenderState() {
        return new WoolyCowRenderState();
    }

    @Override
    public void extractRenderState(WoolyCowEntity entity, WoolyCowRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.sheared = entity.isSheared();
        state.variant = entity.getVariant().getSerializedName();
        state.neckAngle = entity.getNeckAngle(partialTick);
        state.headAngle = entity.getHeadAngle(partialTick);
    }

    @Override
    public @NotNull Identifier getTextureLocation(WoolyCowRenderState state) {
        return state.sheared
                ? Meadow.identifier(String.format("textures/entity/cow/%s_sheared.png", state.variant))
                : Meadow.identifier(String.format("textures/entity/cow/%s.png", state.variant));
    }
}
