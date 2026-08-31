package dev.jmiahman.hearthwind.skills.party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import dev.jmiahman.hearthwind.survival.PartySyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class PartySync {
    private PartySync() {}

    public static void syncTo(ServerPlayer player) {
        if (player == null) return;
        Party party = PartyManager.getPartyByPlayer(player.getUUID());
        if (party == null) {
            clearFor(player);
            return;
        }

        MinecraftServer server = player.level().getServer();
        List<PartySyncPayload.MemberInfo> memberInfos = new ArrayList<>();
        for (UUID memberId : party.getMembers()) {
            if (server != null) {
                ServerPlayer member = server.getPlayerList().getPlayer(memberId);
                if (member != null) {
                    int dist = (member.level() == player.level())
                            ? (int) Math.sqrt(member.distanceToSqr(player))
                            : -1;
                    memberInfos.add(new PartySyncPayload.MemberInfo(
                            member.getName().getString(),
                            member.getHealth(),
                            member.getMaxHealth(),
                            dist,
                            party.isLeader(memberId)));
                }
            }
        }

        PartySyncPayload payload = new PartySyncPayload(
                party.getName(),
                party.isLeader(player.getUUID()),
                party.isPvpEnabled(),
                memberInfos);

        if (ServerPlayNetworking.canSend(player, PartySyncPayload.TYPE)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void syncParty(Party party, MinecraftServer server) {
        if (party == null || server == null) return;
        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                syncTo(member);
            }
        }
    }

    public static void clearFor(ServerPlayer player) {
        if (player == null) return;
        PartySyncPayload empty = new PartySyncPayload(
                "", false, false, Collections.emptyList());
        if (ServerPlayNetworking.canSend(player, PartySyncPayload.TYPE)) {
            ServerPlayNetworking.send(player, empty);
        }
    }
}
