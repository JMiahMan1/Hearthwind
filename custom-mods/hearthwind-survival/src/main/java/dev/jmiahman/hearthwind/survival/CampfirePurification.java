package dev.jmiahman.hearthwind.survival;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import dev.jmiahman.hearthwind.survival.mixin.CampfireBlockEntityAccessor;

    /**
     * Dehydration parity for boiling water bottles on a campfire.
     *
     * <p>The reference mod's {@code CampfireBlockMixin} lets a water bottle be
     * placed on a campfire with a 1000-tick cook time, and its
     * {@code CampfireBlockEntityMixin} swaps the finished bottle for a
     * {@code dehydration:purified_water} potion. Vanilla 26.2 rejects the
     * placement because no campfire cooking recipe accepts a potion, so this
     * helper performs both halves and the mixins only wire it in.
     *
     * <p>Reverse-engineered truth (see {@code docs/AGED_HYDRATION.md}): the
     * reference accepts the bottle on a campfire of <b>either</b> state - it
     * has no lit check at all - but the completion only runs inside the lit
     * server tick, so a bottle on a cold fire simply sits there. We keep that
     * behaviour and add the message the reference lacks, because "nothing
     * happens" on a dark campfire is the single most confusing part of the
     * loop. The reference also plays no sound and no particle when the bottle
     * finishes; we ring a chime and puff steam so the player sees the loop
     * close (recorded in {@code docs/PLAYER_CHANGES.md}).
     */
    public final class CampfirePurification {
    /** Reference value: {@code CampfireBlockMixin} places bottles with 1000. */
    public static final int BOIL_TIME = 1000;

    private CampfirePurification() {}

    /** True for a drinkable water bottle ({@code minecraft:potion} + water). */
    public static boolean isWaterPotion(ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return false;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(Potions.WATER);
    }

    /** A {@code minecraft:potion} of {@code dehydration:purified_water}. */
    public static ItemStack purifiedBottle() {
        return PotionContents.createItemStack(Items.POTION, PurifiedWater.PURIFIED_POTION);
    }

    /** True while the campfire is burning, which is when a bottle boils. */
    public static boolean isLit(BlockState state) {
        return state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT);
    }

    /**
     * Tells the player why a bottle on a dark campfire will never purify. The
     * reference mod stays silent here, which is how this bug survives for
     * hours; the rule itself is unchanged.
     */
    public static void warnAboutUnlitFire(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.hearthwind.campfire_needs_fire"));
    }

    /**
     * Places one water bottle into the first empty campfire slot with the
     * reference boil time. Returns false for non-water stacks or a full fire.
     */
    public static boolean placeWaterBottle(ServerLevel level, LivingEntity source,
            CampfireBlockEntity campfire, ItemStack stack) {
        if (!isWaterPotion(stack)) {
            return false;
        }
        CampfireBlockEntityAccessor accessor = (CampfireBlockEntityAccessor) campfire;
        NonNullList<ItemStack> items = accessor.hearthwind$items();
        for (int slot = 0; slot < items.size(); slot++) {
            if (items.get(slot).isEmpty()) {
                accessor.hearthwind$cookingTime()[slot] = BOIL_TIME;
                accessor.hearthwind$cookingProgress()[slot] = 0;
                items.set(slot, stack.consumeAndReturn(1, source));
                BlockPos pos = campfire.getBlockPos();
                BlockState state = campfire.getBlockState();
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(source, state));
                campfire.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                return true;
            }
        }
        return false;
    }

    /**
     * Replaces every finished water bottle with a purified one. Runs before
     * vanilla's cook tick so the un-purified bottle is never dropped.
     */
    public static void tickPurification(ServerLevel level, BlockPos pos, BlockState state,
            CampfireBlockEntity campfire) {
        CampfireBlockEntityAccessor accessor = (CampfireBlockEntityAccessor) campfire;
        NonNullList<ItemStack> items = accessor.hearthwind$items();
        int[] progress = accessor.hearthwind$cookingProgress();
        int[] time = accessor.hearthwind$cookingTime();
        boolean changed = false;
        for (int slot = 0; slot < items.size(); slot++) {
            if (!isWaterPotion(items.get(slot))) {
                continue;
            }
            // Vanilla increments progress later in this same tick, so treat
            // "one tick short" as done to beat its fallback drop.
            if (progress[slot] + 1 >= time[slot]) {
                // Eject the finished bottle off the fire's edge with the same
                // face-pop vanilla uses for dispensers. Block.popResource
                // spawns it dead-centre at the campfire's base with zero
                // velocity, so it sat half-buried in the fire (looking like
                // it never finished) and despawned after five minutes -
                // players reported "the bottle disappeared".
                Direction direction = Direction.from2DDataValue(
                        Math.floorMod(slot + state.getValue(CampfireBlock.FACING).get2DDataValue(), 4));
                Block.popResourceFromFace(level, pos, direction, purifiedBottle());
                items.set(slot, ItemStack.EMPTY);
                level.sendBlockUpdated(pos, state, state, 3);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                // The reference mod is silent here; a player watching a bottle
                // boil needs to know the loop closed, and the fire's own smoke
                // is easy to miss. Hearthwind addition, listed in
                // docs/PLAYER_CHANGES.md.
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6F, 1.4F);
                spawnBurst(level, pos, state, slot);
                changed = true;
            } else {
                spawnSteam(level, pos, state, slot);
            }
        }
        if (changed) {
            campfire.setChanged();
        }
    }

    /**
     * White steam over a boiling bottle instead of the dark item smoke
     * vanilla emits ({@code particleTick} suppresses that smoke for water
     * slots). Mirrors vanilla's per-slot position and chance.
     */
    private static void spawnSteam(ServerLevel level, BlockPos pos, BlockState state, int slot) {
        RandomSource random = level.getRandom();
        if (random.nextFloat() >= 0.2F) {
            return;
        }
        Direction direction = Direction.from2DDataValue(
                Math.floorMod(slot + state.getValue(CampfireBlock.FACING).get2DDataValue(), 4));
        double x = pos.getX() + 0.5 - direction.getStepX() * 0.3125 + direction.getClockWise().getStepX() * 0.3125;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5 - direction.getStepZ() * 0.3125 + direction.getClockWise().getStepZ() * 0.3125;
        level.sendParticles(ParticleTypes.WHITE_SMOKE, x, y, z, 2, 0.05, 0.01, 0.05, 0.0);
    }

    /**
     * The visible end of the boil: a puff of steam and a few rising bubbles
     * over the slot that just finished, so the loop reads as finished even
     * when the player is not staring straight at the fire.
     */
    private static void spawnBurst(ServerLevel level, BlockPos pos, BlockState state, int slot) {
        Direction direction = Direction.from2DDataValue(
                Math.floorMod(slot + state.getValue(CampfireBlock.FACING).get2DDataValue(), 4));
        double x = pos.getX() + 0.5 - direction.getStepX() * 0.3125 + direction.getClockWise().getStepX() * 0.3125;
        double y = pos.getY() + 0.6;
        double z = pos.getZ() + 0.5 - direction.getStepZ() * 0.3125 + direction.getClockWise().getStepZ() * 0.3125;
        level.sendParticles(ParticleTypes.WHITE_SMOKE, x, y, z, 6, 0.06, 0.08, 0.06, 0.02);
        level.sendParticles(ParticleTypes.BUBBLE, x, y, z, 4, 0.05, 0.1, 0.05, 0.1);
    }
}
