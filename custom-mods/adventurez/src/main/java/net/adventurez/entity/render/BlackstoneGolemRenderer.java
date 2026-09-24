package net.adventurez.entity.render;

import net.adventurez.entity.BlackstoneGolemEntity;
import net.adventurez.entity.model.BlackstoneGolemModel;
import net.adventurez.entity.render.feature.BlackstoneGolemBlueLavaFeatureRenderer;
import net.adventurez.entity.render.feature.StoneGolemLavaFeatureRenderer;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class BlackstoneGolemRenderer extends MobRenderer<BlackstoneGolemEntity, AdventureRenderState, BlackstoneGolemModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/blackstone_golem.png");

    public BlackstoneGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new BlackstoneGolemModel(context.bakeLayer(RenderInit.BLACKSTONE_GOLEM_LAYER)), 1.7F);
        this.addLayer(new StoneGolemLavaFeatureRenderer(this));
        this.addLayer(new BlackstoneGolemBlueLavaFeatureRenderer(this));
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(BlackstoneGolemEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    protected void scale(AdventureRenderState state, PoseStack poseStack) {
        poseStack.scale(2.4F, 2.4F, 2.4F);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
