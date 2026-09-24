package net.adventurez.entity.render;

import net.adventurez.entity.NightmareEntity;
import net.adventurez.entity.render.feature.NightmareEyesFeatureRenderer;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.equine.BabyHorseModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class NightmareRenderer extends AbstractHorseRenderer<NightmareEntity, EquineRenderState, HorseModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/nightmare.png");

    public NightmareRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseModel(context.bakeLayer(ModelLayers.HORSE)), new BabyHorseModel(context.bakeLayer(ModelLayers.HORSE_BABY)));
        this.addLayer(new NightmareEyesFeatureRenderer(this));
    }

    @Override
    public EquineRenderState createRenderState() {
        return new EquineRenderState();
    }

    @Override
    public Identifier getTextureLocation(EquineRenderState state) {
        return TEXTURE;
    }
}
