package net.satisfy.vinery.client.render.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.Identifier;
import net.satisfy.vinery.core.Vinery;

// 26.2: vanilla AbstractBoatRenderer owns the wobble/submit flow; we only
// supply the dark-cherry texture and baked hull model, same visuals as before.
public class DarkCherryBoatRenderer extends AbstractBoatRenderer {
    public static final ModelLayerLocation BOAT_LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Vinery.MOD_ID, "boat/dark_cherry"), "main");
    public static final ModelLayerLocation CHEST_BOAT_LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Vinery.MOD_ID, "chest_boat/dark_cherry"), "main");

    private final EntityModel<BoatRenderState> model;

    public DarkCherryBoatRenderer(EntityRendererProvider.Context context, boolean hasChest) {
        super(context, Identifier.fromNamespaceAndPath(Vinery.MOD_ID,
                hasChest ? "textures/entity/chest_boat/dark_cherry.png" : "textures/entity/boat/dark_cherry.png"));
        ModelPart baked = context.bakeLayer(hasChest ? CHEST_BOAT_LAYER : BOAT_LAYER);
        this.model = hasChest ? new BoatModel(baked) : new BoatModel(baked);
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.model;
    }
}
