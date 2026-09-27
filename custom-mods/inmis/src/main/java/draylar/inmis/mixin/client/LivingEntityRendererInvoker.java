package draylar.inmis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererInvoker {
    @Invoker("addLayer")
    boolean inmis$addLayer(RenderLayer<?, ?> layer);

    @Accessor("layers")
    java.util.List<RenderLayer<?, ?>> inmis$layers();
}
