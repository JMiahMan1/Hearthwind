package net.smitherz.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smitherz.util.UpgradeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.smitherz.init.ConfigInit;
import net.smitherz.tooltip.SmitherTooltipData;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {Item.class},
   priority = 1001
)
public class ItemMixin {
   @Inject(
      method = {"onCraftedBy"},
      at = {@At("TAIL")}
   )
   private void onCraftByPlayerMixin(ItemStack stack, Player player, CallbackInfo info) {
      if (!stack.isEmpty()) {
         UpgradeHelper.setGemSlots(stack, -1);
      }
   }

   @Inject(
      method = {"onCraftedPostProcess"},
      at = {@At("TAIL")}
   )
   private void onCraftMixin(ItemStack stack, Level world, CallbackInfo info) {
      if (!world.isClientSide() && !stack.isEmpty()) {
         UpgradeHelper.setGemSlots(stack, -1);
      }
   }

   @Inject(
      method = {"getTooltipImage"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void tooltipImageMixin(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> info) {
      if (UpgradeHelper.isUpgradeable(stack)) {
         int gemSlotSize = UpgradeHelper.getGemSlotSize(stack);
         if (gemSlotSize > 0) {
            NonNullList<ItemStack> inventory = NonNullList.create();
            UpgradeHelper.getGemStacks(stack).forEach(inventory::add);
            info.setReturnValue(Optional.of(new SmitherTooltipData(inventory, gemSlotSize)));
         }
      }
   }

   @Inject(
      method = {"appendHoverText"},
      at = {@At("TAIL")}
   )
   private void appendHoverTextMixin(
      ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type, CallbackInfo info
   ) {
      if (UpgradeHelper.isUpgradeable(stack) && ConfigInit.CONFIG.showZeroSlotsInfo && UpgradeHelper.getGemSlotSize(stack) <= 0) {
         tooltip.accept(Component.translatable("item.smitherz.zero_slots"));
      }
   }
}
