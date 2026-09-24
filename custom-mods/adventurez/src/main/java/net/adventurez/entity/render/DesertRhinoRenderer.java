package net.adventurez.entity.render;

import net.adventurez.entity.DesertRhinoEntity;
import net.adventurez.entity.model.DesertRhinoModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class DesertRhinoRenderer extends MobRenderer<DesertRhinoEntity, AdventureRenderState, DesertRhinoModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/desert_rhino.png");

    public DesertRhinoRenderer(EntityRendererProvider.Context context) {
        super(context, new DesertRhinoModel(context.bakeLayer(RenderInit.DESERT_RHINO_LAYER)), 1.5F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(DesertRhinoEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
