package net.satisfy.meadow.client.model;

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
import net.satisfy.meadow.Meadow;

public class FurLeggingsModel extends Model<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Meadow.identifier("fur_leggings"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public FurLeggingsModel(ModelPart root) {
        super(root, id -> net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(id));
        this.root = root;
        this.body = root.getChild("body");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(3, 35).addBox(-4.0F, 9.0F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.325F)), PartPose.ZERO);

        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(29, 39).addBox(-0.8F, -3.0F, -3.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(16, 48).mirror().addBox(-2.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2F)).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
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

    public void copyBody(ModelPart bodyModel) {
        copyPart(body, bodyModel);
    }

    public void copyLegs(ModelPart rightLegModel, ModelPart leftLegModel) {
        copyPart(rightLeg, rightLegModel);
        copyPart(leftLeg, leftLegModel);
    }
}
