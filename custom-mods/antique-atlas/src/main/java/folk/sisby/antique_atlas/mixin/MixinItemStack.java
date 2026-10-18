package folk.sisby.antique_atlas.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ItemStack.class})
public class MixinItemStack {
   @ModifyReturnValue(
      method = {"use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"},
      at = {@At("RETURN")}
   )
   protected net.minecraft.world.InteractionResult openAtlasWithItem(
      net.minecraft.world.InteractionResult original,
      net.minecraft.world.level.Level world,
      net.minecraft.world.entity.player.Player user,
      net.minecraft.world.InteractionHand hand
   ) {
      return world.isClientSide()
            && original == InteractionResult.PASS
            && AntiqueAtlas.isHandheldAtlas(user.getItemInHand(hand))
            && AntiqueAtlas.openAtlasScreen() != null
         ? InteractionResult.SUCCESS
         : original;
   }
}
