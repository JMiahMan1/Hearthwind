package dev.jmiahman.hearthwind.survival;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DietSyncPayload(float[] nutrients) implements CustomPacketPayload {
    public static final Type<DietSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "diet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DietSyncPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public DietSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    int size = buf.readInt();
                    float[] arr = new float[size];
                    for (int i = 0; i < size; i++) arr[i] = buf.readFloat();
                    return new DietSyncPayload(arr);
                }
                @Override
                public void encode(RegistryFriendlyByteBuf buf, DietSyncPayload payload) {
                    buf.writeInt(payload.nutrients().length);
                    for (float v : payload.nutrients()) buf.writeFloat(v);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
