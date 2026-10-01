package dev.jmiahman.hearthwind.survival.revive;

import java.util.Optional;
import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Port of revive 1.0.7, the downed/revive mod Aged 3.1.2 ships, rebuilt
 * in-tree as {@code hearthwind-survival}.
 *
 * <h2>What the reference actually does</h2>
 *
 * Aged's {@code revive.json5} sets only three keys, so almost everything
 * comes from the mod's constructor defaults, read out of
 * {@code ReviveConfig}'s bytecode:
 *
 * <table border="1">
 *   <caption>revive 1.0.7 defaults vs Aged</caption>
 *   <tr><th>key</th><th>default</th><th>Aged</th></tr>
 *   <tr><td>{@code timer}</td><td>-1 (off)</td><td>unset - so -1</td></tr>
 *   <tr><td>{@code reviveHealthPoints}</td><td>2</td><td>unset - so 2</td></tr>
 *   <tr><td>{@code reviveSupportiveHealthPoints}</td><td>10</td><td>unset, and unreachable</td></tr>
 *   <tr><td>{@code effectAftermath}</td><td>600</td><td>unset - so 600</td></tr>
 *   <tr><td>{@code allowReviveWithHand}</td><td>false</td><td><b>true</b></td></tr>
 *   <tr><td>{@code dropRandomOnExplosion}</td><td>true</td><td><b>false</b></td></tr>
 *   <tr><td>{@code rotationSpeed}</td><td>0.4</td><td><b>0.3</b></td></tr>
 *   <tr><td>{@code showDeathCoordinates}</td><td>true</td><td>unset - so true</td></tr>
 * </table>
 *
 * <h2>What this port does</h2>
 *
 * <ul>
 *   <li><b>No bleedout timer.</b> The reference's server tick hook returns at
 *       bytecode offset 27 when {@code timer == -1}, so a downed player in
 *       Aged never dies on a timer and never drops their inventory. We used
 *       to run a 1200-tick (60 s) bleedout that killed the player and dropped
 *       their loot; both are gone (0.1.47).</li>
 *   <li><b>Revive is one click, not a 3-second hold.</b> An ally's sneaking
 *       empty-hand right-click ARMS the downed player's Revive button
 *       ({@code PlayerEntityMixin.method_5664} gates on
 *       {@code allowReviveWithHand && isSneaking()} and on the held stack not
 *       being a potion), and then the downed player presses it once -
 *       {@code ReviveServerPacket} revives immediately.</li>
 *   <li><b>Revive to 2 HP</b>, {@code reviveHealthPoints}, not the 6.0 we
 *       invented.</li>
 *   <li><b>A 600-tick aftermath effect</b> is applied on revive, as the
 *       reference does at packet offsets 113-129.</li>
 * </ul>
 *
 * <p>Our player is never technically dead (we cancel the death), so the Revive
 * button lives on our downed overlay rather than on a vanilla death screen.
 * The mechanic - one click, gated on an ally having armed it - is the
 * reference's.
 */
public final class ReviveManager {
    /**
     * {@code ReviveConfig.reviveHealthPoints} (constructor offset 41). Aged
     * leaves it unset, so this is 2 HP - one heart.
     */
    public static final float REVIVE_HEALTH = 2.0f;

    /** Health the downed player is left at. Matches the reference's own value. */
    public static final float DOWNED_HEALTH = 2.0f;

    private ReviveManager() {}

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof ServerPlayer player) {
                return onFatalDamage(player, damageSource);
            }
            return true;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && entity instanceof ServerPlayer target && player instanceof ServerPlayer reviver) {
                return onInteract(reviver, target, hand);
            }
            return InteractionResult.PASS;
        });

        ServerPlayNetworking.registerGlobalReceiver(DownedRevivePayload.TYPE,
                (payload, context) -> context.server().execute(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        onSelfRevive(player);
                    }
                }));
    }

    public static boolean onFatalDamage(ServerPlayer player, net.minecraft.world.damagesource.DamageSource damageSource) {
        if (player.getAbilities().invulnerable) {
            return true;
        }

        // The reference skips its downed state entirely in singleplayer
        // (PlayerEntityMixin.method_6108 returns before arming anything when
        // the world is a client world), so with nobody to revive you there is
        // no point going down.
        if (player.level() != null && player.level().isClientSide()) {
            return true;
        }

        DownedState.Data current = DownedState.get(player);
        if (current.isDowned()) {
            // Already downed. The reference cancels the vanilla death outright
            // and has no second-blow rule, but a downed body that can be
            // finished off is how the reference's "no timer" state still has a
            // cost: we keep the second blow fatal so a downed player is not
            // invulnerable.
            DownedState.clear(player);
            sync(player, false);
            return true;
        }

        DownedState.set(player, new DownedState.Data(true, Optional.empty()));
        player.setHealth(DOWNED_HEALTH);

        // Immobilisation. No duration: the reference has no timer, so nothing
        // here should expire on its own.
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, MobEffectInstance.INFINITE_DURATION, 4, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, MobEffectInstance.INFINITE_DURATION, 4, false, false, false));

        if (player.level() instanceof ServerLevel level) {
            level.playSound(null, player.blockPosition(),
                    SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 0.7f);
        }

        player.sendSystemMessage(Component.literal(
                "§c§lDOWNED! §7You cannot get up on your own - an ally must crouch and use (right-click) you with an empty hand."));

        sync(player, true);
        return false;
    }

    /**
     * An ally's interaction on a downed player. Ported from revive 1.0.7's
     * {@code PlayerEntityMixin.method_5664}, which gates the arming on
     *
     * <pre>{@code allowReviveWithHand && player.isSneaking()
     *         && potion(stack).equals(Potions.EMPTY)}</pre>
     *
     * (bytecode offsets 59-72), so it must be a crouch with a non-potion item -
     * an empty hand is the intended case.
     */
    public static InteractionResult onInteract(ServerPlayer reviver, ServerPlayer target, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!DownedState.isDowned(target)) {
            return InteractionResult.PASS;
        }
        if (reviver.getUUID().equals(target.getUUID())) {
            return InteractionResult.PASS;
        }
        // 26.2 split the old isSneaking() in two: isShiftKeyDown() is the
        // KEY (which is what the reference's isSneaking() meant) and
        // isCrouching() is the CROUCHING POSE, which only gets set when the
        // entity ticks. Gating on the pose would make this unreachable for
        // anything that has not ticked, and it would also reject a player who
        // is sneaking without the animation.
        if (!reviver.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!isArmingHand(reviver, hand)) {
            return InteractionResult.PASS;
        }

        DownedState.set(target, new DownedState.Data(true, Optional.of(reviver.getUUID())));

        if (target.level() instanceof ServerLevel level) {
            level.playSound(null, target.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 0.6f);
        }
        reviver.sendSystemMessage(Component.literal("§eYou can pick " + target.getName().getString() + " back up."));
        target.sendSystemMessage(Component.literal("§a" + reviver.getName().getString() + " can pick you up."));
        sync(target, true);
        return InteractionResult.SUCCESS_SERVER;
    }


    /**
     * The reference requires the held stack to NOT be a potion
     * ({@code PotionContentsComponent.getPotion(stack).equals(Potions.EMPTY)}
     * at bytecode offset 34-40), so an empty hand - or any non-potion item -
     * arms the revive and a potion does not.
     */
    private static boolean isArmingHand(ServerPlayer reviver, InteractionHand hand) {
        var stack = reviver.getItemInHand(hand);
        if (stack.isEmpty()) {
            return true;
        }
        var contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        return contents == null || contents.potion().isEmpty();
    }

    /**
     * The downed player's own Revive click. The reference fires this from a
     * button on the death screen and revives immediately - there is no hold
     * and no progress bar anywhere in the bytecode.
     */
    public static void onSelfRevive(ServerPlayer player) {
        DownedState.Data data = DownedState.get(player);
        if (!data.isDowned() || data.armedBy().isEmpty()) {
            return;
        }
        Optional<UUID> armedBy = data.armedBy();
        ServerPlayer reviver = player.level() != null && player.level().getServer() != null
                ? player.level().getServer().getPlayerList().getPlayer(armedBy.get())
                : null;
        completeRevive(player, reviver);
    }

    /**
     * Per-player downed upkeep. There is deliberately no countdown and no
     * timeout kill here any more - see the class javadoc.
     */
    public static void tickPlayer(ServerPlayer player) {
        DownedState.Data data = DownedState.get(player);
        if (!data.isDowned()) {
            return;
        }

        // The ally who armed us may have logged off; the button stays live
        // either way, exactly as the reference's `canRevive` boolean does.
        if (player.tickCount % 20 == 0) {
            sync(player, true);
        }
    }

    public static void completeRevive(ServerPlayer downed, ServerPlayer reviver) {
        DownedState.clear(downed);
        downed.removeAllEffects();
        downed.setHealth(REVIVE_HEALTH);
        // ReviveConfig.effectAftermath, 600 ticks, amplifier 0, not ambient,
        // no particles, icon shown - packet offsets 113-129.
        downed.addEffect(new MobEffectInstance(AftermathMobEffect.HOLDER,
                AftermathMobEffect.DURATION_TICKS, 0, false, false, true));

        if (downed.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HEART,
                    downed.getX(), downed.getY() + 1.0, downed.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);
            level.playSound(null, downed.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);
        }

        String by = reviver != null ? reviver.getName().getString() : "an ally";
        downed.sendSystemMessage(Component.literal("§aYou were picked up by §e" + by + "§a."));
        if (reviver != null) {
            reviver.sendSystemMessage(Component.literal("§aYou picked up §e" + downed.getName().getString() + "§a."));
        }

        sync(downed, false);
    }

    public static void sync(ServerPlayer player, boolean isDowned) {
        if (player != null && player.connection != null) {
            try {
                if (ServerPlayNetworking.canSend(player, DownedSyncPayload.TYPE)) {
                    boolean armed = isDowned && DownedState.isArmed(player);
                    ServerPlayNetworking.send(player, new DownedSyncPayload(isDowned, armed,
                            player.getBlockX(), player.getBlockY(), player.getBlockZ()));
                }
            } catch (Exception ignored) {}
        }
    }
}