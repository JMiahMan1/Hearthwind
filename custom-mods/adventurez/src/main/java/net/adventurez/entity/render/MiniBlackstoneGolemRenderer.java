package net.adventurez.entity.render;

import net.adventurez.entity.MiniBlackstoneGolemEntity;
import net.adventurez.entity.model.MiniBlackstoneGolemModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class MiniBlackstoneGolemRenderer extends MobRenderer<MiniBlackstoneGolemEntity, AdventureRenderState, MiniBlackstoneGolemModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/mini_blackstone_golem.png");

    public MiniBlackstoneGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new MiniBlackstoneGolemModel(context.bakeLayer(RenderInit.MINI_BLACKSTONE_GOLEM_LAYER)), 0.7F);
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(MiniBlackstoneGolemEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
