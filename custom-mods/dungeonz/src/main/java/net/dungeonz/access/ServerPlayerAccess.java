package net.dungeonz.access;

import net.minecraft.class_2338;
import net.minecraft.class_3218;
import org.jetbrains.annotations.Nullable;

public interface ServerPlayerAccess {

    public void setDungeonInfo(class_3218 world, class_2338 portalPos, class_2338 playerPos);

    @Nullable
    public class_3218 getOldServerWorld();

    public class_2338 getDungeonPortalBlockPos();

    public class_2338 getDungeonSpawnBlockPos();
}
