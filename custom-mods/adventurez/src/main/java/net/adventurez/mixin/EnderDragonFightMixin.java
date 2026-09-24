package net.adventurez.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.adventurez.init.ConfigInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;

@Mixin(EnderDragonFight.class)
public class EnderDragonFightMixin {

    @Shadow
    private boolean hasPreviouslyKilledDragon;

    @Shadow
    @Mutable
    @Final
    private ServerLevel level;

    @Shadow
    @Mutable
    @Final
    private BlockPos origin;

    @Inject(method = "setDragonKilled", at = @At("HEAD"))
    public void dragonKilledMixin(EnderDragon dragon, CallbackInfo info) {
        BlockPos eggPos = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.origin);
        if (ConfigInit.CONFIG.resummoned_ender_dragon_drops_egg && this.hasPreviouslyKilledDragon
                && this.level.getBlockState(eggPos).getBlock() != Blocks.DRAGON_EGG) {
            this.level.setBlock(eggPos, Blocks.DRAGON_EGG.defaultBlockState(), 3);
        }
    }
}
