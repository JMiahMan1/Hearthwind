package net.adventurez.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.adventurez.entity.BlazeGuardianEntity;
import net.adventurez.entity.VoidFragmentEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerExplosion;

@Mixin(ServerExplosion.class)
public class ExplosionMixin {
    @Shadow
    @Final
    private Entity source;

    @Inject(method = "hurtEntities", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void hurtEntitiesMixin(CallbackInfo info, List<Entity> list) {
        if (this.source instanceof BlazeGuardianEntity || this.source instanceof VoidFragmentEntity) {
            List<Entity> removeList = new ArrayList<>();
            for (Entity entity : list) {
                if (!(entity instanceof Player)) {
                    removeList.add(entity);
                }
            }
            list.removeAll(removeList);
        }
    }
}
