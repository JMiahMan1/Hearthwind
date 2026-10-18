package net.smitherz.item;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemAttributeModifiers.Builder;
import org.jetbrains.annotations.Nullable;

public class Gem extends Item {
   private final Map<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier> entityAttributeModifiers;
   private final float linkChance;
   private final float linkBreakChance;
   private final float unlinkChance;
   @Nullable
   private final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> restrictionTag;

   public Gem(
      net.minecraft.world.item.Item.Properties settings,
      Map<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier> entityAttributeModifiers,
      float linkChance,
      float linkBreakChance,
      float unlinkChance,
      @Nullable net.minecraft.tags.TagKey<net.minecraft.world.item.Item> restrictionTag
   ) {
      super(withAttributes(settings, entityAttributeModifiers));
      this.entityAttributeModifiers = entityAttributeModifiers;
      this.linkChance = linkChance;
      this.linkBreakChance = linkBreakChance;
      this.restrictionTag = restrictionTag;
      this.unlinkChance = unlinkChance;
   }

   private static net.minecraft.world.item.Item.Properties withAttributes(
      net.minecraft.world.item.Item.Properties settings,
      java.util.Map<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier> modifiers
   ) {
      net.minecraft.world.item.component.ItemAttributeModifiers.Builder builder = net.minecraft.world.item.component.ItemAttributeModifiers.builder();
      for (java.util.Map.Entry<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier> entry : modifiers
         .entrySet()) {
         builder.add(entry.getKey(), entry.getValue(), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);
      }

      return settings.attributes(builder.build());
   }

   public float getLinkChance() {
      return this.linkChance;
   }

   public float getLinkBreakChance() {
      return this.linkBreakChance;
   }

   public float getUnlinkChance() {
      return this.unlinkChance;
   }

   public boolean canLinkToItemStack(ItemStack stack) {
      return this.restrictionTag == null || stack.is(this.restrictionTag);
   }

   public Map<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier> getEntityAttributeModifiers() {
      return this.entityAttributeModifiers;
   }

   public void appendHoverText(
      net.minecraft.world.item.ItemStack stack,
      net.minecraft.world.item.Item.TooltipContext context,
      net.minecraft.world.item.component.TooltipDisplay display,
      java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip,
      net.minecraft.world.item.TooltipFlag type
   ) {
      if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)) {
         if (this.getLinkBreakChance() < 1.0E-5F) {
            tooltip.accept(
               Component.translatable("item.smitherz.gem.tooltip", new Object[]{String.format("%.1f", this.getLinkChance() * 100.0F)})
                  .withStyle(ChatFormatting.GRAY)
            );
         } else {
            tooltip.accept(
               Component.translatable(
                     "item.smitherz.gem.tooltip_2",
                     new Object[]{String.format("%.1f", this.getLinkChance() * 100.0F), String.format("%.1f", this.getLinkBreakChance() * 100.0F)}
                  )
                  .withStyle(ChatFormatting.GRAY)
            );
         }

         tooltip.accept(
            Component.translatable("item.smitherz.gem.tooltip_3", new Object[]{String.format("%.1f", this.getUnlinkChance() * 100.0F)})
               .withStyle(ChatFormatting.GRAY)
         );
      }

      super.appendHoverText(stack, context, display, tooltip, type);
   }
}
