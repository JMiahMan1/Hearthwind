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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I", ordinal = 0), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void doItemUseMixin(CallbackInfo info, InteractionHand[] var1, int var2, int var3, InteractionHand hand, ItemStack itemStack) {
        if (player != null && !player.isCreative() && itemStack.getItem() instanceof BlockItem && player.level().dimension() == DimensionInit.DUNGEON_WORLD
                && !((ClientPlayerAccess) player).getPlaceableBlockIdList().contains(BuiltInRegistries.BLOCK.getId(((BlockItem) itemStack.getItem()).getBlock()))) {
            info.cancel();
        }
    }

    @Inject(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/BlockHitResult;getDirection()Lnet/minecraft/core/Direction;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void handleBlockBreakingMixin(boolean bl, CallbackInfo info, ItemStack heldItem, BlockHitResult blockHitResult, BlockPos blockPos) {
        if (player != null && !player.isCreative() && player.level().dimension() == DimensionInit.DUNGEON_WORLD
                && !((ClientPlayerAccess) player).getBreakableBlockIdList().contains(BuiltInRegistries.BLOCK.getId(player.level().getBlockState(blockPos).getBlock()))) {
            gameMode.stopDestroyBlock();
            info.cancel();
        }
    }

}
