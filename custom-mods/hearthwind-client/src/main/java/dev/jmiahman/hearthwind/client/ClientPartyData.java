package dev.jmiahman.hearthwind.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClientPartyData {
    private static String partyName = "";
    private static boolean isLeader = false;
    private static boolean pvpEnabled = false;
    private static final List<MemberEntry> members = new ArrayList<>();

    public record MemberEntry(String name, float health, float maxHealth, int distance, boolean isLeader) {}

    private ClientPartyData() {}

    public static void update(String name, boolean leader, boolean pvp, List<MemberEntry> memberList) {
        partyName = name == null ? "" : name;
        isLeader = leader;
        pvpEnabled = pvp;
        members.clear();
        if (memberList != null) {
            members.addAll(memberList);
        }
    }

    public static void clear() {
        partyName = "";
        isLeader = false;
        pvpEnabled = false;
        members.clear();
    }

    public static boolean inParty() {
        return !partyName.isEmpty() && !members.isEmpty();
    }

    public static String partyName() { return partyName; }
    public static boolean isLeader() { return isLeader; }
    public static boolean pvpEnabled() { return pvpEnabled; }
    public static List<MemberEntry> members() { return Collections.unmodifiableList(members); }
}
