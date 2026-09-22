package io.wispforest.lavender;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.lavender.book.LavenderBookItem;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;

import java.util.UUID;

public class Lavender implements ModInitializer {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "lavender";
    public static final SoundEvent ITEM_BOOK_OPEN = SoundEvent.createVariableRangeEvent(id("item.book.open"));

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(Lavender.id("main"));

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.ITEM, id("dynamic_book"), LavenderBookItem.DYNAMIC_BOOK);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ITEM_BOOK_OPEN.location(), ITEM_BOOK_OPEN);

        PayloadTypeRegistry.clientboundPlay().register(WorldUUIDPayload.ID, CodecUtils.toPacketCodec(WorldUUIDPayload.ENDEC));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sender.sendPacket(new WorldUUIDPayload(server.overworld().getDataStorage().computeIfAbsent(WorldUUIDState.TYPE).id));
        });

        LavenderClientRecipeCache.initialize();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static class WorldUUIDState extends SavedData {

        public static final SavedDataType<WorldUUIDState> TYPE = new SavedDataType<>(
                Lavender.id("lavender_world_id"),
                () -> new WorldUUIDState(UUID.randomUUID()),
                com.mojang.serialization.MapCodec.unit(() -> new WorldUUIDState(new UUID(0, 0))).codec(),
                DataFixTypes.LEVEL);

        public final UUID id;

        private WorldUUIDState(UUID id) {
            this.id = id;
        }

        public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registryLookup) {
            nbt.putString("UUID", id.toString());
            return nbt;
        }

        public static WorldUUIDState read(CompoundTag nbt, HolderLookup.Provider lookup) {
            var raw = nbt.getStringOr("UUID", "00000000-0000-0000-0000-000000000000");
            return new WorldUUIDState(java.util.UUID.fromString(raw));
        }
    }

    public record WorldUUIDPayload(UUID worldUuid) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<WorldUUIDPayload> ID = new CustomPacketPayload.Type<>(Lavender.id("world_uuid"));
        public static final Endec<WorldUUIDPayload> ENDEC = StructEndecBuilder.of(
                BuiltInEndecs.UUID.fieldOf("world_uuid", WorldUUIDPayload::worldUuid),
                WorldUUIDPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
