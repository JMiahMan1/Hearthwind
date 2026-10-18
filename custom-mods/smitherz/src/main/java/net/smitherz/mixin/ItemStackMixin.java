package net.smitherz.mixin;

import java.util.function.BiConsumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers.Entry;
import net.smitherz.init.ItemInit;
import net.smitherz.item.component.GemComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({ItemStack.class})
public class ItemStackMixin {
   @Inject(
      method = {"forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"
      )},
      locals = LocalCapture.CAPTURE_FAILSOFT
   )
   private void applyAttributeModifiersMixin(
      net.minecraft.world.entity.EquipmentSlot slot,
      BiConsumer<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier> attributeModifierConsumer,
      CallbackInfo info,
      net.minecraft.world.item.component.ItemAttributeModifiers attributeModifiersComponent
   ) {
      this.applyGemAttributeModifier(attributeModifiersComponent, slot, null, attributeModifierConsumer);
   }

   @Unique
   private void applyGemAttributeModifier(
      @Nullable net.minecraft.world.item.component.ItemAttributeModifiers attributeModifiersComponent,
      @Nullable net.minecraft.world.entity.EquipmentSlot equipmentSlot,
      @Nullable net.minecraft.world.entity.EquipmentSlotGroup attributeModifierSlot,
      BiConsumer<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier> attributeModifierConsumer
   ) {
      ItemStack itemStack = (ItemStack)(Object)this;
      if (itemStack.get(ItemInit.GEMS) != null) {
         if (attributeModifiersComponent.modifiers().isEmpty()) {
            attributeModifiersComponent = itemStack.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
         }

         if (equipmentSlot != null && ((Entry)attributeModifiersComponent.modifiers().getFirst()).slot().test(equipmentSlot)
            || attributeModifierSlot != null && ((Entry)attributeModifiersComponent.modifiers().getFirst()).slot().equals(attributeModifierSlot)) {
            for (int i = 0; i < ((GemComponent)itemStack.get(ItemInit.GEMS)).gems().size(); i++) {
               ItemStack gem = (ItemStack)((GemComponent)itemStack.get(ItemInit.GEMS)).gems().get(i);
               gem.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).modifiers().forEach(entry -> attributeModifierConsumer.accept(entry.attribute(), entry.modifier()));
            }
         }
      }
   }
}
