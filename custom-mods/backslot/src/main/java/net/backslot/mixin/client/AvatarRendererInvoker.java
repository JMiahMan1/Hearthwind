package net.backslot.mixin.client;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntityRenderer.class)
public interface AvatarRendererInvoker {

    @Invoker("addLayer")
    boolean backslot$addLayer(RenderLayer<?, ?> layer);

    @Accessor("layers")
    java.util.List<RenderLayer<?, ?>> backslot$layers();
}
