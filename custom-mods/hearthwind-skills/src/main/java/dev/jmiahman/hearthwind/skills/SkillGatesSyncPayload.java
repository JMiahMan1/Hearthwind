package dev.jmiahman.hearthwind.skills;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record SkillGatesSyncPayload(boolean enabled, List<Entry> entries) implements CustomPacketPayload {
    public static final Type<SkillGatesSyncPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("hearthwind_skills", "gate_digest"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillGatesSyncPayload> CODEC = new StreamCodec<>() {
        @Override
        public SkillGatesSyncPayload decode(RegistryFriendlyByteBuf buffer) {
            boolean enabled = buffer.readBoolean();
            int count = buffer.readVarInt();
            java.util.ArrayList<Entry> entries = new java.util.ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                entries.add(new Entry(Kind.fromWire(buffer.readVarInt()), buffer.readIdentifier(),
                        buffer.readUtf(), buffer.readVarInt()));
            }
            return new SkillGatesSyncPayload(enabled, entries);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, SkillGatesSyncPayload payload) {
            buffer.writeBoolean(payload.enabled());
            buffer.writeVarInt(payload.entries().size());
            for (Entry entry : payload.entries()) {
                buffer.writeVarInt(entry.kind().wireId());
                buffer.writeIdentifier(entry.target());
                buffer.writeUtf(entry.skill());
                buffer.writeVarInt(entry.level());
            }
        }
    };

    public SkillGatesSyncPayload {
        entries = List.copyOf(entries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Kind {
        BREAK(0),
        BLOCK_USE(1),
        ITEM_USE(2),
        CRAFT(3),
        SMITHING(4),
        BREWING(5),
        ENTITY(6);

        private final int wireId;

        Kind(int wireId) {
            this.wireId = wireId;
        }

        public int wireId() {
            return wireId;
        }

        public static Kind fromWire(int wireId) {
            for (Kind kind : values()) {
                if (kind.wireId == wireId) {
                    return kind;
                }
            }
            throw new IllegalArgumentException("Unknown skill gate kind: " + wireId);
        }
    }

    public record Entry(Kind kind, Identifier target, String skill, int level) {
    }
}
