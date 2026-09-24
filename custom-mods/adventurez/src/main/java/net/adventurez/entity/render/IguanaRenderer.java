package net.adventurez.entity.render;

import net.adventurez.entity.IguanaEntity;
import net.adventurez.entity.model.IguanaModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class IguanaRenderer extends MobRenderer<IguanaEntity, AdventureRenderState, IguanaModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/iguana.png");

    public IguanaRenderer(EntityRendererProvider.Context context) {
        super(context, new IguanaModel(context.bakeLayer(RenderInit.IGUANA_LAYER)), 0.7F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(IguanaEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
