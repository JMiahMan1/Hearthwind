package com.chailotl.particular.sushi_bar.owo.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.config.ui.component.ListOptionContainer;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent.OnChanged;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.ui.event.MouseDown;
import io.wispforest.owo.ui.event.MouseEnter;
import io.wispforest.owo.ui.event.MouseLeave;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

public class IdentifierListOptionContainer extends ListOptionContainer {
   public IdentifierListOptionContainer(Option option) {
      super(option);
   }

   protected void refreshOptions() {
      this.collapsibleChildren.clear();
      Class<?> listType = ReflectionUtils.getTypeArgument(this.backingOption.backingField().field().getGenericType(), 0);

      for (int i = 0; i < this.backingList.size(); i++) {
         FlowLayout container = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
         container.verticalAlignment(VerticalAlignment.CENTER);
         int optionIndex = i;
         LabelComponent label = UIComponents.label(TextOps.withFormatting("- ", new ChatFormatting[]{ChatFormatting.GRAY}));
         label.margins(Insets.left(10));
         if (!this.backingOption.detached()) {
            label.cursorStyle(CursorStyle.HAND);
            label.mouseEnter().subscribe((MouseEnter)() -> label.text(TextOps.withFormatting("x ", new ChatFormatting[]{ChatFormatting.GRAY})));
            label.mouseLeave().subscribe((MouseLeave)() -> label.text(TextOps.withFormatting("- ", new ChatFormatting[]{ChatFormatting.GRAY})));
            label.mouseDown().subscribe((MouseDown)(event, bl) -> {
               this.backingList.remove(optionIndex);
               this.refreshResetButton();
               this.refreshOptions();
               UISounds.playInteractionSound();
               return true;
            });
         }

         container.child(label);
         ConfigTextBox box = new ConfigTextBox();
         box.setValue(this.backingList.get(i).toString());
         box.moveCursorToStart(false);
         box.setBordered(false);
         box.margins(Insets.vertical(2));
         box.horizontalSizing(Sizing.fill(95));
         box.verticalSizing(Sizing.fixed(8));
         box.inputPredicate(s -> s.matches("[a-z0-9_.:\\-]*"));
         box.applyPredicate(s -> Identifier.tryParse(s) != null);
         box.valueParser(Identifier::parse);
         if (!this.backingOption.detached()) {
            box.onChanged().subscribe((OnChanged)s -> {
               if (box.isValid()) {
                  this.backingList.set(optionIndex, box.parsedValue());
                  this.refreshResetButton();
               }
            });
         } else {
            box.active = false;
         }

         container.child(box);
         this.collapsibleChildren.add(container);
      }

      this.contentLayout.clearChildren();
      if (this.expanded) {
         this.contentLayout.children(this.collapsibleChildren);
      }
      this.refreshResetButton();
   }
}
