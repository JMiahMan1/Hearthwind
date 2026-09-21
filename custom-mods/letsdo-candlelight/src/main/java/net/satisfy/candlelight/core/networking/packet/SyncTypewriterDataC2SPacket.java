package net.satisfy.candlelight.core.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.satisfy.candlelight.core.networking.CandlelightMessages;
import org.jetbrains.annotations.NotNull;

public record SyncTypewriterDataC2SPacket(CompoundTag nbt, BlockPos pos, boolean sign) implements CustomPacketPayload {

    public static final Type<SyncTypewriterDataC2SPacket> TYPE = new Type<>(CandlelightMessages.TYPEWRITER_SYNC);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTypewriterDataC2SPacket> STREAM_CODEC =
            StreamCodec.of(SyncTypewriterDataC2SPacket::toNetwork, SyncTypewriterDataC2SPacket::fromNetwork);

    public static void toNetwork(RegistryFriendlyByteBuf buf, SyncTypewriterDataC2SPacket msg) {
        buf.writeNbt(msg.nbt());
        buf.writeBlockPos(msg.pos());
        buf.writeBoolean(msg.sign());
    }

    public static SyncTypewriterDataC2SPacket fromNetwork(RegistryFriendlyByteBuf buf) {
        CompoundTag nbt = buf.readNbt();
        BlockPos pos = buf.readBlockPos();
        boolean sign = buf.readBoolean();
        return new SyncTypewriterDataC2SPacket(nbt, pos, sign);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
