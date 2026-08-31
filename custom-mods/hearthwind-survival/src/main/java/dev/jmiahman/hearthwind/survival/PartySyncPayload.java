package dev.jmiahman.hearthwind.survival;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PartySyncPayload(
        String partyName,
        boolean isLeader,
        boolean pvpEnabled,
        List<MemberInfo> members) implements CustomPacketPayload {

    public static final Type<PartySyncPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("hearthwind", "party_sync"));

    public static final StreamCodec<ByteBuf, MemberInfo> MEMBER_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MemberInfo::name,
            ByteBufCodecs.FLOAT, MemberInfo::health,
            ByteBufCodecs.FLOAT, MemberInfo::maxHealth,
            ByteBufCodecs.VAR_INT, MemberInfo::distance,
            ByteBufCodecs.BOOL, MemberInfo::isLeader,
            MemberInfo::new);

    public static final StreamCodec<ByteBuf, PartySyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PartySyncPayload::partyName,
            ByteBufCodecs.BOOL, PartySyncPayload::isLeader,
            ByteBufCodecs.BOOL, PartySyncPayload::pvpEnabled,
            MEMBER_CODEC.apply(ByteBufCodecs.list()), PartySyncPayload::members,
            PartySyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record MemberInfo(String name, float health, float maxHealth, int distance, boolean isLeader) {}
}
