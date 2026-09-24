package net.adventurez.mixin.client;

import net.adventurez.entity.DragonEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "isServerControlledInventory", at = @At("HEAD"), cancellable = true)
    private void isServerControlledInventory(CallbackInfoReturnable<Boolean> info) {
        if (this.minecraft.player != null && this.minecraft.player.isPassenger() && this.minecraft.player.getVehicle() instanceof DragonEntity dragon && dragon.hasChest()) {
            info.setReturnValue(true);
        }
    }
}
