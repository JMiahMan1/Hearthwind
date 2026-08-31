package dev.jmiahman.hearthwind.primitive.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dev.jmiahman.hearthwind.primitive.BarkItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Accessor + strip hook for tree barks: stripping a log spawns its bark item
 * at the hit position (earlystage parity).
 */
@Mixin(AxeItem.class)
public interface AxeItemAccessor {

    @Accessor("STRIPPABLES")
    static Map<Block, Block> getStrippedBlocks() {
        throw new AssertionError("mixin accessor was not applied");
    }
}
