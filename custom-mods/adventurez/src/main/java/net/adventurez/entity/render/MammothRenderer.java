package net.adventurez.entity.render;

import net.adventurez.entity.MammothEntity;
import net.adventurez.entity.model.MammothModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class MammothRenderer extends MobRenderer<MammothEntity, AdventureRenderState, MammothModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/mammoth.png");

    public MammothRenderer(EntityRendererProvider.Context context) {
        super(context, new MammothModel(context.bakeLayer(RenderInit.MAMMOTH_LAYER)), 1.2F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(MammothEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    protected void scale(AdventureRenderState state, PoseStack poseStack) {
        float scale = state.isBaby ? 1.15F : 2.1F;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
