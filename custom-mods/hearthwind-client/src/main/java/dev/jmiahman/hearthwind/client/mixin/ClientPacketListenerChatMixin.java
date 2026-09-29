package dev.jmiahman.hearthwind.client.mixin;

import dev.jmiahman.hearthwind.client.chat.StartupChatFilter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops other mods' system chat during the window after joining a world. See
 * {@link StartupChatFilter} for what is and is not filtered; overlays and
 * player chat pass through untouched.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerChatMixin {

    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)
    private void hearthwind$dropModStartupChat(ClientboundSystemChatPacket packet, CallbackInfo info) {
        if (StartupChatFilter.shouldDrop(packet)) {
            info.cancel();
        }
    }
}
