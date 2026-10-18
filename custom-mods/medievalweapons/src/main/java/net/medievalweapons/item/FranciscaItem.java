package net.medievalweapons.item;

import java.util.function.Supplier;
import net.medievalweapons.entity.FranciscaEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class FranciscaItem extends Item implements ProjectileItem {
   private final ToolMaterial toolMaterial;
   private final Supplier<net.minecraft.world.entity.EntityType<FranciscaEntity>> typeSupplier;
   private net.minecraft.world.entity.EntityType<FranciscaEntity> cachedType = null;

   public FranciscaItem(
      ToolMaterial toolMaterial,
      Supplier<net.minecraft.world.entity.EntityType<FranciscaEntity>> typeSupplier,
      net.minecraft.world.item.Item.Properties settings
   ) {
      super(settings);
      this.toolMaterial = toolMaterial;
      this.typeSupplier = typeSupplier;
   }

   public ToolMaterial getToolMaterial() {
      return this.toolMaterial;
   }

   public net.minecraft.world.entity.EntityType<FranciscaEntity> getType() {
      if (this.cachedType == null) {
         this.cachedType = this.typeSupplier.get();
      }

      return this.cachedType;
   }

   public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
      if (user instanceof Player playerEntity) {
         int i = this.getUseDuration(stack, user) - remainingUseTicks;
         if (i >= 10) {
            if (!world.isClientSide()) {
               stack.hurtAndBreak(1, playerEntity, user.getUsedItemHand());
               FranciscaEntity franciscaEntity = new FranciscaEntity(world, playerEntity, stack);
               franciscaEntity.shootFromRotation(playerEntity, playerEntity.getXRot(), playerEntity.getYRot(), 0.0F, 1.5F, 1.0F);
               if (playerEntity.isCreative()) {
                  franciscaEntity.pickup = Pickup.CREATIVE_ONLY;
               }

               world.addFreshEntity(franciscaEntity);
               world.playSound(null, franciscaEntity, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
               if (!playerEntity.isCreative()) {
                  playerEntity.getInventory().removeItem(stack);
               }
            }

            playerEntity.awardStat(Stats.ITEM_USED.get(this));
            return true;
         }
      }

      return false;
   }

   public InteractionResult use(net.minecraft.world.level.Level world, net.minecraft.world.entity.player.Player user, net.minecraft.world.InteractionHand hand) {
      ItemStack itemStack = user.getItemInHand(hand);
      if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
         return InteractionResult.FAIL;
      } else {
         user.startUsingItem(hand);
         return InteractionResult.CONSUME;
      }
   }

   public ItemUseAnimation getUseAnimation(ItemStack stack) {
      return ItemUseAnimation.SPEAR;
   }

   public int getUseDuration(ItemStack stack, LivingEntity user) {
      return 72000;
   }

   public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
      FranciscaEntity franciscaEntity = new FranciscaEntity(world, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
      franciscaEntity.pickup = Pickup.ALLOWED;
      return franciscaEntity;
   }

   public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      super.hurtEnemy(stack, target, attacker);
   }

   public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
   }
}
