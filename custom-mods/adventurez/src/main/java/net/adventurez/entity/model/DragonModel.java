package net.adventurez.entity.model;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import com.google.common.collect.ImmutableList;

import net.adventurez.entity.DragonEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.EntityModel;
import net.adventurez.entity.render.AdventureRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class DragonModel extends EntityModel<AdventureRenderState> {
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart spike;
    private final ModelPart wingLeft;
    private final ModelPart wingTipLeft;
    private final ModelPart wingRight;
    private final ModelPart wingTipRight;
    private final ModelPart rearLegRight;
    private final ModelPart rearLegRightTip;
    private final ModelPart rearLegRightTip_r1;
    private final ModelPart rearFootRight;
    private final ModelPart rearLegLeft;
    private final ModelPart rearLegLefttip;
    private final ModelPart rearLegLefttip_r1;
    private final ModelPart rearFootLeft;
    private final ModelPart frontLegRight;
    private final ModelPart frontLegRight_r1;
    private final ModelPart frontLegRighttip;
    private final ModelPart frontLegRighttip_r1;
    private final ModelPart frontFootRight;
    private final ModelPart frontLegLeft;
    private final ModelPart frontLegLeft_r1;
    private final ModelPart frontLegLefttip;
    private final ModelPart frontLegLefttip_r1;
    private final ModelPart frontFootLeft;
    private final ModelPart neck;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart neck4;
    private final ModelPart head;
    private final ModelPart otherEars;
    private final ModelPart otherEars_r1;
    private final ModelPart otherEars_r2;
    private final ModelPart ears;
    private final ModelPart jaw;
    private final ModelPart tail;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart tail6;
    private final ModelPart otherTail;
    private final ModelPart otherTail_r1;
    private final ModelPart otherTail_r2;
    private final ModelPart otherTail_r3;
    private final ModelPart otherTail_r4;
    private final ModelPart extraTail;
    private final ModelPart tail7;

    private float betweenFloater;
    private float startFlyingTicker;
    private boolean endFlying;
    private int endFlyingTicker;
    private boolean randomYawFire;
    private int randomYawFireTick;

    public DragonModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.spike = this.body.getChild("spike");
        this.chest = this.body.getChild("chest");
        this.wingLeft = root.getChild("wingLeft");
        this.wingTipLeft = this.wingLeft.getChild("wingTipLeft");
        this.wingRight = root.getChild("wingRight");
        this.wingTipRight = this.wingRight.getChild("wingTipRight");
        this.rearLegRight = root.getChild("rearLegRight");
        this.rearLegRightTip = this.rearLegRight.getChild("rearLegRightTip");
        this.rearFootRight = this.rearLegRightTip.getChild("rearFootRight");
        this.rearLegRightTip_r1 = this.rearLegRightTip.getChild("rearLegRightTip_r1");
        this.rearLegLeft = root.getChild("rearLegLeft");
        this.rearLegLefttip = this.rearLegLeft.getChild("rearLegLefttip");
        this.rearFootLeft = this.rearLegLefttip.getChild("rearFootLeft");
        this.rearLegLefttip_r1 = this.rearLegLefttip.getChild("rearLegLefttip_r1");
        this.frontLegRight = root.getChild("frontLegRight");
        this.frontLegRighttip = this.frontLegRight.getChild("frontLegRighttip");
        this.frontFootRight = this.frontLegRighttip.getChild("frontFootRight");
        this.frontLegRighttip_r1 = this.frontLegRighttip.getChild("frontLegRighttip_r1");
        this.frontLegRight_r1 = this.frontLegRight.getChild("frontLegRight_r1");
        this.frontLegLeft = root.getChild("frontLegLeft");
        this.frontLegLefttip = this.frontLegLeft.getChild("frontLegLefttip");
        this.frontFootLeft = this.frontLegLefttip.getChild("frontFootLeft");
        this.frontLegLefttip_r1 = this.frontLegLefttip.getChild("frontLegLefttip_r1");
        this.frontLegLeft_r1 = this.frontLegLeft.getChild("frontLegLeft_r1");
        this.neck = root.getChild("neck");
        this.neck2 = this.neck.getChild("neck2");
        this.neck3 = this.neck2.getChild("neck3");
        this.neck4 = this.neck3.getChild("neck4");
        this.head = this.neck4.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.ears = this.head.getChild("ears");
        this.otherEars = this.head.getChild("otherEars");
        this.otherEars_r2 = this.otherEars.getChild("otherEars_r2");
        this.otherEars_r1 = this.otherEars.getChild("otherEars_r1");
        this.tail = root.getChild("tail");
        this.tail2 = this.tail.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.tail4 = this.tail3.getChild("tail4");
        this.tail5 = this.tail4.getChild("tail5");
        this.tail6 = this.tail5.getChild("tail6");
        this.tail7 = this.tail6.getChild("tail7");
        this.extraTail = this.tail6.getChild("extraTail");
        this.otherTail = this.tail6.getChild("otherTail");
        this.otherTail_r4 = this.otherTail.getChild("otherTail_r4");
        this.otherTail_r3 = this.otherTail.getChild("otherTail_r3");
        this.otherTail_r2 = this.otherTail.getChild("otherTail_r2");
        this.otherTail_r1 = this.otherTail.getChild("otherTail_r1");
    }

    public static LayerDefinition getMeshDefinition() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition modelPartData1 = modelPartData.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 168).addBox(-12.0F, -11.0F, -32.0F, 24.0F, 24.0F, 64.0F).texOffs(116, 256).addBox(-1.0F, -17.0F, -26.0F, 2.0F, 6.0F, 12.0F).texOffs(60, 256).addBox(-1.0F, -17.0F, 14.0F, 2.0F, 6.0F, 12.0F), PartPose.offset(0.0F, -8.0F, -2.0F));
        modelPartData1.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(252, 196).addBox(-15.0F, -11.0F, 0.0F, 3.0F, 12.0F, 14.0F).texOffs(247, 86).addBox(12.0F, -11.0F, 0.0F, 3.0F, 12.0F, 14.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        modelPartData1.addOrReplaceChild("spike", CubeListBuilder.create().texOffs(88, 256).addBox(-1.0F, -6.0F, -5.0F, 2.0F, 6.0F, 12.0F), PartPose.offset(0.0F, -11.0F, -1.0F));
        PartDefinition modelPartData2 = modelPartData.addOrReplaceChild("wingLeft",
                CubeListBuilder.create().texOffs(168, 0).addBox(-56.0F, -4.0F, -4.0F, 56.0F, 8.0F, 8.0F).texOffs(112, 112).addBox(-56.0F, 0.0F, 2.0F, 56.0F, 0.0F, 56.0F),
                PartPose.offset(-12.0F, -18.0F, -24.0F));
        modelPartData2.addOrReplaceChild("wingTipLeft", CubeListBuilder.create().texOffs(168, 24).addBox(-56.0F, -3.0F, -2.0F, 56.0F, 4.0F, 4.0F).texOffs(0, 112).addBox(-56.0F, 0.0F, 2.0F, 56.0F, 0.0F, 56.0F),
                PartPose.offset(-56.0F, 0.0F, 0.0F));
        PartDefinition modelPartData3 = modelPartData.addOrReplaceChild("wingRight",
                CubeListBuilder.create().texOffs(112, 168).addBox(0.0F, -4.0F, -4.0F, 56.0F, 8.0F, 8.0F).texOffs(0, 56).addBox(0.0F, 0.0F, 2.0F, 56.0F, 0.0F, 56.0F),
                PartPose.offset(12.0F, -18.0F, -24.0F));
        modelPartData3.addOrReplaceChild("wingTipRight", CubeListBuilder.create().texOffs(168, 16).addBox(0.0F, -3.0F, -2.0F, 56.0F, 4.0F, 4.0F).texOffs(0, 0).addBox(0.0F, 0.0F, 2.0F, 56.0F, 0.0F, 56.0F),
                PartPose.offset(56.0F, 0.0F, 0.0F));
        PartDefinition modelPartData4 = modelPartData.addOrReplaceChild("rearLegRight", CubeListBuilder.create().texOffs(0, 40).addBox(-8.0F, -4.2322F, -6.0798F, 12.0F, 28.0F, 12.0F),
                PartPose.offset(16.0F, -5.0F, 22.0F));
        PartDefinition modelPartData5 = modelPartData4.addOrReplaceChild("rearLegRightTip", CubeListBuilder.create(), PartPose.offset(-1.0F, 20.327F, 1.5816F));
        modelPartData5.addOrReplaceChild("rearLegRightTip_r1", CubeListBuilder.create().texOffs(176, 184).addBox(-6.0F, -7.5F, -7.0F, 10.0F, 32.0F, 11.0F), PartPose.offset(0.0F, 1.1105F, -0.517F));
        modelPartData5.addOrReplaceChild("rearFootRight", CubeListBuilder.create().texOffs(168, 66).addBox(-5.0F, 0.0F, -15.0F, 12.0F, 6.0F, 19.0F), PartPose.offset(-2.0F, 16.0502F, 14.6409F));
        PartDefinition modelPartData6 = modelPartData.addOrReplaceChild("rearLegLeft", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -5.1472F, -5.6383F, 12.0F, 28.0F, 12.0F),
                PartPose.offset(-16.0F, -5.0F, 21.0F));
        PartDefinition modelPartData7 = modelPartData6.addOrReplaceChild("rearLegLefttip", CubeListBuilder.create(), PartPose.offset(2.0F, 19.9876F, 2.1028F));
        modelPartData7.addOrReplaceChild("rearLegLefttip_r1", CubeListBuilder.create().texOffs(0, 80).addBox(-4.0F, -6.6164F, -6.486F, 10.0F, 32.0F, 11.0F), PartPose.offset(-1.0F, 0.6549F, -0.4294F));
        modelPartData7.addOrReplaceChild("rearFootLeft", CubeListBuilder.create().texOffs(0, 182).addBox(-6.0F, 0.3F, -14.7F, 12.0F, 6.0F, 19.0F), PartPose.offset(0.0F, 15.6549F, 15.0706F));
        PartDefinition modelPartData8 = modelPartData.addOrReplaceChild("frontLegRight", CubeListBuilder.create(), PartPose.offset(12.0F, -3.0F, -25.0F));
        modelPartData8.addOrReplaceChild("frontLegRight_r1", CubeListBuilder.create().texOffs(252, 32).addBox(-4.0F, -38.2535F, -12.1057F, 8.0F, 24.0F, 8.0F), PartPose.offset(0.0F, 30.8475F, 16.5052F));
        PartDefinition modelPartData9 = modelPartData8.addOrReplaceChild("frontLegRighttip", CubeListBuilder.create(), PartPose.offset(0.0F, 16.207F, -0.1549F));
        modelPartData9.addOrReplaceChild("frontLegRighttip_r1", CubeListBuilder.create().texOffs(0, 256).addBox(-2.9F, 3.0F, -39.0F, 7.0F, 17.0F, 8.0F), PartPose.offset(-1.0F, -16.207F, 32.1549F));
        modelPartData9.addOrReplaceChild("frontFootRight",
                CubeListBuilder.create().texOffs(256, 256).addBox(-4.8F, 0.0F, -15.1665F, 7.0F, 5.0F, 8.0F).texOffs(240, 168).addBox(-4.8F, -1.0F, -7.1665F, 7.0F, 6.0F, 8.0F),
                PartPose.offset(1.0F, 10.793F, 3.1549F));
        PartDefinition modelPartData10 = modelPartData.addOrReplaceChild("frontLegLeft", CubeListBuilder.create(), PartPose.offset(-12.0F, -3.0F, -25.0F));
        modelPartData10.addOrReplaceChild("frontLegLeft_r1", CubeListBuilder.create().texOffs(224, 238).addBox(-4.0F, -38.2535F, -12.1057F, 8.0F, 24.0F, 8.0F), PartPose.offset(0.0F, 30.8475F, 16.5052F));
        PartDefinition modelPartData11 = modelPartData10.addOrReplaceChild("frontLegLefttip", CubeListBuilder.create(), PartPose.offset(0.0F, 15.4023F, 0.2817F));
        modelPartData11.addOrReplaceChild("frontLegLefttip_r1", CubeListBuilder.create().texOffs(30, 256).addBox(-4.9F, 4.0F, -37.0F, 7.0F, 17.0F, 8.0F), PartPose.offset(1.0F, -16.4023F, 30.7183F));
        modelPartData11.addOrReplaceChild("frontFootLeft",
                CubeListBuilder.create().texOffs(256, 243).addBox(-3.8F, -0.3F, -14.4825F, 7.0F, 5.0F, 8.0F).texOffs(144, 256).addBox(-3.8F, -1.3F, -6.4825F, 7.0F, 6.0F, 8.0F),
                PartPose.offset(0.0F, 12.1611F, 3.9136F));
        PartDefinition modelPartData12 = modelPartData.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(218, 184).addBox(-7.0F, -8.0F, -10.0F, 14.0F, 16.0F, 10.0F).texOffs(168, 66).addBox(-1.0F, -12.0F, -8.0F, 2.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, -10.0F, -34.0F));
        PartDefinition modelPartData13 = modelPartData12.addOrReplaceChild("neck2",
                CubeListBuilder.create().texOffs(0, 207).addBox(-7.0F, -7.0F, -10.0F, 14.0F, 15.0F, 10.0F).texOffs(168, 32).addBox(-1.0F, -11.0F, -8.0F, 2.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, -2.0F, -10.0F));
        PartDefinition modelPartData14 = modelPartData13.addOrReplaceChild("neck3",
                CubeListBuilder.create().texOffs(112, 207).addBox(-7.0F, -7.0F, -10.0F, 14.0F, 14.0F, 10.0F).texOffs(39, 117).addBox(-1.0F, -11.0F, -8.0F, 2.0F, 7.0F, 6.0F),
                PartPose.offset(0.0F, -1.0F, -10.0F));
        PartDefinition modelPartData15 = modelPartData14.addOrReplaceChild("neck4",
                CubeListBuilder.create().texOffs(176, 238).addBox(-7.0F, -6.0F, -10.0F, 14.0F, 12.0F, 10.0F).texOffs(0, 182).addBox(-1.0F, -10.0F, -8.0F, 2.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, -2.0F, -10.0F));
        PartDefinition modelPartData16 = modelPartData15.addOrReplaceChild("head", CubeListBuilder.create().texOffs(112, 184).addBox(-8.0F, -3.0F, -30.0F, 16.0F, 7.0F, 16.0F).texOffs(168, 32).addBox(-10.0F, -10.0F, -16.0F, 20.0F, 18.0F, 16.0F).texOffs(39, 130).addBox(2.0F, -5.0F, -28.0F, 3.0F, 4.0F, 4.0F).texOffs(0, 123).addBox(-5.0F, -5.0F, -28.0F, 3.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, -10.0F));
        PartDefinition modelPartData17 = modelPartData16.addOrReplaceChild("otherEars", CubeListBuilder.create(), PartPose.offset(0.0F, -9.0F, -7.0F));
        modelPartData17.addOrReplaceChild("otherEars_r1", CubeListBuilder.create().texOffs(42, 80).addBox(-2.0F, -13.0F, -1.0F, 4.0F, 15.0F, 3.0F), PartPose.offset(5.0F, 0.0F, 0.0F));
        modelPartData17.addOrReplaceChild("otherEars_r2", CubeListBuilder.create().texOffs(42, 98).addBox(-2.0F, -13.0F, -1.0F, 4.0F, 15.0F, 3.0F), PartPose.offset(-5.0F, 0.0F, 0.0F));
        modelPartData16.addOrReplaceChild("ears", CubeListBuilder.create().texOffs(194, 260).addBox(8.0F, -5.0F, -4.0F, 4.0F, 7.0F, 6.0F).texOffs(174, 260).addBox(-2.0F, -5.0F, -4.0F, 4.0F, 7.0F, 6.0F),
                PartPose.offset(-5.0F, -10.0F, -6.0F));
        modelPartData16.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(168, 91).addBox(-6.0F, -2.0F, -14.0F, 12.0F, 4.0F, 14.0F), PartPose.offset(0.0F, 6.0F, -16.0F));
        PartDefinition modelPartData18 = modelPartData.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 153).addBox(-7.0F, -7.0F, -1.0F, 14.0F, 14.0F, 15.0F).texOffs(160, 219).addBox(-1.0F, -12.0F, 3.0F, 2.0F, 5.0F, 8.0F),
                PartPose.offset(0.0F, -9.0F, 30.0F));
        PartDefinition modelPartData19 = modelPartData18.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(0, 123).addBox(-6.0F, -6.0F, -1.0F, 12.0F, 12.0F, 15.0F).texOffs(36, 40).addBox(-1.0F, -10.0F, 3.0F, 2.0F, 4.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 14.0F));
        PartDefinition modelPartData20 = modelPartData19.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(203, 212).addBox(-5.0F, -6.0F, -1.0F, 10.0F, 11.0F, 15.0F).texOffs(36, 0).addBox(-1.0F, -10.0F, 2.0F, 2.0F, 4.0F, 8.0F),
                PartPose.offset(0.0F, 1.0F, 14.0F));
        PartDefinition modelPartData21 = modelPartData20.addOrReplaceChild("tail4",
                CubeListBuilder.create().texOffs(215, 76).addBox(-2.0F, -5.0F, -1.0F, 8.0F, 9.0F, 15.0F).texOffs(238, 214).addBox(1.0F, -8.0F, 3.0F, 2.0F, 3.0F, 8.0F),
                PartPose.offset(-2.0F, 0.0F, 14.0F));
        PartDefinition modelPartData22 = modelPartData21.addOrReplaceChild("tail5",
                CubeListBuilder.create().texOffs(225, 51).addBox(-3.0F, -4.0F, -1.0F, 6.0F, 8.0F, 15.0F).texOffs(176, 227).addBox(-1.0F, -7.0F, 3.0F, 2.0F, 3.0F, 8.0F),
                PartPose.offset(2.0F, 0.0F, 14.0F));
        PartDefinition modelPartData23 = modelPartData22.addOrReplaceChild("tail6",
                CubeListBuilder.create().texOffs(248, 223).addBox(0.0F, -2.0F, -1.0F, 4.0F, 5.0F, 15.0F).texOffs(160, 184).addBox(1.0F, -5.0F, 3.0F, 2.0F, 3.0F, 8.0F),
                PartPose.offset(-2.0F, 0.0F, 14.0F));
        PartDefinition modelPartData24 = modelPartData23.addOrReplaceChild("otherTail", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 8.0F));
        modelPartData24.addOrReplaceChild("otherTail_r1", CubeListBuilder.create().texOffs(38, 207).addBox(-12.0F, -1.0F, -1.0F, 11.0F, 2.0F, 2.0F), PartPose.offset(2.0F, 0.0F, 0.0F));
        modelPartData24.addOrReplaceChild("otherTail_r2", CubeListBuilder.create().texOffs(206, 100).addBox(1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F), PartPose.offset(2.0F, 0.0F, 0.0F));
        modelPartData24.addOrReplaceChild("otherTail_r3", CubeListBuilder.create().texOffs(150, 207).addBox(-12.0F, -1.0F, -1.0F, 11.0F, 2.0F, 2.0F), PartPose.offset(2.0F, 0.0F, -6.0F));
        modelPartData24.addOrReplaceChild("otherTail_r4", CubeListBuilder.create().texOffs(218, 107).addBox(1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F), PartPose.offset(2.0F, 0.0F, -6.0F));
        modelPartData23.addOrReplaceChild("extraTail", CubeListBuilder.create().texOffs(210, 32).addBox(4.0F, 0.0F, -7.0F, 11.0F, 0.0F, 14.0F).texOffs(29, 182).addBox(-10.0F, 0.0F, -7.0F, 10.0F, 0.0F, 14.0F),
                PartPose.offset(0.0F, 0.0F, 7.0F));
        modelPartData23.addOrReplaceChild("tail7", CubeListBuilder.create().texOffs(252, 64).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 3.0F, 15.0F).texOffs(0, 0).addBox(0.0F, -3.0F, 5.0F, 0.0F, 2.0F, 4.0F),
                PartPose.offset(2.0F, 0.0F, 14.0F));
        return LayerDefinition.create(modelData, 512, 512);
    }

    @Override
    public void setupAnim(AdventureRenderState entity) {
        float limbAngle = entity.limbAngle;
        float limbDistance = entity.limbDistance;
        float animationProgress = entity.animationProgress;
        float yHeadRot = entity.yHeadRot;
        float headPitch = entity.headPitch;
        float f = entity.f;
        float g = entity.g;
        float h = entity.h;
        float i = entity.i;
        float j = entity.j;
        // Extras
        this.renderExtras(entity);
        // General Body
        this.otherEars.xRot = -1.0036F;
        this.otherEars_r1.xRot = 0.1745F;
        this.otherEars_r1.zRot = 0.2182F;
        this.otherEars_r2.xRot = 0.1745F;
        this.otherEars_r2.zRot = -0.2182F;
        this.otherTail_r1.yRot = 0.7418F;
        this.otherTail_r2.yRot = -0.7418F;
        this.otherTail_r3.yRot = 0.6109F;
        this.otherTail_r4.yRot = -0.7418F;

        this.rearLegRight.xRot = -0.9599F;
        this.rearLegRightTip.xRot = 1.309F;
        this.rearLegRightTip_r1.xRot = 0.9599F;
        this.rearFootRight.xRot = -0.3491F;
        this.rearLegLeft.xRot = -0.9599F;
        this.rearLegLefttip.xRot = 1.309F;
        this.rearLegLefttip_r1.xRot = 0.9599F;
        this.rearFootLeft.xRot = -0.3491F;
        this.frontLegRight.xRot = 0.3491F;
        this.frontLegRight_r1.xRot = 0.2618F;
        this.frontLegRighttip.xRot = -1.5708F;
        this.frontLegRighttip_r1.xRot = 0.2618F;
        this.frontFootRight.xRot = 1.2217F;
        this.frontLegLeft.xRot = 0.3491F;
        this.frontLegLeft_r1.xRot = 0.2618F;
        this.frontLegLefttip.xRot = -1.5708F;
        this.frontLegLefttip_r1.xRot = 0.2618F;
        this.frontFootLeft.xRot = 1.2217F;

        // Watching Animation
        // Head glitches while riding
        this.head.yRot = yHeadRot * (0.017453292F / 6.0F); // 0.0049453292F
        this.head.xRot = headPitch * 0.0037453292F;
        this.neck.yRot = yHeadRot * (0.017453292F / 5.0F);
        this.neck2.yRot = yHeadRot * (0.017453292F / 5.0F);
        this.neck3.yRot = yHeadRot * (0.017453292F / 4.0F);
        this.neck4.yRot = yHeadRot * (0.017453292F / 4.0F);

        this.neck.xRot = headPitch * 0.0017453292F;
        this.neck2.xRot = headPitch * 0.0017453292F;
        this.neck3.xRot = headPitch * 0.0017453292F;
        this.neck4.xRot = headPitch * 0.0017453292F;

        // Fire Breath
        Boolean isFireBreathing = entity.getEntityData().get(DragonEntity.FIRE_BREATH);
        if (isFireBreathing) {
            this.jaw.xRot = 0.4F;
        }

        float slowlyIncreasingFloat = ((float) Math.floorMod(entity.level().getGameTime(), 100L) + animationProgress) / 100.0F;

        // Between Animation
        if (entity.getEntityData().get(DragonEntity.CLIENT_START_FLYING)) {
            if (entity.getEntityData().get(DragonEntity.IS_START_FLYING)) {
                startFlyingTicker = Mth.clamp(startFlyingTicker + 0.05164F, 0.0F, 1.4981F);
            } else {
                startFlyingTicker = Mth.clamp(startFlyingTicker - 0.05164F, 0.0F, 1.4981F);
            }
            this.wingRight.zRot = 0.6981F - startFlyingTicker;
            this.wingLeft.zRot = -0.6981F + startFlyingTicker;
            this.wingTipLeft.zRot = 2.618F - (startFlyingTicker * 1.32033909618F);
            this.wingTipRight.zRot = -2.618F + (startFlyingTicker * 1.32033909618F);
            betweenFloater = 12.566370614F * slowlyIncreasingFloat;
            if (entity.onGround()) {
                float walkFloat = 32.0F;
                this.rearLegRight.xRot = -0.9599F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;
                this.rearLegLeft.xRot = -0.9599F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;

                this.rearFootRight.xRot = -0.3491F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.9F;
                this.rearFootLeft.xRot = -0.3491F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.9F;

                this.frontLegRight.xRot = 0.3491F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;
                this.frontLegLeft.xRot = 0.3491F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;

                this.frontLegRighttip.xRot = -1.5708F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.8F;
                this.frontLegLefttip.xRot = -1.5708F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.8F;

                this.frontFootRight.xRot = 1.2217F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.2F;
                this.frontFootLeft.xRot = 1.2217F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.2F;
            }
        } else

        // While Flying Animation
        if (entity.getEntityData().get(DragonEntity.IS_FLYING)) {
            startFlyingTicker = 0.0F;
            // mediumSpeedSin goes from -1 to 1
            float mediumSpeedSin = Mth.cos(12.566370614F * slowlyIncreasingFloat - (betweenFloater + 3.1415926535897F));
            // Wings
            this.wingRight.zRot = mediumSpeedSin * 0.8F; // double pi inside cos does it make faster
            this.wingTipRight.zRot = this.wingRight.zRot * 0.8F;
            this.wingLeft.zRot = -this.wingRight.zRot;
            this.wingTipLeft.zRot = -this.wingTipRight.zRot;

            // Body Floating
            float bodyFloating = -mediumSpeedSin - 4.0F;
            this.body.y = bodyFloating;
            this.wingLeft.y = bodyFloating - 10.0F;
            this.wingRight.y = bodyFloating - 10.0F; // Dont know why it has to be -10F
            this.rearLegLeft.y = bodyFloating;
            this.rearLegRight.y = bodyFloating;
            this.frontLegLeft.y = bodyFloating;
            this.frontLegRight.y = bodyFloating;
            this.neck.y = bodyFloating;
            this.tail.y = bodyFloating;
            // head floating
            float headFloat = Mth.cos(bodyFloating / 2.0F);
            this.neck2.y = -headFloat;
            this.neck3.y = -headFloat;
            this.neck4.y = -headFloat * 0.9F;
            this.head.y = -headFloat * 0.8F;
            // yaw
            if (!isFireBreathing) {
                this.jaw.xRot = -headFloat * 0.3F;
            }
            // tail
            this.tail2.y = Mth.cos(6.2831855F * slowlyIncreasingFloat - (6.2831855F / 3F));
            this.tail3.y = Mth.cos(6.2831855F * slowlyIncreasingFloat - (6.2831855F / 3F));
            this.tail4.y = Mth.cos(6.2831855F * slowlyIncreasingFloat - (6.2831855F / 4F));
            this.tail5.y = Mth.cos(6.2831855F * slowlyIncreasingFloat - (6.2831855F / 4F));
            this.tail5.y = Mth.cos(6.2831855F * slowlyIncreasingFloat - (6.2831855F / 5F));
            this.tail6.y = Mth.cos(6.2831855F * slowlyIncreasingFloat - (6.2831855F / 5F));
            this.tail7.y = Mth.cos(6.2831855F * slowlyIncreasingFloat - (6.2831855F / 6F)) * 0.5F;
            if (entity.onGround()) {
                float walkFloat = 32.0F;
                this.rearLegRight.xRot = -0.9599F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;
                this.rearLegLeft.xRot = -0.9599F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;

                this.rearFootRight.xRot = -0.3491F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.9F;
                this.rearFootLeft.xRot = -0.3491F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.9F;

                this.frontLegRight.xRot = 0.3491F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;
                this.frontLegLeft.xRot = 0.3491F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;

                this.frontLegRighttip.xRot = -1.5708F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.8F;
                this.frontLegLefttip.xRot = -1.5708F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.8F;

                this.frontFootRight.xRot = 1.2217F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.2F;
                this.frontFootLeft.xRot = 1.2217F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.2F;
            } else {
                // Legs rear
                this.rearLegRight.xRot = 0.5672F;
                this.rearLegRightTip.xRot = -0.2182F;
                this.rearLegRightTip_r1.xRot = 0.9599F;
                this.rearFootRight.xRot = 1.4835F;

                this.rearLegLeft.xRot = 0.5672F;
                this.rearLegLefttip.xRot = -0.2182F;
                this.rearLegLefttip_r1.xRot = 0.9599F;
                this.rearFootLeft.xRot = 1.4835F;

                // Legs front
                this.frontLegRight.xRot = 0.7418F;
                this.frontLegRight_r1.xRot = 0.2618F;
                this.frontLegRighttip.xRot = -1.0472F;
                this.frontLegRighttip_r1.xRot = 0.2618F;
                this.frontFootRight.xRot = 1.4835F;

                this.frontLegLeft.xRot = 0.7418F;
                this.frontLegLeft_r1.xRot = 0.2618F;
                this.frontLegLefttip.xRot = -1.0472F;
                this.frontLegLefttip_r1.xRot = 0.2618F;
                this.frontFootLeft.xRot = 1.4835F;
            }
        } else

        // End of Flying Animation
        if (entity.getEntityData().get(DragonEntity.CLIENT_END_FLYING)) {
            float mediumSpeedSin = Mth.cos(12.566370614F * slowlyIncreasingFloat - (betweenFloater + 3.1415926535897F)); // 1to-1
            if (mediumSpeedSin < 0.04F && mediumSpeedSin > -0.04F) {
                endFlying = true;
            }
            if (endFlying == false) {
                this.wingRight.zRot = mediumSpeedSin * 0.8F;
                this.wingTipRight.zRot = this.wingRight.zRot * 0.8F;
                this.wingLeft.zRot = -this.wingRight.zRot;
                this.wingTipLeft.zRot = -this.wingTipRight.zRot;
            } else {
                endFlyingTicker++;
                // Wings
                if (endFlyingTicker <= 10) {
                    startFlyingTicker = Mth.clamp(startFlyingTicker + 0.06205F, 0.0F, 0.6981F);
                    // a*b=c
                    this.wingRight.zRot = startFlyingTicker;
                    this.wingLeft.zRot = -startFlyingTicker;
                    this.wingTipLeft.zRot = (startFlyingTicker * 3.75017905F);
                    this.wingTipRight.zRot = -(startFlyingTicker * 3.75017905F);

                } else {
                    entity.getEntityData().set(DragonEntity.CLIENT_END_FLYING, false);
                    endFlyingTicker = 0;
                    startFlyingTicker = 0;
                    endFlying = false;
                }

            }

            if (entity.onGround()) {
                float walkFloat = 32.0F;
                this.rearLegRight.xRot = -0.9599F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;
                this.rearLegLeft.xRot = -0.9599F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;

                this.rearFootRight.xRot = -0.3491F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.9F;
                this.rearFootLeft.xRot = -0.3491F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.9F;

                this.frontLegRight.xRot = 0.3491F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;
                this.frontLegLeft.xRot = 0.3491F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;

                this.frontLegRighttip.xRot = -1.5708F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.8F;
                this.frontLegLefttip.xRot = -1.5708F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.8F;

                this.frontFootRight.xRot = 1.2217F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.2F;
                this.frontFootLeft.xRot = 1.2217F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.2F;
            }

        } else

        // Walk Animation
        if (!entity.isInSittingPose() || !entity.getPassengers().isEmpty()) {
            float walkFloat = 32.0F;
            this.rearLegRight.xRot = -0.9599F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;
            this.rearLegLeft.xRot = -0.9599F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;

            this.rearFootRight.xRot = -0.3491F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.9F;
            this.rearFootLeft.xRot = -0.3491F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.9F;

            this.frontLegRight.xRot = 0.3491F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;
            this.frontLegLeft.xRot = 0.3491F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F;

            this.frontLegRighttip.xRot = -1.5708F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.8F;
            this.frontLegLefttip.xRot = -1.5708F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.8F;

            this.frontFootRight.xRot = 1.2217F + 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.2F;
            this.frontFootLeft.xRot = 1.2217F - 0.3F * Mth.degreesDifference(limbAngle, walkFloat) * limbDistance * 1.3F * 0.2F;

            // tail
            this.tail.xRot = -0.1309F;
            this.tail2.xRot = -0.1309F;
            this.tail3.xRot = -0.1309F;
            this.tail4.xRot = -0.1745F;
            this.tail5.xRot = 0.1309F;
            this.tail6.xRot = 0.1745F;
            this.tail7.xRot = 0.2618F;

            this.tail.yRot = 0.0436F * Mth.degreesDifference(limbAngle, 60.0F) * limbDistance;
            this.tail2.yRot = 0.0436F * Mth.degreesDifference(limbAngle, 60.0F) * limbDistance;
            this.tail3.yRot = 0.0436F * Mth.degreesDifference(limbAngle, 60.0F) * limbDistance;
            this.tail4.yRot = 0.0436F * Mth.degreesDifference(limbAngle, 60.0F) * limbDistance;
            this.tail5.yRot = 0.0436F * Mth.degreesDifference(limbAngle, 70.0F) * limbDistance;
            this.tail6.yRot = 0.0436F * Mth.degreesDifference(limbAngle, 70.0F) * limbDistance;
            this.tail7.yRot = 0.0436F * Mth.degreesDifference(limbAngle, 80.0F) * limbDistance;

            this.tail2.y = 0.0F;
            this.tail3.y = 0.0F;
            this.tail4.y = 0.0F;
            this.tail5.y = 0.0F;
            this.tail5.y = 0.0F;
            this.tail6.y = 0.0F;
            this.tail7.y = 0.0F;

            // wings
            this.wingLeft.zRot = -0.6981F + 0.0236F * Mth.degreesDifference(limbAngle, 40.0F) * limbDistance;
            this.wingTipLeft.zRot = 2.618F;
            this.wingRight.zRot = 0.6981F - 0.0236F * Mth.degreesDifference(limbAngle, 40.0F) * limbDistance;
            this.wingTipRight.zRot = -2.618F;

            // Jaw
            if (!isFireBreathing) {
                this.jaw.xRot = 0.0F;
            }
        } else// if (entity.isInSittingPose())

        // Sitting Position Animation
        {
            this.tail.xRot = -0.1309F;
            this.tail2.xRot = -0.1309F;
            this.tail3.xRot = -0.1309F;
            this.tail4.xRot = -0.1745F;
            this.tail5.xRot = 0.1309F;
            this.tail6.xRot = 0.1745F;
            this.tail7.xRot = 0.1745F;

            this.tail.yRot = -0.0436F;
            this.tail2.yRot = -0.0436F;
            this.tail3.yRot = -0.0873F;
            this.tail4.yRot = -0.0436F;
            this.tail5.yRot = -0.2182F;
            this.tail6.yRot = -0.2182F;
            this.tail6.zRot = -0.0436F;
            this.tail7.zRot = -0.1309F;

            // Wings
            this.wingLeft.zRot = -0.6981F;
            this.wingTipLeft.zRot = 2.618F;
            this.wingRight.zRot = 0.6981F;
            this.wingTipRight.zRot = -2.618F;

            // Random Yaw Fire
            Float yawPitch = Math.abs(Mth.cos(6.2831853071F * slowlyIncreasingFloat) * 0.3F);
            if (entity.getSize() == 3 && !this.randomYawFire && yawPitch <= 0.01F && entity.level().getRandom().nextInt(16) == 0 && !isFireBreathing) {
                this.randomYawFire = true;
            }
            if (this.randomYawFire) {
                this.randomYawFireTick++;
                Float yawler = this.head.yRot + this.neck.yRot + this.neck2.yRot + this.neck3.yRot + this.neck4.yRot;
                Float pitcher = this.head.xRot + this.neck.xRot + this.neck2.xRot + this.neck3.xRot + this.neck4.xRot;
                yawler = (yawler / (float) (Math.PI)) * 1.5F;
                pitcher = pitcher * 3F;
                entity.level().addParticle(ParticleTypes.FLAME, true, false, entity.getX() + Math.sin((entity.yBodyRot / 360F) * 2F * Math.PI + (Math.PI) + yawler) * 6D, entity.getY() - pitcher + 1.95D,
                        entity.getZ() - Math.cos((entity.yBodyRot / 360F) * 2F * Math.PI + (Math.PI) + yawler) * 6D, 0.0D, 0.0D, 0.0D);

                this.jaw.xRot = yawPitch;
                if ((this.randomYawFireTick > 10 && yawPitch <= 0.01F) || isFireBreathing) {
                    this.randomYawFireTick = 0;
                    this.randomYawFire = false;
                }
            }

        }

    }

    private void renderExtras(AdventureRenderState entity) {
        if (entity.getEntityData().get(DragonEntity.HAS_SADDLE)) {
            this.spike.visible = false;
        } else {
            this.spike.visible = true;
        }
        if (entity.getEntityData().get(DragonEntity.HAS_CHEST)) {
            this.chest.visible = true;
        } else {
            this.chest.visible = false;
        }
        if (entity.getEntityData().get(DragonEntity.OTHER_EARS)) {
            this.otherEars.visible = true;
            this.ears.visible = false;
        } else {
            this.otherEars.visible = false;
            this.ears.visible = true;
        }
        if (entity.getEntityData().get(DragonEntity.OTHER_TAIL)) {
            this.otherTail.visible = true;
            this.extraTail.visible = false;
        } else {
            this.otherTail.visible = false;
            this.extraTail.visible = true;
        }

    }

}
