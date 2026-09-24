package net.adventurez.entity.model;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import com.google.common.collect.ImmutableList;

import net.adventurez.entity.TheEyeEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.EntityModel;
import net.adventurez.entity.render.AdventureRenderState;

@Environment(EnvType.CLIENT)
public class TheEyeModel extends EntityModel<AdventureRenderState> {
    private final ModelPart middle;
    private final ModelPart leftSite;
    private final ModelPart rightSite;

    public TheEyeModel(ModelPart root) {
        super(root);
        this.middle = root.getChild("middle");
        this.leftSite = root.getChild("leftSite");
        this.rightSite = root.getChild("rightSite");
    }

    public static LayerDefinition getMeshDefinition() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("middle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -6.0F, -1.0F, 3.0F, 13.0F, 2.0F), PartPose.offset(0.0F, 15.0F, 0.0F));
        modelPartData.addOrReplaceChild("leftSite", CubeListBuilder.create().texOffs(10, 10).addBox(-3.5F, -5.0F, -1.0F, 2.0F, 11.0F, 2.0F).texOffs(18, 18).addBox(-4.5F, -4.0F, -1.0F, 1.0F, 9.0F, 2.0F).texOffs(10, 0).addBox(-5.5F, -3.0F, -1.0F, 1.0F, 7.0F, 2.0F).texOffs(8, 23).addBox(-6.5F, -1.0F, -1.0F, 1.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 15.0F, 0.0F));
        modelPartData.addOrReplaceChild("rightSite", CubeListBuilder.create().texOffs(0, 15).addBox(1.5F, -5.0F, -1.0F, 2.0F, 11.0F, 2.0F).texOffs(16, 0).addBox(3.5F, -4.0F, -1.0F, 1.0F, 9.0F, 2.0F).texOffs(20, 9).addBox(4.5F, -3.0F, -1.0F, 1.0F, 7.0F, 2.0F).texOffs(22, 0).addBox(5.5F, -1.0F, -1.0F, 1.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 15.0F, 0.0F));
        return LayerDefinition.create(modelData, 32, 32);
    }


    @Override
    public void setupAnim(AdventureRenderState eye) {
        float animationProgress = eye.animationProgress;
        float limbAngle = eye.limbAngle;
        float limbDistance = eye.limbDistance;
        float headPitch = eye.headPitch;
        this.middle.xRot = headPitch * 0.017453292F;
        this.leftSite.xRot = headPitch * 0.017453292F;
        this.rightSite.xRot = headPitch * 0.017453292F;
        // Split

        if (eye.getHealth() < (eye.getMaxHealth() / 3.0F) && !(eye.getEntityData().get(TheEyeEntity.INVUL_TIMER) > 0)) {
            this.leftSite.x = -1.0F;
            this.rightSite.x = 1.0F;
        } else {
            this.leftSite.x = 0F;
            this.rightSite.x = 0F;
        }

    }

}
