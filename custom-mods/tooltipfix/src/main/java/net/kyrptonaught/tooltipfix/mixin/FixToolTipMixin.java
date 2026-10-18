package net.kyrptonaught.tooltipfix.mixin;

import java.util.ArrayList;
import java.util.List;
import net.kyrptonaught.tooltipfix.Helper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class FixToolTipMixin {
   private static final String TOOLTIP_METHOD =
      "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V";

   @Shadow
   public abstract int guiWidth();

   @ModifyVariable(method = {TOOLTIP_METHOD}, at = @At("HEAD"), index = 2, argsOnly = true)
   public List<ClientTooltipComponent> makeListMutable(List<ClientTooltipComponent> value) {
      return new ArrayList<>(value);
   }

   @Inject(method = {TOOLTIP_METHOD}, at = @At("HEAD"))
   public void fix(
      Font textRenderer,
      List<ClientTooltipComponent> components,
      int x,
      int y,
      ClientTooltipPositioner positioner,
      Identifier identifier,
      CallbackInfo ci
   ) {
      Helper.newFix(components, textRenderer, x, this.guiWidth());
   }

   @ModifyVariable(
      method = {TOOLTIP_METHOD},
      at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;pushMatrix()Lorg/joml/Matrix3x2fStack;"),
      index = 12
   )
   public int modifyRenderX(
      int value,
      Font textRenderer,
      List<ClientTooltipComponent> components,
      int x,
      int y,
      ClientTooltipPositioner positioner,
      Identifier identifier
   ) {
      return Helper.shouldFlip(components, textRenderer, x);
   }
}
