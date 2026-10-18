package net.jmb19905.niftycarts.client.renderer.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.client.renderer.entity.CartRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.banner.BannerModel;

@Environment(EnvType.CLIENT)
public final class AnimalCartModel extends CartModel<CartRenderState> {
   public AnimalCartModel(ModelPart root, BannerModel bannerModel, BannerFlagModel flagModel) {
      super(root, bannerModel, flagModel);
   }

   public static LayerDefinition createLayer() {
      MeshDefinition def = CartModel.createDefinition(9.0F, 25.0F);
      EasyMeshBuilder axis = new EasyMeshBuilder("axis", 0, 21);
      axis.addBox(-12.5F, -1.0F, -1.0F, 25.0F, 2.0F, 2.0F);
      EasyMeshBuilder cartBase = new EasyMeshBuilder("cartBase", 0, 0);
      cartBase.addBox(-15.5F, -10.0F, -2.0F, 29.0F, 20.0F, 1.0F);
      cartBase.xRot = (float) (-Math.PI / 2);
      cartBase.yRot = (float) (-Math.PI / 2);
      EasyMeshBuilder shaft = new EasyMeshBuilder("shaft", 0, 25);
      shaft.setRotationPoint(0.0F, -5.0F, -15.0F);
      shaft.yRot = (float) (Math.PI / 2);
      shaft.addBox(0.0F, -0.5F, -8.0F, 20.0F, 2.0F, 1.0F);
      shaft.addBox(0.0F, -0.5F, 7.0F, 20.0F, 2.0F, 1.0F);
      EasyMeshBuilder boardLeft = new EasyMeshBuilder("boardLeft", 0, 28);
      boardLeft.addBox(-10.0F, -14.5F, 9.0F, 8.0F, 31.0F, 2.0F);
      boardLeft.xRot = (float) (-Math.PI / 2);
      boardLeft.zRot = (float) (Math.PI / 2);
      EasyMeshBuilder boardRight = new EasyMeshBuilder("boardRight", 0, 28);
      boardRight.addBox(-10.0F, -14.5F, -11.0F, 8.0F, 31.0F, 2.0F);
      boardRight.xRot = (float) (-Math.PI / 2);
      boardRight.zRot = (float) (Math.PI / 2);
      EasyMeshBuilder boardBack = new EasyMeshBuilder("boardBack", 20, 28);
      boardBack.addBox(-9.0F, -10.0F, 12.5F, 18.0F, 8.0F, 2.0F);
      EasyMeshBuilder boardFront = new EasyMeshBuilder("boardFront", 20, 28);
      boardFront.addBox(-9.0F, -10.0F, -16.5F, 18.0F, 8.0F, 2.0F);
      EasyMeshBuilder body = CartModel.createBody(9);
      body.addChild(axis);
      body.addChild(cartBase);
      body.addChild(shaft);
      body.addChild(boardLeft);
      body.addChild(boardRight);
      body.addChild(boardBack);
      body.addChild(boardFront);
      body.build(def.getRoot());
      return LayerDefinition.create(def, 64, 64);
   }
}
