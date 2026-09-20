package net.dungeonz.compat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.dungeonz.DungeonzMain;
import net.minecraft.server.level.ServerPlayer;

public final class HearthwindGroups {
    public record Group(List<UUID> members, Optional<UUID> leader) {
        public Group {
            members = List.copyOf(members);
        }
    }

    private HearthwindGroups() {}

    public static List<UUID> groupPlayerIdList(ServerPlayer player) {
        return group(player).members();
    }

    public static boolean admits(ServerPlayer player, UUID firstOccupant) {
        return groupPlayerIdList(player).contains(firstOccupant);
    }

    public static Group group(ServerPlayer player) {
        if (!DungeonzMain.isPartyAddonLoaded) {
            return new Group(List.of(), Optional.empty());
        }
        try {
            if (DungeonzMain.isHearthwindSkillsLoaded) {
                Object party = PartyApi.GET_PARTY.invoke(null, player.getUUID());
                if (party == null) {
                    return new Group(List.of(), Optional.empty());
                }
                return new Group(uuids(PartyApi.GET_MEMBERS.invoke(party)), Optional.of((UUID) PartyApi.GET_LEADER.invoke(party)));
            }
            Object manager = player.getClass().getMethod("getGroupManager").invoke(player);
            return new Group(uuids(manager.getClass().getMethod("getGroupPlayerIdList").invoke(manager)), Optional.empty());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot read dungeon admission party", exception);
        }
    }

    private static List<UUID> uuids(Object value) {
        return ((List<?>) value).stream().map(UUID.class::cast).toList();
    }

    private static final class PartyApi {
        private static final Method GET_PARTY;
        private static final Method GET_MEMBERS;
        private static final Method GET_LEADER;

        static {
            try {
                GET_PARTY = Class.forName("dev.jmiahman.hearthwind.skills.party.PartyManager").getMethod("getPartyByPlayer", UUID.class);
                Class<?> party = Class.forName("dev.jmiahman.hearthwind.skills.party.Party");
                GET_MEMBERS = party.getMethod("getMembers");
                GET_LEADER = party.getMethod("getLeader");
            } catch (ReflectiveOperationException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }
    }
}
