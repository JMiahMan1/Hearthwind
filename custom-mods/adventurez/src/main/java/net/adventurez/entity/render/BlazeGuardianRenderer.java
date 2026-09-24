package net.adventurez.entity.render;

import net.adventurez.entity.BlazeGuardianEntity;
import net.adventurez.entity.model.BlazeGuardianModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class BlazeGuardianRenderer extends MobRenderer<BlazeGuardianEntity, AdventureRenderState, BlazeGuardianModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/blaze_guardian.png");

    public BlazeGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new BlazeGuardianModel(context.bakeLayer(RenderInit.BLAZE_GUARDIAN_LAYER)), 0.7F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(BlazeGuardianEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
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
