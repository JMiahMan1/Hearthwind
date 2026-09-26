package net.adventurez.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.adventurez.entity.BlazeGuardianEntity;
import net.adventurez.entity.VoidFragmentEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.AABB;

@Mixin(ServerExplosion.class)
public class ExplosionMixin {
    @Shadow
    @Final
    private Entity source;

    // 26.2: hurtEntities() is private and builds its target list via
    // ServerLevel#getEntities. Redirect the call (instead of capturing the
    // local at INVOKE_ASSIGN) so PuzzlesLib's @ModifyVariable on the same
    // method cannot break the injection point.
    @Redirect(method = "hurtEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private List<Entity> hurtEntitiesMixin(ServerLevel level, Entity except, AABB box) {
        List<Entity> list = level.getEntities(except, box);
        if (this.source instanceof BlazeGuardianEntity || this.source instanceof VoidFragmentEntity) {
            list.removeIf(entity -> !(entity instanceof Player));
        }
        return list;
    }
}
