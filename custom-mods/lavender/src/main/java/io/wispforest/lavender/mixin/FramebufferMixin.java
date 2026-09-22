package io.wispforest.lavender.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.wispforest.lavender.pond.LavenderFramebufferExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderTarget.class)
public class FramebufferMixin implements LavenderFramebufferExtension {

    @Unique
    private boolean enableDepthTest = false;

    @Override
    public void lavender$enableDepthTest() {
        this.enableDepthTest = true;
    }
}
