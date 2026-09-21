package net.satisfy.brewery.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.satisfy.brewery.Brewery;
import net.satisfy.brewery.client.model.BeerElementalModel;
import net.satisfy.brewery.core.entity.BeerElementalEntity;
import org.jetbrains.annotations.NotNull;


public class
BeerElementalRenderer extends MobRenderer<BeerElementalEntity, BeerElementalModel.State, BeerElementalModel> {

    private static final Identifier TEXTURE = Brewery.identifier("textures/entity/beer_elemental.png");

    public BeerElementalRenderer(EntityRendererProvider.Context context) {
        super(context, new BeerElementalModel(context.bakeLayer(BeerElementalModel.BEER_ELEMENTAL_MODEL_LAYER)), 0.7f);
    }

    @Override
    public BeerElementalModel.State createRenderState() {
        return new BeerElementalModel.State();
    }

    @Override
    public void extractRenderState(BeerElementalEntity entity, BeerElementalModel.State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.headYaw = entity.getYHeadRot() - entity.getYRot();
        state.headPitch = entity.getXRot();
    }

    @Override
    public @NotNull Identifier getTextureLocation(BeerElementalModel.State state) {
        return TEXTURE;
    }
}
