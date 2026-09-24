package net.adventurez.entity.render;

import net.adventurez.entity.VoidShadeEntity;
import net.adventurez.entity.model.VoidShadeModel;
import net.adventurez.entity.render.feature.VoidShadeEyesFeatureRenderer;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class VoidShadeRenderer extends MobRenderer<VoidShadeEntity, AdventureRenderState, VoidShadeModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/void_shade.png");

    public VoidShadeRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidShadeModel(context.bakeLayer(RenderInit.VOID_SHADE_LAYER)), 0.5F);
        this.addLayer(new VoidShadeEyesFeatureRenderer(this));
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(VoidShadeEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
