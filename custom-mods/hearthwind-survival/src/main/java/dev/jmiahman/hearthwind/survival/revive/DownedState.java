package dev.jmiahman.hearthwind.survival.revive;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * A downed player's state.
 *
 * <p><b>There is no bleedout countdown, and that is the reference's
 * behaviour, not an omission.</b> revive 1.0.7's {@code timer} config key
 * defaults to {@code -1} (ReviveConfig constructor offset 5) and Aged's
 * {@code revive.json5} does not set it. The server tick hook reads:
 *
 * <pre>{@code deadTime++;
 * if (world.isClient) return;
 * if (CONFIG.timer == -1) return;              // bytecode offset 27
 * if (CONFIG.timer < deadTime) { ... kill ... }
 * }</pre>
 *
 * so with Aged's config the entire hook is a no-op: a downed player lies
 * there indefinitely and is never killed by a timer. We shipped a 1200-tick
 * (60 s) bleedout that killed the player AND dropped their inventory, which
 * Aged never does. Both the countdown field and that kill path are gone as of
 * 0.1.47.
 *
 * <p>What remains is the ARMING flag: {@code armedBy} holds the ally whose
 * sneaking empty-hand interaction made the revive button live. The reference
 * stores the same thing as a pair of booleans on the player
 * ({@code canRevive} / {@code isSupportiveRevival}, set by
 * {@code PlayerEntityMixin.method_5664}).
 */
public final class DownedState {
    public record Data(boolean isDowned, java.util.Optional<UUID> armedBy) {
        public static final Data HEALTHY = new Data(false, java.util.Optional.empty());
    }

    public static final AttachmentType<Data> ATTACHMENT =
            AttachmentRegistry.<Data>builder()
                    .persistent(RecordCodecBuilder.create(i -> i.group(
                            Codec.BOOL.optionalFieldOf("isDowned", false).forGetter(Data::isDowned),
                            UUIDUtil.CODEC.optionalFieldOf("armedBy").forGetter(Data::armedBy)
                    ).apply(i, Data::new)))
                    .copyOnDeath()
                    .buildAndRegister(Identifier.fromNamespaceAndPath("hearthwind", "downed"));

    private DownedState() {}

    public static Data get(ServerPlayer player) {
        return player.getAttachedOrElse(ATTACHMENT, Data.HEALTHY);
    }

    public static void set(ServerPlayer player, Data data) {
        player.setAttached(ATTACHMENT, data);
    }

    public static boolean isDowned(ServerPlayer player) {
        return get(player).isDowned();
    }

    /** True once an ally has armed the revive button (reference: {@code canRevive}). */
    public static boolean isArmed(ServerPlayer player) {
        return get(player).armedBy().isPresent();
    }

    public static void clear(ServerPlayer player) {
        set(player, Data.HEALTHY);
    }
}