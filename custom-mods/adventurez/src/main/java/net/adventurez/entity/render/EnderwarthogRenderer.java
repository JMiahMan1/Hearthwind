package net.adventurez.entity.render;

import net.adventurez.entity.EnderwarthogEntity;
import net.adventurez.entity.model.EnderwarthogModel;
import net.adventurez.entity.render.feature.EnderwarthogEyesFeatureRenderer;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class EnderwarthogRenderer extends MobRenderer<EnderwarthogEntity, AdventureRenderState, EnderwarthogModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/enderwarthog.png");

    public EnderwarthogRenderer(EntityRendererProvider.Context context) {
        super(context, new EnderwarthogModel(context.bakeLayer(RenderInit.ENDERWARTHOG_LAYER)), 1.5F);
        this.addLayer(new EnderwarthogEyesFeatureRenderer(this));
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(EnderwarthogEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
