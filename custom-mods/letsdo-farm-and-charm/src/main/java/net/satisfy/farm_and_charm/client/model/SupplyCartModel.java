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
import net.satisfy.farm_and_charm.client.renderer.entity.SupplyCartRenderer;

public class SupplyCartModel extends EntityModel<SupplyCartRenderer.State> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(FarmAndCharm.identifier("supply_cart"), "main");
    private final ModelPart cart;
    private final ModelPart right_wheel;
    private final ModelPart left_wheel;
    private final ModelPart chest;

    public SupplyCartModel(ModelPart root) {
        super(root);
        this.cart = root.getChild("cart");
        this.right_wheel = root.getChild("right_wheel");
        this.left_wheel = root.getChild("left_wheel");
        this.chest = root.getChild("chest");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("cart",
                CubeListBuilder.create()
                        .texOffs(38, 43).addBox(-12.0F, 4.5F, -16.0833F, 24.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(88, 0).addBox(5.0F, 1.5F, 9.9167F, 3.0F, 3.0F, 18.0F, new CubeDeformation(0.0F))
                        .texOffs(88, 0).addBox(-8.0F, 1.5F, 9.9167F, 3.0F, 3.0F, 18.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 43).addBox(-12.0F, -7.5F, -30.0833F, 3.0F, 9.0F, 32.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 43).mirror().addBox(9.0F, -7.5F, -30.0833F, 3.0F, 9.0F, 32.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(88, 21).addBox(-9.0F, -7.5F, -1.0833F, 18.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-12.0F, 1.5F, -30.0833F, 24.0F, 3.0F, 40.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 12.5F - 1.5F, 10.0833F)
        );

        partdefinition.addOrReplaceChild("right_wheel",
                CubeListBuilder.create()
                        .texOffs(76, 43).addBox(-2.0F, -8.0F, -8.0F, 3.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(14.0F, 16.0F - 1.5F, -5.0F)
        );

        partdefinition.addOrReplaceChild("left_wheel",
                CubeListBuilder.create()
                        .texOffs(76, 43).mirror().addBox(2.0F, -8.0F, -8.0F, 3.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(-17.0F, 16.0F - 1.5F, -5.0F)
        );

        PartDefinition chest = partdefinition.addOrReplaceChild("chest",
                CubeListBuilder.create()
                        .texOffs(0, 130).addBox(-7.0F, -22.0F, -6.0F, 14.0F, 10.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 26.0F - 1.5F, 0.0F)
        );

        chest.addOrReplaceChild("chest_lid",
                CubeListBuilder.create()
                        .texOffs(0, 111).addBox(-7.0F, -27.0F, -6.0F, 14.0F, 5.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 111).addBox(-1.0F, -24.0F, -7.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO
        );

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(SupplyCartRenderer.State state) {
        this.right_wheel.xRot = state.wheelRotation;
        this.left_wheel.xRot = state.wheelRotation;

        this.cart.yRot = state.wobbleYaw;
        this.chest.yRot = state.wobbleYaw;
        this.right_wheel.yRot = state.wobbleYaw;
        this.left_wheel.yRot = state.wobbleYaw;
    }
}