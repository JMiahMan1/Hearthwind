package draylar.inmis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.inmis.client.InmisBackpackLayer;
import draylar.inmis.client.InmisRenderStateHolder;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void inmis$addBackpackLayer(CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        RenderLayerParent<AvatarRenderState, PlayerModel> parent = (RenderLayerParent<AvatarRenderState, PlayerModel>) this;
        ((LivingEntityRendererInvoker) this).inmis$addLayer(new InmisBackpackLayer(parent));
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void inmis$captureChestItem(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        ((InmisRenderStateHolder) state).inmis$setChestItem(entity.getItemBySlot(EquipmentSlot.CHEST));
    }
}
