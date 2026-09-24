package net.adventurez.entity.model;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.HumanoidModel;
import net.adventurez.entity.render.AdventureRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

@Environment(EnvType.CLIENT)
public class WitherPuppetModel extends HumanoidModel<AdventureRenderState> {

    public WitherPuppetModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition getMeshDefinition() {
        MeshDefinition modelData = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -3.0F, -2.0F, 8.0F, 7.0F, 4.0F), PartPose.offset(0.0F, 14.0F, 0.0F));
        modelPartData.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 10.0F, 0.0F));
        modelPartData.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 27).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offset(5.0F, 12.0F, 0.0F));
        modelPartData.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(24, 24).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offset(-5.0F, 12.0F, 0.0F));
        modelPartData.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(2.0F, 17.0F, 0.0F));
        modelPartData.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(-2.0F, 17.0F, 0.0F));
        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public void setupAnim(AdventureRenderState puppet) {
        float animationProgress = puppet.animationProgress;
        super.setupAnim(puppet);
        float k = Mth.sin(puppet.walkAnimationSpeed * 3.1415927F);
        float f = puppet.limbAngle;
        float g = puppet.limbDistance;
        this.rightArm.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 2.0F * g * 0.5F;
        this.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        if (k > 0) {
            this.rightArm.yRot = -(0.1F - k * 0.6F);
            this.rightArm.xRot = -k * 1.5F;
        }
        this.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
        this.leftLeg.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
    }

}
