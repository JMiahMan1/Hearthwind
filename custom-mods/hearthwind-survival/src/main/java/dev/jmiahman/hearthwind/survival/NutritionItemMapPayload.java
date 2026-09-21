package dev.jmiahman.hearthwind.survival;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client copy of the nutrition item map. Sent on join so the
 * client can print the Shift tooltip without shipping the datapack.
 */
public record NutritionItemMapPayload(List<Entry> entries) implements CustomPacketPayload {
    public record Entry(Identifier item, int carbohydrates, int protein, int fat, int vitamins, int minerals) {
        public int get(int index) {
            return switch (index) {
                case 0 -> this.carbohydrates;
                case 1 -> this.protein;
                case 2 -> this.fat;
                case 3 -> this.vitamins;
                case 4 -> this.minerals;
                default -> 0;
            };
        }
    }

    public static final Type<NutritionItemMapPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "nutrition_items"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NutritionItemMapPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public NutritionItemMapPayload decode(RegistryFriendlyByteBuf buf) {
                    int count = buf.readVarInt();
                    List<Entry> entries = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        Identifier item = buf.readIdentifier();
                        entries.add(new Entry(item, buf.readVarInt(), buf.readVarInt(),
                                buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
                    }
                    return new NutritionItemMapPayload(entries);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, NutritionItemMapPayload payload) {
                    buf.writeVarInt(payload.entries().size());
                    for (Entry entry : payload.entries()) {
                        buf.writeIdentifier(entry.item());
                        buf.writeVarInt(entry.carbohydrates());
                        buf.writeVarInt(entry.protein());
                        buf.writeVarInt(entry.fat());
                        buf.writeVarInt(entry.vitamins());
                        buf.writeVarInt(entry.minerals());
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
