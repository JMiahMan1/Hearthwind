package net.jmb19905.niftycarts.client.renderer.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.client.renderer.entity.ReaperRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class ReaperModel extends CartModel<ReaperRenderState> {
   private final ModelPart frame;
   private final ModelPart foldedFrame;

   public ReaperModel(ModelPart root, BannerModel bannerModel, BannerFlagModel flagModel) {
      super(root, bannerModel, flagModel);
      this.frame = root.getChild("body").getChild("frame");
      this.foldedFrame = root.getChild("body").getChild("foldedFrame");
   }

   public void setupAnim(ReaperRenderState state) {
      super.setupAnim(state);
      if (state.folded) {
         this.frame.visible = false;
         this.foldedFrame.visible = true;
      } else {
         this.frame.visible = true;
         this.foldedFrame.visible = false;
      }
   }

   private static EasyMeshBuilder createFrame() {
      EasyMeshBuilder frame = new EasyMeshBuilder("frame", 0, 0);
      frame.addBox(14.0F, 5.0F, -1.0F, 26.0F, 2.0F, 2.0F);
      frame.addBox(-3.99F, -10.5F, 3.0F, 44.0F, 2.0F, 2.0F);
      EasyMeshBuilder s = new EasyMeshBuilder("s", 0, 0);
      s.setRotationPoint(0.0F, -5.0F, -15.0F);
      s.yRot = (float) (Math.PI / 2);
      EasyMeshBuilder t = new EasyMeshBuilder("t", 0, 0);
      t.zRot = -0.26F;
      EasyMeshBuilder shaftConnector1 = new EasyMeshBuilder("shaftConnector1", 0, 0);
      shaftConnector1.addBox(-17.01F, 0.0F, 14.01F, 13.0F, 2.0F, 2.0F);
      EasyMeshBuilder shaftConnector2 = new EasyMeshBuilder("shaftConnector2", 0, 0);
      shaftConnector2.yRot = (float) (Math.PI / 2);
      shaftConnector2.addBox(-14.0F, 0.01F, -6.0F, 21.0F, 2.0F, 2.0F);
      t.addChild(shaftConnector1);
      t.addChild(shaftConnector2);
      s.addChild(t);
      frame.addChild(s);
      EasyMeshBuilder axleConnectors = new EasyMeshBuilder("axleConnectors", 32, 12);
      axleConnectors.setRotationPoint(14.0F, 10.0F, 0.0F);
      axleConnectors.xRot = -0.27F;
      axleConnectors.addBox(0.0F, -15.0F, 0.0F, 2.0F, 15.0F, 2.0F);
      axleConnectors.addBox(23.99F, -15.0F, 0.0F, 2.0F, 15.0F, 2.0F);
      EasyMeshBuilder blades = new EasyMeshBuilder("blades", 0, 32);
      blades.setRotationPoint(27.0F, 11.0F, 0.0F);
      blades.addBox(12.0F, -3.0F, -6.0F, 0.01F, 5.0F, 8.0F);
      blades.addBox(-12.0F, -3.0F, -6.0F, 0.01F, 5.0F, 8.0F);
      blades.addBox(-12.0F, 0.0F, -1.0F, 24.0F, 1.0F, 1.0F);

      for (int i = 0; i < 12; i++) {
         int offset = i * 2 - 11;
         blades.addBox(offset - 0.5F, 1.0F, -4.0F, 1.0F, 0.01F, 3.0F);
      }

      EasyMeshBuilder frameParts = new EasyMeshBuilder("frameParts");
      frameParts.setRotationPoint(0.0F, -5.0F, -1.0F);
      frameParts.addChild(axleConnectors);
      frameParts.addChild(blades);
      frame.addChild(frameParts);
      return frame;
   }

   private static EasyMeshBuilder createFoldedFrame() {
      EasyMeshBuilder frame = new EasyMeshBuilder("foldedFrame", 0, 0);
      frame.addBox(-3.99F, -10.5F, 3.0F, 18.0F, 2.0F, 2.0F);
      EasyMeshBuilder foldedFrame = new EasyMeshBuilder("foldedInFrame", 0, 0);
      foldedFrame.setRotationPoint(14.001F, -10.5F, 3.0F);
      foldedFrame.setRotationAngles(0.0F, (float) (Math.PI / 2), 0.0F);
      foldedFrame.addBox(-2.0F, 15.5F, 0.0F, 26.0F, 2.0F, 2.0F);
      foldedFrame.addBox(-1.99F, 0.0F, 0.0F, 26.0F, 2.0F, 2.0F);
      frame.addChild(foldedFrame);
      EasyMeshBuilder s = new EasyMeshBuilder("s", 0, 0);
      s.setRotationPoint(0.0F, -5.0F, -15.0F);
      s.yRot = (float) (Math.PI / 2);
      EasyMeshBuilder t = new EasyMeshBuilder("t", 0, 0);
      t.zRot = -0.26F;
      EasyMeshBuilder shaftConnector1 = new EasyMeshBuilder("shaftConnector1", 0, 0);
      shaftConnector1.addBox(-20.01F, 0.0F, 14.01F, 16.0F, 2.0F, 2.0F);
      EasyMeshBuilder shaftConnector2 = new EasyMeshBuilder("shaftConnector2", 0, 0);
      shaftConnector2.yRot = (float) (Math.PI / 2);
      shaftConnector2.addBox(-14.0F, 0.01F, -6.0F, 21.0F, 2.0F, 2.0F);
      t.addChild(shaftConnector1);
      t.addChild(shaftConnector2);
      s.addChild(t);
      frame.addChild(s);
      EasyMeshBuilder axleConnectors = new EasyMeshBuilder("axleConnectors", 32, 12);
      axleConnectors.setRotationPoint(14.0F, 10.0F, 4.0F);
      axleConnectors.addBox(-2.0F, -15.0F, 0.0F, 2.0F, 15.0F, 2.0F);
      axleConnectors.addBox(21.99F, -15.0F, 0.0F, 2.0F, 15.0F, 2.0F);
      EasyMeshBuilder blades = new EasyMeshBuilder("blades", 0, 32);
      blades.setRotationPoint(27.0F, 11.0F, 4.0F);
      blades.setRotationAngles((float) (-Math.PI / 2), 0.0F, 0.0F);
      blades.addBox(10.0F, -3.0F, -6.0F, 0.01F, 5.0F, 8.0F);
      blades.addBox(-14.0F, -3.0F, -6.0F, 0.01F, 5.0F, 8.0F);
      blades.addBox(-14.0F, 0.0F, -1.0F, 24.0F, 1.0F, 1.0F);

      for (int i = 0; i < 12; i++) {
         int offset = i * 2 - 13;
         blades.addBox(offset - 0.5F, 1.0F, -4.0F, 1.0F, 0.01F, 3.0F);
      }

      EasyMeshBuilder frameParts = new EasyMeshBuilder("frameParts");
      frameParts.setRotationPoint(10.0F, -5.0F, 17.0F);
      frameParts.setRotationAngles(0.0F, (float) (Math.PI / 2), 0.0F);
      frameParts.addChild(axleConnectors);
      frameParts.addChild(blades);
      frame.addChild(frameParts);
      return frame;
   }

   public static LayerDefinition createLayer() {
      MeshDefinition def = CartModel.createDefinition(7.0F, 20.0F);
      EasyMeshBuilder axle = new EasyMeshBuilder("axle", 0, 0);
      axle.addBox(-10.0F, -1.0F, -1.0F, 20.0F, 2.0F, 2.0F);
      EasyMeshBuilder frame = createFrame();
      EasyMeshBuilder foldedFrame = createFoldedFrame();
      EasyMeshBuilder seat = new EasyMeshBuilder("seat", 8, 12);
      seat.addBox(-4.0F, -6.0F, 1.0F, 8.0F, 2.0F, 8.0F);
      EasyMeshBuilder seatConnectors = new EasyMeshBuilder("seatConnectors", 0, 12);
      seatConnectors.setRotationPoint(0.0F, 0.0F, 3.0F);
      seatConnectors.xRot = (float) (-Math.PI / 8);
      seatConnectors.addBox(1.0F, -5.0F, -0.5F, 2.0F, 10.0F, 2.0F);
      seatConnectors.addBox(-3.0F, -5.0F, -0.5F, 2.0F, 10.0F, 2.0F);
      seat.addChild(seatConnectors);
      EasyMeshBuilder shaft = new EasyMeshBuilder("shaft", 8, 8);
      shaft.zRot = -0.07F;
      shaft.addBox(0.0F, 0.0F, -8.01F, 17.0F, 2.0F, 1.0F);
      shaft.addBox(0.0F, 0.0F, 7.01F, 17.0F, 2.0F, 1.0F);
      EasyMeshBuilder shaftConnector0 = new EasyMeshBuilder("shaftConnector0", 0, 27);
      shaftConnector0.zRot = -0.26F;
      shaftConnector0.addBox(-16.0F, 0.0F, -8.0F, 16.0F, 2.0F, 1.0F);
      shaftConnector0.addBox(-16.0F, 0.0F, 7.0F, 16.0F, 2.0F, 1.0F);
      EasyMeshBuilder shafts = new EasyMeshBuilder("shafts");
      shafts.setRotationPoint(0.0F, 0.0F, -14.0F);
      shafts.yRot = (float) (Math.PI / 2);
      shafts.addChild(shaft);
      shafts.addChild(shaftConnector0);
      float rimLength = 4.0F;
      float angle = (float) (Math.PI / 8);
      float wheelRadius = rimLength / 2.0F / (Mth.sin(angle) / Mth.cos(angle));
      EasyMeshBuilder parts = new EasyMeshBuilder("parts");
      parts.setRotationPoint(0.0F, -5.0F, -1.0F);
      parts.addChild(shafts);
      parts.addChild(seat);
      float radius = 3.5F / (Mth.sin(angle) / Mth.cos(angle));
      EasyMeshBuilder body = CartModel.createBody(9);
      body.setRotationPoint(0.0F, -radius, 1.0F);
      body.addChild(axle);
      body.addChild(frame);
      body.addChild(foldedFrame);
      body.addChild(parts);
      body.build(def.getRoot());
      return LayerDefinition.create(def, 64, 64);
   }
}
