package net.adventurez.entity.model;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import com.google.common.collect.ImmutableList;
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

@Environment(EnvType.CLIENT)
public class TinyEyeModel extends EntityModel<AdventureRenderState> {
    private final ModelPart body;

    public TinyEyeModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
    }

    public static LayerDefinition getMeshDefinition() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(10, 10).addBox(-3.5F, -5.0F, -1.0F, 2.0F, 11.0F, 2.0F).texOffs(0, 0).addBox(-1.5F, -6.0F, -1.0F, 3.0F, 13.0F, 2.0F).texOffs(18, 18).addBox(-4.5F, -4.0F, -1.0F, 1.0F, 9.0F, 2.0F).texOffs(10, 0).addBox(-5.5F, -3.0F, -1.0F, 1.0F, 7.0F, 2.0F).texOffs(8, 23).addBox(-6.5F, -1.0F, -1.0F, 1.0F, 3.0F, 2.0F).texOffs(0, 15).addBox(1.5F, -5.0F, -1.0F, 2.0F, 11.0F, 2.0F).texOffs(16, 0).addBox(3.5F, -4.0F, -1.0F, 1.0F, 9.0F, 2.0F).texOffs(20, 9).addBox(4.5F, -3.0F, -1.0F, 1.0F, 7.0F, 2.0F).texOffs(22, 0).addBox(5.5F, -1.0F, -1.0F, 1.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        return LayerDefinition.create(modelData, 32, 32);
    }

    @Override
    public void setupAnim(AdventureRenderState bullet) {
        float animationProgress = bullet.animationProgress;
        float yHeadRot = bullet.yHeadRot;
        this.body.yRot = yHeadRot * 0.017453292F;
    }
}