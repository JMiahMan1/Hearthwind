package net.satisfy.candlelight.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.satisfy.candlelight.Candlelight;

public class FlowerCrownModel extends Model<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Candlelight.identifier("flower_crown"), "main");

    private final ModelPart root;
    private final ModelPart flowerCrown;

    public FlowerCrownModel(ModelPart root) {
        super(root, id -> RenderTypes.entityCutout(id));
        this.root = root;
        this.flowerCrown = root.getChild("flower_crown");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild("flower_crown", CubeListBuilder.create()
                .texOffs(0, 5)
                .addBox(-4.0F, -8.75F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.2F)), PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    public ArmorModelWrapper asModel() {
        return new ArmorModelWrapper(this.root);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
    }

    protected static void copyPart(ModelPart dst, ModelPart src) {
        dst.x = src.x;
        dst.y = src.y;
        dst.z = src.z;
        dst.xRot = src.xRot;
        dst.yRot = src.yRot;
        dst.zRot = src.zRot;
    }

    public void copyHead(ModelPart headModel) {
        copyPart(flowerCrown, headModel);
    }
}