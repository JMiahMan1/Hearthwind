package net.adventurez.entity.render;

import net.adventurez.entity.PiglinBeastEntity;
import net.adventurez.entity.model.PiglinBeastModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class PiglinBeastRenderer extends MobRenderer<PiglinBeastEntity, AdventureRenderState, PiglinBeastModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/piglin_beast.png");

    public PiglinBeastRenderer(EntityRendererProvider.Context context) {
        super(context, new PiglinBeastModel(context.bakeLayer(RenderInit.PIGLIN_BEAST_LAYER)), 1.0F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(PiglinBeastEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    protected void scale(AdventureRenderState state, PoseStack poseStack) {
        poseStack.scale(1.1F, 1.1F, 1.1F);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
