package net.dungeonz.mixin.misc;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.DimensionInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.ServerExplosion;

// 26.x: Explosion is now an interface; block breaking runs in ServerExplosion.
// Same behavior as before: explosions inside the dungeon dimension keep blocks.
@Mixin(ServerExplosion.class)
public class ExplosionMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Mutable
    @Final
    private BlockInteraction blockInteraction;

    @Inject(method = "explode", at = @At("HEAD"))
    private void affectWorldMixin(CallbackInfoReturnable<Integer> info) {
        if (level.dimension() == DimensionInit.DUNGEON_WORLD) {
            blockInteraction = BlockInteraction.KEEP;
        }
    }

}
