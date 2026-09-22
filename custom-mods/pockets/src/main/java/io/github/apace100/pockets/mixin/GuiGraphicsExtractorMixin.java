package io.github.apace100.pockets.mixin;

import io.github.apace100.pockets.PocketUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {

	@Inject(method = "itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
	private void pockets$renderPocketBar(Font font, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
		if (!PocketUtil.hasPockets(stack)) {
			return;
		}
		int occupancy = PocketUtil.getPocketOccupancy(stack);
		if (occupancy <= 0) {
			return;
		}
		int yOffset = stack.isBarVisible() ? -2 : 0;
		int filled = Math.min(1 + 12 * occupancy / PocketUtil.MAX_OCCUPANCY, 13);
		int drawX = x + 2;
		int drawY = y + 13 + yOffset;
		GuiGraphicsExtractor self = (GuiGraphicsExtractor) (Object) this;
		self.fill(drawX, drawY, drawX + 13, drawY + 2, 0xFF000000);
		self.fill(drawX, drawY, drawX + filled, drawY + 1, 0xFF6666FF);
	}
}
