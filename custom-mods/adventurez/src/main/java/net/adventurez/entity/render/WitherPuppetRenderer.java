package net.adventurez.entity.render;

import net.adventurez.entity.WitherPuppetEntity;
import net.adventurez.entity.model.WitherPuppetModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class WitherPuppetRenderer extends HumanoidMobRenderer<WitherPuppetEntity, AdventureRenderState, WitherPuppetModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/wither_puppet.png");

    public WitherPuppetRenderer(EntityRendererProvider.Context context) {
        super(context, new WitherPuppetModel(context.bakeLayer(RenderInit.WITHER_PUPPET_LAYER)), 0.5F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(WitherPuppetEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
