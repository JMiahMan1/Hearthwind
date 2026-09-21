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

public class DressChestplateModel extends Model<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Candlelight.identifier("dress"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public DressChestplateModel(ModelPart root) {
        super(root, id -> RenderTypes.entityCutout(id));
        this.root = root;
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(3, 36).addBox(-4.0F, 10.0F, -2.0F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.325F))
                        .texOffs(16, 16).addBox(-4.0F, -1.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.3F)),
                PartPose.ZERO
        );

        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(10, 32).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 12.0F, 6.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(32, 37).mirror().addBox(-2.5F, -1.0F, -2.5F, 5.0F, 12.0F, 6.0F, new CubeDeformation(0.25F)).mirror(false),
                PartPose.offset(1.9F, 12.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0F, -3.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.3F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).mirror().addBox(-1.0F, -3.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false),
                PartPose.offset(5.0F, 2.0F, 0.0F)
        );

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

    @SuppressWarnings("unused")
    public void copyBody(ModelPart baseBody, ModelPart leftArmModel, ModelPart rightArmModel, ModelPart leftLegModel, ModelPart rightLegModel) {
        copyPart(body, baseBody);
        copyPart(leftArm, leftArmModel);
        copyPart(rightArm, rightArmModel);
        copyPart(leftLeg, leftLegModel);
        copyPart(rightLeg, rightLegModel);
    }
}
