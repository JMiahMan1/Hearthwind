package net.jmb19905.niftycarts.util;

import net.jmb19905.niftycarts.entity.AbstractDrawnEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public final class CartWheel {
   private float rotation;
   private float rotationIncrement;
   private final float offsetX;
   private final float offsetZ;
   private final float circumference;
   private double posX;
   private double posZ;
   private double prevPosX;
   private double prevPosZ;
   private final AbstractDrawnEntity cart;

   public CartWheel(AbstractDrawnEntity cartIn, float offsetXIn, float offsetZIn, float circumferenceIn) {
      this.cart = cartIn;
      this.offsetX = offsetXIn;
      this.offsetZ = offsetZIn;
      this.circumference = circumferenceIn;
      this.posX = this.prevPosX = cartIn.getX();
      this.posZ = this.prevPosZ = cartIn.getZ();
   }

   public CartWheel(AbstractDrawnEntity cartIn, float offsetX) {
      this(cartIn, offsetX, 0.0F, (float) (Math.PI * 5.0 / 4.0));
   }

   public void tick() {
      this.rotation = this.rotation + this.rotationIncrement;
      this.prevPosX = this.posX;
      this.prevPosZ = this.posZ;
      float yaw = (float)Math.toRadians(this.cart.getYRot());
      float nx = -Mth.sin(yaw);
      float nz = Mth.cos(yaw);
      this.posX = this.cart.getX() + nx * this.offsetZ - nz * this.offsetX;
      this.posZ = this.cart.getZ() + nz * this.offsetZ + nx * this.offsetX;
      double dx = this.posX - this.prevPosX;
      double dz = this.posZ - this.prevPosZ;
      float distanceTravelled = (float)Math.sqrt(dx * dx + dz * dz);
      double dxNormalized = dx / distanceTravelled;
      double dzNormalized = dz / distanceTravelled;
      float travelledForward = Mth.sign(dxNormalized * nx + dzNormalized * nz);
      if (distanceTravelled > 0.2) {
         BlockPos blockpos = new BlockPos(Mth.floor(this.posX), Mth.floor(this.cart.getY() - 0.2F), Mth.floor(this.posZ));
         BlockState blockstate = this.cart.level().getBlockState(blockpos);
         if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            this.cart
               .level()
               .addParticle(
                  new BlockParticleOption(ParticleTypes.BLOCK, blockstate), this.posX, this.cart.getY(), this.posZ, dx, distanceTravelled, dz
               );
         }
      }

      this.rotationIncrement = travelledForward * distanceTravelled * this.circumference * 0.2F;
   }

   public void clearIncrement() {
      this.rotationIncrement = 0.0F;
   }

   public float getRotation() {
      return this.rotation;
   }

   public float getRotationIncrement() {
      return this.rotationIncrement;
   }
}
