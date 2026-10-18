package net.jmb19905.niftycarts.client.renderer.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.client.renderer.entity.SeedDrillRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.banner.BannerModel;

@Environment(EnvType.CLIENT)
public final class SeedDrillModel extends CartModel<SeedDrillRenderState> {
   public SeedDrillModel(ModelPart root, BannerModel bannerModel, BannerFlagModel flagModel) {
      super(root, bannerModel, flagModel);
   }

   public static LayerDefinition createLayer() {
      MeshDefinition def = CartModel.createDefinition(9.0F, 30.0F);
      EasyMeshBuilder axis = new EasyMeshBuilder("axis", 0, 0);
      axis.addBox(-15.0F, -1.0F, -1.0F, 30.0F, 2.0F, 2.0F);
      EasyMeshBuilder shaft = new EasyMeshBuilder("shaft", 0, 8);
      shaft.zRot = -0.07F;
      shaft.addBox(0.0F, 0.0F, -8.0F, 20.0F, 2.0F, 1.0F);
      shaft.addBox(0.0F, 0.0F, 7.0F, 20.0F, 2.0F, 1.0F);
      EasyMeshBuilder shaftConnector = new EasyMeshBuilder("shaftConnector", 0, 27);
      shaftConnector.zRot = -0.26F;
      shaftConnector.addBox(-16.0F, 0.0F, -8.0F, 16.0F, 2.0F, 1.0F);
      shaftConnector.addBox(-16.0F, 0.0F, 7.0F, 16.0F, 2.0F, 1.0F);
      EasyMeshBuilder shafts = new EasyMeshBuilder("shafts");
      shafts.setRotationPoint(0.0F, 0.0F, -14.0F);
      shafts.yRot = (float) (Math.PI / 2);
      shafts.addChild(shaft);
      shafts.addChild(shaftConnector);
      EasyMeshBuilder seedBox = new EasyMeshBuilder("seedBox", 0, 11);
      seedBox.setRotationPoint(0.0F, 3.0F, 1.0F);
      seedBox.addBox(-14.0F, 0.0F, -6.0F, 28.0F, 1.0F, 12.0F);
      EasyMeshBuilder seedBoxWalls = new EasyMeshBuilder("seedBoxWalls", 2, 8);
      seedBoxWalls.addBox(-15.0F, -8.0F, -7.0F, 30.0F, 9.0F, 1.0F);
      seedBoxWalls.addBox(-15.0F, -8.0F, 6.0F, 30.0F, 9.0F, 1.0F);
      EasyMeshBuilder seedBoxSideWalls = new EasyMeshBuilder("seedBoxSideWalls", 2, 8);
      seedBoxSideWalls.setRotationPoint(0.0F, 0.0F, 0.0F);
      seedBoxSideWalls.yRot = (float) (-Math.PI / 2);
      seedBoxSideWalls.addBox(-6.0F, -8.0F, -15.0F, 12.0F, 9.0F, 1.0F);
      seedBoxSideWalls.addBox(-6.0F, -8.0F, 14.0F, 12.0F, 9.0F, 1.0F);
      seedBox.addChild(seedBoxWalls);
      seedBox.addChild(seedBoxSideWalls);
      EasyMeshBuilder hoppers = new EasyMeshBuilder("hoppers", 0, 57);
      hoppers.setRotationPoint(0.0F, 4.0F, 4.0F);
      hoppers.addBox(-13.5F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      hoppers.addBox(-8.5F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      hoppers.addBox(-3.5F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      hoppers.addBox(1.5F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      hoppers.addBox(6.5F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      hoppers.addBox(11.5F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      EasyMeshBuilder hopperExtensions = new EasyMeshBuilder("hopperExtensions", 0, 57);
      hopperExtensions.setRotationPoint(0.0F, 4.75F, 0.0F);
      hopperExtensions.xRot = (float) (Math.PI / 8);
      hopperExtensions.addBox(-13.51F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      hopperExtensions.addBox(-8.51F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      hopperExtensions.addBox(-3.51F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      hopperExtensions.addBox(1.51F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      hopperExtensions.addBox(6.51F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      hopperExtensions.addBox(11.51F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F);
      hoppers.addChild(hopperExtensions);
      EasyMeshBuilder hopperSupport = new EasyMeshBuilder("hopperSupport", 0, 0);
      hopperSupport.setRotationPoint(0.0F, 6.0F, 0.0F);
      hopperSupport.addBox(-13.5F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F, 0.1F);
      hopperSupport.addBox(-8.5F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F, 0.1F);
      hopperSupport.addBox(-3.5F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F, 0.1F);
      hopperSupport.addBox(1.5F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F, 0.1F);
      hopperSupport.addBox(6.5F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F, 0.1F);
      hopperSupport.addBox(11.5F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F, 0.1F);
      EasyMeshBuilder parts = new EasyMeshBuilder("parts");
      parts.setRotationPoint(0.0F, -5.0F, -1.0F);
      parts.addChild(shafts);
      parts.addChild(seedBox);
      parts.addChild(hoppers);
      parts.addChild(hopperSupport);
      EasyMeshBuilder body = CartModel.createBody(9);
      body.addChild(axis);
      body.addChild(parts);
      body.build(def.getRoot());
      return LayerDefinition.create(def, 64, 64);
   }
}
