package dev.jmiahman.hearthwind.survival;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client list of translation keys for the threshold effects that
 * back the Nutrients panel hover tooltips (negative and positive per
 * nutrient). Sent on join; refreshed on datapack reload.
 */
public record NutritionEffectsPayload(List<List<String>> positive, List<List<String>> negative)
        implements CustomPacketPayload {

    public static final Type<NutritionEffectsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "nutrition_effects"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NutritionEffectsPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public NutritionEffectsPayload decode(RegistryFriendlyByteBuf buf) {
                    List<List<String>> positive = readGroups(buf);
                    List<List<String>> negative = readGroups(buf);
                    return new NutritionEffectsPayload(positive, negative);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, NutritionEffectsPayload payload) {
                    writeGroups(buf, payload.positive());
                    writeGroups(buf, payload.negative());
                }
            };

    private static List<List<String>> readGroups(RegistryFriendlyByteBuf buf) {
        List<List<String>> groups = new ArrayList<>(HearthwindSurvivalDiet.NUTRIENT_COUNT);
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            int count = buf.readVarInt();
            List<String> keys = new ArrayList<>(count);
            for (int k = 0; k < count; k++) {
                keys.add(buf.readUtf());
            }
            groups.add(keys);
        }
        return groups;
    }

    private static void writeGroups(RegistryFriendlyByteBuf buf, List<List<String>> groups) {
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            List<String> keys = i < groups.size() ? groups.get(i) : List.of();
            buf.writeVarInt(keys.size());
            for (String key : keys) {
                buf.writeUtf(key);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
