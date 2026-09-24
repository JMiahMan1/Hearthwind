package net.adventurez.entity.render;

import net.adventurez.entity.SoulReaperEntity;
import net.adventurez.entity.model.SoulReaperModel;
import net.adventurez.entity.render.feature.SoulReaperEyesFeatureRenderer;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class SoulReaperRenderer extends HumanoidMobRenderer<SoulReaperEntity, AdventureRenderState, SoulReaperModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/soul_reaper.png");

    public SoulReaperRenderer(EntityRendererProvider.Context context) {
        super(context, new SoulReaperModel(context.bakeLayer(RenderInit.SOUL_REAPER_LAYER)), 0.5F);
        this.addLayer(new SoulReaperEyesFeatureRenderer(this));
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(SoulReaperEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
        state.mainHandItem = entity.getMainHandItem().copy();
    }

    @Override
    protected void scale(AdventureRenderState state, PoseStack poseStack) {
        poseStack.scale(1.2F, 1.2F, 1.2F);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
