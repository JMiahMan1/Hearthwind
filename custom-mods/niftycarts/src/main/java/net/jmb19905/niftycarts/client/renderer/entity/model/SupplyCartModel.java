package net.jmb19905.niftycarts.client.renderer.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.client.renderer.entity.CargoCartRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.banner.BannerModel;

@Environment(EnvType.CLIENT)
public final class SupplyCartModel extends CargoCartModel<CargoCartRenderState> {
   public SupplyCartModel(ModelPart root, BannerModel bannerModel, BannerFlagModel flagModel) {
      super(root, bannerModel, flagModel);
   }

   @Override
   public void setupAnim(CargoCartRenderState state) {
      super.setupAnim(state);
   }

   public static LayerDefinition createLayer() {
      MeshDefinition def = CartModel.createDefinition(9.0F, 25.0F);
      EasyMeshBuilder boardBottom = new EasyMeshBuilder("boardBottom", 0, 0);
      boardBottom.addBox(-15.5F, -11.0F, -2.0F, 29.0F, 22.0F, 1.0F);
      boardBottom.yRot = (float) (-Math.PI / 2);
      boardBottom.xRot = (float) (-Math.PI / 2);
      EasyMeshBuilder axis = new EasyMeshBuilder("axis", 0, 23);
      axis.addBox(-12.5F, -1.0F, -1.0F, 25.0F, 2.0F, 2.0F);
      EasyMeshBuilder shaft = new EasyMeshBuilder("shaft", 0, 28);
      shaft.setRotationPoint(0.0F, -5.0F, -15.0F);
      shaft.yRot = (float) (Math.PI / 2);
      shaft.addBox(0.0F, -2.5F, -8.0F, 20.0F, 2.0F, 1.0F);
      shaft.addBox(0.0F, -2.5F, 7.0F, 20.0F, 2.0F, 1.0F);
      EasyMeshBuilder boardFront = new EasyMeshBuilder("boardFront", 0, 31);
      boardFront.addBox(-12.0F, -12.0F, -15.5F, 24.0F, 10.0F, 1.0F);
      EasyMeshBuilder[] boardsSide = new EasyMeshBuilder[4];
      boardsSide[0] = new EasyMeshBuilder("boards_side_0", 0, 27);
      boardsSide[0].addBox(-13.5F, -7.0F, 0.0F, 28.0F, 3.0F, 1.0F);
      boardsSide[0].setRotationPoint(-11.0F, -5.0F, -1.0F);
      boardsSide[0].yRot = (float) (-Math.PI / 2);
      boardsSide[1] = new EasyMeshBuilder("boards_side_1", 0, 27);
      boardsSide[1].addBox(-14.5F, -7.0F, 0.0F, 28.0F, 3.0F, 1.0F);
      boardsSide[1].setRotationPoint(11.0F, -5.0F, -1.0F);
      boardsSide[1].yRot = (float) (Math.PI / 2);
      boardsSide[2] = new EasyMeshBuilder("boards_side_2", 0, 27);
      boardsSide[2].addBox(-13.5F, -2.0F, 0.0F, 28.0F, 3.0F, 1.0F);
      boardsSide[2].setRotationPoint(-11.0F, -5.0F, -1.0F);
      boardsSide[2].yRot = (float) (-Math.PI / 2);
      boardsSide[3] = new EasyMeshBuilder("boards_side_3", 0, 27);
      boardsSide[3].addBox(-14.5F, -2.0F, 0.0F, 28.0F, 3.0F, 1.0F);
      boardsSide[3].setRotationPoint(11.0F, -5.0F, -1.0F);
      boardsSide[3].yRot = (float) (Math.PI / 2);
      EasyMeshBuilder[] boardsRear = new EasyMeshBuilder[]{new EasyMeshBuilder("boards_rear_0", 42, 0), null};
      boardsRear[0].addBox(10.0F, -12.0F, 13.5F, 2.0F, 11.0F, 1.0F);
      boardsRear[1] = new EasyMeshBuilder("boards_rear_1", 42, 0);
      boardsRear[1].addBox(-12.0F, -12.0F, 13.5F, 2.0F, 11.0F, 1.0F);
      EasyMeshBuilder flowerBasket = new EasyMeshBuilder("flowerBasket");
      flowerBasket.setTextureOffset(-17, 45).addBox(-8.0F, -6.0F, -11.5F, 16.0F, 1.0F, 17.0F);
      flowerBasket.setTextureOffset(16, 45).addBox(-10.0F, -7.0F, 5.5F, 20.0F, 5.0F, 2.0F);
      EasyMeshBuilder frontSide = new EasyMeshBuilder("frontSide", 16, 45);
      frontSide.yRot = (float) Math.PI;
      frontSide.addBox(-10.0F, -7.0F, 11.5F, 20.0F, 5.0F, 2.0F);
      flowerBasket.addChild(frontSide);
      EasyMeshBuilder leftSide = new EasyMeshBuilder("leftSide", 16, 52);
      leftSide.yRot = (float) (Math.PI / 2);
      leftSide.addBox(-5.5F, -7.0F, 8.0F, 17.0F, 5.0F, 2.0F);
      flowerBasket.addChild(leftSide);
      EasyMeshBuilder rightSide = new EasyMeshBuilder("rightSide", 16, 52);
      rightSide.yRot = (float) (-Math.PI / 2);
      rightSide.addBox(-11.5F, -7.0F, 8.0F, 17.0F, 5.0F, 2.0F);
      flowerBasket.addChild(rightSide);
      EasyMeshBuilder extraWheel = createWheel("extraWheel", 5.0F, 2.0F, 9.0F, 0.0F, -1.0F, 1, 1);
      EasyMeshBuilder body = CartModel.createBody(9);
      body.addChild(axis);
      body.addChild(shaft);
      body.addChild(boardBottom);
      body.addChild(boardFront);
      body.addChild(boardsRear[0]);
      body.addChild(boardsRear[1]);
      body.addChild(boardsSide[0]);
      body.addChild(boardsSide[1]);
      body.addChild(boardsSide[2]);
      body.addChild(boardsSide[3]);
      body.addChild(flowerBasket);
      body.addChild(extraWheel);
      body.build(def.getRoot());
      return LayerDefinition.create(def, 64, 64);
   }
}
