package net.adventurez.entity.render;

import net.adventurez.entity.SkunkEntity;
import net.adventurez.entity.model.SkunkModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class SkunkRenderer extends MobRenderer<SkunkEntity, AdventureRenderState, SkunkModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/skunk.png");

    public SkunkRenderer(EntityRendererProvider.Context context) {
        super(context, new SkunkModel(context.bakeLayer(RenderInit.SKUNK_LAYER)), 0.4F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(SkunkEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
