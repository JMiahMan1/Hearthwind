package dev.jmiahman.hearthwind.jobs.mixin;

import dev.jmiahman.hearthwind.jobs.JobEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Placing a block pays the builder job.
 *
 * <p>26.2 fabric-api has no block-place event at all, so this sits on
 * {@link BlockItem#place} - the single method every block placement goes
 * through, whatever opened the menu or dispatched the click - and only fires
 * for a placement that actually succeeded. Nothing is inferred from the
 * surrounding interaction, so a refused or impossible placement pays nothing.
 *
 * <p>The block paid for is the item's own block, NOT the world read-back:
 * {@code BlockPlaceContext#getClickedPos} is the position that was clicked,
 * which is the support block whenever the click hit a solid face, so reading
 * it back would report stone for a plank.
 *
 * <p>The hook is deliberately not on {@code BlockBehaviour#onPlace}: that
 * method carries no player and also fires for world generation and chunk
 * loading, which would hand out builder XP for terrain the player never
 * touched.
 */
@Mixin(BlockItem.class)
public abstract class BlockItemJobXpMixin {
    @Inject(method = "place", at = @At("RETURN"))
    private void hearthwind$builderPlacement(BlockPlaceContext placeContext,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (!(cir.getReturnValue() instanceof InteractionResult.Success)) {
            return;
        }
        if (!(placeContext.getPlayer() instanceof ServerPlayer sp)) {
            return;
        }
        JobEvents.awardBlockPlaced(sp, ((BlockItem) (Object) this).getBlock().defaultBlockState());
    }
}
