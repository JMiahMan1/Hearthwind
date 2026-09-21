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

public class CookingBootsModel extends Model<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Candlelight.identifier("cooking_boots"), "main");

    private final ModelPart root;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public CookingBootsModel(ModelPart root) {
        super(root, id -> RenderTypes.entityCutout(id));
        this.root = root;
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 6.75F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.325F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 48).mirror().addBox(-2.0F, 6.75F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.325F)).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
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

    public void copyLegs(ModelPart rightLegModel, ModelPart leftLegModel) {
        copyPart(rightLeg, rightLegModel);
        copyPart(leftLeg, leftLegModel);
    }
}
