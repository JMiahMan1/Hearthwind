package net.dungeonz.mixin.misc;

import net.dungeonz.init.DimensionInit;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {

    public FallingBlockEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

//    @Inject(method = "spawnFromBlock", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
//    private static void spawnFromBlockMixin(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> info, FallingBlockEntity fallingBlockEntity) {
//        if (world instanceof ServerWorld serverWorld && world.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
//        }
//    }
//
//    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;sendToOtherNearbyPlayers(Lnet/minecraft/entity/Entity;Lnet/minecraft/network/packet/Packet;)V"))
//    private void tickMixin(CallbackInfo info) {
//        if (this.getWorld() instanceof ServerWorld serverWorld && serverWorld.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
//        }
//    }
}
