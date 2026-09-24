package net.adventurez.mixin.client;

import net.adventurez.init.ItemInit;
import net.adventurez.item.GildedNetheriteArmor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(ScreenEffectRenderer.class)
public class InGameOverlayRendererMixin {
    @Redirect(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"))
    private boolean suppressFireOverlay(LocalPlayer player) {
        return player.isOnFire() && !(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(ItemInit.GILDED_NETHERITE_CHESTPLATE) && GildedNetheriteArmor.isStoneGolemArmorActive(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)));
    }
}
