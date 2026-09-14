package net.dungeonz.mixin.misc;

import net.dungeonz.util.DungeonHelper;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1684;
import net.minecraft.class_1937;
import net.minecraft.class_239;
import net.minecraft.class_3222;
import net.minecraft.class_3857;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.DimensionInit;

@Mixin(class_1684.class)
public abstract class EnderPearlEntityMixin extends class_3857 {

    public EnderPearlEntityMixin(class_1299<? extends class_3857> entityType, class_1937 world) {
        super(entityType, world);
    }

    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasVehicle()Z"), cancellable = true)
    protected void onCollisionMixin(class_239 hitResult, CallbackInfo info) {
        if (this.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD) {
            class_1297 entity = this.method_24921();
            if (entity instanceof class_3222 serverPlayerEntity && DungeonHelper.getCurrentDungeon(serverPlayerEntity) != null && !DungeonHelper.getCurrentDungeon(serverPlayerEntity).isEnderPearlAllowed()) {
                this.method_31472();
                info.cancel();
            }
        }
    }
}
