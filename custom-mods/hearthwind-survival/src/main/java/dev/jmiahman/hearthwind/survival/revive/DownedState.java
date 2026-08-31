package dev.jmiahman.hearthwind.survival.revive;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class DownedState {
    public record Data(boolean isDowned, int bleedoutTicks, int reviveTicks, java.util.Optional<UUID> reviverUuid) {
        public static final Data HEALTHY = new Data(false, 0, 0, java.util.Optional.empty());
    }

    public static final AttachmentType<Data> ATTACHMENT =
            AttachmentRegistry.<Data>builder()
                    .persistent(RecordCodecBuilder.create(i -> i.group(
                            Codec.BOOL.optionalFieldOf("isDowned", false).forGetter(Data::isDowned),
                            Codec.INT.optionalFieldOf("bleedoutTicks", 0).forGetter(Data::bleedoutTicks),
                            Codec.INT.optionalFieldOf("reviveTicks", 0).forGetter(Data::reviveTicks),
                            UUIDUtil.CODEC.optionalFieldOf("reviverUuid").forGetter(Data::reviverUuid)
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

    public static void clear(ServerPlayer player) {
        set(player, Data.HEALTHY);
    }
}
