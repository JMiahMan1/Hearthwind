package net.dungeonz.mixin;

import net.dungeonz.init.DimensionInit;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1934;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_3222;
import net.minecraft.class_7923;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1657.class)
public abstract class PlayerEntityMixin extends class_1309 {

    @Shadow
    public abstract boolean isCreative();

    public PlayerEntityMixin(class_1299<? extends class_1309> entityType, class_1937 world) {
        super(entityType, world);
    }

    @Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"), cancellable = true)
    protected void dropInventoryMixin(CallbackInfo info) {
        if (!this.method_37908().method_8608() && !this.isCreative() && this.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD
                && DungeonHelper.getCurrentDungeon((class_3222) (Object) this).isKeepInventory()) {
            info.cancel();
        }
    }

    @Inject(method = "isBlockBreakingRestricted", at = @At(value = "HEAD"), cancellable = true)
    private void isBlockBreakingRestrictedMixin(class_1937 world, class_2338 pos, class_1934 gameMode, CallbackInfoReturnable<Boolean> info) {
        if (!world.method_8608() && !this.isCreative() && this.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD
                && !DungeonHelper.getCurrentDungeon((class_3222) (Object) this).getBreakableBlockIdList().contains(class_7923.field_41175.method_10206(world.method_8320(pos).method_26204()))) {
            info.setReturnValue(true);

        }
    }

    @Inject(method = "canPlaceOn", at = @At(value = "HEAD"), cancellable = true)
    public void canPlaceOnMixin(class_2338 pos, class_2350 facing, class_1799 stack, CallbackInfoReturnable<Boolean> info) {
        class_1657 playerEntity = (class_1657) (Object) this;
        if (playerEntity != null && !playerEntity.method_7337() && this.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD) {
            info.setReturnValue(false);
        }
    }

    @Override
    public boolean method_6082(double x, double y, double z, boolean particleEffects) {
        if (this.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD) {
            return false;
        }
        return super.method_6082(x, y, z, particleEffects);
    }

    @Override
    public boolean method_37222(class_1293 effect, class_1297 source) {
        if (!this.method_37908().method_8608() && effect.method_5579().comp_349().method_5573() && this.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD) {
            if (DungeonHelper.getCurrentDungeon((class_3222) (Object) this) != null && !DungeonHelper.getCurrentDungeon((class_3222) (Object) this).isPositiveEffectsAllowed()) {
                return false;
            }
        }
        return super.method_37222(effect, source);
    }

}
