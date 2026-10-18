package net.medievalweapons.item;

import java.util.Map;
import net.medievalweapons.MedievalMain;
import net.medievalweapons.init.ParticleInit;
import net.medievalweapons.init.SoundInit;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class HealingStaffItem extends Item {
   private static final Map<net.minecraft.world.item.ToolMaterial, Integer> ADDITION = Map.of(
      ToolMaterial.WOOD, 1, ToolMaterial.STONE, 2, ToolMaterial.IRON, 2, ToolMaterial.GOLD, 3, ToolMaterial.DIAMOND, 4, ToolMaterial.NETHERITE, 5
   );
   public static final Identifier ATTACK_BONUS_MODIFIER_ID = MedievalMain.identifierOf("range_attack_bonus");
   private final int addition;

   public HealingStaffItem(ToolMaterial toolMaterial, int addition, Properties settings) {
      super(settings);
      if (addition == -1) {
         this.addition = ADDITION.get(toolMaterial);
      } else {
         this.addition = addition;
      }
   }

   public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
      if (user instanceof Player playerEntity) {
         int i = this.getUseDuration(stack, user) - remainingUseTicks;
         if (i >= 30) {
            if (!world.isClientSide()) {
               stack.hurtAndBreak(3, playerEntity, user.getUsedItemHand());
               world.playSound(null, playerEntity.blockPosition(), SoundInit.MAGIC_SHOT_EVENT, SoundSource.PLAYERS, 0.9F, 1.0F);
               HitResult hitResult = playerEntity.pick(8.0 + this.addition, 1.0F, false);
               if (hitResult.getType().equals(Type.BLOCK)) {
                  AreaEffectCloud areaEffectCloudEntity = new AreaEffectCloud(
                     world, hitResult.getLocation().x, hitResult.getLocation().y + 0.1, hitResult.getLocation().z
                  );
                  areaEffectCloudEntity.setOwner(user);
                  areaEffectCloudEntity.setCustomParticle(ParticleInit.HEALING_AURA_PARTICLE);
                  areaEffectCloudEntity.setRadius(this.addition);
                  areaEffectCloudEntity.setDuration(this.addition * 100);
                  areaEffectCloudEntity.setRadiusPerTick(-(12.0F - areaEffectCloudEntity.getRadius()) / 500.0F);
                  areaEffectCloudEntity.addEffect(new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 0));
                  world.playSound(
                     null,
                     hitResult.getLocation().x,
                     hitResult.getLocation().y + 0.1,
                     hitResult.getLocation().z,
                     SoundInit.MAGIC_HEAL_AURA_EVENT,
                     SoundSource.NEUTRAL,
                     0.9F,
                     1.0F
                  );
                  world.addFreshEntity(areaEffectCloudEntity);
               }
            }

            playerEntity.getCooldowns().addCooldown(stack, 100 + this.addition * 20);
            return true;
         }
      }

      return false;
   }

   public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
      int i = this.getUseDuration(stack, user) - remainingUseTicks;
      if (user instanceof Player playerEntity && i >= 30) {
         if (world.isClientSide()) {
            HitResult hitResult = playerEntity.pick(8.0 + this.addition, 0.0F, false);
            if (hitResult.getType().equals(Type.BLOCK)) {
               playerEntity.level()
                  .addParticle(
                     ParticleInit.HEALING_AURA_PARTICLE,
                     hitResult.getLocation().x - 0.2 + world.getRandom().nextDouble() * 0.4,
                     hitResult.getLocation().y - 0.1 + world.getRandom().nextDouble() * 0.2,
                     hitResult.getLocation().z - 0.2 + world.getRandom().nextDouble() * 0.4,
                     0.0,
                     0.0,
                     0.0
                  );
            }

            for (int u = 0; u < 3; u++) {
               playerEntity.level()
                  .addParticle(
                     ParticleInit.HEALING_AURA_PARTICLE,
                     playerEntity.getRandomX(0.5),
                     playerEntity.getRandomY(),
                     playerEntity.getRandomZ(0.5),
                     0.0,
                     0.0,
                     0.0
                  );
            }
         } else if (i % 80 == 0 && i < 241) {
            stack.hurtAndBreak(1, playerEntity, user.getUsedItemHand());
            playerEntity.addEffect(new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 0));
         }
      }
   }

   public InteractionResult use(
      net.minecraft.world.level.Level world, net.minecraft.world.entity.player.Player user, net.minecraft.world.InteractionHand hand
   ) {
      ItemStack itemStack = user.getItemInHand(hand);
      if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
         return InteractionResult.FAIL;
      } else {
         user.startUsingItem(hand);
         return InteractionResult.CONSUME;
      }
   }

   public ItemUseAnimation getUseAnimation(ItemStack stack) {
      return ItemUseAnimation.BLOCK;
   }

   public int getUseDuration(ItemStack stack, LivingEntity user) {
      return 72000;
   }
}
