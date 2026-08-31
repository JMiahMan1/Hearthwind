package dev.jmiahman.hearthwind.skills.party;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.jmiahman.hearthwind.skills.Skill;
import dev.jmiahman.hearthwind.skills.SkillXp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class PartyManager {
    private static final Map<UUID, Party> partiesById = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> playerToParty = new ConcurrentHashMap<>();
    private static final Map<UUID, Invite> pendingInvites = new ConcurrentHashMap<>();
    private static final Map<UUID, ServerPlayer> activePlayers = new ConcurrentHashMap<>();

    public record Invite(UUID partyId, UUID fromPlayer, long timestamp) {}

    private PartyManager() {}

    public static Party getParty(UUID partyId) {
        if (partyId == null) return null;
        return partiesById.get(partyId);
    }

    public static Party getPartyByPlayer(UUID playerUuid) {
        if (playerUuid == null) return null;
        UUID partyId = playerToParty.get(playerUuid);
        return partyId == null ? null : partiesById.get(partyId);
    }

    public static void reset() {
        partiesById.clear();
        playerToParty.clear();
        pendingInvites.clear();
        activePlayers.clear();
    }

    public static boolean areInSameParty(UUID playerA, UUID playerB) {
        if (playerA == null || playerB == null || playerA.equals(playerB)) return false;
        UUID partyA = playerToParty.get(playerA);
        UUID partyB = playerToParty.get(playerB);
        return partyA != null && partyA.equals(partyB);
    }

    public static Party createParty(ServerPlayer leader, String name) {
        if (leader == null) return null;
        activePlayers.put(leader.getUUID(), leader);
        if (getPartyByPlayer(leader.getUUID()) != null) {
            leader.sendSystemMessage(Component.literal("You are already in a party! Leave it first.").withStyle(ChatFormatting.RED));
            return null;
        }
        UUID partyId = UUID.randomUUID();
        String partyName = (name == null || name.isBlank()) ? (leader.getName().getString() + "'s Party") : name.trim();
        Party party = new Party(partyId, partyName, leader.getUUID());
        partiesById.put(partyId, party);
        playerToParty.put(leader.getUUID(), partyId);

        leader.sendSystemMessage(Component.literal("Created party: " + partyName).withStyle(ChatFormatting.GREEN));
        PartySync.syncTo(leader);
        return party;
    }

    public static void removePlayer(UUID uuid) {
        if (uuid != null) {
            activePlayers.remove(uuid);
        }
    }

    public static boolean invitePlayer(ServerPlayer leader, ServerPlayer target) {
        if (leader == null || target == null) {
            return false;
        }
        activePlayers.put(leader.getUUID(), leader);
        activePlayers.put(target.getUUID(), target);
        Party party = getPartyByPlayer(leader.getUUID());
        if (party == null) {
            leader.sendSystemMessage(Component.literal("You are not in a party! Create one with /party create").withStyle(ChatFormatting.RED));
            return false;
        }
        if (!party.isLeader(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("Only the party leader can invite players!").withStyle(ChatFormatting.RED));
            return false;
        }
        if (getPartyByPlayer(target.getUUID()) != null) {
            leader.sendSystemMessage(Component.literal(target.getName().getString() + " is already in a party!").withStyle(ChatFormatting.RED));
            return false;
        }

        pendingInvites.put(target.getUUID(), new Invite(party.getId(), leader.getUUID(), System.currentTimeMillis()));
        leader.sendSystemMessage(Component.literal("Invited " + target.getName().getString() + " to the party!").withStyle(ChatFormatting.GREEN));
        target.sendSystemMessage(Component.literal(leader.getName().getString() + " invited you to join '" + party.getName() + "'! Type /party accept to join.").withStyle(ChatFormatting.GOLD));
        return true;
    }

    public static boolean acceptInvite(ServerPlayer player) {
        if (player != null) activePlayers.put(player.getUUID(), player);
        Invite invite = pendingInvites.remove(player.getUUID());
        if (invite == null || System.currentTimeMillis() - invite.timestamp() > 60000) {
            player.sendSystemMessage(Component.literal("You have no pending party invitations (or it expired).").withStyle(ChatFormatting.RED));
            return false;
        }
        Party party = getParty(invite.partyId());
        if (party == null) {
            player.sendSystemMessage(Component.literal("That party no longer exists.").withStyle(ChatFormatting.RED));
            return false;
        }
        if (getPartyByPlayer(player.getUUID()) != null) {
            player.sendSystemMessage(Component.literal("You are already in a party!").withStyle(ChatFormatting.RED));
            return false;
        }

        party.addMember(player.getUUID());
        playerToParty.put(player.getUUID(), party.getId());

        MinecraftServer server = player.level().getServer();
        broadcast(party, server, Component.literal(player.getName().getString() + " joined the party!").withStyle(ChatFormatting.GREEN));
        PartySync.syncParty(party, server);
        return true;
    }

    public static boolean leaveParty(ServerPlayer player) {
        Party party = getPartyByPlayer(player.getUUID());
        if (party == null) {
            player.sendSystemMessage(Component.literal("You are not in a party!").withStyle(ChatFormatting.RED));
            return false;
        }
        playerToParty.remove(player.getUUID());
        party.removeMember(player.getUUID());
        player.sendSystemMessage(Component.literal("You left the party.").withStyle(ChatFormatting.YELLOW));
        PartySync.clearFor(player);

        MinecraftServer server = player.level().getServer();
        if (party.getMembers().isEmpty()) {
            partiesById.remove(party.getId());
        } else {
            if (party.isLeader(player.getUUID())) {
                UUID newLeader = party.getMembers().get(0);
                party.setLeader(newLeader);
                broadcast(party, server, Component.literal(player.getName().getString() + " left. New leader is now " + getPlayerName(newLeader, server)).withStyle(ChatFormatting.YELLOW));
            } else {
                broadcast(party, server, Component.literal(player.getName().getString() + " left the party.").withStyle(ChatFormatting.YELLOW));
            }
            PartySync.syncParty(party, server);
        }
        return true;
    }

    public static boolean kickPlayer(ServerPlayer leader, ServerPlayer target) {
        Party party = getPartyByPlayer(leader.getUUID());
        if (party == null || !party.isLeader(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("Only the party leader can kick members!").withStyle(ChatFormatting.RED));
            return false;
        }
        if (!party.isMember(target.getUUID()) || target.getUUID().equals(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("Cannot kick this player.").withStyle(ChatFormatting.RED));
            return false;
        }
        playerToParty.remove(target.getUUID());
        party.removeMember(target.getUUID());

        target.sendSystemMessage(Component.literal("You were kicked from the party.").withStyle(ChatFormatting.RED));
        PartySync.clearFor(target);
        MinecraftServer server = leader.level().getServer();
        broadcast(party, server, Component.literal(target.getName().getString() + " was kicked from the party.").withStyle(ChatFormatting.YELLOW));
        PartySync.syncParty(party, server);
        return true;
    }

    public static boolean disbandParty(ServerPlayer leader) {
        Party party = getPartyByPlayer(leader.getUUID());
        if (party == null || !party.isLeader(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("Only the party leader can disband the party!").withStyle(ChatFormatting.RED));
            return false;
        }
        MinecraftServer server = leader.level().getServer();
        broadcast(party, server, Component.literal("The party has been disbanded by the leader.").withStyle(ChatFormatting.RED));
        for (UUID member : party.getMembers()) {
            playerToParty.remove(member);
            if (server != null) {
                ServerPlayer sp = server.getPlayerList().getPlayer(member);
                if (sp != null) {
                    PartySync.clearFor(sp);
                }
            }
        }
        partiesById.remove(party.getId());
        return true;
    }

    public static void togglePvp(ServerPlayer leader, boolean enable) {
        Party party = getPartyByPlayer(leader.getUUID());
        if (party == null || !party.isLeader(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("Only the party leader can change PvP settings!").withStyle(ChatFormatting.RED));
            return;
        }
        party.setPvpEnabled(enable);
        MinecraftServer server = leader.level().getServer();
        broadcast(party, server, Component.literal("Party Friendly Fire has been turned " + (enable ? "ON" : "OFF")).withStyle(enable ? ChatFormatting.RED : ChatFormatting.GREEN));
        PartySync.syncParty(party, server);
    }

    public static void shareXp(ServerPlayer source, Skill skill, int points) {
        if (source == null || skill == null || points <= 0) return;
        Party party = getPartyByPlayer(source.getUUID());
        if (party == null || party.getMembers().size() <= 1) return;

        MinecraftServer server = source.level().getServer();
        if (server == null) return;

        int dimensionMembers = 0;
        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = findMember(memberId, source, server);
            if (member != null && member.level() == source.level()) {
                dimensionMembers++;
            }
        }

        // Aged parity: +5% XP bonus per player in the same dimension
        double bonusMultiplier = 1.0 + 0.05 * Math.max(0, dimensionMembers - 1);
        int sharedPoints = Math.max(1, (int) Math.round(points * bonusMultiplier));

        for (UUID memberId : party.getMembers()) {
            if (memberId.equals(source.getUUID())) continue;
            ServerPlayer member = findMember(memberId, source, server);
            if (member != null && member.level() == source.level() && member.distanceToSqr(source) <= 32 * 32) {
                SkillXp.award(member, skill, sharedPoints);
            }
        }
    }

    private static ServerPlayer findMember(UUID memberId, ServerPlayer source, MinecraftServer server) {
        ServerPlayer cached = activePlayers.get(memberId);
        if (cached != null) return cached;
        if (server != null) {
            ServerPlayer sp = server.getPlayerList().getPlayer(memberId);
            if (sp != null) return sp;
        }
        if (source != null && source.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            net.minecraft.world.entity.Entity e = sl.getEntity(memberId);
            if (e instanceof ServerPlayer sp) return sp;
            for (var p : sl.players()) {
                if (p instanceof ServerPlayer sp && sp.getUUID().equals(memberId)) {
                    return sp;
                }
            }
        }
        return null;
    }

    private static void broadcast(Party party, MinecraftServer server, Component msg) {
        if (server == null) return;
        for (UUID member : party.getMembers()) {
            ServerPlayer sp = findMember(member, null, server);
            if (sp != null) {
                sp.sendSystemMessage(msg);
            }
        }
    }

    private static String getPlayerName(UUID uuid, MinecraftServer server) {
        if (server == null) return "Unknown";
        ServerPlayer sp = findMember(uuid, null, server);
        return sp == null ? "Unknown" : sp.getName().getString();
    }
}
