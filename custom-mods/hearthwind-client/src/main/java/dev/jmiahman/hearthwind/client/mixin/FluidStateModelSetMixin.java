package dev.jmiahman.hearthwind.client.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.jmiahman.hearthwind.survival.PurifiedWater;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

/**
 * 26.2 bakes fluid models only for vanilla water/lava; everything else gets
 * the missing model. Registers purified water with the Dehydration light-blue
 * tint over vanilla water sprites.
 */
@Mixin(FluidStateModelSet.class)
public abstract class FluidStateModelSetMixin {
    @Inject(method = "bake", at = @At("RETURN"), cancellable = true)
    private static void hearthwind$addPurifiedWater(MaterialBaker baker,
            CallbackInfoReturnable<Map<Fluid, FluidModel>> cir) {
        Map<Fluid, FluidModel> map = new HashMap<>(cir.getReturnValue());
        FluidModel.Unbaked unbaked = new FluidModel.Unbaked(
                new Material(Identifier.withDefaultNamespace("block/water_still")),
                new Material(Identifier.withDefaultNamespace("block/water_flow")),
                new Material(Identifier.withDefaultNamespace("block/water_overlay")),
                BlockTintSources.constant(0xFF388392));
        FluidModel baked = unbaked.bake(baker, () -> "dehydration:purified_water");
        if (PurifiedWater.STILL != null) {
            map.put(PurifiedWater.STILL, baked);
        }
        if (PurifiedWater.FLOWING != null) {
            map.put(PurifiedWater.FLOWING, baked);
        }
        cir.setReturnValue(map);
    }
}
