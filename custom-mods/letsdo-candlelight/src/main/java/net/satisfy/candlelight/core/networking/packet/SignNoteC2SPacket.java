package net.satisfy.candlelight.core.networking.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.satisfy.candlelight.core.networking.CandlelightMessages;
import org.jetbrains.annotations.NotNull;

public record SignNoteC2SPacket(CompoundTag stack, int slot, boolean sign) implements CustomPacketPayload {

    public static final Type<SignNoteC2SPacket> TYPE = new Type<>(CandlelightMessages.SIGN_NOTE);

    public static final StreamCodec<RegistryFriendlyByteBuf, SignNoteC2SPacket> STREAM_CODEC =
            StreamCodec.of(SignNoteC2SPacket::toNetwork, SignNoteC2SPacket::fromNetwork);

    public static void toNetwork(RegistryFriendlyByteBuf buf, SignNoteC2SPacket msg) {
        buf.writeNbt(msg.stack());
        buf.writeInt(msg.slot());
        buf.writeBoolean(msg.sign());
    }

    public static SignNoteC2SPacket fromNetwork(RegistryFriendlyByteBuf buf) {
        CompoundTag stack = buf.readNbt();
        int slot = buf.readInt();
        boolean sign = buf.readBoolean();
        return new SignNoteC2SPacket(stack, slot, sign);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

