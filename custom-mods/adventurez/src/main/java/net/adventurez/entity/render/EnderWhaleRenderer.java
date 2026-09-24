package net.adventurez.entity.render;

import net.adventurez.entity.EnderWhaleEntity;
import net.adventurez.entity.model.EnderWhaleModel;
import net.adventurez.entity.render.feature.EnderWhaleGlowFeatureRenderer;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class EnderWhaleRenderer extends MobRenderer<EnderWhaleEntity, AdventureRenderState, EnderWhaleModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/ender_whale.png");

    public EnderWhaleRenderer(EntityRendererProvider.Context context) {
        super(context, new EnderWhaleModel(context.bakeLayer(RenderInit.ENDER_WHALE_LAYER)), 2.3F);
        this.addLayer(new EnderWhaleGlowFeatureRenderer(this));
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(EnderWhaleEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
