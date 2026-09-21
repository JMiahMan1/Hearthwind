package net.satisfy.meadow.client.renderer.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.Identifier;
import net.satisfy.meadow.Meadow;

// 26.2: vanilla AbstractBoatRenderer owns the wobble/submit flow; we only
// supply the pine texture and baked hull model, same visuals as before.
public class PineBoatRenderer extends AbstractBoatRenderer {
    public static final ModelLayerLocation BOAT_LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Meadow.MOD_ID, "boat/pine"), "main");
    public static final ModelLayerLocation CHEST_BOAT_LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Meadow.MOD_ID, "chest_boat/pine"), "main");

    private final EntityModel<BoatRenderState> model;

    public PineBoatRenderer(EntityRendererProvider.Context context, boolean hasChest) {
        super(context, Identifier.fromNamespaceAndPath(Meadow.MOD_ID,
                hasChest ? "textures/entity/chest_boat/pine.png" : "textures/entity/boat/pine.png"));
        ModelPart baked = context.bakeLayer(hasChest ? CHEST_BOAT_LAYER : BOAT_LAYER);
        this.model = new BoatModel(baked);
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.model;
    }
}
