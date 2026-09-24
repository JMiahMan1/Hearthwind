package net.adventurez.mixin.client;

import net.adventurez.access.EntityAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.golem.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(IronGolemRenderer.class)
public class IronGolemEntityRendererMixin {
    private static final Identifier BLACKSTONED_TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/blackstone_golem.png");

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void extractRenderState(IronGolem entity, IronGolemRenderState state, float partialTicks, CallbackInfo info) {
        IronGolemRenderStateHelper.set(((EntityAccess) (Object) entity).getTrackedDataBoolean() != null && entity.getEntityData().get(((EntityAccess) (Object) entity).getTrackedDataBoolean()));
    }

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void getTextureLocation(IronGolemRenderState state, CallbackInfoReturnable<Identifier> info) {
        if (IronGolemRenderStateHelper.isBlackstoned()) {
            info.setReturnValue(BLACKSTONED_TEXTURE);
        }
    }
}
