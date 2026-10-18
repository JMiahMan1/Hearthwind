package net.smitherz.tooltip;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemAttributeModifiers.Entry;
import net.smitherz.init.ConfigInit;

@Environment(EnvType.CLIENT)
public class SmitherTooltipComponent implements ClientTooltipComponent {
   public static final Identifier TEXTURE = Identifier.parse("smitherz:textures/gui/gem_slots.png");
   private final net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> inventory;
   private final int gemSlotSize;

   public SmitherTooltipComponent(SmitherTooltipData data) {
      this.inventory = data.getInventory();
      this.gemSlotSize = data.getGemSlotSize();
   }

   public int getHeight(Font font) {
      if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)) {
         return 33 + this.getTooltipText().size() * 9;
      } else {
         return !ConfigInit.CONFIG.showHoldShiftInfo ? 20 : 34;
      }
   }

   public int getWidth(Font textRenderer) {
      if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)) {
         int minTextWidth = 0;

         for (Component text : this.getTooltipText()) {
            if (minTextWidth < textRenderer.width(text) + 12) {
               minTextWidth = textRenderer.width(text) + 12;
            }
         }

         return Math.max(this.gemSlotSize * 18, minTextWidth);
      } else {
         return this.gemSlotSize * 18;
      }
   }

   public void extractImage(Font textRenderer, int x, int y, int tooltipWidth, int tooltipHeight, GuiGraphicsExtractor context) {
      boolean shiftKeyPressed = InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340);
      if (ConfigInit.CONFIG.showHoldShiftInfo && !shiftKeyPressed) {
         context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 20, 0.0F, 18.0F, 9, 10, 128, 128);
         context.text(textRenderer, Component.translatable("item.modifiers.shift"), x + 12, y + 22, 0, true);
      }

      if (shiftKeyPressed) {
         context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 20, 0.0F, 18.0F, 9, 10, 128, 128);
         context.text(textRenderer, Component.translatable("item.modifiers.pressed_shift"), x + 12, y + 22, 0, true);
         List<net.minecraft.network.chat.Component> list = this.getTooltipText();
         if (!list.isEmpty()) {
            int o = 33;

            for (Component text : list) {
               context.text(textRenderer, text, x + 12, y + o, 0, true);
               o += 9;
            }
         }
      }

      if (this.gemSlotSize > 0) {
         for (int i = 0; i < this.gemSlotSize; i++) {
            this.drawSlot(x + i * 18, y, i, context, textRenderer);
         }
      }
   }

   private List<net.minecraft.network.chat.Component> getTooltipText() {
      ArrayList<net.minecraft.network.chat.Component> list = Lists.newArrayList();
      Map<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>, List<Object>> map = new HashMap<>();

      for (ItemStack itemStack : this.inventory) {
         if (!itemStack.isEmpty()) {
            List<net.minecraft.world.item.component.ItemAttributeModifiers.Entry> entries = itemStack.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers();
            if (!entries.isEmpty()) {
               for (Entry entry : entries) {
                  AttributeModifier entityAttributeModifier = entry.modifier();
                  double d = entityAttributeModifier.amount();
                  double e = entityAttributeModifier.operation() != Operation.ADD_MULTIPLIED_BASE && entityAttributeModifier.operation() != Operation.ADD_MULTIPLIED_TOTAL
                     ? (entry.attribute().equals(Attributes.KNOCKBACK_RESISTANCE) ? d * 10.0 : d)
                     : d * 100.0;
                  if (map.containsKey(entry.attribute())) {
                     double totalValue = e
                        + ((Boolean)map.get(entry.attribute()).get(2) ? (Double)map.get(entry.attribute()).get(1) : -(Double)map.get(entry.attribute()).get(1));
                     if (totalValue > 0.0) {
                        list.set(
                           (Integer)map.get(entry.attribute()).get(0),
                           Component.translatable(
                                 "attribute.modifier.plus." + entityAttributeModifier.operation().id(),
                                 new Object[]{
                                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(totalValue),
                                    Component.translatable(((Attribute)entry.attribute().value()).getDescriptionId())
                                 }
                              )
                              .withStyle(ChatFormatting.BLUE)
                        );
                     } else {
                        list.set(
                           (Integer)map.get(entry.attribute()).get(0),
                           Component.translatable(
                                 "attribute.modifier.take." + entityAttributeModifier.operation().id(),
                                 new Object[]{
                                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(totalValue *= -1.0),
                                    Component.translatable(((Attribute)entry.attribute().value()).getDescriptionId())
                                 }
                              )
                              .withStyle(ChatFormatting.RED)
                        );
                     }

                     map.put(entry.attribute(), List.of((Integer)map.get(entry.attribute()).get(0), totalValue, totalValue > 0.0));
                  } else {
                     if (d > 0.0) {
                        list.add(
                           Component.translatable(
                                 "attribute.modifier.plus." + entityAttributeModifier.operation().id(),
                                 new Object[]{
                                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e),
                                    Component.translatable(((Attribute)entry.attribute().value()).getDescriptionId())
                                 }
                              )
                              .withStyle(ChatFormatting.BLUE)
                        );
                     } else {
                        list.add(
                           Component.translatable(
                                 "attribute.modifier.take." + entityAttributeModifier.operation().id(),
                                 new Object[]{
                                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e *= -1.0),
                                    Component.translatable(((Attribute)entry.attribute().value()).getDescriptionId())
                                 }
                              )
                              .withStyle(ChatFormatting.RED)
                        );
                     }

                     map.put(entry.attribute(), List.of(list.size() - 1, e, d > 0.0));
                  }
               }
            }
         }
      }

      return list;
   }

   private void drawSlot(int x, int y, int index, GuiGraphicsExtractor context, Font textRenderer) {
      context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, 18, 18, 128, 128);
      if (this.inventory.size() > index) {
         ItemStack itemStack = (ItemStack)this.inventory.get(index);
         context.item(itemStack, x + 1, y + 1);
         context.itemDecorations(textRenderer, itemStack, x + 1, y + 1);
      }
   }
}
