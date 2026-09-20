package net.dungeonz.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.DimensionInit;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.registries.BuiltInRegistries;

@Mixin(ServerPlayerGameMode.class)
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
    @Final
    protected ServerPlayer player;

    //net/minecraft/block/Block.onBreak (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/block/BlockState;
    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"), cancellable = true)
    private void tryBreakBlockMixin(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if (!player.level().isClientSide() && player.level().dimension() == DimensionInit.DUNGEON_WORLD) {
            if (!player.isCreative() && DungeonHelper.getCurrentDungeon(player) != null
                    && !DungeonHelper.getCurrentDungeon(player).getBreakableBlockIdList().contains(BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(pos).getBlock()))) {
                info.setReturnValue(false);
            } else {
                if (DungeonHelper.getDungeonPortalEntity(player) != null && !DungeonHelper.getDungeonPortalEntity(player).getReplaceBlockIdMap().containsKey(pos)) {
                    DungeonHelper.getDungeonPortalEntity(player).addReplaceBlockId(pos, player.level().getBlockState(pos).getBlock());
                    DungeonHelper.getDungeonPortalEntity(player).setChanged();
                }
            }
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasInfiniteMaterials()Z"), cancellable = true)
    private void interactBlockMixin(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> info) {
        if (world.dimension() == DimensionInit.DUNGEON_WORLD && !player.isCreative() && stack.getItem() instanceof BlockItem) {
            if (DungeonHelper.getCurrentDungeon(player) != null) {
                if (!DungeonHelper.getCurrentDungeon(player).getplaceableBlockIdList().contains(BuiltInRegistries.BLOCK.getKey(((BlockItem) stack.getItem()).getBlock()))) {
                    info.setReturnValue(InteractionResult.PASS);
                } else if (DungeonHelper.getDungeonPortalEntity(player) != null) {
                    if (!DungeonHelper.getDungeonPortalEntity(player).getReplaceBlockIdMap().containsKey(hitResult.getBlockPos().relative(hitResult.getDirection()))) {
                        DungeonHelper.getDungeonPortalEntity(player).addReplaceBlockId(hitResult.getBlockPos().relative(hitResult.getDirection()), Blocks.AIR);
                        DungeonHelper.getDungeonPortalEntity(player).setChanged();
                    }
                }
            }
        }
    }
}
