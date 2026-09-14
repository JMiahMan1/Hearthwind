package net.dungeonz.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.block.logic.DungeonSpawnerLogic;
import net.dungeonz.init.BlockInit;
import net.minecraft.class_1269;
import net.minecraft.class_1299;
import net.minecraft.class_1799;
import net.minecraft.class_1826;
import net.minecraft.class_1838;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_5712;

@Mixin(class_1826.class)
public class SpawnEggItemMixin {

    @Inject(method = "useOnBlock", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void useOnBlockMixin(class_1838 context, CallbackInfoReturnable<class_1269> info, class_1937 world, class_1799 itemStack, class_2338 blockPos, class_2350 direction,
            class_2680 blockState) {
        class_2586 blockEntity;
        if (blockState.method_27852(BlockInit.DUNGEON_SPAWNER) && (blockEntity = world.method_8321(blockPos)) instanceof DungeonSpawnerEntity) {
            DungeonSpawnerLogic dungeonSpawnerLogic = ((DungeonSpawnerEntity) blockEntity).getLogic();
            class_1299<?> entityType = this.getEntityType(itemStack);
            dungeonSpawnerLogic.setEntityId(entityType);
            blockEntity.method_5431();
            world.method_8413(blockPos, blockState, blockState, class_2248.field_31036);
            world.method_33596(context.method_8036(), class_5712.field_28733, blockPos);
            itemStack.method_7934(1);
            info.setReturnValue(class_1269.field_21466);
        }

    }

    @Shadow
    public class_1299<?> getEntityType(class_1799 stack) {
        return null;
    }
}
