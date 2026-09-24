package net.adventurez.entity.render;

import net.adventurez.entity.RedFungusEntity;
import net.adventurez.entity.model.RedFungusModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class RedFungusRenderer extends MobRenderer<RedFungusEntity, AdventureRenderState, RedFungusModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/red_fungus.png");

    public RedFungusRenderer(EntityRendererProvider.Context context) {
        super(context, new RedFungusModel(context.bakeLayer(RenderInit.RED_FUNGUS_LAYER)), 0.3F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(RedFungusEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
