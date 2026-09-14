package net.dungeonz.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.dungeonz.access.ClientPlayerAccess;
import net.dungeonz.init.DimensionInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_636;
import net.minecraft.class_746;
import net.minecraft.class_7923;

@Environment(EnvType.CLIENT)
@Mixin(class_310.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public class_746 player;

    @Shadow
    @Nullable
    public class_636 interactionManager;

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void doItemUseMixin(CallbackInfo info, class_1268[] var1, int var2, int var3, class_1268 hand, class_1799 itemStack) {
        if (player != null && !player.method_7337() && itemStack.method_7909() instanceof class_1747 && player.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD
                && !((ClientPlayerAccess) player).getPlaceableBlockIdList().contains(class_7923.field_41175.method_10206(((class_1747) itemStack.method_7909()).method_7711()))) {
            info.cancel();
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/BlockHitResult;getSide()Lnet/minecraft/util/math/Direction;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void handleBlockBreakingMixin(boolean bl, CallbackInfo info, class_3965 blockHitResult, class_2338 blockPos) {
        if (player != null && !player.method_7337() && player.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD
                && !((ClientPlayerAccess) player).getBreakableBlockIdList().contains(class_7923.field_41175.method_10206(player.method_37908().method_8320(blockPos).method_26204()))) {
            interactionManager.method_2925();
            info.cancel();
        }
    }

}
