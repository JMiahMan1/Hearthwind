package dev.jmiahman.hearthwind.survival;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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
import org.jspecify.annotations.Nullable;

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
     * Tells the player why the bottle will not go on. Hearthwind deviation,
     * requested 0.1.57: the reference has no {@code LIT} check, so it accepts a
     * bottle on a dark fire and then never boils it - a state with no progress,
     * no result and (since 0.1.49) no message, which is indistinguishable from
     * a bug. Refusing outright is the only outcome a player can act on.
     * Recorded in docs/RELEASE_1.0_PARITY.md 5.11 and docs/PLAYER_CHANGES.md.
     */
    public static void rejectUnlitFire(@Nullable LivingEntity source) {
        if (source instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("message.hearthwind.campfire_needs_fire"));
        }
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
        if (!isLit(campfire.getBlockState())) {
            rejectUnlitFire(source);
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
                // The reference spawns the result at the raw block corner -
                // CampfireBlockEntityMixin does ItemScatterer.spawn(world,
                // pos.getX(), pos.getY(), pos.getZ(), newStack) with no +0.5 -
                // with ItemScatterer's own eject velocity. 26.2 dropped
                // ItemScatterer, so this mirrors its 1.20.1 body: the position
                // goes in untouched and the velocity is
                // f1*(cos a), f1, 0.2*f1*(sin a) with f1 in [0.1, 0.3].
                spawnAtCorner(level, pos);
                items.set(slot, ItemStack.EMPTY);
                level.sendBlockUpdated(pos, state, state, 3);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                changed = true;
            }
            // No completion sound and no particle. The reference
            // (CampfireBlockEntityMixin) cancels vanilla's spawn and does
            // nothing else: no chime, no steam, no burst. We added all three in
            // 0.1.33 as Hearthwind extras; under the parity rule they go again
            // (0.1.49).
        }
        if (changed) {
            campfire.setChanged();
        }
    }

    /** ItemScatterer.spawn(level, x, y, z, stack), verbatim in behaviour. */
    private static void spawnAtCorner(ServerLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        float angle = random.nextFloat() * ((float) Math.PI * 2F);
        float strength = random.nextFloat() * 0.2F + 0.1F;
        ItemEntity item = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), purifiedBottle());
        item.setDeltaMovement(strength * (float) Math.cos(angle), strength,
                strength * (float) Math.sin(angle) * 0.2F);
        level.addFreshEntity(item);
    }
}
