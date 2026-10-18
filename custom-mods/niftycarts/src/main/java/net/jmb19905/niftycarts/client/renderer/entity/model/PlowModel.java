package net.jmb19905.niftycarts.client.renderer.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.client.renderer.entity.PlowRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.banner.BannerModel;

@Environment(EnvType.CLIENT)
public final class PlowModel extends CartModel<PlowRenderState> {
   private final ModelPart[] plowShaftUpper = new ModelPart[3];
   private final ModelPart[] plowShaftLower = new ModelPart[3];

   public PlowModel(ModelPart root, BannerModel bannerModel, BannerFlagModel flagModel) {
      super(root, bannerModel, flagModel);
      ModelPart parts = root.getChild("body").getChild("parts");

      for (int i = 0; i < this.plowShaftUpper.length; i++) {
         this.plowShaftUpper[i] = parts.getChild("plow_shaft_upper_" + i);
         this.plowShaftLower[i] = this.plowShaftUpper[i].getChild("plow_shaft_lower_" + i);
      }
   }

   public ModelPart getShaft(int original) {
      return this.plowShaftLower[original];
   }

   public void setupAnim(PlowRenderState state) {
      super.setupAnim(state);

      for (ModelPart renderer : this.plowShaftUpper) {
         renderer.xRot = (float)(state.plowing ? (Math.PI / 4) - Math.toRadians(state.pitch) : Math.PI * 2.0 / 5.0);
      }
   }

   public static LayerDefinition createLayer() {
      MeshDefinition def = CartModel.createDefinition(9.0F, 25.0F);
      EasyMeshBuilder axis = new EasyMeshBuilder("axis", 0, 0);
      axis.addBox(-12.5F, -1.0F, -1.0F, 25.0F, 2.0F, 2.0F);
      EasyMeshBuilder[] triangle = new EasyMeshBuilder[]{new EasyMeshBuilder("triangle_0", 0, 4), null, null};
      triangle[0].addBox(-7.5F, -9.0F, 0.0F, 15.0F, 2.0F, 2.0F);
      triangle[1] = new EasyMeshBuilder("triangle_1", 0, 11);
      triangle[1].addBox(-5.0F, -9.0F, 0.5F, 2.0F, 14.0F, 2.0F);
      triangle[1].zRot = -0.175F;
      triangle[2] = new EasyMeshBuilder("triangle_2", 0, 11);
      triangle[2].mirror(true);
      triangle[2].addBox(3.0F, -9.0F, 0.5F, 2.0F, 14.0F, 2.0F);
      triangle[2].zRot = 0.175F;
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
      EasyMeshBuilder[] plowShaftUpper = new EasyMeshBuilder[3];
      EasyMeshBuilder[] plowShaftLower = new EasyMeshBuilder[3];

      for (int i = 0; i < plowShaftUpper.length; i++) {
         plowShaftUpper[i] = new EasyMeshBuilder("plow_shaft_upper_" + i, 56, 0);
         plowShaftUpper[i].addBox(-1.0F, -2.0F, -2.0F, 2.0F, 30.0F, 2.0F);
         plowShaftUpper[i].setRotationPoint(-3.0F + 3 * i, -7.0F, 0.0F);
         plowShaftUpper[i].yRot = -0.523599F + (float) (Math.PI / 6) * i;
         plowShaftLower[i] = new EasyMeshBuilder("plow_shaft_lower_" + i, 42, 4);
         plowShaftLower[i].addBox(-1.0F, -0.7F, -0.7F, 2.0F, 10.0F, 2.0F);
         plowShaftLower[i].setRotationPoint(0.0F, 28.0F, -1.0F);
         plowShaftLower[i].xRot = (float) (Math.PI / 4);
         plowShaftUpper[i].addChild(plowShaftLower[i]);
      }

      EasyMeshBuilder plowHandle = new EasyMeshBuilder("plow_handle", 50, 4);
      plowHandle.addBox(-0.5F, 0.0F, -0.5F, 1.0F, 18.0F, 1.0F);
      plowHandle.setRotationPoint(0.0F, 33.0F, 5.0F);
      plowHandle.xRot = (float) (Math.PI / 2);
      plowShaftUpper[1].addChild(plowHandle);
      EasyMeshBuilder plowHandleGrip = new EasyMeshBuilder("plow_handle_grip", 50, 23);
      plowHandleGrip.addBox(-0.5F, 0.0F, -1.0F, 1.0F, 5.0F, 1.0F);
      plowHandleGrip.setRotationPoint(0.0F, 32.8F, 21.0F);
      plowHandleGrip.xRot = (float) (Math.PI / 4);
      plowShaftUpper[1].addChild(plowHandleGrip);
      EasyMeshBuilder parts = new EasyMeshBuilder("parts");
      parts.setRotationPoint(0.0F, -5.0F, -1.0F);
      parts.addChild(shafts);
      parts.addChild(triangle[0]);
      parts.addChild(triangle[1]);
      parts.addChild(triangle[2]);
      parts.addChild(plowShaftUpper[0]);
      parts.addChild(plowShaftUpper[1]);
      parts.addChild(plowShaftUpper[2]);
      EasyMeshBuilder body = CartModel.createBody(9);
      body.addChild(axis);
      body.addChild(parts);
      body.build(def.getRoot());
      return LayerDefinition.create(def, 64, 64);
   }
}
