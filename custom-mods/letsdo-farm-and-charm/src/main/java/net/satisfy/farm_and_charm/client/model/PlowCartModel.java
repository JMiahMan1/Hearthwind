package net.satisfy.farm_and_charm.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.satisfy.farm_and_charm.FarmAndCharm;
import net.satisfy.farm_and_charm.client.renderer.entity.PlowCartRenderer;

public class PlowCartModel extends EntityModel<PlowCartRenderer.State> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(FarmAndCharm.identifier("plow"), "main");

    private final ModelPart cart;
    private final ModelPart right_wheel;
    private final ModelPart left_wheel;

    private final ModelPart plow_r1;
    private final ModelPart plow_r2;
    private final ModelPart plow_r3;
    private final ModelPart plow_r4;

    public PlowCartModel(ModelPart root) {
        super(root);
        this.cart = root.getChild("cart");
        this.right_wheel = root.getChild("right_wheel");
        this.left_wheel = root.getChild("left_wheel");

        this.plow_r1 = this.cart.getChild("plow_r1");
        this.plow_r2 = this.cart.getChild("plow_r2");
        this.plow_r3 = this.cart.getChild("plow_r3");
        this.plow_r4 = this.cart.getChild("plow_r4");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition cart = partdefinition.addOrReplaceChild("cart", CubeListBuilder.create()
                        .texOffs(153, 44).addBox(-11.0F, 0.5F, 4.9167F, 22.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(159, 8).addBox(-2.0F, 0.5F, -27.0833F, 4.0F, 4.0F, 32.0F, new CubeDeformation(0.0F))
                        .texOffs(88, 0).addBox(5.0F, 2.5F, 10.9167F, 3.0F, 3.0F, 18.0F, new CubeDeformation(0.0F))
                        .texOffs(88, 0).addBox(-8.0F, 2.5F, 10.9167F, 3.0F, 3.0F, 18.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 12.5F, 10.0833F));

        cart.addOrReplaceChild("plow_r1", CubeListBuilder.create()
                        .texOffs(173, 61).addBox(-12.0F, -4.0F, 4.0F, 16.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.4142F, 8.0F, -28.9706F, 0.0F, -0.7854F, 0.0F));

        cart.addOrReplaceChild("plow_r2", CubeListBuilder.create()
                        .texOffs(173, 61).addBox(-4.0F, -4.0F, 4.0F, 16.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.4142F, 8.0F, -28.9706F, 0.0F, 0.7854F, 0.0F));

        cart.addOrReplaceChild("plow_r3", CubeListBuilder.create()
                        .texOffs(173, 61).addBox(-12.0F, -4.0F, 4.0F, 16.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.4142F, 8.0F, -12.9706F, 0.0F, -0.7854F, 0.0F));

        cart.addOrReplaceChild("plow_r4", CubeListBuilder.create()
                        .texOffs(173, 61).addBox(-4.0F, -4.0F, 4.0F, 16.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.4142F, 8.0F, -12.9706F, 0.0F, 0.7854F, 0.0F));

        partdefinition.addOrReplaceChild("right_wheel", CubeListBuilder.create()
                        .texOffs(76, 43).addBox(-1.5F, -8.0F, -8.0F, 3.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(12.5F, 16.0F, 18.0F));

        partdefinition.addOrReplaceChild("left_wheel", CubeListBuilder.create()
                        .texOffs(76, 43).addBox(-1.5F, -8.0F, -8.0F, 3.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-12.5F, 16.0F, 18.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(PlowCartRenderer.State state) {
        this.right_wheel.xRot = state.wheelRotation;
        this.left_wheel.xRot = state.wheelRotation;

        this.plow_r1.visible = state.plowVisible;
        this.plow_r2.visible = state.plowVisible;
        this.plow_r3.visible = state.plowVisible;
        this.plow_r4.visible = state.plowVisible;

        this.cart.yRot = state.wobbleYaw;
        this.right_wheel.yRot = state.wobbleYaw;
        this.left_wheel.yRot = state.wobbleYaw;
    }
}