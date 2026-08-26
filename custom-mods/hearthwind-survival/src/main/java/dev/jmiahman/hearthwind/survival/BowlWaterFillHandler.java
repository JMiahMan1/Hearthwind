package dev.jmiahman.hearthwind.survival;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
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
 * (small sip, dirty) for the truly stranded.
 *
 * Server-authoritative, runs on both logical sides but only mutates on server.
 */
public final class BowlWaterFillHandler {
    private static final double BARE_HAND_HYDRATION = 2.0; // small sip

    private BowlWaterFillHandler() {}

    public static void register() {
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
            if (held.isEmpty()) {
                if (world instanceof Level lvl && !lvl.isClientSide() && player instanceof ServerPlayer sp) {
                    HearthwindSurvivalThirst.addHydration(sp, BARE_HAND_HYDRATION);
                    // 75% chance of thirst effect when drinking bare-hand
                    if (sp.getRandom().nextFloat() < 0.75f) {
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                ThirstMobEffect.HOLDER, 300, 0));
                    }
                    sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                            "You cup water in your hands and drink.").withStyle(net.minecraft.ChatFormatting.BLUE));
                    lvl.playSound(null, pos, SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.8f, 1.0f);
                }
                return InteractionResult.SUCCESS_SERVER;
            }

            return InteractionResult.PASS;
        });
    }
}
