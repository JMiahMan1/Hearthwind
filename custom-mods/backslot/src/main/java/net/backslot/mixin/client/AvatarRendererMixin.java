package net.backslot.mixin.client;

import net.backslot.BackSlotSlots;
import net.backslot.client.BackSlotRenderStateHolder;
import net.backslot.client.BeltItemLayer;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.backslot.client.BackItemLayer;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void backslot$addSlotLayers(CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        RenderLayerParent<AvatarRenderState, PlayerModel> parent = (RenderLayerParent<AvatarRenderState, PlayerModel>) this;
        ((AvatarRendererInvoker) this).backslot$addLayer(new BackItemLayer(parent));
        ((AvatarRendererInvoker) this).backslot$addLayer(new BeltItemLayer(parent));
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void backslot$captureSlotItems(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        BackSlotRenderStateHolder holder = (BackSlotRenderStateHolder) state;
        holder.backslot$setBackItem(BackSlotSlots.get(entity, net.backslot.BackSlot.BACK_SLOT));
        holder.backslot$setBeltItem(BackSlotSlots.get(entity, net.backslot.BackSlot.BELT_SLOT));
    }
}
