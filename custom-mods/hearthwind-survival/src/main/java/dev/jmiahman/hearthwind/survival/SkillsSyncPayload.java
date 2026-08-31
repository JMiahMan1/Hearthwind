package dev.jmiahman.hearthwind.survival;

import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Full skill-state sync (all 12 skills, not just level-ups): sent on login
 * and after every level-up so panel tabs and toasts always reflect reality.
 */
public record SkillsSyncPayload(List<String> skills, List<Integer> levels) implements CustomPacketPayload {
    public static final Type<SkillsSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "skills_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillsSyncPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public SkillsSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    int count = buf.readVarInt();
                    String[] skills = new String[count];
                    int[] levels = new int[count];
                    for (int i = 0; i < count; i++) {
                        skills[i] = buf.readUtf();
                        levels[i] = buf.readVarInt();
                    }
                    return new SkillsSyncPayload(List.of(skills), toBoxed(levels));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, SkillsSyncPayload payload) {
                    buf.writeVarInt(payload.skills().size());
                    for (int i = 0; i < payload.skills().size(); i++) {
                        buf.writeUtf(payload.skills().get(i));
                        buf.writeVarInt(payload.levels().get(i));
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static List<Integer> toBoxed(int[] values) {
        Integer[] boxed = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            boxed[i] = values[i];
        }
        return List.of(boxed);
    }
}
