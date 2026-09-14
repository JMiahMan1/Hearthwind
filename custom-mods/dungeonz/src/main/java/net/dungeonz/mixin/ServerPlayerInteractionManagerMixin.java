package net.dungeonz.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dungeonz.init.DimensionInit;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1838;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3222;
import net.minecraft.class_3225;
import net.minecraft.class_3965;
import net.minecraft.class_7923;

@Mixin(class_3225.class)
public class ServerPlayerInteractionManagerMixin {

    // @Inject(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I", ordinal = 0), cancellable = true)
    // private void interactItemMixin(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> info) {
    // System.out.println("INTERACT");
    // if (world.getRegistryKey() == DimensionInit.DUNGEON_WORLD && !player.isCreative() && stack.getItem() instanceof BlockItem) {
    // System.out.println("INT " + DungeonHelper.getCurrentDungeon(player).getplaceableBlockIdList().contains(Registry.BLOCK.getRawId(((BlockItem) stack.getItem()).getBlock())));
    // if (DungeonHelper.getCurrentDungeon(player) != null
    // && !DungeonHelper.getCurrentDungeon(player).getplaceableBlockIdList().contains(Registry.BLOCK.getRawId(((BlockItem) stack.getItem()).getBlock()))) {
    // info.setReturnValue(ActionResult.PASS);
    // }
    // }
    // }

    @Shadow
    @Mutable
    @Final
    protected class_3222 player;

    //net/minecraft/block/Block.onBreak (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/block/BlockState;
    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/block/BlockState;"), cancellable = true)
    private void tryBreakBlockMixin(class_2338 pos, CallbackInfoReturnable<Boolean> info) {
        if (!player.method_37908().method_8608() && player.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD) {
            if (!player.method_7337() && DungeonHelper.getCurrentDungeon(player) != null
                    && !DungeonHelper.getCurrentDungeon(player).getBreakableBlockIdList().contains(class_7923.field_41175.method_10206(player.method_37908().method_8320(pos).method_26204()))) {
                info.setReturnValue(false);
            } else {
                if (DungeonHelper.getDungeonPortalEntity(player) != null && !DungeonHelper.getDungeonPortalEntity(player).getReplaceBlockIdMap().containsKey(pos)) {
                    DungeonHelper.getDungeonPortalEntity(player).addReplaceBlockId(pos, player.method_37908().method_8320(pos).method_26204());
                    DungeonHelper.getDungeonPortalEntity(player).method_5431();
                }
            }
        }
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;isCreative()Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void interactBlockMixin(class_3222 player, class_1937 world, class_1799 stack, class_1268 hand, class_3965 hitResult, CallbackInfoReturnable<class_1269> info, class_2338 blockPos,
            class_2680 blockState, boolean bl, boolean bl2, class_1799 itemStack, class_1838 itemUsageContext) {
        if (world.method_27983() == DimensionInit.DUNGEON_WORLD && !player.method_7337() && stack.method_7909() instanceof class_1747) {
            if (DungeonHelper.getCurrentDungeon(player) != null) {
                if (!DungeonHelper.getCurrentDungeon(player).getplaceableBlockIdList().contains(class_7923.field_41175.method_10206(((class_1747) stack.method_7909()).method_7711()))) {
                    info.setReturnValue(class_1269.field_5811);
                } else if (DungeonHelper.getDungeonPortalEntity(player) != null) {
                    if (!DungeonHelper.getDungeonPortalEntity(player).getReplaceBlockIdMap().containsKey(itemUsageContext.method_8037().method_10093(itemUsageContext.method_8038()))) {
                        DungeonHelper.getDungeonPortalEntity(player).addReplaceBlockId(itemUsageContext.method_8037().method_10093(itemUsageContext.method_8038()), class_2246.field_10124);
                        DungeonHelper.getDungeonPortalEntity(player).method_5431();
                    }
                }
            }
        }
    }
}
