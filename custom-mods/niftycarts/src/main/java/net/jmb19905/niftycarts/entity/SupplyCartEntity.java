package net.jmb19905.niftycarts.entity;

import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.NiftyCartsConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Entity.MoveFunction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SupplyCartEntity extends AbstractCargoCart {
   public SupplyCartEntity(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Entity> entityTypeIn, net.minecraft.world.level.Level worldIn
   ) {
      super(entityTypeIn, worldIn, 54);
   }

   @Override
   public Item getCartItem() {
      return NiftyCarts.SUPPLY_CART.get(this.getWoodType());
   }

   @Override
   public NiftyCartsConfig.CartConfig getConfig() {
      return NiftyCartsConfig.get().supplyCart;
   }

   public float getPassengersRidingOffsetY(EntityDimensions entityDimensions, float f) {
      return (entityDimensions.height() - 0.5625F) * f;
   }

   @NotNull
   protected Vec3 getPassengerAttachmentPoint(@NotNull Entity entity, @NotNull EntityDimensions entityDimensions, float f) {
      Vec3 forward = this.getLookAngle().scale(-0.68);
      return new Vec3(forward.x, this.getPassengersRidingOffsetY(entityDimensions, f) + forward.y, forward.z);
   }

   protected void positionRider(@NotNull Entity passenger, @NotNull MoveFunction moveFunction) {
      super.positionRider(passenger, moveFunction);
      if (this.hasPassenger(passenger)) {
         passenger.setYBodyRot(this.getYRot() + 180.0F);
         float f2 = Mth.wrapDegrees(passenger.getYRot() - this.getYRot() + 180.0F);
         float f1 = Mth.clamp(f2, -105.0F, 105.0F);
         passenger.yRotO += f1 - f2;
         passenger.setYRot(passenger.getYRot() + (f1 - f2));
         passenger.setYHeadRot(passenger.getYRot());
      }
   }

   @Override
   protected InteractionResult onInteractNotOpen(Player player, InteractionHand hand) {
      InteractionResult bannerResult = this.useBanner(player, hand);
      if (bannerResult.consumesAction()) {
         return bannerResult;
      } else if (this.isVehicle()) {
         return InteractionResult.PASS;
      } else if (!this.level().isClientSide()) {
         return (InteractionResult)(player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS);
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   @Override
   protected AbstractContainerMenu createMenuLootUnpacked(int i, Inventory inventory, Player player) {
      return ChestMenu.sixRows(i, inventory, this);
   }
}
