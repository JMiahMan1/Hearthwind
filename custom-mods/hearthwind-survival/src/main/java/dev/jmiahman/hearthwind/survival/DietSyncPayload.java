package dev.jmiahman.hearthwind.survival;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Five integer nutrient levels (0..maxNutrition) in NutritionZ order:
 * carbohydrates, protein, fat, vitamins, minerals.
 */
public record DietSyncPayload(int carbohydrates, int protein, int fat, int vitamins, int minerals)
        implements CustomPacketPayload {

    public static final Type<DietSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "diet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DietSyncPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public DietSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    return new DietSyncPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                            buf.readVarInt(), buf.readVarInt());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, DietSyncPayload payload) {
                    buf.writeVarInt(payload.carbohydrates());
                    buf.writeVarInt(payload.protein());
                    buf.writeVarInt(payload.fat());
                    buf.writeVarInt(payload.vitamins());
                    buf.writeVarInt(payload.minerals());
                }
            };

    public static DietSyncPayload of(int[] nutrients) {
        return new DietSyncPayload(nutrients[0], nutrients[1], nutrients[2], nutrients[3], nutrients[4]);
    }

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
