package net.travelerz.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Hud;
import net.travelerz.TravelerzMain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin({Hud.class})
public class InGameHudMixin {
   @ModifyConstant(
      method = {"extractTitle"},
      constant = {@Constant(
         floatValue = 4.0F
      )}
   )
   private float renderModifyTitleMixin(float original) {
      return original * TravelerzMain.CONFIG.titleSize;
   }

   @ModifyConstant(
      method = {"extractTitle"},
      constant = {@Constant(
         floatValue = 2.0F
      )}
   )
   private float renderModifySubtitleMixin(float original) {
      return original * TravelerzMain.CONFIG.titleSize;
   }

   @ModifyConstant(
      method = {"extractTitle"},
      constant = {@Constant(
         intValue = -10
      )}
   )
   private int renderModifyTitlePosYMixin(int original) {
      return original + TravelerzMain.CONFIG.titleY;
   }

   @ModifyConstant(
      method = {"extractTitle"},
      constant = {@Constant(
         intValue = 5
      )}
   )
   private int renderModifySubtitlePosYMixin(int original) {
      return original + (int)(TravelerzMain.CONFIG.titleY * 2.0F);
   }

   @ModifyVariable(
      method = {"extractTitle"},
      at = {@At(
         value = "INVOKE_ASSIGN",
         target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I",
         ordinal = 0
      )},
      ordinal = 1
   )
   private int renderModifyTitlePosXMixin(int original) {
      return original + TravelerzMain.CONFIG.titleX;
   }

   @ModifyVariable(
      method = {"extractTitle"},
      at = {@At(
         value = "INVOKE_ASSIGN",
         target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I",
         ordinal = 1
      )},
      ordinal = 3
   )
   private int renderModifySubtitlePosXMixin(int original) {
      return original + (int)(TravelerzMain.CONFIG.titleX * 2.0F);
   }
}
