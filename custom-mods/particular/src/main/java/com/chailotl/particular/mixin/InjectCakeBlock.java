package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CakeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({CakeBlock.class})
public class InjectCakeBlock {
   @Inject(
      at = {@At("TAIL")},
      method = {"tryEat"}
   )
   private static void makeSounds(
      net.minecraft.world.level.LevelAccessor world,
      net.minecraft.core.BlockPos pos,
      net.minecraft.world.level.block.state.BlockState state,
      net.minecraft.world.entity.player.Player player,
      CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir
   ) {
      if (Main.CONFIG.cakeEatingParticles()) {
         ItemStack cake = Items.CAKE.getDefaultInstance();
         net.minecraft.world.item.component.Consumable consumable = cake.get(net.minecraft.core.component.DataComponents.CONSUMABLE);
         if (consumable != null) {
            player.playSound(consumable.sound().value(), 1.0F, 1.0F);
         }
         ((InvokerLivingEntity)player).spawnParticles(cake, 5);
      }
   }
}
