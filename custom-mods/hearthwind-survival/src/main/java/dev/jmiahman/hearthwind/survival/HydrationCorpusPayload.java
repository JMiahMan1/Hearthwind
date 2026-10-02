package dev.jmiahman.hearthwind.survival;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -&gt; client copy of the hydration corpus, sent once on join.
 *
 * <p>Needed because 26.2 draws item tooltips entirely on the client
 * ({@code Item#getTooltipImage} takes no player, unlike 1.20.1's
 * server-side {@code getTooltipImage}) while the corpus is a world datapack
 * the client never reads. Shipping the map keeps one source of truth - a
 * second corpus file on the client would drift.
 */
public record HydrationCorpusPayload(List<Entry> entries) implements CustomPacketPayload {
    public record Entry(Identifier item, int hydration) {}

    public static final Type<HydrationCorpusPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "hydration_corpus"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HydrationCorpusPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public HydrationCorpusPayload decode(RegistryFriendlyByteBuf buf) {
                    int count = buf.readVarInt();
                    List<Entry> entries = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        entries.add(new Entry(buf.readIdentifier(), buf.readVarInt()));
                    }
                    return new HydrationCorpusPayload(entries);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, HydrationCorpusPayload payload) {
                    buf.writeVarInt(payload.entries().size());
                    for (Entry entry : payload.entries()) {
                        buf.writeIdentifier(entry.item());
                        buf.writeVarInt(entry.hydration());
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}