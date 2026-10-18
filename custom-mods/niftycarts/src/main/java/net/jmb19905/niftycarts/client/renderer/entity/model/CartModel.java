package net.jmb19905.niftycarts.client.renderer.entity.model;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.client.renderer.entity.CartRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class CartModel<T extends CartRenderState> extends net.minecraft.client.model.EntityModel<T> {
   protected final ModelPart body;
   private final BannerModel bannerModel;
   private final BannerFlagModel flagModel;
   private final ModelPart flag;
   private final ModelPart pole;
   private final ModelPart bar;
   @Nullable
   protected final ModelPart rotating;
   protected final Set<net.minecraft.client.model.geom.ModelPart> leftWheels = new HashSet<>();
   protected final Set<net.minecraft.client.model.geom.ModelPart> rightWheels = new HashSet<>();

   protected CartModel(ModelPart root, BannerModel bannerModel, BannerFlagModel flagModel) {
      this(root, 1, bannerModel, flagModel);
   }

   protected CartModel(ModelPart root, int axleCount, BannerModel bannerModel, BannerFlagModel flagModel) {
      super(root);
      this.body = root.getChild("body");
      this.bannerModel = bannerModel;
      this.flagModel = flagModel;
      if (root.hasChild("rotating")) {
         this.rotating = root.getChild("rotating");
      } else {
         this.rotating = null;
      }

      for (int i = 0; i < axleCount; i++) {
         this.leftWheels.add(root.getChild("leftWheel_" + i));
         this.rightWheels.add(root.getChild("rightWheel_" + i));
      }

      this.pole = bannerModel.getChildPart("pole");
      this.bar = bannerModel.getChildPart("bar");
      this.flag = flagModel.getChildPart("flag");
   }

   public ModelPart getBody() {
      return this.body;
   }

   public BannerModel getBannerModel() {
      return this.bannerModel;
   }

   public BannerFlagModel getFlagModel() {
      return this.flagModel;
   }

   public void setupAnim(T state) {
      if (this.rotating == null) {
         this.body.xRot = (float)Math.toRadians(state.pitch);
      } else {
         this.rotating.xRot = (float)Math.toRadians(state.pitch);
      }

      this.rightWheels.forEach(part -> part.xRot = (float)(state.wheelRotation0 + state.wheelRotationInc0 * state.delta));
      this.leftWheels.forEach(part -> part.xRot = (float)(state.wheelRotation1 + state.wheelRotationInc1 * state.delta));
      float time = state.timeSinceHit - state.delta;
      float rot;
      if (time > 0.0F) {
         float damage = Math.max(state.damage - state.delta, 0.0F) * 0.5F;
         rot = (float)Math.toRadians(Mth.sin(time) * time * damage / 40.0F * -state.forward);
      } else {
         rot = 0.0F;
      }

      this.rightWheels.forEach(part -> part.zRot = rot);
      this.leftWheels.forEach(part -> part.zRot = rot);
      this.pole.zRot = -0.3F;
      this.pole.x = 17.48F;
      this.pole.y = 12.43F;
      this.bar.x = -4.0F;
      this.bar.y = 16.01F;
      this.bar.z = 0.01F;
      this.flag.x = -4.0F;
      this.flag.y = -26.0F;
      this.flag.z = 1.5F;
      float k = (Math.floorMod((int)(state.x * 7.0 + state.y * 9.0 + state.z * 13.0 + state.ageInTicks), 100) + state.delta)
         / 100.0F;
      this.flag.xRot = 0.01F * Mth.cos((float) (Math.PI * 2) * k) * (float) Math.PI;
   }

   protected static EasyMeshBuilder createWheel(
      String name, float xOffset, float yOffset, float rimLength, float axleDist, float boxOffset, int axle, int maxAxle
   ) {
      float angle = (float) (Math.PI / 8);
      float wheelRadius = rimLength / 2.0F / (Mth.sin(angle) / Mth.cos(angle));
      EasyMeshBuilder wheel = new EasyMeshBuilder(name, 46, 60);
      float d = axle - (maxAxle - 1) / 2.0F;
      wheel.setRotationPoint(xOffset, yOffset - wheelRadius, 1.0F + axleDist * d);
      wheel.addBox(boxOffset, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F);

      for (int i = 0; i < 8; i++) {
         EasyMeshBuilder rim = new EasyMeshBuilder("rim_" + i, 58, 64 - ((int)rimLength + 1));
         float epsilon = 0.001F * i % 2.0F;
         rim.addBox(boxOffset + epsilon, -rimLength / 2.0F, wheelRadius - 1.0F, 2.0F, rimLength, 1.0F);
         rim.xRot = i * (float) Math.PI / 4.0F;
         wheel.addChild(rim);
         EasyMeshBuilder spoke = new EasyMeshBuilder("spoke_" + i, 54, 64 - Mth.ceil(wheelRadius - 1.0F));
         spoke.addBox(0.5F + boxOffset, 1.0F, -0.5F, 1.0F, wheelRadius - 2.0F, 1.0F);
         spoke.xRot = i * (float) Math.PI / 4.0F;
         wheel.addChild(spoke);
      }

      return wheel;
   }

   public static MeshDefinition createDefinition(float rimLength, float axleLength) {
      return createDefinition(rimLength, axleLength, 1, 0.0F);
   }

   public static MeshDefinition createDefinition(float rimLength, float axleLength, int axleCount, float axleDist) {
      MeshDefinition def = new MeshDefinition();
      float xOffset = axleLength / 2.0F + 2.0F;

      for (int k = 0; k < axleCount; k++) {
         EasyMeshBuilder leftWheel = createWheel("leftWheel_" + k, xOffset, 0.0F, rimLength, axleDist, -2.0F, k, axleCount);
         leftWheel.build(def.getRoot());
         EasyMeshBuilder rightWheel = createWheel("rightWheel_" + k, -xOffset, 0.0F, rimLength, axleDist, 0.0F, k, axleCount);
         rightWheel.build(def.getRoot());
      }

      return def;
   }

   public static EasyMeshBuilder createBody(int rimLength) {
      EasyMeshBuilder body = new EasyMeshBuilder("body");
      float angle = (float) (Math.PI / 8);
      float wheelRadius = rimLength / 2.0F / (Mth.sin(angle) / Mth.cos(angle));
      body.setRotationPoint(0.0F, -wheelRadius, 1.0F);
      return body;
   }
}
