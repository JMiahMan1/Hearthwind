package dev.jmiahman.hearthwind.primitive;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tree-felling: when a player chops a trunk log with an axe, all connected
 * log blocks directly above the chop point fall / harvest together (parity
 * with tree felling in Aged). Sneaking disables felling for precision.
 */
public final class TreeFelling {
    private static final int MAX_FELL_LOGS = 32;
    // ThreadLocal prevents re-entrancy per-thread (safe for concurrent players on server tick thread)
    private static final ThreadLocal<Boolean> fellingActive = ThreadLocal.withInitial(() -> false);

    private TreeFelling() {}

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(TreeFelling::onBlockBroken);
    }

    static void onBlockBroken(net.minecraft.world.level.Level world,
            net.minecraft.world.entity.player.Player player, BlockPos pos,
            BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        if (fellingActive.get() || !state.is(BlockTags.LOGS) || !(player instanceof ServerPlayer sp)
                || sp.isCreative() || sp.isShiftKeyDown() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack held = sp.getMainHandItem();
        boolean isAxe = !held.isEmpty() && (held.is(ItemTags.AXES) || held.getItem() instanceof AxeItem
                || held.is(HearthwindPrimitiveItems.FLINT_AXE) || held.is(HearthwindPrimitiveItems.STEEL_AXE));

        if (!isAxe) {
            return;
        }

        fellTree(serverLevel, sp, pos, held);
    }

    public static int fellTree(ServerLevel world, ServerPlayer player, BlockPos rootPos, ItemStack axe) {
        fellingActive.set(true);
        int felledCount = 0;
        try {
            Queue<BlockPos> queue = new ArrayDeque<>();
            Set<BlockPos> visited = new HashSet<>();
            queue.add(rootPos);
            visited.add(rootPos);

            while (!queue.isEmpty() && felledCount < MAX_FELL_LOGS) {
                BlockPos current = queue.poll();

                // Check neighbors: strictly upwards and adjacent (dx in -1..1, dz in -1..1, dy in 0..1)
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        for (int dy = 0; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0 && dz == 0) {
                                continue;
                            }
                            BlockPos neighbor = current.offset(dx, dy, dz);
                            // Only search above the original cut point within horizontal radius of 2
                            if (neighbor.getY() < rootPos.getY() || Math.abs(neighbor.getX() - rootPos.getX()) > 2
                                    || Math.abs(neighbor.getZ() - rootPos.getZ()) > 2) {
                                continue;
                            }
                            if (visited.add(neighbor)) {
                                BlockState neighborState = world.getBlockState(neighbor);
                                if (neighborState.is(BlockTags.LOGS)) {
                                    queue.add(neighbor);
                                    // Destroy block with drops and player attribution
                                    try {
                                        world.destroyBlock(neighbor, true, player);
                                    } catch (Exception e) {
                                        world.destroyBlock(neighbor, true, null);
                                    }
                                    felledCount++;
                                    try {
                                        if (axe.isDamageableItem() && player.connection != null) {
                                            axe.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            fellingActive.set(false);
        }
        return felledCount;
    }
}
