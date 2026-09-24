package net.adventurez.entity.render;

import net.adventurez.entity.SummonerEntity;
import net.adventurez.entity.model.SummonerModel;
import net.adventurez.entity.render.feature.SummonerEntityShieldFeatureRenderer;
import net.adventurez.entity.render.feature.SummonerEyesFeatureRenderer;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class SummonerRenderer extends MobRenderer<SummonerEntity, AdventureRenderState, SummonerModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/summoner.png");

    public SummonerRenderer(EntityRendererProvider.Context context) {
        super(context, new SummonerModel(context.bakeLayer(RenderInit.SUMMONER_LAYER)), 0.7F);
        this.addLayer(new SummonerEntityShieldFeatureRenderer(this));
        this.addLayer(new SummonerEyesFeatureRenderer(this));
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(SummonerEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
        state.spellcasting = entity.isSpellcasting();
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
