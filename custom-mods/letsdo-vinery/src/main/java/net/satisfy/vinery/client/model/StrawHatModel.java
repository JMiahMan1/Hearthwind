package net.satisfy.vinery.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;


import net.satisfy.vinery.core.Vinery;

public class StrawHatModel extends Model<net.minecraft.client.renderer.entity.state.HumanoidRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Vinery.identifier("straw_hat"), "main");
    private final ModelPart root;
    private final ModelPart top_part;

    public StrawHatModel(ModelPart root) {
        super(root, id -> net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(id));
        this.root = root;
        this.top_part = root.getChild("top_part");
    }
    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition top_part = partdefinition.addOrReplaceChild("top_part", CubeListBuilder.create()
                .texOffs(-17, 13).addBox(-8.5F, -6.0F, -8.5F, 17.0F, 0.0F, 17.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.5F, -10.0F, -4.5F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    public ArmorModelWrapper asModel() {
        return new ArmorModelWrapper(this.root);
    }

    @Override
    public void setupAnim(net.minecraft.client.renderer.entity.state.HumanoidRenderState state) {
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
        copyPart(top_part, model);
    }
}
