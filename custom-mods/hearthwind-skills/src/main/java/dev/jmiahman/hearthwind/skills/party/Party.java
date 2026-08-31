package dev.jmiahman.hearthwind.skills.party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Party {
    private final UUID id;
    private String name;
    private UUID leader;
    private final List<UUID> members = new ArrayList<>();
    private boolean pvpEnabled = false;

    public Party(UUID id, String name, UUID leader) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.members.add(leader);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public List<UUID> getMembers() { return Collections.unmodifiableList(members); }
    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }

    public boolean addMember(UUID member) {
        if (!members.contains(member)) {
            return members.add(member);
        }
        return false;
    }

    public boolean removeMember(UUID member) {
        return members.remove(member);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }
}
