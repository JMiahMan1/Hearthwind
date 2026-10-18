package net.jmb19905.niftycarts.client.renderer.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.client.renderer.entity.WagonRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class WagonModel extends CartModel<WagonRenderState> {
   private static final float Z_FIGHTING_EPSILON = 0.001F;
   private final ModelPart roof;
   private final WagonChestModel chestModel;

   public WagonModel(ModelPart root, ModelPart roofRoot, WagonChestModel chestModel, BannerModel bannerModel, BannerFlagModel flagModel) {
      super(root, 2, bannerModel, flagModel);
      this.roof = roofRoot.getChild("roof");
      this.chestModel = chestModel;
   }

   public WagonChestModel getChestModel() {
      return this.chestModel;
   }

   public void setupAnim(WagonRenderState state) {
      super.setupAnim(state);
      if (state.hasRoof) {
         switch (state.unfurled) {
            case 0:
               this.roof.getChild("roofUnfurled").visible = true;
               this.roof.getChild("roofPartUnfurled").visible = false;
               this.roof.getChild("roofFurled").visible = false;
               break;
            case 1:
               this.roof.getChild("roofUnfurled").visible = false;
               this.roof.getChild("roofPartUnfurled").visible = true;
               this.roof.getChild("roofFurled").visible = false;
               break;
            default:
               this.roof.getChild("roofUnfurled").visible = false;
               this.roof.getChild("roofPartUnfurled").visible = false;
               this.roof.getChild("roofFurled").visible = true;
         }
      } else {
         this.roof.getChild("roofUnfurled").visible = false;
         this.roof.getChild("roofPartUnfurled").visible = false;
         this.roof.getChild("roofFurled").visible = false;
      }

      this.chestModel.setupAnim(state);
   }

   public static LayerDefinition createRoofLayer() {
      float axleLength = 35.0F;
      MeshDefinition def = new MeshDefinition();
      EasyMeshBuilder roof = new EasyMeshBuilder("roof", 0, 0);
      roof.setRotationAngles(0.0F, (float) (Math.PI / 2), 0.0F);
      EasyMeshBuilder roofTop = new EasyMeshBuilder("roofTop", 0, 0);
      roofTop.setRotationPoint(0.0F, -25.0F, 0.0F);
      roofTop.setRotationAngles((float) (Math.PI / 2), 0.0F, 0.0F);
      roofTop.addBox(
         -32.002F,
         -axleLength / 2.0F + Mth.sqrt(2.0F) * 8.0F,
         Mth.sqrt(2.0F) * 8.0F - 2.0F,
         24.0F,
         35.0F - Mth.sqrt(2.0F) * 16.0F,
         2.0F
      );
      roofTop.addBox(
         8.002F, -axleLength / 2.0F + Mth.sqrt(2.0F) * 8.0F, Mth.sqrt(2.0F) * 8.0F - 2.0F, 24.0F, 35.0F - Mth.sqrt(2.0F) * 16.0F, 2.0F
      );
      roofTop.addBox(
         -8.0F, -axleLength / 2.0F + 1.0F + Mth.sqrt(112.5F), Mth.sqrt(112.5F) - 1.0F, 16.0F, 33.0F - 2.0F * Mth.sqrt(112.5F), 1.0F
      );
      roof.addChild(roofTop);
      EasyMeshBuilder roofUnfurled = new EasyMeshBuilder("roofUnfurled", 0, 0);
      roofUnfurled.addBox(-32.002F, -25.0F, -axleLength / 2.0F - 0.001F, 24.0F, 15.0F, 2.0F);
      roofUnfurled.addBox(-8.0F, -25.0F, -axleLength / 2.0F + 1.0F, 16.0F, 15.0F, 1.0F);
      roofUnfurled.addBox(8.002F, -25.0F, -axleLength / 2.0F - 0.001F, 24.0F, 15.0F, 2.0F);
      roofUnfurled.addBox(-32.002F, -25.0F, axleLength / 2.0F - 2.0F + 0.001F, 24.0F, 15.0F, 2.0F);
      roofUnfurled.addBox(-8.0F, -25.0F, axleLength / 2.0F - 2.0F, 16.0F, 15.0F, 1.0F);
      roofUnfurled.addBox(8.002F, -25.0F, axleLength / 2.0F - 2.0F + 0.001F, 24.0F, 15.0F, 2.0F);
      EasyMeshBuilder roofBackFlaps = new EasyMeshBuilder("roofBackFlaps", 0, 0);
      roofBackFlaps.setRotationAngles(0.0F, (float) (Math.PI / 2), 0.0F);
      roofBackFlaps.addBox(-axleLength / 2.0F, -25.0F, -33.0F, 10.0F, 15.0F, 1.0F);
      roofBackFlaps.addBox(axleLength / 2.0F - 10.0F, -25.0F, -32.999F, 10.0F, 15.0F, 1.0F);
      roofBackFlaps.addBox(
         -axleLength / 2.0F + Mth.sqrt(2.0F) * 8.0F,
         -25.0F - Mth.sqrt(2.0F) * 8.0F,
         -33.002F,
         35.0F - Mth.sqrt(2.0F) * 16.0F,
         10.0F,
         1.0F
      );
      roofUnfurled.addChild(roofBackFlaps);
      EasyMeshBuilder roofBackFlapsAngledLeft = new EasyMeshBuilder("roofBackFlapsAngledLeft", 0, 0);
      roofBackFlapsAngledLeft.setRotationPoint(-axleLength / 2.0F, -25.0F, 0.0F);
      roofBackFlapsAngledLeft.setRotationAngles(0.0F, 0.0F, (float) (Math.PI / 4));
      roofBackFlapsAngledLeft.addBox(0.0F, -16.0F, -33.001F, 10.0F, 16.0F, 1.0F);
      roofBackFlaps.addChild(roofBackFlapsAngledLeft);
      EasyMeshBuilder roofBackFlapsAngledRight = new EasyMeshBuilder("roofBackFlapsAngledRight", 0, 0);
      roofBackFlapsAngledRight.setRotationPoint(axleLength / 2.0F, -25.0F, 0.0F);
      roofBackFlapsAngledRight.setRotationAngles(0.0F, 0.0F, (float) (-Math.PI / 4));
      roofBackFlapsAngledRight.addBox(-10.0F, -16.0F, -33.0F, 10.0F, 16.0F, 1.0F);
      roofBackFlaps.addChild(roofBackFlapsAngledRight);
      EasyMeshBuilder roofFrontFlaps = new EasyMeshBuilder("roofFrontFlaps", 0, 0);
      roofFrontFlaps.setRotationAngles(0.0F, (float) (Math.PI / 2), 0.0F);
      roofFrontFlaps.addBox(-axleLength / 2.0F, -25.0F, 32.0F, 10.0F, 15.0F, 1.0F);
      roofFrontFlaps.addBox(axleLength / 2.0F - 10.0F, -25.0F, 31.999F, 10.0F, 15.0F, 1.0F);
      roofFrontFlaps.addBox(
         -axleLength / 2.0F + Mth.sqrt(2.0F) * 8.0F,
         -25.0F - Mth.sqrt(2.0F) * 8.0F,
         32.002F,
         35.0F - Mth.sqrt(2.0F) * 16.0F,
         10.0F,
         1.0F
      );
      roofUnfurled.addChild(roofFrontFlaps);
      EasyMeshBuilder roofFrontFlapsAngledLeft = new EasyMeshBuilder("roofFrontFlapsAngledLeft", 0, 0);
      roofFrontFlapsAngledLeft.setRotationPoint(-axleLength / 2.0F, -25.0F, 0.0F);
      roofFrontFlapsAngledLeft.setRotationAngles(0.0F, 0.0F, (float) (Math.PI / 4));
      roofFrontFlapsAngledLeft.addBox(0.0F, -16.0F, 32.001F, 10.0F, 16.0F, 1.0F);
      roofFrontFlaps.addChild(roofFrontFlapsAngledLeft);
      EasyMeshBuilder roofFrontFlapsAngledRight = new EasyMeshBuilder("roofFrontFlapsAngledRight", 0, 0);
      roofFrontFlapsAngledRight.setRotationPoint(axleLength / 2.0F, -25.0F, 0.0F);
      roofFrontFlapsAngledRight.setRotationAngles(0.0F, 0.0F, (float) (-Math.PI / 4));
      roofFrontFlapsAngledRight.addBox(-10.0F, -16.0F, 32.0F, 10.0F, 16.0F, 1.0F);
      roofFrontFlaps.addChild(roofFrontFlapsAngledRight);
      EasyMeshBuilder roofLeftSlopeMiddle = new EasyMeshBuilder("roofLeftSlopeMiddle", 0, 0);
      roofLeftSlopeMiddle.setRotationPoint(0.0F, -25.0F, axleLength / 2.0F - 1.0F);
      roofLeftSlopeMiddle.setRotationAngles((float) (Math.PI / 4), 0.0F, 0.0F);
      roofLeftSlopeMiddle.addBox(-8.0F, -15.0F, -1.0F, 16.0F, 15.0F, 1.0F);
      roofUnfurled.addChild(roofLeftSlopeMiddle);
      EasyMeshBuilder roofLeftSlopeOuter = new EasyMeshBuilder("roofLeftSlopeOuter", 0, 0);
      roofLeftSlopeOuter.setRotationPoint(0.0F, -25.0F, axleLength / 2.0F);
      roofLeftSlopeOuter.setRotationAngles((float) (Math.PI / 4), 0.0F, 0.0F);
      roofLeftSlopeOuter.addBox(-32.0F, -16.0F, -2.0F, 24.0F, 16.0F, 2.0F);
      roofLeftSlopeOuter.addBox(8.0F, -16.0F, -2.0F, 24.0F, 16.0F, 2.0F);
      roofUnfurled.addChild(roofLeftSlopeOuter);
      EasyMeshBuilder roofRightSlopeMiddle = new EasyMeshBuilder("roofRightSlopeMiddle", 0, 0);
      roofRightSlopeMiddle.setRotationPoint(0.0F, -25.0F, -axleLength / 2.0F + 1.0F);
      roofRightSlopeMiddle.setRotationAngles((float) (-Math.PI / 4), 0.0F, 0.0F);
      roofRightSlopeMiddle.addBox(-8.0F, -15.0F, 0.0F, 16.0F, 15.0F, 1.0F);
      roofUnfurled.addChild(roofRightSlopeMiddle);
      EasyMeshBuilder roofRightSlopeOuter = new EasyMeshBuilder("roofRightSlopeOuter", 0, 0);
      roofRightSlopeOuter.setRotationPoint(0.0F, -25.0F, -axleLength / 2.0F);
      roofRightSlopeOuter.setRotationAngles((float) (-Math.PI / 4), 0.0F, 0.0F);
      roofRightSlopeOuter.addBox(-32.0F, -16.0F, 0.0F, 24.0F, 16.0F, 2.0F);
      roofRightSlopeOuter.addBox(8.0F, -16.0F, 0.0F, 24.0F, 16.0F, 2.0F);
      roofUnfurled.addChild(roofRightSlopeOuter);
      roof.addChild(roofUnfurled);
      EasyMeshBuilder roofPartUnfurled = new EasyMeshBuilder("roofPartUnfurled", 0, 0);
      roofPartUnfurled.addChild(roofLeftSlopeMiddle);
      roofPartUnfurled.addChild(roofLeftSlopeOuter);
      roofPartUnfurled.addChild(roofRightSlopeMiddle);
      roofPartUnfurled.addChild(roofRightSlopeOuter);
      roofPartUnfurled.addBox(-32.002F, -25.0F, -axleLength / 2.0F - 0.001F, 24.0F, 7.0F, 2.0F);
      roofPartUnfurled.addBox(-8.0F, -25.0F, -axleLength / 2.0F + 1.0F, 16.0F, 7.0F, 1.0F);
      roofPartUnfurled.addBox(8.002F, -25.0F, -axleLength / 2.0F - 0.001F, 24.0F, 7.0F, 2.0F);
      roofPartUnfurled.addBox(-32.002F, -20.0F, -axleLength / 2.0F - 0.001F - 1.0F, 24.0F, 2.0F, 1.0F);
      roofPartUnfurled.addBox(-8.0F, -20.0F, -axleLength / 2.0F, 16.0F, 2.0F, 1.0F);
      roofPartUnfurled.addBox(8.002F, -20.0F, -axleLength / 2.0F - 0.001F - 1.0F, 24.0F, 2.0F, 1.0F);
      roofPartUnfurled.addBox(-32.002F, -25.0F, axleLength / 2.0F - 2.0F + 0.001F, 24.0F, 7.0F, 2.0F);
      roofPartUnfurled.addBox(-8.0F, -25.0F, axleLength / 2.0F - 2.0F, 16.0F, 7.0F, 1.0F);
      roofPartUnfurled.addBox(8.002F, -25.0F, axleLength / 2.0F - 2.0F + 0.001F, 24.0F, 7.0F, 2.0F);
      roofPartUnfurled.addBox(-32.002F, -20.0F, axleLength / 2.0F + 0.001F, 24.0F, 2.0F, 1.0F);
      roofPartUnfurled.addBox(-8.0F, -20.0F, axleLength / 2.0F - 1.0F, 16.0F, 2.0F, 1.0F);
      roofPartUnfurled.addBox(8.002F, -20.0F, axleLength / 2.0F + 0.001F, 24.0F, 2.0F, 1.0F);
      EasyMeshBuilder roofBackFlapsPartUnfurled = new EasyMeshBuilder("roofBackFlapsPartUnfurled", 0, 0);
      roofBackFlapsPartUnfurled.setRotationAngles(0.0F, (float) (Math.PI / 2), 0.0F);
      roofBackFlapsPartUnfurled.addBox(-axleLength / 2.0F, -25.0F, -33.0F, 10.0F, 7.0F, 1.0F);
      roofBackFlapsPartUnfurled.addBox(axleLength / 2.0F - 10.0F, -25.0F, -32.999F, 10.0F, 7.0F, 1.0F);
      roofBackFlapsPartUnfurled.addBox(-axleLength / 2.0F, -20.0F, -34.0F, 10.0F, 2.0F, 1.0F);
      roofBackFlapsPartUnfurled.addBox(axleLength / 2.0F - 10.0F, -20.0F, -33.999F, 10.0F, 2.0F, 1.0F);
      roofBackFlapsPartUnfurled.addBox(
         -axleLength / 2.0F + Mth.sqrt(2.0F) * 8.0F,
         -25.0F - Mth.sqrt(2.0F) * 8.0F,
         -33.002F,
         35.0F - Mth.sqrt(2.0F) * 16.0F,
         10.0F,
         1.0F
      );
      roofPartUnfurled.addChild(roofBackFlapsPartUnfurled);
      roofBackFlapsPartUnfurled.addChild(roofBackFlapsAngledLeft);
      roofBackFlapsPartUnfurled.addChild(roofBackFlapsAngledRight);
      EasyMeshBuilder roofFrontFlapsPartUnfurled = new EasyMeshBuilder("roofFrontFlapsPartUnfurled", 0, 0);
      roofFrontFlapsPartUnfurled.setRotationAngles(0.0F, (float) (Math.PI / 2), 0.0F);
      roofFrontFlapsPartUnfurled.addBox(-axleLength / 2.0F, -25.0F, 32.0F, 10.0F, 7.0F, 1.0F);
      roofFrontFlapsPartUnfurled.addBox(axleLength / 2.0F - 10.0F, -25.0F, 31.999F, 10.0F, 7.0F, 1.0F);
      roofFrontFlapsPartUnfurled.addBox(-axleLength / 2.0F, -20.0F, 33.0F, 10.0F, 2.0F, 1.0F);
      roofFrontFlapsPartUnfurled.addBox(axleLength / 2.0F - 10.0F, -20.0F, 32.999F, 10.0F, 2.0F, 1.0F);
      roofFrontFlapsPartUnfurled.addBox(
         -axleLength / 2.0F + Mth.sqrt(2.0F) * 8.0F,
         -25.0F - Mth.sqrt(2.0F) * 8.0F,
         32.002F,
         35.0F - Mth.sqrt(2.0F) * 16.0F,
         10.0F,
         1.0F
      );
      roofPartUnfurled.addChild(roofFrontFlapsPartUnfurled);
      roofFrontFlapsPartUnfurled.addChild(roofFrontFlapsAngledLeft);
      roofFrontFlapsPartUnfurled.addChild(roofFrontFlapsAngledRight);
      roof.addChild(roofPartUnfurled);
      EasyMeshBuilder furled = new EasyMeshBuilder("roofFurled", 0, 0);
      EasyMeshBuilder roofLeftSlopeMiddleFurled = new EasyMeshBuilder("roofLeftSlopeMiddleFurled", 0, 0);
      roofLeftSlopeMiddleFurled.setRotationPoint(0.0F, -25.0F, axleLength / 2.0F - 1.0F);
      roofLeftSlopeMiddleFurled.setRotationAngles((float) (Math.PI / 4), 0.0F, 0.0F);
      roofLeftSlopeMiddleFurled.addBox(-8.0F, -15.0F, -1.0F, 16.0F, 5.0F, 1.0F);
      roofLeftSlopeMiddleFurled.addBox(-8.0F, -13.0F, 0.0F, 16.0F, 3.0F, 1.0F);
      furled.addChild(roofLeftSlopeMiddleFurled);
      EasyMeshBuilder roofLeftSlopeOuterFurled = new EasyMeshBuilder("roofLeftSlopeOuterFurled", 0, 0);
      roofLeftSlopeOuterFurled.setRotationPoint(0.0F, -25.0F, axleLength / 2.0F);
      roofLeftSlopeOuterFurled.setRotationAngles((float) (Math.PI / 4), 0.0F, 0.0F);
      roofLeftSlopeOuterFurled.addBox(-32.0F, -16.0F, -2.0F, 24.0F, 6.0F, 2.0F);
      roofLeftSlopeOuterFurled.addBox(8.0F, -16.0F, -2.0F, 24.0F, 6.0F, 2.0F);
      roofLeftSlopeOuterFurled.addBox(-32.0F, -14.0F, 0.0F, 24.0F, 4.0F, 1.0F);
      roofLeftSlopeOuterFurled.addBox(8.0F, -14.0F, 0.0F, 24.0F, 4.0F, 1.0F);
      furled.addChild(roofLeftSlopeOuterFurled);
      EasyMeshBuilder roofRightSlopeMiddleFurled = new EasyMeshBuilder("roofRightSlopeMiddleFurled", 0, 0);
      roofRightSlopeMiddleFurled.setRotationPoint(0.0F, -25.0F, -axleLength / 2.0F + 1.0F);
      roofRightSlopeMiddleFurled.setRotationAngles((float) (-Math.PI / 4), 0.0F, 0.0F);
      roofRightSlopeMiddleFurled.addBox(-8.0F, -15.0F, 0.0F, 16.0F, 5.0F, 1.0F);
      roofRightSlopeMiddleFurled.addBox(-8.0F, -13.0F, -1.0F, 16.0F, 3.0F, 1.0F);
      furled.addChild(roofRightSlopeMiddleFurled);
      EasyMeshBuilder roofRightSlopeOuterFurled = new EasyMeshBuilder("roofRightSlopeOuterFurled", 0, 0);
      roofRightSlopeOuterFurled.setRotationPoint(0.0F, -25.0F, -axleLength / 2.0F);
      roofRightSlopeOuterFurled.setRotationAngles((float) (-Math.PI / 4), 0.0F, 0.0F);
      roofRightSlopeOuterFurled.addBox(-32.0F, -16.0F, 0.0F, 24.0F, 6.0F, 2.0F);
      roofRightSlopeOuterFurled.addBox(8.0F, -16.0F, 0.0F, 24.0F, 6.0F, 2.0F);
      roofRightSlopeOuterFurled.addBox(-32.0F, -14.0F, -1.0F, 24.0F, 4.0F, 1.0F);
      roofRightSlopeOuterFurled.addBox(8.0F, -14.0F, -1.0F, 24.0F, 4.0F, 1.0F);
      furled.addChild(roofRightSlopeOuterFurled);
      EasyMeshBuilder roofBackFlapsFurled = new EasyMeshBuilder("roofBackFlapsFurled", 0, 0);
      roofBackFlapsFurled.setRotationAngles(0.0F, (float) (Math.PI / 2), 0.0F);
      roofBackFlapsFurled.addBox(
         -axleLength / 2.0F + Mth.sqrt(2.0F) * 8.0F,
         -25.0F - Mth.sqrt(2.0F) * 8.0F,
         -33.001F,
         35.0F - Mth.sqrt(2.0F) * 16.0F,
         5.0F,
         1.0F
      );
      roofBackFlapsFurled.addBox(
         -axleLength / 2.0F - 3.0F + Mth.sqrt(2.0F) * 8.0F,
         -23.0F - Mth.sqrt(2.0F) * 8.0F + 0.001F,
         -34.002F,
         41.0F - Mth.sqrt(2.0F) * 16.0F,
         3.0F,
         2.0F
      );
      furled.addChild(roofBackFlapsFurled);
      EasyMeshBuilder roofFrontFlapsFurled = new EasyMeshBuilder("roofFrontFlapsFurled", 0, 0);
      roofFrontFlapsFurled.setRotationAngles(0.0F, (float) (Math.PI / 2), 0.0F);
      roofFrontFlapsFurled.addBox(
         -axleLength / 2.0F + Mth.sqrt(2.0F) * 8.0F,
         -25.0F - Mth.sqrt(2.0F) * 8.0F,
         32.001F,
         35.0F - Mth.sqrt(2.0F) * 16.0F,
         5.0F,
         1.0F
      );
      roofFrontFlapsFurled.addBox(
         -axleLength / 2.0F - 3.0F + Mth.sqrt(2.0F) * 8.0F,
         -23.0F - Mth.sqrt(2.0F) * 8.0F + 0.001F,
         32.002F,
         41.0F - Mth.sqrt(2.0F) * 16.0F,
         3.0F,
         2.0F
      );
      furled.addChild(roofFrontFlapsFurled);
      roof.addChild(furled);
      roof.build(def.getRoot());
      return LayerDefinition.create(def, 16, 16);
   }

   public static LayerDefinition createLayer() {
      int rimLength = 10;
      float axleLength = 35.0F;
      float axleDist = 40.0F;
      MeshDefinition def = CartModel.createDefinition(rimLength, axleLength, 2, axleDist);
      EasyMeshBuilder axle0 = new EasyMeshBuilder("axle_0", 0, 60);
      axle0.addBox(-axleLength / 2.0F, -1.0F, -1.0F - axleDist / 2.0F, Mth.ceil(axleLength / 2.0F), 2.0F, 2.0F);
      axle0.addBox(-axleLength / 2.0F + Mth.ceil(axleLength / 2.0F), -1.0F, -1.0F - axleDist / 2.0F, Mth.floor(axleLength / 2.0F), 2.0F, 2.0F);
      EasyMeshBuilder axle1 = new EasyMeshBuilder("axle_1", 0, 60);
      axle1.addBox(-axleLength / 2.0F, -1.0F, -1.0F + axleDist / 2.0F, Mth.ceil(axleLength / 2.0F), 2.0F, 2.0F);
      axle1.addBox(-axleLength / 2.0F + Mth.ceil(axleLength / 2.0F), -1.0F, -1.0F + axleDist / 2.0F, Mth.floor(axleLength / 2.0F), 2.0F, 2.0F);
      EasyMeshBuilder base = new EasyMeshBuilder("base", 0, 0);
      base.addBox(
         -axleLength / 2.0F + 2.0F,
         -2.0F,
         -1.0F - axleDist / 2.0F - 10.0F,
         (float)Math.ceil((axleLength - 4.0F) / 2.0F),
         1.0F,
         (axleDist + 22.0F) / 2.0F - 1.0F
      );
      base.addBox(
         (float)(-axleLength / 2.0F + 2.0F + Math.ceil((axleLength - 4.0F) / 2.0F)),
         -2.0F,
         -1.0F - axleDist / 2.0F - 10.0F,
         (float)Math.floor((axleLength - 4.0F) / 2.0F),
         1.0F,
         (axleDist + 22.0F) / 2.0F - 1.0F
      );
      base.addBox(-axleLength / 2.0F + 2.0F, -2.0F, -1.0F, (float)Math.ceil((axleLength - 4.0F) / 2.0F), 1.0F, (axleDist + 22.0F) / 2.0F + 1.0F);
      base.addBox(
         (float)(-axleLength / 2.0F + 2.0F + Math.ceil((axleLength - 4.0F) / 2.0F)),
         -2.0F,
         -1.0F,
         (float)Math.floor((axleLength - 4.0F) / 2.0F),
         1.0F,
         (axleDist + 22.0F) / 2.0F + 1.0F
      );
      EasyMeshBuilder walls = new EasyMeshBuilder("walls", 0, 0);
      EasyMeshBuilder rightWalls = new EasyMeshBuilder("rightWalls", 0, 2);
      rightWalls.setRotationAngles(0.0F, (float) (Math.PI / 2), 0.0F);
      rightWalls.addBox(-32.0F, -10.0F, axleLength / 2.0F - 4.0F, 16.0F, 8.0F, 2.0F);
      rightWalls.addBox(-16.0F, -10.0F, axleLength / 2.0F - 4.0F, 16.0F, 8.0F, 2.0F);
      rightWalls.addBox(0.0F, -10.0F, axleLength / 2.0F - 4.0F, 16.0F, 8.0F, 2.0F);
      rightWalls.addBox(16.0F, -10.0F, axleLength / 2.0F - 4.0F, 16.0F, 8.0F, 2.0F);
      EasyMeshBuilder leftWalls = new EasyMeshBuilder("leftWalls", 0, 2);
      leftWalls.setRotationAngles(0.0F, (float) (-Math.PI / 2), 0.0F);
      leftWalls.addBox(-32.0F, -10.0F, axleLength / 2.0F - 4.0F, 16.0F, 8.0F, 2.0F);
      leftWalls.addBox(-16.0F, -10.0F, axleLength / 2.0F - 4.0F, 16.0F, 8.0F, 2.0F);
      leftWalls.addBox(0.0F, -10.0F, axleLength / 2.0F - 4.0F, 16.0F, 8.0F, 2.0F);
      leftWalls.addBox(16.0F, -10.0F, axleLength / 2.0F - 4.0F, 16.0F, 8.0F, 2.0F);
      EasyMeshBuilder wallBeams = new EasyMeshBuilder("wallBeams", 0, 3);
      wallBeams.setRotationAngles(0.0F, (float) (Math.PI / 2), 0.0F);
      wallBeams.addBox(-32.0F, -10.0F, -axleLength / 2.0F + 1.0F, 4.0F, 8.0F, 1.0F);
      wallBeams.addBox(-12.0F, -10.0F, -axleLength / 2.0F + 1.0F, 4.0F, 8.0F, 1.0F);
      wallBeams.addBox(8.0F, -10.0F, -axleLength / 2.0F + 1.0F, 4.0F, 8.0F, 1.0F);
      wallBeams.addBox(28.0F, -10.0F, -axleLength / 2.0F + 1.0F, 4.0F, 8.0F, 1.0F);
      wallBeams.addBox(-32.0F, -10.0F, axleLength / 2.0F - 2.0F, 4.0F, 8.0F, 1.0F);
      wallBeams.addBox(-12.0F, -10.0F, axleLength / 2.0F - 2.0F, 4.0F, 8.0F, 1.0F);
      wallBeams.addBox(8.0F, -10.0F, axleLength / 2.0F - 2.0F, 4.0F, 8.0F, 1.0F);
      wallBeams.addBox(28.0F, -10.0F, axleLength / 2.0F - 2.0F, 4.0F, 8.0F, 1.0F);
      walls.addChild(wallBeams);
      walls.addChild(leftWalls);
      walls.addChild(rightWalls);
      EasyMeshBuilder beams = new EasyMeshBuilder("beams", 0, 32);
      beams.addBox(-axleLength / 2.0F + 2.0F, -25.0F, -32.002F, 1.0F, 15.0F, 4.0F);
      beams.addBox(-axleLength / 2.0F + 2.0F, -25.0F, -11.998F, 1.0F, 15.0F, 4.0F);
      beams.addBox(-axleLength / 2.0F + 2.0F, -25.0F, 7.998F, 1.0F, 15.0F, 4.0F);
      beams.addBox(-axleLength / 2.0F + 2.0F, -25.0F, 28.002F, 1.0F, 15.0F, 4.0F);
      beams.addBox(axleLength / 2.0F - 3.0F, -25.0F, -32.002F, 1.0F, 15.0F, 4.0F);
      beams.addBox(axleLength / 2.0F - 3.0F, -25.0F, -11.998F, 1.0F, 15.0F, 4.0F);
      beams.addBox(axleLength / 2.0F - 3.0F, -25.0F, 7.998F, 1.0F, 15.0F, 4.0F);
      beams.addBox(axleLength / 2.0F - 3.0F, -25.0F, 28.002F, 1.0F, 15.0F, 4.0F);
      walls.addChild(beams);
      EasyMeshBuilder backAndFrontWalls = new EasyMeshBuilder("backAndFront", 0, 3);
      backAndFrontWalls.addBox(-axleLength / 2.0F + 4.0F, -10.0F, -32.0F, axleLength - 8.0F, 8.0F, 1.0F);
      backAndFrontWalls.addBox(-axleLength / 2.0F + 4.0F, -10.0F, 31.0F, 8.0F, 8.0F, 1.0F);
      backAndFrontWalls.addBox(axleLength / 2.0F - 12.0F, -10.0F, 31.0F, 8.0F, 8.0F, 1.0F);
      walls.addChild(backAndFrontWalls);
      EasyMeshBuilder rotating = new EasyMeshBuilder("rotating", 0, 0);
      rotating.setRotationPoint(0.0F, -13.0F, -20.0F);
      rotating.addBox(-8.5F, 0.0F, -35.0F, 1.0F, 2.0F, 36.0F);
      rotating.addBox(7.5F, 0.0F, -35.0F, 1.0F, 2.0F, 36.0F);
      rotating.build(def.getRoot());
      EasyMeshBuilder wallLeft = new EasyMeshBuilder("beamsAngledLeft", 0, 32);
      wallLeft.setRotationPoint(axleLength / 2.0F - 2.0F, -25.0F, 0.0F);
      wallLeft.setRotationAngles(0.0F, 0.0F, (float) (-Math.PI / 4));
      wallLeft.addBox(-1.0F, -14.0F, -32.001F, 1.0F, 14.0F, 4.0F);
      wallLeft.addBox(-1.0F, -14.0F, -11.999F, 1.0F, 14.0F, 4.0F);
      wallLeft.addBox(-1.0F, -14.0F, 7.999F, 1.0F, 14.0F, 4.0F);
      wallLeft.addBox(-1.0F, -14.0F, 28.001F, 1.0F, 14.0F, 4.0F);
      walls.addChild(wallLeft);
      EasyMeshBuilder wallRight = new EasyMeshBuilder("beamsAngledRight", 0, 32);
      wallRight.setRotationPoint(-axleLength / 2.0F + 2.0F, -25.0F, 0.0F);
      wallRight.setRotationAngles(0.0F, 0.0F, (float) (Math.PI / 4));
      wallRight.addBox(0.0F, -14.0F, -32.001F, 1.0F, 14.0F, 4.0F);
      wallRight.addBox(0.0F, -14.0F, -11.999F, 1.0F, 14.0F, 4.0F);
      wallRight.addBox(0.0F, -14.0F, 7.999F, 1.0F, 14.0F, 4.0F);
      wallRight.addBox(0.0F, -14.0F, 28.001F, 1.0F, 14.0F, 4.0F);
      walls.addChild(wallRight);
      EasyMeshBuilder wallTop = new EasyMeshBuilder("beamsTop", 3, 32);
      wallTop.setRotationPoint(0.0F, -25.0F, 0.0F);
      wallTop.setRotationAngles((float) (Math.PI / 2), (float) (Math.PI / 2), 0.0F);
      wallTop.addBox(
         -32.0F,
         -axleLength / 2.0F + 2.0F + Mth.sqrt(2.0F) * 7.0F,
         (float)(Math.sqrt(2.0) * 7.0 - 1.0),
         4.0F,
         31.0F - Mth.sqrt(2.0F) * 14.0F,
         1.0F
      );
      wallTop.addBox(
         -12.0F,
         -axleLength / 2.0F + 2.0F + Mth.sqrt(2.0F) * 7.0F,
         (float)(Math.sqrt(2.0) * 7.0 - 1.0),
         4.0F,
         31.0F - Mth.sqrt(2.0F) * 14.0F,
         1.0F
      );
      wallTop.addBox(
         8.0F,
         -axleLength / 2.0F + 2.0F + Mth.sqrt(2.0F) * 7.0F,
         (float)(Math.sqrt(2.0) * 7.0 - 1.0),
         4.0F,
         31.0F - Mth.sqrt(2.0F) * 14.0F,
         1.0F
      );
      wallTop.addBox(
         28.0F,
         -axleLength / 2.0F + 2.0F + Mth.sqrt(2.0F) * 7.0F,
         (float)(Math.sqrt(2.0) * 7.0 - 1.0),
         4.0F,
         31.0F - Mth.sqrt(2.0F) * 14.0F,
         1.0F
      );
      walls.addChild(wallTop);
      EasyMeshBuilder body = CartModel.createBody(rimLength);
      body.addChild(axle0);
      body.addChild(axle1);
      body.addChild(base);
      body.addChild(walls);
      body.build(def.getRoot());
      return LayerDefinition.create(def, 64, 64);
   }

   public static LayerDefinition createChestLayer() {
      MeshDefinition def = new MeshDefinition();
      EasyMeshBuilder backChest = new EasyMeshBuilder("chest0", 0, 0);
      backChest.setRotationPoint(0.0F, 0.0F, -31.0F);
      createChest(backChest);
      backChest.build(def.getRoot());
      EasyMeshBuilder middleChest = new EasyMeshBuilder("chest1", 0, 0);
      middleChest.setRotationPoint(0.0F, 0.0F, -15.0F);
      createChest(middleChest);
      middleChest.build(def.getRoot());
      EasyMeshBuilder frontChest = new EasyMeshBuilder("chest2", 0, 0);
      frontChest.setRotationPoint(0.0F, 0.0F, 1.0F);
      createChest(frontChest);
      frontChest.build(def.getRoot());
      return LayerDefinition.create(def, 128, 64);
   }

   private static void createChest(EasyMeshBuilder root) {
      EasyMeshBuilder chestBottom = new EasyMeshBuilder("chestBottom", 0, 19);
      chestBottom.setRotationPoint(13.0F, -2.0F, 0.0F);
      chestBottom.setRotationAngles((float) Math.PI, (float) Math.PI, 0.0F);
      chestBottom.addBox(0.0F, 0.0F, 0.0F, 26.0F, 10.0F, 14.0F);
      root.addChild(chestBottom);
      EasyMeshBuilder chestLid = new EasyMeshBuilder("chestLid", 0, 0);
      chestLid.setRotationPoint(13.0F, -11.0F, 0.0F);
      chestLid.setRotationAngles((float) Math.PI, (float) Math.PI, 0.0F);
      chestLid.addBox(0.0F, 0.0F, 0.0F, 26.0F, 5.0F, 14.0F);
      root.addChild(chestLid);
      EasyMeshBuilder chestLock = new EasyMeshBuilder("chestLock", 0, 0);
      chestLock.setRotationPoint(14.0F, 3.0F, 14.0F);
      chestLock.setRotationAngles((float) Math.PI, (float) Math.PI, 0.0F);
      chestLock.addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 1.0F);
      chestLid.addChild(chestLock);
   }
}
