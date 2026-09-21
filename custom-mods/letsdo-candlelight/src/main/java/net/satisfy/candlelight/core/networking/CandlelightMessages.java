package net.satisfy.candlelight.core.networking;

import dev.architectury.networking.NetworkManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.core.networking.handler.SignNoteC2SPacketHandler;
import net.satisfy.candlelight.core.networking.handler.TypewriterDataC2SPacketHandler;
import net.satisfy.candlelight.core.networking.packet.SetWallDecorationTextPacket;
import net.satisfy.candlelight.core.networking.packet.SignNoteC2SPacket;
import net.satisfy.candlelight.core.networking.packet.SyncTypewriterDataC2SPacket;

public class CandlelightMessages {
    public static final Identifier TYPEWRITER_SYNC = Candlelight.identifier("typewriter_sync");
    public static final Identifier SIGN_NOTE = Candlelight.identifier("sign_note");
    public static final Identifier SET_SIGN_TEXT = Candlelight.identifier("set_sign_text");

    public static void sendSetSignTextToServer(SetWallDecorationTextPacket packet) {
        NetworkManager.sendToServer(packet);
    }

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), SetWallDecorationTextPacket.TYPE, SetWallDecorationTextPacket.STREAM_CODEC, (packet, context) -> context.queue(() -> SetWallDecorationTextPacket.handle(packet, (ServerPlayer) context.getPlayer())));
        NetworkManager.registerReceiver(NetworkManager.c2s(), SyncTypewriterDataC2SPacket.TYPE, SyncTypewriterDataC2SPacket.STREAM_CODEC, new TypewriterDataC2SPacketHandler());
        NetworkManager.registerReceiver(NetworkManager.c2s(), SignNoteC2SPacket.TYPE, SignNoteC2SPacket.STREAM_CODEC, new SignNoteC2SPacketHandler());
    }
}