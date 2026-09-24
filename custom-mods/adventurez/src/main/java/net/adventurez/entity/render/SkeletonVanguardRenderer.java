package net.adventurez.entity.render;

import net.adventurez.entity.SkeletonVanguardEntity;
import net.adventurez.entity.model.SkeletonVanguardModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class SkeletonVanguardRenderer extends MobRenderer<SkeletonVanguardEntity, AdventureRenderState, SkeletonVanguardModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/skeleton_vanguard.png");

    public SkeletonVanguardRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonVanguardModel(context.bakeLayer(RenderInit.SKELETON_VANGUARD_LAYER)), 0.5F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(SkeletonVanguardEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
