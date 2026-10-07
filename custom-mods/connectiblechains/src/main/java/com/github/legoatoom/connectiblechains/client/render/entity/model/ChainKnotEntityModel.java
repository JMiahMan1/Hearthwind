package com.github.legoatoom.connectiblechains.client.render.entity.model;

import com.github.legoatoom.connectiblechains.client.render.entity.state.ChainKnotEntityRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

@Environment(EnvType.CLIENT)
public class ChainKnotEntityModel extends net.minecraft.client.model.EntityModel<ChainKnotEntityRenderState> {
   private static final String KNOT = "knot";

   public ChainKnotEntityModel(ModelPart root) {
      super(root);
   }

   public static LayerDefinition getTexturedModelData() {
      MeshDefinition modelData = new MeshDefinition();
      PartDefinition modelPartData = modelData.getRoot();
      PartDefinition knot = modelPartData.addOrReplaceChild("knot", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 0.0F, -3.1416F));
      PartDefinition south_r1 = knot.addOrReplaceChild(
         "south_r1",
         CubeListBuilder.create()
            .texOffs(-4, 3)
            .mirror()
            .addBox(-2.5F, 0.0F, -3.0F, 4.0F, 0.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(0.0F, -0.5F, 3.0F, 1.5708F, 0.0F, -1.5708F)
      );
      PartDefinition north_r1 = knot.addOrReplaceChild(
         "north_r1",
         CubeListBuilder.create().texOffs(-4, 3).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -0.0F, -3.0F, 1.5708F, 0.0F, 1.5708F)
      );
      PartDefinition east_r1 = knot.addOrReplaceChild(
         "east_r1",
         CubeListBuilder.create().texOffs(-4, 8).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-3.0F, -0.0F, 0.0F, -3.1416F, 0.0F, 1.5708F)
      );
      PartDefinition west_r1 = knot.addOrReplaceChild(
         "west_r1",
         CubeListBuilder.create()
            .texOffs(-4, 8)
            .mirror()
            .addBox(-2.5F, 0.0F, -3.0F, 4.0F, 0.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(3.0F, -0.5F, 0.0F, 0.0F, 0.0F, -1.5708F)
      );
      return LayerDefinition.create(modelData, 16, 16);
   }
}
