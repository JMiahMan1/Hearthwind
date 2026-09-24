package net.adventurez.entity.render;

import net.adventurez.entity.BrownFungusEntity;
import net.adventurez.entity.model.BrownFungusModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class BrownFungusRenderer extends MobRenderer<BrownFungusEntity, AdventureRenderState, BrownFungusModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/brown_fungus.png");

    public BrownFungusRenderer(EntityRendererProvider.Context context) {
        super(context, new BrownFungusModel(context.bakeLayer(RenderInit.BROWN_FUNGUS_LAYER)), 0.4F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(BrownFungusEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    protected void scale(AdventureRenderState state, PoseStack poseStack) {
        poseStack.scale(1.3F, 1.3F, 1.3F);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
