package net.smitherz.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.smitherz.init.TagInit;

public class Hammer extends Item {
   private final float chance;

   public Hammer(Properties settings, float chance) {
      super(settings);
      this.chance = chance;
   }

   public float getBonusChance() {
      return this.chance / 100.0F;
   }

   public void appendHoverText(
      net.minecraft.world.item.ItemStack stack,
      net.minecraft.world.item.Item.TooltipContext context,
      net.minecraft.world.item.component.TooltipDisplay display,
      java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip,
      net.minecraft.world.item.TooltipFlag type
   ) {
      if (stack.is(TagInit.BONUS_ITEMS)) {
         tooltip.accept(Component.translatable("item.smitherz.link_bonus", new Object[]{this.chance}).withStyle(ChatFormatting.BLUE));
      } else if (stack.is(TagInit.EXTRACTION_ITEMS)) {
         tooltip.accept(Component.translatable("item.smitherz.extraction_bonus", new Object[]{this.chance}).withStyle(ChatFormatting.BLUE));
      }

      super.appendHoverText(stack, context, display, tooltip, type);
   }
}
