package net.adventurez.entity.model;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import com.google.common.collect.ImmutableList;

import net.adventurez.entity.PiglinBeastEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.EntityModel;
import net.adventurez.entity.render.AdventureRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class PiglinBeastModel extends EntityModel<AdventureRenderState> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftHorn;
    private final ModelPart rightHorn;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart flagHolder;
    private final ModelPart flag;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public PiglinBeastModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightEar = this.head.getChild("rightEar");
        this.leftEar = this.head.getChild("leftEar");
        this.rightHorn = this.head.getChild("rightHorn");
        this.leftHorn = this.head.getChild("leftHorn");
        this.body = root.getChild("body");
        this.flagHolder = this.body.getChild("flagHolder");
        this.flag = this.flagHolder.getChild("flag");
        this.leftArm = root.getChild("leftArm");
        this.rightArm = root.getChild("rightArm");
        this.leftLeg = root.getChild("leftLeg");
        this.rightLeg = root.getChild("rightLeg");
    }

    public static LayerDefinition getMeshDefinition() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition modelPartData1 = modelPartData.addOrReplaceChild("head", CubeListBuilder.create().texOffs(74, 0).addBox(-5.0F, -11.0F, -5.0F, 10.0F, 9.0F, 10.0F).texOffs(57, 11).addBox(3.0F, -5.0F, -6.0F, 3.0F, 2.0F, 1.0F).texOffs(48, 56).addBox(-6.0F, -5.0F, -6.0F, 3.0F, 2.0F, 1.0F).texOffs(0, 38).addBox(-3.0F, -7.0F, -6.0F, 6.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, -16.0F, -1.0F));
        modelPartData1.addOrReplaceChild("leftHorn", CubeListBuilder.create().texOffs(51, 30).addBox(-4.7379F, 1.2403F, 0.62F, 6.0F, 2.0F, 1.0F), PartPose.offset(-3.0F, -6.0F, -7.0F));
        modelPartData1.addOrReplaceChild("rightHorn", CubeListBuilder.create().texOffs(0, 14).addBox(-1.8264F, 1.6629F, -0.3197F, 6.0F, 2.0F, 1.0F), PartPose.offset(3.0F, -7.0F, -6.0F));
        modelPartData1.addOrReplaceChild("hair",
                CubeListBuilder.create().texOffs(61, 32).addBox(-3.0F, -8.0F, 6.0F, 0.0F, 4.0F, 1.0F).texOffs(0, 51).addBox(-3.0F, -7.0F, 8.0F, 0.0F, 3.0F, 1.0F).texOffs(10, 51).addBox(-3.0F, -9.0F, 4.0F, 0.0F, 5.0F, 1.0F).texOffs(50, 46).addBox(-3.0F, -6.0F, 10.0F, 0.0F, 2.0F, 1.0F).texOffs(48, 48).addBox(-3.0F, 1.0F, 11.0F, 0.0F, 1.0F, 3.0F).texOffs(8, 45).addBox(-3.0F, -1.0F, 11.0F, 0.0F, 1.0F, 2.0F).texOffs(13, 12).addBox(-3.0F, -3.0F, 11.0F, 0.0F, 1.0F, 2.0F).texOffs(0, 49).addBox(-3.0F, -4.0F, 11.0F, 0.0F, 1.0F, 1.0F).texOffs(8, 41).addBox(-3.0F, -2.0F, 11.0F, 0.0F, 1.0F, 3.0F).texOffs(0, 40).addBox(-3.0F, 0.0F, 11.0F, 0.0F, 1.0F, 4.0F).texOffs(0, 39).addBox(-3.0F, 2.0F, 11.0F, 0.0F, 1.0F, 4.0F).texOffs(8, 40).addBox(-3.0F, 3.0F, 11.0F, 0.0F, 1.0F, 3.0F).texOffs(13, 37).addBox(-3.0F, 4.0F, 11.0F, 0.0F, 1.0F, 1.0F).texOffs(61, 1).addBox(-3.0F, -8.0F, 2.0F, 0.0F, 4.0F, 1.0F).texOffs(48, 46).addBox(-3.0F, -6.0F, 3.0F, 0.0F, 2.0F, 1.0F).texOffs(0, 29).addBox(-3.0F, -7.0F, 5.0F, 0.0F, 3.0F, 1.0F).texOffs(14, 14).addBox(-3.0F, -6.0F, 7.0F, 0.0F, 2.0F, 1.0F).texOffs(2, 49).addBox(-3.0F, -5.0F, 9.0F, 0.0F, 1.0F, 1.0F),
                PartPose.offset(3.0F, -7.0F, -6.0F));
        modelPartData1.addOrReplaceChild("leftEar", CubeListBuilder.create().texOffs(57, 0).addBox(-5.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F), PartPose.offset(-5.0F, -8.0F, 0.0F));
        modelPartData1.addOrReplaceChild("rightEar", CubeListBuilder.create().texOffs(48, 49).addBox(-5.0F, -0.0603F, -3.0F, 6.0F, 1.0F, 6.0F), PartPose.offset(5.0F, -8.0F, 0.0F));
        PartDefinition modelPartData2 = modelPartData.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(47, 56).addBox(-8.0F, 12.0F, -8.0F, 18.0F, 3.0F, 13.0F).texOffs(0, 30).addBox(-8.0F, 10.0F, -9.0F, 18.0F, 2.0F, 15.0F).texOffs(0, 0).addBox(-9.0F, -3.0F, -10.0F, 20.0F, 13.0F, 17.0F).texOffs(74, 19).addBox(-9.0F, 10.0F, -10.0F, 20.0F, 10.0F, 0.0F).texOffs(72, 72).addBox(-9.0F, 10.0F, 7.0F, 20.0F, 10.0F, 0.0F)
                        .texOffs(29, 55).addBox(11.0F, 10.0F, -10.0F, 0.0F, 10.0F, 17.0F).texOffs(29, 55).addBox(-9.0F, 10.0F, -10.0F, 0.0F, 10.0F, 17.0F).texOffs(52, 33).addBox(-8.0F, -5.0F, -7.0F, 18.0F, 2.0F, 14.0F).texOffs(0, 47).addBox(-8.0F, -15.0F, -5.0F, 18.0F, 10.0F, 12.0F),
                PartPose.offset(-1.0F, -3.0F, 1.0F));
        PartDefinition modelPartData3 = modelPartData2.addOrReplaceChild("flagHolder", CubeListBuilder.create().texOffs(76, 95).addBox(-2.0F, -37.9319F, 0.4824F, 2.0F, 29.0F, 2.0F).texOffs(46, 72).addBox(-3.0F, -42.0F, -1.0F, 4.0F, 5.0F, 18.0F).texOffs(0, 0).addBox(-3.0F, -8.9319F, -0.5176F, 4.0F, 10.0F, 4.0F), PartPose.offset(4.0F, 8.0F, 6.0F));
        modelPartData3.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 79).addBox(0.0F, -0.6136F, -6.8755F, 0.0F, 24.0F, 14.0F), PartPose.offset(-1.0F, -38.0F, 9.0F));
        modelPartData.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(28, 93).addBox(-3.3939F, -1.3963F, -3.0F, 5.0F, 24.0F, 6.0F), PartPose.offset(-9.0F, -15.0F, 2.0F));
        modelPartData.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(84, 89).addBox(-0.899F, -1.1716F, -3.0F, 5.0F, 25.0F, 6.0F).texOffs(101, 71).addBox(0.8422F, 18.7943F, -6.0F, 2.0F, 3.0F, 11.0F)
                .texOffs(51, 39).addBox(-0.1578F, 18.7943F, -8.0F, 3.0F, 4.0F, 2.0F).texOffs(0, 30).addBox(-0.1578F, 17.7943F, -11.0F, 4.0F, 5.0F, 3.0F).texOffs(0, 69).addBox(-1.1578F, 16.7943F, -28.0F, 6.0F, 7.0F, 17.0F).texOffs(51, 33).addBox(-0.1578F, 17.7943F, -29.0F, 4.0F, 5.0F, 1.0F).texOffs(64, 13).addBox(1.3951F, 15.9987F, -14.0F, 3.0F, 1.0F, 1.0F)
                .texOffs(57, 7).addBox(0.3951F, 15.9987F, -21.0F, 1.0F, 1.0F, 3.0F).texOffs(12, 0).addBox(1.361F, 14.2576F, -27.0F, 1.0F, 3.0F, 1.0F).texOffs(0, 0).addBox(3.5517F, 13.8093F, -24.0F, 1.0F, 3.0F, 1.0F).texOffs(11, 30).addBox(2.5858F, 14.5505F, -19.0F, 1.0F, 2.0F, 1.0F).texOffs(57, 0).addBox(4.9319F, 17.5176F, -24.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(48, 52).addBox(4.6731F, 18.4836F, -16.0F, 2.0F, 1.0F, 1.0F).texOffs(0, 56).addBox(4.3801F, 19.7083F, -25.0F, 4.0F, 1.0F, 1.0F).texOffs(64, 10).addBox(4.0F, 21.6401F, -24.0F, 3.0F, 1.0F, 1.0F).texOffs(0, 52).addBox(3.5F, 21.6401F, -17.0F, 2.0F, 1.0F, 3.0F).texOffs(57, 2).addBox(3.3449F, 23.572F, -26.0F, 1.0F, 2.0F, 1.0F).texOffs(56, 56).addBox(0.4471F, 23.7955F, -24.0F, 1.0F, 2.0F, 1.0F).texOffs(0, 47).addBox(1.413F, 23.7F, -19.0F, 1.0F, 2.0F, 1.0F).texOffs(8, 48).addBox(2.3789F, 23.3131F, -16.0F, 1.0F, 2.0F, 1.0F).texOffs(62, 7).addBox(-2.9331F, 21.0872F, -27.0F, 3.0F, 1.0F, 1.0F).texOffs(57, 14).addBox(-3.3813F, 18.8965F, -23.0F, 3.0F, 1.0F, 1.0F).texOffs(48, 49).addBox(-2.1225F, 17.9306F, -16.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(0, 47).addBox(-2.3813F, 19.8965F, -18.0F, 2.0F, 1.0F, 4.0F), PartPose.offset(8.0F, -15.0F, 2.0F));
        modelPartData.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(96, 49).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 13.0F, 7.0F), PartPose.offset(-3.9F, 11.0F, 0.0F));
        modelPartData.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(50, 95).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 13.0F, 7.0F), PartPose.offset(3.9F, 11.0F, 0.0F));
        return LayerDefinition.create(modelData, 128, 128);
    }

    @Override
    public void setupAnim(AdventureRenderState entity) {
        float limbAngle = entity.limbAngle;
        float limbDistance = entity.limbDistance;
        float animationProgress = entity.animationProgress;
        float yHeadRot = entity.yHeadRot;
        float headPitch = entity.headPitch;
        this.head.yRot = yHeadRot * 0.0079453292F;
        this.head.xRot = headPitch * 0.0027453292F;
        this.leftHorn.yRot = -0.3491F;
        this.leftHorn.zRot = 1.1345F;
        this.rightHorn.yRot = 0.3491F;
        this.rightHorn.zRot = -1.1345F;
        this.leftEar.zRot = -1.1345F;
        this.rightEar.zRot = -1.9199F;
        this.leftArm.zRot = 0.2618F;
        this.rightArm.zRot = -0.2618F;
        this.flagHolder.xRot = -0.2618F;
        this.flag.xRot = 0.1745F;
        // Animation
        // Legs
        this.rightLeg.xRot = Mth.cos(limbAngle * 0.6662F + 3.1415927F) * 1.4F * limbDistance;
        this.leftLeg.xRot = Mth.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
        // Flag
        flag.zRot = Mth.cos(animationProgress / 10) / 4;
        // Arms
        this.rightArm.xRot = Mth.cos(limbAngle * 0.6662F + 3.1415927F) * 2.0F * limbDistance * 0.3F;
        this.leftArm.xRot = -Mth.cos(limbAngle * 0.6662F + 3.1415927F) * 2.0F * limbDistance * 0.3F;

        float attackTick = entity.getEntityData().get(PiglinBeastEntity.ATTACK_TICK_VISUAL);
        if (attackTick > 0F) {
            float g = attackTick;
            float h = Mth.sin(g * 3.1415927F);
            float i = Mth.sin(attackTick * 3.1415927F) * -(this.head.xRot - 0.7F) * 0.75F;
            rightArm.xRot = (float) ((double) rightArm.xRot - ((double) h * 1.2D + (double) i));
            rightArm.zRot += Mth.sin(attackTick * 3.1415927F) * -0.4F - limbDistance;
        }

        float handUp = entity.getEntityData().get(PiglinBeastEntity.LEAD_ARM);
        if (handUp > 0.0F) {
            float g = handUp;
            float h = Mth.sin(g * 3.1415927F);
            float i = Mth.sin(handUp * 3.1415927F) * -(this.head.xRot - 0.7F) * 0.75F;
            leftArm.xRot = (float) ((double) leftArm.xRot - ((double) h * 1.2D + (double) i));
        }
        // Ears
        rightEar.zRot = rightEar.zRot + ((Mth.cos((limbAngle * 0.6662F + 3.1415927F) / 3) * limbDistance) / 2);
        leftEar.zRot = leftEar.zRot - ((Mth.cos((limbAngle * 0.6662F + 3.1415927F) / 3) * limbDistance) / 2);
    }

}