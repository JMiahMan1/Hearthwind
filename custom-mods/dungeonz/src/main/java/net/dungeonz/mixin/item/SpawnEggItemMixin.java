package net.dungeonz.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.block.logic.DungeonSpawnerLogic;
import net.dungeonz.init.BlockInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemMixin {

    @Inject(method = "useOn", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void useOnBlockMixin(UseOnContext context, CallbackInfoReturnable<InteractionResult> info, Level world, ItemStack itemStack, EntityType<?> entityType, ServerLevel serverWorld,
            BlockPos blockPos, Direction direction, BlockState blockState) {
        BlockEntity blockEntity;
        if (blockState.is(BlockInit.DUNGEON_SPAWNER) && (blockEntity = world.getBlockEntity(blockPos)) instanceof DungeonSpawnerEntity) {
            DungeonSpawnerLogic dungeonSpawnerLogic = ((DungeonSpawnerEntity) blockEntity).getLogic();
            dungeonSpawnerLogic.setEntityId(entityType);
            blockEntity.setChanged();
            world.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL);
            world.gameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
            itemStack.shrink(1);
            info.setReturnValue(InteractionResult.CONSUME);
        }

    }
}
