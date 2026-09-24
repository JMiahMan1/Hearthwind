package net.fleshz.network.packet;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RackPacket(List<Identifier> rackItems, List<Identifier> rackResultItems, List<Integer> rackResultTimes)
        implements CustomPacketPayload {

    public static final Type<RackPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("fleshz", "rack_recipes_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RackPacket> PACKET_CODEC = new StreamCodec<>() {
        @Override
        public RackPacket decode(RegistryFriendlyByteBuf buf) {
            List<Identifier> items = readIdList(buf);
            List<Identifier> results = readIdList(buf);
            int n = buf.readVarInt();
            List<Integer> times = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                times.add(buf.readVarInt());
            }
            return new RackPacket(items, results, times);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, RackPacket payload) {
            writeIdList(buf, payload.rackItems);
            writeIdList(buf, payload.rackResultItems);
            buf.writeVarInt(payload.rackResultTimes.size());
            for (Integer time : payload.rackResultTimes) {
                buf.writeVarInt(time);
            }
        }

        private List<Identifier> readIdList(RegistryFriendlyByteBuf buf) {
            int n = buf.readVarInt();
            List<Identifier> list = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                list.add(buf.readIdentifier());
            }
            return list;
        }

        private void writeIdList(RegistryFriendlyByteBuf buf, List<Identifier> list) {
            buf.writeVarInt(list.size());
            for (Identifier id : list) {
                buf.writeIdentifier(id);
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
