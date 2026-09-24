package net.adventurez.entity.render;

import net.adventurez.entity.AmethystGolemEntity;
import net.adventurez.entity.model.AmethystGolemModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class AmethystGolemRenderer extends MobRenderer<AmethystGolemEntity, AdventureRenderState, AmethystGolemModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/amethyst_golem.png");
    private static final Identifier OTHER_TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/deepslate_amethyst_golem.png");

    public AmethystGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new AmethystGolemModel(context.bakeLayer(RenderInit.AMETHYST_GOLEM_LAYER)), 0.7F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(AmethystGolemEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
        state.deepslateVariant = entity.getEntityData().get(AmethystGolemEntity.DEEPSLATE_VARIANT);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return state.deepslateVariant ? OTHER_TEXTURE : TEXTURE;
    }
}
