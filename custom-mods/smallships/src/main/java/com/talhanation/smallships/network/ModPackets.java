package com.talhanation.smallships.network;

import com.talhanation.smallships.SmallShipsMod;
import com.talhanation.smallships.network.fabric.ModPacketsImpl;
import com.talhanation.smallships.network.packet.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class ModPackets {
    public static void registerPackets() {
        registerNonPacket(ServerboundOpenShipScreenPacket.TYPE, ServerboundOpenShipScreenPacket.CODEC, ModPacket.Side.SERVERBOUND);
        registerNonPacket(ServerboundToggleShipSailPacket.TYPE, ServerboundToggleShipSailPacket.CODEC, ModPacket.Side.SERVERBOUND);
        registerNonPacket(ServerboundShootShipCannonPacket.TYPE, ServerboundShootShipCannonPacket.CODEC, ModPacket.Side.SERVERBOUND);
        registerNonPacket(ServerboundShootGroundCannonPacket.TYPE, ServerboundShootGroundCannonPacket.CODEC, ModPacket.Side.SERVERBOUND);
        registerNonPacket(ServerboundEnterCannonBarrelPacket.TYPE, ServerboundEnterCannonBarrelPacket.CODEC, ModPacket.Side.SERVERBOUND);
        registerNonPacket(ServerboundSetSailStatePacket.TYPE, ServerboundSetSailStatePacket.CODEC, ModPacket.Side.SERVERBOUND);
        registerNonPacket(ServerboundUpdateShipControlPacket.TYPE, ServerboundUpdateShipControlPacket.CODEC, ModPacket.Side.SERVERBOUND);
    }

    @SuppressWarnings("unchecked")
    private static <T extends ModPacket> void registerNonPacket(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, ModPacket.Side side) {
        registerPacket((CustomPacketPayload.Type<ModPacket>)type, (StreamCodec<RegistryFriendlyByteBuf, ModPacket>)codec, side);
    }

    public static void registerPacket(CustomPacketPayload.Type<ModPacket> type, StreamCodec<RegistryFriendlyByteBuf, ModPacket> codec, ModPacket.Side side) {
        ModPacketsImpl.registerPacket(type, codec, side);
    }

    public static void serverSendPacket(ServerPlayer player, ModPacket packet) {
        ModPacketsImpl.serverSendPacket(player, packet);
    }

    public static void clientSendPacket(ModPacket packet) {
        ModPacketsImpl.clientSendPacket(packet);
    }

    @SuppressWarnings("SameParameterValue")
    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID, id);
    }
}
