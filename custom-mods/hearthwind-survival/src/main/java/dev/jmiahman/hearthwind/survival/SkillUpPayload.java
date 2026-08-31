package dev.jmiahman.hearthwind.survival;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SkillUpPayload(String skill, int newLevel) implements CustomPacketPayload {
    public static final Type<SkillUpPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("hearthwind_survival", "skill_up"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillUpPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public SkillUpPayload decode(RegistryFriendlyByteBuf buf) {
                    String skill = buf.readUtf();
                    int newLevel = buf.readInt();
                    return new SkillUpPayload(skill, newLevel);
                }
                @Override
                public void encode(RegistryFriendlyByteBuf buf, SkillUpPayload payload) {
                    buf.writeUtf(payload.skill());
                    buf.writeInt(payload.newLevel());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
