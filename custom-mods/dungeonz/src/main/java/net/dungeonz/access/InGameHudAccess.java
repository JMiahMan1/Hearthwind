package net.dungeonz.access;

public interface InGameHudAccess {

    public void setDungeonCountdownTicks(int dungeonCountdown);

    public int getDungeonCountdownTicks();

    public int getDungeonCountdownRemainingTicks();

}
