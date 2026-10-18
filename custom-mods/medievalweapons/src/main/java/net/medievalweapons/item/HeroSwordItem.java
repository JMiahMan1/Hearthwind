package net.medievalweapons.item;

import java.util.function.Consumer;
import net.medievalweapons.MedievalMain;
import net.medievalweapons.init.CompatInit;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item.Properties;

public class HeroSwordItem extends Item {
   public static final Identifier ATTACK_BONUS_MODIFIER_ID = MedievalMain.identifierOf("double_handed_attack_bonus");

   public HeroSwordItem(ToolMaterial toolMaterial, Properties settings) {
      super(settings);
   }

   public void appendHoverText(
      net.minecraft.world.item.ItemStack stack,
      net.minecraft.world.item.Item.TooltipContext context,
      TooltipDisplay display,
      Consumer<Component> tooltip,
      net.minecraft.world.item.TooltipFlag type
   ) {
      super.appendHoverText(stack, context, display, tooltip, type);
      if (!CompatInit.isBetterCombatLoaded) {
         tooltip.accept(Component.translatable("item.medievalweapons.double_handed.tooltip"));
      }
   }

   public ItemUseAnimation getUseAnimation(ItemStack stack) {
      return ItemUseAnimation.BLOCK;
   }

   public InteractionResult use(net.minecraft.world.level.Level world, net.minecraft.world.entity.player.Player user, net.minecraft.world.InteractionHand hand) {
      user.startUsingItem(hand);
      return InteractionResult.CONSUME;
   }
}
