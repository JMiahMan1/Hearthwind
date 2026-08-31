package dev.jmiahman.hearthwind.primitive.mixin;

import java.util.Optional;

import dev.jmiahman.hearthwind.primitive.BarkItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stripping a log spawns its bark item at the stripped block (earlystage
 * parity). Injection lands in evaluateNewBlockState right before the
 * AXE_STRIP sound, which only fires on the successful strip branch (the
 * copper-deoxidation branch plays no sound here).
 */
@Mixin(AxeItem.class)
abstract class AxeItemMixin {

    @Inject(method = "evaluateNewBlockState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",
                    ordinal = 0))
    private void hearthwind$spawnBarkOnStrip(Level level, BlockPos pos, Player player, BlockState state,
            CallbackInfoReturnable<Optional<BlockState>> cir) {
        Item bark = BarkItem.BARK_ITEMS.get(state.getBlock());
        if (bark != null && level instanceof ServerLevel serverLevel) {
            ItemStack stack = new ItemStack(bark);
            ItemEntity entity = new ItemEntity(serverLevel,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            entity.setDefaultPickUpDelay();
            serverLevel.addFreshEntity(entity);
        }
    }
}
