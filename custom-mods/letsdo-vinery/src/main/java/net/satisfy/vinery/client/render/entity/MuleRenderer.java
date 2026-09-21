package net.satisfy.vinery.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.resources.Identifier;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.entity.TraderMuleEntity;
import org.jetbrains.annotations.NotNull;

// 26.2: vanilla HorseModel owns the equine animation; custom mule texture kept.
@Environment(value = EnvType.CLIENT)
public class MuleRenderer extends MobRenderer<TraderMuleEntity, EquineRenderState, HorseModel> {
    private static final Identifier TEXTURE = Vinery.identifier("textures/entity/wandering_mule.png");

    public MuleRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseModel(context.bakeLayer(ModelLayers.HORSE)), 0.5f);
    }

    @Override
    public EquineRenderState createRenderState() {
        return new EquineRenderState();
    }

    @Override
    public @NotNull Identifier getTextureLocation(EquineRenderState state) {
        return TEXTURE;
    }
}
