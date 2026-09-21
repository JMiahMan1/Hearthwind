package net.satisfy.candlelight.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.satisfy.candlelight.Candlelight;

public class DinnerBellModel extends Model<BlockEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Candlelight.identifier("dinner_bell"), "main");
    private final ModelPart root;

    public DinnerBellModel(ModelPart root) {
        super(root, id -> RenderTypes.entityCutout(id));
        this.root = root;
    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition dinner_bell_base = partdefinition.addOrReplaceChild("dinner_bell_base", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(4.0F, -24.0F, 4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 10).mirror().addBox(5.0F, -22.0F, 5.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition dinner_bell_button = partdefinition.addOrReplaceChild("dinner_bell_button", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(7.0F, -19.0F, 7.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(BlockEntityRenderState state) {
    }
}
