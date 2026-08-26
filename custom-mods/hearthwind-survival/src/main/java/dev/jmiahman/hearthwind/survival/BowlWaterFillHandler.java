package dev.jmiahman.hearthwind.survival;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

/**
 * Early-game water: right-click a water source with an empty {@code minecraft:bowl}
 * to obtain {@code dehydration:water_bowl}. This mirrors the original
 * dehydration mod's bowl-filling and makes thirst solvable in the first minutes
 * without a bucket (which is iron-gated). Also allows bare-hand drinking
 * (small sip, dirty) for the truly stranded - deliberately tedious to push
 * players toward bowls/campfires.
 *
 * Server-authoritative, runs on both logical sides but only mutates on server.
 */
public final class BowlWaterFillHandler {
    private static final double BARE_HAND_HYDRATION = 1.0; // tiny, vs 6 per bowl
    private static final int BARE_HAND_THIRST_DURATION = 400; // 20s, vs 15s for bowl
    private static final float BARE_HAND_THIRST_CHANCE = 0.90f;
    private static final long BARE_HAND_COOLDOWN_TICKS = 60; // 3s - spam is slow
    private static final java.util.Map<java.util.UUID, Long> bareHandCooldowns =
            new java.util.concurrent.ConcurrentHashMap<>();

    private BowlWaterFillHandler() {}

    public static void register() {
        // Right-click water block with bowl/empty hand
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack held = player.getItemInHand(hand);
            BlockPos pos = hitResult.getBlockPos();
            // Must target a water source (or waterlogged). Check fluid state, not block
            boolean isWater = world.getFluidState(pos).is(Fluids.WATER)
                    || world.getBlockState(pos).is(Blocks.WATER);
            if (!isWater) {
                return InteractionResult.PASS;
            }

            // 1) Bowl -> water_bowl (consume one bowl, give filled)
            if (held.is(Items.BOWL)) {
                if (world instanceof Level lvl && !lvl.isClientSide() && player instanceof ServerPlayer sp) {
                    if (!sp.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                    ItemStack filled = new ItemStack(DehydrationItems.WATER_BOWL);
                    if (!sp.getInventory().add(filled)) {
                        sp.drop(filled, false);
                    }
                    lvl.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.9f, 1.0f);
                }
                return InteractionResult.SUCCESS_SERVER;
            }

            // 2) Bare hand on water -> tiny dirty sip (no item needed, for day-0)
            // Deliberately tedious: 1 hydration vs 6 per bowl, 90% thirst 20s,
            // 3s cooldown, small hunger exhaustion, and a nudge toward bowls.
            if (held.isEmpty()) {
                if (world instanceof Level lvl && !lvl.isClientSide() && player instanceof ServerPlayer sp) {
                    long now = lvl.getGameTime();
                    long last = bareHandCooldowns.getOrDefault(sp.getUUID(), 0L);
                    if (now - last < BARE_HAND_COOLDOWN_TICKS) {
                        long left = (BARE_HAND_COOLDOWN_TICKS - (now - last)) / 20 + 1;
                        sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                                "Cupping water is slow... craft a bowl (3 planks) for a proper drink. (" + left + "s)")
                                .withStyle(net.minecraft.ChatFormatting.GRAY));
                        return InteractionResult.SUCCESS_SERVER;
                    }
                    bareHandCooldowns.put(sp.getUUID(), now);
                    HearthwindSurvivalThirst.addHydration(sp, BARE_HAND_HYDRATION);
                    if (sp.getRandom().nextFloat() < BARE_HAND_THIRST_CHANCE) {
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                ThirstMobEffect.HOLDER, BARE_HAND_THIRST_DURATION, 0));
                    }
                    sp.getFoodData().addExhaustion(0.6f);
                    sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                            "You cup water in your hands and sip - barely helps.").withStyle(net.minecraft.ChatFormatting.BLUE));
                    lvl.playSound(null, pos, SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.5f, 0.9f);
                }
                return InteractionResult.SUCCESS_SERVER;
            }

            return InteractionResult.PASS;
        });

        // Right-click air while in water with empty hand - same dirty sip
        // (covers swimming case where no block is hit), also tedious
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack held = player.getItemInHand(hand);
            if (!held.isEmpty() || !player.isInWater()) {
                return InteractionResult.PASS;
            }
            if (world instanceof Level lvl && !lvl.isClientSide() && player instanceof ServerPlayer sp) {
                long now = lvl.getGameTime();
                long last = bareHandCooldowns.getOrDefault(sp.getUUID(), 0L);
                if (now - last < BARE_HAND_COOLDOWN_TICKS) {
                    return InteractionResult.SUCCESS_SERVER;
                }
                bareHandCooldowns.put(sp.getUUID(), now);
                HearthwindSurvivalThirst.addHydration(sp, BARE_HAND_HYDRATION);
                if (sp.getRandom().nextFloat() < BARE_HAND_THIRST_CHANCE) {
                    sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            ThirstMobEffect.HOLDER, BARE_HAND_THIRST_DURATION, 0));
                }
                sp.getFoodData().addExhaustion(0.6f);
                sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                        "You cup water in your hands and sip - barely helps.").withStyle(net.minecraft.ChatFormatting.BLUE));
                lvl.playSound(null, sp.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.5f, 0.9f);
            }
            return InteractionResult.SUCCESS_SERVER;
        });
    }
}
