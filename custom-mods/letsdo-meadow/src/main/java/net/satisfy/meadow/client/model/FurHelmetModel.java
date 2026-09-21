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

public class FurHelmetModel extends Model<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Meadow.identifier("fur_hat"), "main");

    private final ModelPart root;
    private final ModelPart head;

    public FurHelmetModel(ModelPart root) {
        super(root, id -> net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(id));
        this.root = root;
        this.head = root.getChild("head");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -8.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)).texOffs(36, 0).addBox(-3.0F, -10.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(4, 22).addBox(6.0F, -11.0F, -6.0F, 0.0F, 3.0F, 12.0F, new CubeDeformation(0.0F)).texOffs(4, 22).addBox(-6.0F, -11.0F, -6.0F, 0.0F, 3.0F, 12.0F, new CubeDeformation(0.0F)).texOffs(28, 34).addBox(-3.0F, -6.0F, -6.0F, 6.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, 0.0F));

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

    public void copyHead(ModelPart model) {
        copyPart(head, model);
    }
}
