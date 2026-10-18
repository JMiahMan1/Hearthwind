package net.smitherz.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.smitherz.item.Gem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin({ItemStack.class})
public class ItemStackClientMixin {
   @WrapOperation(
      method = {"lambda$addAttributeTooltips$0"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"
      )}
   )
   private net.minecraft.network.chat.MutableComponent method_57370Mixin(String key, Operation<net.minecraft.network.chat.MutableComponent> original) {
      return ((ItemStack)(Object)this).getItem() instanceof Gem
         ? (MutableComponent)original.call(new Object[]{"item.modifiers.linked"})
         : (MutableComponent)original.call(new Object[]{key});
   }
}
