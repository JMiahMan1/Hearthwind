package net.backslot.network;

import io.netty.buffer.ByteBuf;
import net.backslot.BackSlot;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** C2S request to swap the held item with the back (41) or belt (42) slot. */
public record SwitchSlotPayload(int slot) implements CustomPacketPayload {

    public static final Type<SwitchSlotPayload> TYPE = new Type<>(BackSlot.id("switch_slot"));

    public static final StreamCodec<ByteBuf, SwitchSlotPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SwitchSlotPayload::slot, SwitchSlotPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
