package net.satisfy.meadow.client.gui.handler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.satisfy.meadow.Meadow;

import java.util.ArrayList;
import java.util.List;

/**
 * Server -> client sync of the woodcutter's currently available recipe
 * results. 26.2 no longer sends full recipes to the client, so the menu
 * resolves matches server-side and mirrors display results here. Crafting
 * stays server-authoritative (selected index validates against the server
 * recipe list).
 */
public record WoodcutterRecipesPayload(int containerId, List<ItemStack> results) implements CustomPacketPayload {
    public static final Type<WoodcutterRecipesPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Meadow.MOD_ID, "woodcutter_recipes"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WoodcutterRecipesPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public WoodcutterRecipesPayload decode(RegistryFriendlyByteBuf buf) {
                    int containerId = buf.readVarInt();
                    int size = buf.readVarInt();
                    List<ItemStack> results = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        results.add(ItemStack.STREAM_CODEC.decode(buf));
                    }
                    return new WoodcutterRecipesPayload(containerId, results);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, WoodcutterRecipesPayload payload) {
                    buf.writeVarInt(payload.containerId());
                    buf.writeVarInt(payload.results().size());
                    for (ItemStack stack : payload.results()) {
                        ItemStack.STREAM_CODEC.encode(buf, stack);
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
